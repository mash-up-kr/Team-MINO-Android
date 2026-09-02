package team.mino.core.domain.model

/**
 * 방 목록의 한 항목.
 *
 * 방 생성·편집 폼이 쓰는 [Room]과 별개의 타입이다. 목록은 [type]·[placeCount]·[thumbnailImageUrls]를 필요로 하지만
 * 폼이 쓰는 필드 대부분은 쓰지 않으므로, 두 화면의 합집합을 한 타입에 담으면 어느 쪽에서든 절반이 의미 없는 값이 된다 —
 * `docs/specs/shared-link-receiver/research.md` R-009.
 *
 * [description]은 nullable이 아니다. 설명이 없는 방은 빈 문자열을 갖고, 서버가 내려준 `null`은 Mapper가 흡수한다 —
 * [Room]과 같은 규칙이다.
 *
 * [placeCount]는 그 방에 저장된 장소의 수일 뿐 **지금 저장하려는 장소가 이미 있는지를 뜻하지 않는다.**
 *
 * [thumbnailImageUrls]는 콜라주가 그릴 수 있는 만큼만 담는다. 서버가 더 많이 내려주더라도 앞의 것만 남기는 판정은
 * Mapper가 하고, 썸네일이 없는 방은 빈 목록을 갖는다.
 *
 * @property hasPlace 특정 장소가 이 방에 이미 저장돼 있는지. **`null`은 "저장돼 있지 않다"가 아니라 "물어보지
 *  않았다"다** — [team.mino.core.domain.repository.RoomRepository.getRooms]를 `placeId` 없이 부른 호출자
 *  (방 리스트 탭·기존 공유 시트)가 `false`를 사실로 오해하지 않게 가르는 구분이다
 *  (`docs/specs/place-detail/data-model.md` §3).
 * @property matchedPinId 그 장소가 이 방에 저장돼 있을 때 대응하는 핀의 식별자. [hasPlace]가 `true`가 아니면
 *  **항상 `null`이다** — 서버 스키마는 이 필드를 nullable로 표시하지 않지만 저장돼 있지 않은 방에 무엇이 실려 오든
 *  전환 대상이 되지 않도록 Mapper가 지운다(`docs/specs/place-detail/contracts/place-api.md` §4.2).
 */
data class RoomSummary(
    val id: String,
    val name: String,
    val description: String,
    val type: RoomType,
    val color: RoomColor,
    val placeCount: Int,
    val thumbnailImageUrls: List<String>,
    val hasPlace: Boolean? = null,
    val matchedPinId: String? = null,
)
