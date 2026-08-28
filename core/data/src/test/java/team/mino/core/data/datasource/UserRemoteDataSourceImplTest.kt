package team.mino.core.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.extension.convertDomainException
import team.mino.core.data.network.service.UserApiService
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * 검증 대상은 `docs/specs/splash-screen/contracts/profile-registration.md`의 응답 대응표다 —
 * 단, `401`을 `Auth`로 재매핑하지 않고 validator가 만든 `Http(401)`을 그대로 전파한다.
 *
 * 핵심은 `401`을 통째로 "프로필 없음"으로 읽지 않는다는 것(SC-002)이라 401 갈래를 `errorCode`별로 모두 고정한다.
 */
class UserRemoteDataSourceImplTest {
    private fun dataSource(engine: MockEngine): UserRemoteDataSourceImpl =
        UserRemoteDataSourceImpl(
            UserApiService(
                HttpClient(engine) {
                    expectSuccess = true
                    convertDomainException()
                },
            ),
        )

    private fun unauthorized(body: String): MockEngine = MockEngine { respond(body, HttpStatusCode.Unauthorized) }

    @Test
    fun `200이면 등록된 것으로 본다`() =
        runTest {
            assertTrue(dataSource(MockEngine { respondOk("""{"data":{"id":1}}""") }).isRegistered())
        }

    @Test
    fun `users me 엔드포인트를 호출한다`() =
        runTest {
            val engine = MockEngine { respondOk("{}") }

            dataSource(engine).isRegistered()

            val requested = engine.requestHistory.single()

            assertEquals("/api/v1/users/me", requested.url.encodedPath)
        }

    @Test
    fun `401 USER_NOT_REGISTERED는 실패가 아니라 미등록이다`() =
        runTest {
            val engine = unauthorized("""{"errorCode":"USER_NOT_REGISTERED","message":"등록된 유저가 없습니다"}""")

            assertFalse(dataSource(engine).isRegistered())
        }

    @Test
    fun `401 UNAUTHORIZED는 Http 401 그대로 던진다`() =
        runTest {
            assertUnauthorizedRethrown("""{"errorCode":"UNAUTHORIZED","message":"인증이 필요합니다"}""")
        }

    @Test
    fun `401 TOKEN_EXPIRED는 Http 401 그대로 던진다`() =
        runTest {
            assertUnauthorizedRethrown("""{"errorCode":"TOKEN_EXPIRED","message":"토큰이 만료되었습니다"}""")
        }

    @Test
    fun `errorCode가 없는 401은 미등록으로 넘기지 않는다`() =
        runTest {
            assertUnauthorizedRethrown("""{"message":"unauthorized"}""")
        }

    @Test
    fun `모르는 errorCode의 401도 Http 401 그대로 던진다`() =
        runTest {
            assertUnauthorizedRethrown("""{"errorCode":"SOMETHING_NEW","message":"?"}""")
        }

    @Test
    fun `JSON이 아닌 401 본문도 Http 401로 던진다`() =
        runTest {
            assertUnauthorizedRethrown("<html>gateway</html>")
        }

    @Test
    fun `연결 실패는 Network로 던진다`() =
        runTest {
            val engine = MockEngine { throw IOException("no network") }
            var thrown: Throwable? = null

            try {
                dataSource(engine).isRegistered()
            } catch (e: MinoDomainException.Network) {
                thrown = e
            }

            assertNotNull(thrown)
        }

    @Test
    fun `401이 아닌 실패는 해석하지 않고 Http 그대로 재전파한다`() =
        runTest {
            val engine = MockEngine { respond("boom", HttpStatusCode.InternalServerError) }
            var thrown: MinoDomainException.Http? = null

            try {
                dataSource(engine).isRegistered()
            } catch (e: MinoDomainException.Http) {
                thrown = e
            }

            assertEquals(500, thrown?.code)
        }

    /** 401 본문 [body]에 대해 `isRegistered()`가 `false`를 돌려주지 않고 `Http(401)`을 그대로 던지는지. */
    private suspend fun assertUnauthorizedRethrown(body: String) {
        var thrown: MinoDomainException.Http? = null
        var returned: Boolean? = null

        try {
            returned = dataSource(unauthorized(body)).isRegistered()
        } catch (e: MinoDomainException.Http) {
            thrown = e
        }

        // false로 뭉개면 기존 사용자가 온보딩으로 떨어진다(SC-002).
        assertEquals(null, returned)
        assertEquals(401, thrown?.code)
    }
}
