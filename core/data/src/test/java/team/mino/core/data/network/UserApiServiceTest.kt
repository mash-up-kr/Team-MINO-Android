package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.extension.convertDomainException
import team.mino.core.data.network.service.UserApiService
import team.mino.core.errorhandling.MinoDomainException

/**
 * `user` 태그의 네 오퍼레이션은 두 가지를 런타임에만 확인할 수 있다.
 *
 * 하나는 `MinoResponse<ProfileResponse>` 봉투 해제다 — 제네릭 DTO라 직렬화기가 실제로 해결되는지를 컴파일이
 * 보증하지 않는다(ADR `docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md`).
 *
 * 다른 하나는 `GET /api/v1/users/me`의 401 분기다. 서버가 **미등록과 인증 실패를 같은 401로** 내려주므로
 * 응답 본문의 `errorCode`를 읽어야 온보딩이 성립한다. 이 저장소에서 에러 본문을 읽는 유일한 지점이며
 * (`docs/specs/profile/contracts/profile-api-contract.md` §2 협의 항목 ⑤), `USER_NOT_REGISTERED`만
 * 미등록으로 삼키고 나머지 401은 — 모르는 `errorCode`·읽을 수 없는 본문을 포함해 — 전부 전파돼야 한다.
 * 뭉개면 세션이 깨진 기존 사용자가 온보딩으로 떨어진다(splash-screen SC-002).
 *
 * [UserApiService.hasProfile]과 [UserApiService.getMe]는 같은 경로를 부르되 실패 허용치가 다르다 —
 * 진입 게이트인 전자는 **성공 본문 스키마에 의존하지 않아야 한다**(API 계약 §3 · `research.md` D49).
 *
 * **401을 `MinoDomainException.Auth`로 재매핑하지 않는다.** `docs/specs/splash-screen/contracts/profile-registration.md`
 * §서버 응답 대응은 `UNAUTHORIZED`·`TOKEN_EXPIRED`를 `Auth` 리프로 던지라고 적지만, 구현은 validator가 만든
 * `Http(401)`을 그대로 전파한다. **버그가 아니라 그 계약이 아직 반영되지 않은 것이며**, 아래 401 케이스들이
 * `Http(401)`을 고정하고 있는 이유다. 이 어긋남을 닫으려면 매핑 지점(`convertDomainException`)과 그 계약을
 * 함께 봐야 한다(`docs/adr/2026-08-22-domain-exception-mapping-per-source.md`).
 *
 * `MinoIdentityProofPlugin`의 헤더 첨부는 [IdentityProofAttachmentTest]가 소유하므로 여기서 다시 보지 않는다.
 */
class UserApiServiceTest {
    private fun service(engine: MockEngine): UserApiService =
        UserApiService(
            HttpClient(engine) {
                expectSuccess = true
                convertDomainException()
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            },
        )

    // region hasProfile — 등록 여부 게이트

    @Test
    fun `hasProfile은 200이면 등록된 것으로 본다`() =
        runTest {
            assertTrue(service(jsonEngine(PROFILE_BODY)).hasProfile())
        }

    /**
     * [REGISTRATION_ONLY_BODY]는 `ProfileResponse`(`id: String`·`nickname`·`createdAt` 필수)를 만족하지 않는다.
     * 그런데도 `true`여야 한다 — 스플래시 진입 게이트가 성공 본문을 읽지 않기 때문이다. 이 성질을 잃으면
     * 서버 응답 스키마가 조금만 흔들려도 앱을 켜는 모든 사용자가 진입에 실패한다.
     */
    @Test
    fun `hasProfile은 성공 본문이 프로필 스키마와 어긋나도 등록으로 본다`() =
        runTest {
            assertTrue(service(jsonEngine(REGISTRATION_ONLY_BODY)).hasProfile())
        }

