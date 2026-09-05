@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.NotificationPageResponse
import team.mino.core.data.network.dto.response.NotificationPayloadResponse
import team.mino.core.data.network.dto.response.NotificationResponse
import team.mino.core.domain.model.Notification
import team.mino.core.domain.model.NotificationPage
import team.mino.core.domain.model.NotificationTarget
import team.mino.core.domain.model.NotificationType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 중복 저장 알림의 서버 enum. **도메인 이름([NotificationType.PLACE_DUPLICATED])과 어긋나는 유일한 값이라**
 * 상수로 세운다 — 나머지 5종은 서버 이름과 도메인 이름이 같다
 * (`docs/specs/notifications/data-model.md` §1.2).
 */
private const val PIN_DUPLICATED_TYPE = "PIN_DUPLICATED"

/**
 * 알림 한 묶음을 도메인으로 읽는다.
 *
 * **알 수 없는 유형의 항목은 버리되 묶음 전체를 실패시키지 않는다** — `RoomSummaryMapper`가 "방 하나의 값이
 * 어긋났다는 이유로 목록 전체가 실패하면 안 된다"로 세운 규칙과 같다
 * (`docs/specs/notifications/contracts/notification-repository.md` §1). 그래서 [NotificationPage.items]가
 * 서버가 실어 보낸 건수보다 짧을 수 있고, `hasNext`는 그것과 무관하게 서버가 준 값을 그대로 든다 — 버린 만큼
 * 다음 묶음이 당겨지지 않는다.
 *
 * **`page`·`pageSize`를 버리는 것도 이 자리의 몫이다.** DTO는 서버가 준 것을 그대로 들고, 도메인이 무엇을
 * 필요로 하는지는 Mapper가 안다(`docs/specs/notifications/data-model.md` §1.4).
 *
 * 순서를 건드리지 않는다. 서버가 준 최신순 그대로이며 `createdAt`으로 다시 정렬하지 않는다(FR-001).
 */
internal fun NotificationPageResponse.toDomain(): NotificationPage =
    NotificationPage(
        items = data.mapNotNull(NotificationResponse::toDomainOrNull),
        hasNext = pagination.hasNext,
    )

/**
 * 알림 한 건을 도메인으로 읽고, **행으로 그릴 수 없는 항목은 `null`로 떨어뜨린다.** 호출자가 그것을 걸러
 * 나머지를 그린다.
 *
 * 버리는 경우는 둘이다. 유형을 모르면 문구도 도착지도 정할 수 없고, 유형은 알아도 그 유형이 요구하는 대상
 * 식별자가 `payload`에 없으면 갈 곳이 없다. 후자를 [NotificationTarget.None]으로 메우지 않는 것은 그 값이
 * 저장 오류의 자리여서, 메우면 장소·방 알림이 저장 오류 안내 화면으로 잘못 이동하기 때문이다
 * (`docs/specs/notifications/data-model.md` §1.5).
 *
 * [Notification.typeLabel]은 서버가 완성해 준 문구를 그대로 싣는다 — 클라이언트가 6종 문구를 갖지 않는다
 * (`docs/specs/notifications/research.md` D4).
 *
 * [Notification.createdAt]은 서버가 준 절대 시각을 `Instant.parse`로 옮기기만 한다 — `PlaceCommentMapper`가
 * `createdAt`에 쓰는 것과 같은 형태다. **경과 시간 문구로 환산하지 않는다.** 구간 판정과 문구 조립은 목록을
 * 받은 feature가 한다(spec EC-005 · 같은 research D12).
 *
 * [Notification.thumbnailUrl]은 `null`을 메우지 않는다. 플레이스홀더를 그릴지 저장 오류 아이콘을 그릴지는
 * 유형까지 함께 보는 화면의 판정이다(같은 research D5).
 */
private fun NotificationResponse.toDomainOrNull(): Notification? {
    val notificationType = type.toNotificationTypeOrNull() ?: return null
    val target = notificationType.toTargetOrNull(payload) ?: return null

    return Notification(
        id = id,
        type = notificationType,
        typeLabel = typeLabel,
        targetName = targetName,
        thumbnailUrl = thumbnailUrl,
        target = target,
        createdAt = Instant.parse(createdAt),
    )
}

/**
 * 서버 enum 문자열을 도메인 유형으로 읽는다. **이 대응표를 아는 곳은 여기뿐이다**
 * (`NotificationType` KDoc).
 *
 * `NotificationType.valueOf`로 대신하지 않는 이유는 [PIN_DUPLICATED_TYPE] 한 값의 이름이 어긋나서이고, 이름이
 * 같은 5종까지 문자열로 적어 두는 이유는 서버 이름이 도메인 이름을 따라 바뀌지 않기 때문이다 — 한쪽이 바뀌면
 * 이 표만 고친다.
 *
 * 모르는 값은 `null`이다. 도메인 [NotificationType]에 `UNKNOWN` 멤버를 두지 않으므로 흡수할 자리가 없고,
 * 흡수해서도 안 된다 — 그런 항목은 그려지지 않고 버려진다.
 */
private fun String.toNotificationTypeOrNull(): NotificationType? =
    when (this) {
        PIN_DUPLICATED_TYPE -> NotificationType.PLACE_DUPLICATED
        "SAVE_FAILED" -> NotificationType.SAVE_FAILED
        "NEARBY_PLACE" -> NotificationType.NEARBY_PLACE
        "TOP_COMMENTED_PLACE" -> NotificationType.TOP_COMMENTED_PLACE
        "ROOM_MEMBER_JOINED" -> NotificationType.ROOM_MEMBER_JOINED
        "ROOM_JOINED_SELF" -> NotificationType.ROOM_JOINED_SELF
        else -> null
    }

/**
 * `payload`의 nullable 조합을 [NotificationTarget] 세 갈래로 흡수한다.
 *
 * **갈래를 정하는 것은 `payload`의 필드가 아니라 유형이다.** 서버가 판별 필드를 싣지 않아 DTO가 sealed로 설 수
 * 없고, 그래서 그 판정이 이 자리로 온다(`NotificationPayloadResponse` KDoc ·
 * `docs/specs/notifications/data-model.md` §1.3).
 *
 * 장소 대상 3종은 **`placeId`가 아니라 `pinId`로** 연다(spec FR-022 ·
 * `docs/specs/notifications/contracts/notification-api.md` §1). `placeId`는 DTO가 아예 들지 않으므로 여기서
 * 고를 일도 없다.
 *
 * 저장 오류는 `payload`를 보지 않는다 — 서버가 `null`을 주기로 돼 있지만 무엇이 실려 오든 열 대상이 없다.
 *
 * 나머지 유형에서 대상 식별자가 비면 `null`을 돌려 그 항목을 버리게 한다.
 */
private fun NotificationType.toTargetOrNull(payload: NotificationPayloadResponse?): NotificationTarget? =
    when (this) {
        NotificationType.PLACE_DUPLICATED,
        NotificationType.NEARBY_PLACE,
        NotificationType.TOP_COMMENTED_PLACE,
        -> payload?.pinId?.let(NotificationTarget::Pin)

        NotificationType.ROOM_MEMBER_JOINED,
        NotificationType.ROOM_JOINED_SELF,
        -> payload?.roomId?.let(NotificationTarget::Room)

        NotificationType.SAVE_FAILED -> NotificationTarget.None
    }
