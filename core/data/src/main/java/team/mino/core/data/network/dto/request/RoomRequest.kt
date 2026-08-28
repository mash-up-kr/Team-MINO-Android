package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 방 생성·수정 요청.
 *
 * [description]의 `null`은 설명이 없다는 뜻이다. [color]는 색 식별자 문자열이며, 사용자가 색을 고르지 않은 경우에도
 * 저장 경로가 확정한 값이 들어온다 — 요청에 "색 없음"은 없다.
 *
 * 수정이 생성과 같은 본문을 쓰는 것은 수정도 부분 갱신이 아니라 폼의 세 값을 항상 함께 보내기 때문이다.
 * 부분 전송은 "설명을 지웠다"와 "설명을 건드리지 않았다"를 구분하지 못한다. 두 요청이 갈라지면 그때 타입을 나눈다.
 *
 * **[description]에 기본값을 두지 않는다** — 그 금지와 근거의 소유자는
 * `docs/specs/group-room-form/contracts/room-api.md` §5다. `= null`을 붙이면 설명을 지운 변경이 본문에서
 * 빠져 조용히 사라진다. `RoomRequestSerializationTest`가 이것을 지킨다.
 */
@Serializable
internal data class RoomRequest(
    val name: String,
    val description: String?,
    val color: String,
)
