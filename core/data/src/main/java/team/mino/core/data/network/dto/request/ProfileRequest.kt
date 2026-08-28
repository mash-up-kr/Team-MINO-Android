package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 유저 등록(`POST /api/v1/users`)·프로필 수정(`PATCH /api/v1/users/me`) 요청.
 *
 * 두 엔드포인트가 같은 타입을 쓴다. `PATCH`는 서버 스키마상 모든 필드가 선택이지만 **이 앱은 언제나 두 값을
 * 함께 보낸다** — 화면이 닉네임과 아바타를 한 폼으로 다루고, 부분 전송은 "아바타를 안 골랐다"와
 * "아바타를 건드리지 않았다"를 구분하지 못한다. 두 요청이 갈라지면 그때 타입을 나눈다.
 *
 * [nickname]은 클라이언트 검증(`ValidateNicknameUseCase`)을 통과한 값이다. 서버의 `maxLength: 15`보다
 * 넓은 값이 나갈 수 있으며, 그 경우 서버 거절이 저장 실패로 보인다
 * (`docs/specs/profile/spec.md` §5 · EC-014).
 *
 * 계약은 `docs/specs/profile/contracts/profile-api-contract.md` §1이 소유한다.
 */
@Serializable
internal data class ProfileRequest(
    val nickname: String,
    val avatar: AvatarRequest,
)

/**
 * 요청이 싣는 아바타 표현. 서버는 아바타를 색 하나로만 표현한다.
 *
 * [color]는 서버가 정한 색 식별자 문자열이다. 도메인 `ProfileAvatar`와의 대응은 `ProfileMapper`만 안다 —
 * DTO는 도메인 타입을 알지 못하며, 값 목록도 여기에 두지 않는다.
 */
@Serializable
internal data class AvatarRequest(
    val color: String,
)
