package team.mino.core.data.datasource

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import team.mino.core.data.network.service.UserApiService
import team.mino.core.errorhandling.MinoDomainException
import javax.inject.Inject

/**
 * `401` 하나가 "미등록"과 "인증 실패" 둘을 겸하는 엔드포인트라, 그 판정만 지역 catch로 병용한다
 * (`core/data/README.md` §4 마지막 불릿의 "엔드포인트별 특수 정책"). 도메인 예외 매핑 자체는 여기서 하지 않는다 —
 * HTTP 원천의 매핑 지점은 `convertDomainException` 하나다.
 *
 * `errorCode`가 `USER_NOT_REGISTERED`일 때만 미등록이다. 모르는 `errorCode`·읽을 수 없는 본문은 등록 여부를
 * 단정할 근거가 아니므로 `false`로 뭉개지 않고 `Http(401)` 그대로 올려보낸다. 뭉개면 세션이 깨진 기존 사용자가
 * 온보딩으로 떨어진다(SC-002). `errorCode` 문자열은 이 파일 밖으로 나가지 않는다.
 */
internal class UserRemoteDataSourceImpl @Inject constructor(
    private val service: UserApiService,
) : UserRemoteDataSource {
    override suspend fun isRegistered(): Boolean =
        try {
            service.getMe()
            true
        } catch (exception: MinoDomainException.Http) {
            if (exception.code == HttpStatusCode.Unauthorized.value &&
                exception.errorCode() == USER_NOT_REGISTERED
            ) {
                false
            } else {
                throw exception
            }
        }
}

/**
 * 공통 에러 포맷 `{ errorCode, message }`에서 `errorCode`만 꺼낸다. 읽을 수 없으면 `null`.
 *
 * 파싱 실패를 도메인 예외로 감싸지 않는다. 잡는 것은 [SerializationException] 하나이며 — 401 본문이 JSON이 아닌
 * 경우(게이트웨이 HTML 등)는 예상 가능한 실패다 — 나머지 예외와 `CancellationException`은 그대로 전파된다.
 */
private suspend fun MinoDomainException.Http.errorCode(): String? {
    val response = (cause as? ResponseException)?.response ?: return null
    val body =
        try {
            Json.parseToJsonElement(response.bodyAsText())
        } catch (_: SerializationException) {
            return null
        }
    return ((body as? JsonObject)?.get("errorCode") as? JsonPrimitive)?.contentOrNull
}

private const val USER_NOT_REGISTERED = "USER_NOT_REGISTERED"
