package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 목록 API가 `data`와 나란히 싣는 페이지네이션 메타(offset 기반). 서버가 "목록 API 공통"으로 정의한
 * 스키마여서 엔드포인트별로 다시 만들지 않는다.
 *
 * [MinoResponse]와 달리 봉투가 아니다 — 봉투는 알맹이만 남기고 `ApiService`가 버리지만, 이 값은
 * 도메인이 읽는 데이터다. 그래서 이 타입은 페이지 응답 DTO 안에 남아 Mapper까지 올라간다.
 *
 * [hasNext]는 "더 받을 다음 페이지가 있는가"라는 서버의 관점이다. 화면의 관점으로 뒤집는 것
 * (코멘트에서는 `PlaceCommentPage.hasOlder`)은 Mapper의 몫이며 DTO는 서버 이름을 그대로 쓴다.
 */
@Serializable
internal data class PaginationResponse(
    val page: Int,
    val pageSize: Int,
    val hasNext: Boolean,
)
