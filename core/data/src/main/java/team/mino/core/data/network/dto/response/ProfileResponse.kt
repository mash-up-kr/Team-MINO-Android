package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 프로필 조회(`GET /api/v1/users/me`)·등록(`POST /api/v1/users`)·수정(`PATCH /api/v1/users/me`) 응답이
 * 공통으로 담는 유저 표현. 세 엔드포인트의 성공 본문이 같은 형태다.
 *
 * `{ "data": ... }` 봉투는 여기에 넣지 않는다 — `UserApiService`가 [MinoResponse]로 벗긴다
 * (ADR `docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md`).
 *
 * [avatar]가 nullable인 것은 서버 스키마가 `nullable: true`이기 때문이다. **언제 `null`이 되는지는 문서에 없고**
 * (API 계약 §2 협의 항목 ⑥), 받는 쪽은 `null`과 모르는 색을 똑같이 기본 아바타로 읽는다.
 *
 * [createdAt]은 ISO-8601 문자열이다. 화면이 쓰지 않으므로 도메인 모델에는 오르지 않는다.
 *
 * 계약은 `docs/specs/profile/contracts/profile-api-contract.md` §1이 소유한다.
 */
@Serializable
internal data class ProfileResponse(
    val id: String,
    val nickname: String,
    val avatar: AvatarResponse? = null,
    val createdAt: String,
)

/**
 * 응답이 싣는 아바타 표현. 서버는 아바타를 색 하나로만 표현한다.
 *
 * [color]는 서버가 준 색 식별자 문자열이다. 도메인 `ProfileAvatar`와의 대응은 `ProfileMapper`만 안다 —
 * DTO는 도메인 타입을 알지 못한다. 서버 `enum`이 넓어져도 파싱이 깨지지 않도록 문자열로 받고,
 * 모르는 값의 처리는 매퍼가 정한다.
 *
 * 기본값을 둔 것은 아바타 객체가 색 없이 오더라도(빈 객체·다른 필드만 실린 응답) 조회 전체를 실패시키지
 * 않기 위해서다. 빈 문자열은 표에 없는 색과 같게 다뤄져 매퍼가 기본 아바타로 읽는다.
 */
@Serializable
internal data class AvatarResponse(
    val color: String = "",
)

// RoomMemberDetailResponse(RoomSummaryResponse.kt)도 같은 아바타 표현을 쓴다 — 서버가 유저 표현 전반에
// 같은 { color } 봉투를 재사용한다.