    @Test
    fun `hasProfile은 users me 엔드포인트를 호출한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(REGISTRATION_ONLY_BODY) { requested = it }

            service(engine).hasProfile()

            assertEquals(HttpMethod.Get, requested?.method)
            assertEquals(USERS_ME_PATH, requested?.url?.encodedPath)
        }

    @Test
    fun `hasProfile의 401 USER_NOT_REGISTERED는 실패가 아니라 미등록이다`() =
        runTest {
            val engine = jsonEngine(errorBody("USER_NOT_REGISTERED"), HttpStatusCode.Unauthorized)

            assertFalse(service(engine).hasProfile())
        }

    @Test
    fun `hasProfile의 401 UNAUTHORIZED는 Http 401 그대로 던진다`() =
        runTest {
            assertHasProfileRethrows(jsonEngine(errorBody("UNAUTHORIZED"), HttpStatusCode.Unauthorized))
        }

    @Test
    fun `hasProfile의 401 TOKEN_EXPIRED는 Http 401 그대로 던진다`() =
        runTest {
            assertHasProfileRethrows(jsonEngine(errorBody("TOKEN_EXPIRED"), HttpStatusCode.Unauthorized))
        }

    @Test
    fun `hasProfile은 errorCode가 없는 401을 미등록으로 넘기지 않는다`() =
        runTest {
            assertHasProfileRethrows(jsonEngine("""{"message":"unauthorized"}""", HttpStatusCode.Unauthorized))
        }

    @Test
    fun `hasProfile은 모르는 errorCode의 401도 Http 401 그대로 던진다`() =
        runTest {
            assertHasProfileRethrows(jsonEngine(errorBody("SOMETHING_NEW"), HttpStatusCode.Unauthorized))
        }

    @Test
    fun `hasProfile은 JSON이 아닌 401 본문도 Http 401로 던진다`() =
        runTest {
            val engine = MockEngine { respond("<html>gateway</html>", HttpStatusCode.Unauthorized) }

            assertHasProfileRethrows(engine)
        }

    // endregion

    // region getMe — 프리필

    @Test
    fun `getMe는 data 봉투를 벗기고 알맹이만 반환한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY) { requested = it }

            val profile = service(engine).getMe()

            assertEquals(HttpMethod.Get, requested?.method)
            assertEquals(USERS_ME_PATH, requested?.url?.encodedPath)
            assertEquals("u-1", profile?.id)
            assertEquals("꾹이", profile?.nickname)
            assertEquals("red", profile?.avatar?.color)
            assertEquals("2026-08-28T00:00:00Z", profile?.createdAt)
        }

    @Test
    fun `getMe는 401 USER_NOT_REGISTERED를 예외가 아니라 null로 돌려준다`() =
        runTest {
            val engine = jsonEngine(errorBody("USER_NOT_REGISTERED"), HttpStatusCode.Unauthorized)

            assertNull(service(engine).getMe())
        }

    @Test
    fun `getMe는 401 UNAUTHORIZED를 삼키지 않고 전파한다`() =
        runTest {
            val engine = jsonEngine(errorBody("UNAUTHORIZED"), HttpStatusCode.Unauthorized)
            var thrown: MinoDomainException.Http? = null

            try {
                service(engine).getMe()
            } catch (e: MinoDomainException.Http) {
                thrown = e
            }

            assertEquals(401, thrown?.code)
        }

    // endregion

    // region register · updateMe

    @Test
    fun `register는 요청을 보내고 data 봉투를 벗긴 응답을 반환한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY, HttpStatusCode.Created) { requested = it }

            val profile = service(engine).register(profileRequest())

            assertEquals(HttpMethod.Post, requested?.method)
            assertEquals("/api/v1/users", requested?.url?.encodedPath)
            assertEquals(
                """{"nickname":"꾹이","avatar":{"color":"red"}}""",
                (requested?.body as TextContent).text,
            )
            assertEquals(ContentType.Application.Json, requested?.body?.contentType?.withoutParameters())
            assertEquals("u-1", profile.id)
            assertEquals("red", profile.avatar?.color)
        }

    @Test
    fun `register의 409는 지역 처리하지 않고 전파한다`() =
        runTest {
            val engine = jsonEngine(errorBody("USER_ALREADY_REGISTERED"), HttpStatusCode.Conflict)
            var thrown: MinoDomainException.Http? = null

            try {
                service(engine).register(profileRequest())
            } catch (e: MinoDomainException.Http) {
                thrown = e
            }

            assertEquals(409, thrown?.code)
        }

    @Test
    fun `updateMe는 PATCH로 요청을 보내고 data 봉투를 벗긴 응답을 반환한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY) { requested = it }

            val profile = service(engine).updateMe(profileRequest())

            assertEquals(HttpMethod.Patch, requested?.method)
            assertEquals(USERS_ME_PATH, requested?.url?.encodedPath)
            assertEquals(
                """{"nickname":"꾹이","avatar":{"color":"red"}}""",
                (requested?.body as TextContent).text,
            )
            assertEquals("u-1", profile.id)
        }

    // endregion

    /** 401 본문에 대해 `hasProfile()`이 `false`를 돌려주지 않고 `Http(401)`을 그대로 던지는지. */
    private suspend fun assertHasProfileRethrows(engine: MockEngine) {
        var thrown: MinoDomainException.Http? = null
        var returned: Boolean? = null

        try {
            returned = service(engine).hasProfile()
        } catch (e: MinoDomainException.Http) {
            thrown = e
        }

        assertEquals(null, returned)
        assertEquals(401, thrown?.code)
    }

    private fun errorBody(errorCode: String): String = """{"errorCode":"$errorCode","message":"메시지"}"""

    private companion object {
        /** `ProfileResponse`를 만족하지 않는 성공 본문. 게이트는 이것으로도 등록을 판정해야 한다. */
        const val REGISTRATION_ONLY_BODY = """{"data":{"id":1}}"""
    }
}
