package team.mino.core.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 상세([SCR-005])의 장소 카드/리스트 렌더링에 필요한 필드로 한정한 도메인 모델.
 *
 * 서버는 이 모델을 단독으로 내려주지 않는다 — `Pin { id, roomId, place, images, createdBy, createdAt }`이
 * `Place`를 감싸는 실제 응답 구조다(`GET /api/v1/pins`). 이 모델은 **`Pin`과 그 안의 `place`를 합쳐 만든
 * 표현 모델**이며, [id]는 `Pin.id`를 쓴다(`Pin.place.id`가 아니다) — 공유([sharePlaces])·삭제([deletePlace])
 * 호출은 이 값을 그대로 쓴다. 변환은 `PlaceMapper.toDomain()`(`:core:data`)이 전담한다.
 *
 * [commentCount]·[isGgukPick]은 서버 응답에 없어 구현 시 임시 목데이터/플레이스홀더로 채운다
 * (`docs/specs/room-detail/data-model.md` §4 `[TBD]`).
 */
@OptIn(ExperimentalTime::class)
data class Place(
    val id: String,
    val name: String,
    val address: String,
    val category: PlaceCategoryFilter,
    val thumbnailUrl: String?,
    val savedAt: Instant,
    val commentCount: Int,
    val isGgukPick: Boolean,
    val distanceMeters: Double?,
)
