package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 서버가 모든 실패 응답에 내려주는 본문 `{ "errorCode", "message" }`.
 *
 * 성공 봉투([MinoResponse])와 달리 이 본문은 감싸이지 않는다. 프로필만의 형식이 아니므로 타입 이름에
 * feature를 담지 않는다.
 *
 * **읽는 곳은 원칙적으로 없다.** 실패 판정은 `expectSuccess = true`가 만든 예외를 `convertDomainException`이
 * 상태 코드만으로 `MinoDomainException`에 매핑해 끝난다(`core/data/README.md` §4). 이 타입이 필요한 것은
 * `GET /api/v1/users/me`가 인증 실패와 미등록을 같은 `401`로 내려, `errorCode == "USER_NOT_REGISTERED"`를
 * 가려야 온보딩이 성립하는 한 지점뿐이다
 * (`docs/specs/profile/contracts/profile-api-contract.md` §2 협의 항목 ⑤).
 *
 * [errorCode]를 도메인 예외로 승격하지 않는다 — `MinoDomainException`에 새 리프를 만들지 않는다.
 *
 * [message]는 서버 스키마상 항상 오지만 기본값을 둔다. 실패 경로에서 본문이 스키마와 어긋났을 때 파싱 예외가
 * 원래 실패를 덮어쓰면 진짜 원인이 사라지기 때문이다. `RoomSummaryResponse.thumbnailList`가 같은 이유로
 * 기본값을 갖는다.
 */
@Serializable
internal data class ErrorResponse(
    val errorCode: String,
    val message: String? = null,
)
