package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/notifications` 응답 **본문 전체**.
 *
 * [CommentPageResponse]와 같은 이유로 [MinoResponse]로 벗기지 않는다 — 서버가 `data`와 **나란히**
 * [pagination]을 싣기 때문에(`{ "data": [...], "pagination": {...} }`) 봉투 타입으로는 형태를 표현할 수
 * 없다. [PaginationResponse]는 서버가 목록 API 공통으로 정의한 스키마여서 여기서 다시 만들지 않는다.
 *
 * 도메인 `NotificationPage`는 `hasNext`만 들고 `page`·`pageSize`는 버린다 — 그 취사선택은 Mapper의
 * 몫이고 DTO는 서버가 준 것을 그대로 든다
 * (`docs/specs/notifications/data-model.md` §1.4).
 *
 * 계약은 `docs/specs/notifications/contracts/notification-api.md` §1이 소유한다.
 */
@Serializable
internal data class NotificationPageResponse(
    val data: List<NotificationResponse> = emptyList(),
    val pagination: PaginationResponse,
)

/**
 * 알림 한 건. 계약은 `docs/specs/notifications/contracts/notification-api.md` §1이 소유한다.
 *
 * **[type]을 enum으로 좁히지 않는다.** 서버가 유형을 늘리면 역직렬화가 그 자리에서 실패해 목록 전체가
 * 무너지는데, 계약은 알 수 없는 유형의 **항목만 버리고 나머지는 그리라**고 정한다
 * (`docs/specs/notifications/contracts/notification-repository.md` §1). 문자열로 받아 그 판정을 Mapper에
 * 맡긴다. 서버 이름과 도메인 이름이 어긋나는 것도 한 건 있어(`PIN_DUPLICATED`) 대응표는 Mapper만 안다
 * (`NotificationType` KDoc).
 *
 * [createdAt]은 ISO-8601 문자열로 받는다. `kotlin.time.Instant`로 옮기는 것은 Mapper의 몫이고
 * (`docs/specs/notifications/data-model.md` §1.1), 경과 시간 문구로 바꾸는 것은 그보다 위인 feature가
 * 한다. DTO는 서버가 준 절대 시각만 든다.
 *
 * **읽음 여부 필드가 없다** — spec FR-016이 그 상태 자체를 두지 않아 서버 스키마에도 없다.
 */
@Serializable
internal data class NotificationResponse(
    val id: String,
    val type: String,
    val typeLabel: String,
    val targetName: String,
    val thumbnailUrl: String? = null,
    val payload: NotificationPayloadResponse? = null,
    val createdAt: String,
)

/**
 * [NotificationResponse.payload] — 이동 대상 식별자.
 *
 * 서버 스키마는 `oneOf` **세 갈래**다 — 장소 대상 3종은 `{ placeId, pinId }`, `ROOM_*` 2종은
 * `{ roomId }`, 저장 오류는 `null`이다. 세 번째 갈래는 [NotificationResponse.payload]의 nullable이
 * 표현하고, 앞의 둘은 이 타입 하나가 **필드를 nullable로 열어** 표현한다.
 *
 * **sealed로 가르지 않는다.** 서버가 갈래를 구분하는 판별 필드를 본문에 싣지 않아
 * `kotlinx.serialization`이 어느 갈래인지 정할 근거가 없다. 갈래를 정하는 것은
 * [NotificationResponse.type]이고, 그 판정은 Mapper가 한다 — 도메인 `NotificationTarget`이 sealed로
 * 서 있는 자리가 그곳이다(`docs/specs/notifications/data-model.md` §1.3).
 *
 * **`placeId`를 두지 않는다.** 서버가 [pinId]와 함께 주지만 도착지 판정에 쓰지 않으며
 * (같은 계약 §1 — "장소 상세는 `pinId`로 연다"), `ignoreUnknownKeys = true`가 흡수한다.
 * [RoomResponse]가 `pinCount`·`memberCount`를 두지 않는 것과 같은 처리다.
 */
@Serializable
internal data class NotificationPayloadResponse(
    val pinId: String? = null,
    val roomId: String? = null,
)
