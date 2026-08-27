package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 조회·생성·수정 응답이 공통으로 담는 방 표현.
 *
 * 폼이 쓰는 필드만 둔다. 서버가 초대 코드·생성 시각 등을 더 내려줘도 `ignoreUnknownKeys = true`가 흡수하므로
 * 파싱이 깨지지 않는다.
 *
 * [color]는 색 식별자 문자열이다. 이 문자열과 도메인 색의 대응은 `RoomMapper`만 안다.
 */
@Serializable
internal data class RoomResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String,
    val ownerId: String,
)
