package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import team.mino.core.data.network.dto.request.ProfileRequest
import team.mino.core.data.network.dto.response.ErrorResponse
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.ProfileResponse
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException
import javax.inject.Inject

/**
 * `user` 태그의 엔드포인트를 호출하는 서비스. `Authorization: Bearer`는 `minoIdentityProofPlugin`이 붙이므로
 * 이 서비스는 토큰을 다루지 않고, 비2xx는 `convertDomainException`이 `MinoDomainException`으로 바꿔 던지므로
 * [unregisteredAsNull]의 지역 catch를 빼면 여기서 잡지 않는다.
 *
 * 계약은 `docs/specs/profile/contracts/profile-api-contract.md` §1·§3이 소유한다.
 */
internal class UserApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * `GET /api/v1/users/me`로 프로필 등록 여부만 본다. 미등록(`401` + `USER_NOT_REGISTERED`)이면 `false`이고
     * 그 밖의 401은 그대로 전파된다.
     *
     * **성공 본문을 역직렬화하지 않는다** — 호출자가 쓰는 것은 상태 코드뿐이다
     * (`docs/specs/splash-screen/data-model.md` §3.1). [getMe]로 대신 구현하면 진입 게이트가 프로필 본문
     * 스키마에 묶여, 서버가 예상 밖 본문을 주는 순간 앱을 켜는 모든 사용자가 진입에 실패한다
     * (`docs/specs/profile/research.md` D49).
     */
    suspend fun hasProfile(): Boolean = unregisteredAsNull { client.get(USERS_ME_PATH) } != null

    /**
     * 내 프로필을 조회한다. 미등록(`401` + `USER_NOT_REGISTERED`)이면 `null`이고, 다른 401은 그대로 전파된다.
     *
     * 서버가 인증 실패와 미등록을 같은 `401`로 내려주므로 상태 코드만으로는 온보딩 분기를 판정할 수 없다
     * (API 계약 §2 협의 항목 ⑤).
     */
    suspend fun getMe(): ProfileResponse? =
        unregisteredAsNull {
            client
                .get(USERS_ME_PATH)
                .body<MinoResponse<ProfileResponse>>()
                .data
        }

    suspend fun register(request: ProfileRequest): ProfileResponse =
        client
            .post(USERS_PATH) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<MinoResponse<ProfileResponse>>()
            .data

    suspend fun updateMe(request: ProfileRequest): ProfileResponse =
        client
            .patch(USERS_ME_PATH) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<MinoResponse<ProfileResponse>>()
            .data

    /**
     * 미등록(`401` + `USER_NOT_REGISTERED`)이면 `null`, 그 밖의 실패는 그대로 전파한다.
     *
     * 판정을 여기 한 곳에 둔다 — 두 벌로 두면 한쪽만 고쳐도 컴파일과 테스트가 통과해, 진입 게이트와 프리필이
     * 서로 다른 판정을 하게 된다.
     */
    private suspend fun <T> unregisteredAsNull(call: suspend () -> T): T? =
        try {
            call()
        } catch (failure: MinoDomainException.Http) {
            if (failure.code == HttpStatusCode.Unauthorized.value && failure.isUserNotRegistered()) {
                null
            } else {
                throw failure
            }
        }

    /**
     * 401 응답의 본문이 미등록을 가리키는지 읽는다. `convertDomainException`이 감싸기 전 원본
     * [ResponseException]이 응답을 들고 있어 본문을 다시 읽을 수 있다.
     *
     * 본문을 읽지 못하면 `false`다 — 미등록이라 결론낼 수 없으면 인증 실패로 두고 원래의 401을 전파해야 한다.
     * 본문이 JSON이 아니거나 스키마와 어긋날 때 파싱 예외가 진짜 원인을 덮어쓰는 것을 막는다
     * ([ErrorResponse.message]가 기본값을 갖는 것과 같은 이유다).
     *
     * 삼키는 것은 **본문 변환·읽기 실패뿐**이다. 그 밖의 예외는 프로그래머 버그이므로 잡지 않고 CEH로 보낸다
     * (`docs/conventions/error_handling.md` §1). 실패 본문은 게이트웨이가 끼어들면 JSON이 아닐 수 있어
     * 변환 실패가 정상 유저 시나리오에 속하며, 실제로 세 갈래로 나타난다 — 본문이 JSON이 아니거나
     * `Content-Type`이 없으면 [NoTransformationFoundException], 스키마 불일치·빈 본문이면
     * [ContentConvertException], 본문을 네트워크에서 마저 읽지 못하면 [IOException].
     */
    private suspend fun MinoDomainException.Http.isUserNotRegistered(): Boolean {
        val response = (cause as? ResponseException)?.response ?: return false
        return try {
            response.body<ErrorResponse>().errorCode == USER_NOT_REGISTERED
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (notNegotiable: NoTransformationFoundException) {
            false
        } catch (conversionFailure: ContentConvertException) {
            false
        } catch (readFailure: IOException) {
            false
        }
    }

    private companion object {
        const val USERS_PATH = "api/v1/users"
        const val USERS_ME_PATH = "api/v1/users/me"
        const val USER_NOT_REGISTERED = "USER_NOT_REGISTERED"
    }
}
