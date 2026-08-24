package team.mino.core.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방(개인방/공동방) 도메인 모델.
 *
 * - **검증 규칙**: `isPersonal == true`인 [Room]은 사용자당 최대 1개다(개인방은 하나, PRD 「개인방」정의).
 *   `placeCount == 0`이면 `lastPlaceSavedAt == null`이다.
 * - **관계**: `Room` 1 — N `Place`(다른 spec 소유). 이 모델은 `Place` 자체를 담지 않고
 *   [placeCount]·[lastPlaceSavedAt]·[commentCount]로 집계값만 소비한다.
 *
 * @property color 방 대표 색상. `:core:design-system`의 `MinoRoomColor`(ADR 2026-08-14)에 대응하는
 *   식별자를 domain이 UI 레이어 타입에 의존하지 않고 담기 위한 값이다(`core:domain` 은 Android SDK·
 *   `:core:design-system`을 의존할 수 없다 — `core/domain/README.md` §8). 미선택이면 null(회색).
 *   `MinoRoomColor`로의 매핑은 이 모델을 소비하는 feature가 갖는다(ADR 2026-08-14 「결과」).
 */
@OptIn(ExperimentalTime::class)
data class Room(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val isPersonal: Boolean,
    val placeCount: Int,
    val thumbnail: RoomThumbnail,
    val memberSummary: RoomMemberSummary,
    val lastPlaceSavedAt: Instant?,
    val commentCount: Int,
)
