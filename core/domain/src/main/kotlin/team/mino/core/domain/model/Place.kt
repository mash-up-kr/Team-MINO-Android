package team.mino.core.domain.model

import team.mino.core.common.kotlin.geo.GeoPoint
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 상세([SCR-005])의 장소 카드/리스트 렌더링에 필요한 필드로 한정한 도메인 모델.
 *
 * 서버는 이 모델을 단독으로 내려주지 않는다 — `Pin { id, roomId, place, images, createdBy, createdAt }`이
 * `Place`를 감싸는 실제 응답 구조다(`GET /api/v1/pins`). 이 모델은 **`Pin`과 그 안의 `place`를 합쳐 만든
 * 표현 모델**이며, 그래서 식별자를 둘 갖는다 — [id]는 `Pin.id`, [placeId]는 `Pin.place.id`다. 변환은
 * `PlaceMapper.toDomain()`(`:core:data`)이 전담한다.
 *
 * [commentCount]·[isGgukPick]은 서버 응답에 없어 구현 시 임시 목데이터/플레이스홀더로 채운다
 * (`docs/specs/room-detail/data-model.md` §4 `[TBD]`).
 *
 * [location]은 서버가 `Pin.place.lat`·`Pin.place.lng`로 내려주는 실좌표다 — 지도 마커가 이 값을 그대로 쓴다.
 */
@OptIn(ExperimentalTime::class)
data class Place(
    /** 이 방에 담긴 이 한 장을 가리킨다(서버 `Pin.id`). 공유·삭제 호출이 쓰는 값이다. */
    val id: String,
    /**
     * 장소 자체를 가리킨다(서버 `Pin.place.id`). 같은 장소면 어느 방에서 보든 같은 값이라
     * **어느 방에 이미 담겨 있는지**를 묻는 데 쓴다(`RoomRepository.getRooms(placeId)` → `hasPlace`).
     * [id]로는 그 질문을 할 수 없다 — 방마다 값이 다르다.
     */
    val placeId: String,
    val name: String,
    val address: String,
    val category: PlaceCategoryFilter,
    /**
     * `Pin.images`(서버가 이 장소에 저장한 사진 여러 장, 순서 보장) 전체 — 카드형([PlaceGridItem])의
     * 2장 콜라주가 `images[0]`·`images[1]`처럼 서로 다른 사진을 채워야 하기 때문에 첫 장만 남기지 않는다.
     * 비어 있으면 사진이 없는 장소다.
     */
    val thumbnailUrls: List<String>,
    val savedAt: Instant,
    val commentCount: Int,
    val isGgukPick: Boolean,
    val distanceMeters: Double?,
    val location: GeoPoint,
)
