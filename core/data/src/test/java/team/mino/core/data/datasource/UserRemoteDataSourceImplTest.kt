package team.mino.core.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.PROFILE_BODY
import team.mino.core.data.network.USERS_ME_PATH
import team.mino.core.data.network.USERS_PATH
import team.mino.core.data.network.UserApiServiceTest
import team.mino.core.data.network.jsonEngine
import team.mino.core.data.network.profileRequest
import team.mino.core.data.network.service.UserApiService

/**
 * 이 DataSource는 [UserApiService]로 **위임만** 한다(`core/data/README.md` §5). 그래서 여기서 고정하는 것은
 * 네 함수가 각각 대응하는 서비스 함수를 부르고 그 결과를 손대지 않고 돌려주는지, 그 하나뿐이다.
 *
 * 판정 로직은 이 파일이 소유하지 않는다:
 * - `401`이 미등록과 인증 실패를 겸하는 갈래(`errorCode`별 분기·JSON이 아닌 본문 포함)는 [UserApiServiceTest]
 * - 비2xx → `Http`, `IOException` → `Network` 매핑은 [team.mino.core.data.network.DomainExceptionMappingTest]
 *
 * 위임은 요청의 메서드·경로로 확인한다 — 네 서비스 함수가 서로 다른 메서드·경로를 쓰기 때문이다. 실패 갈래를
 * 보지 않으므로 클라이언트는 `data` 봉투를 벗기는 데 필요한 최소 구성만 갖는다.
 */
class UserRemoteDataSourceImplTest {
    private fun dataSource(engine: MockEngine): UserRemoteDataSourceImpl =
        UserRemoteDataSourceImpl(
            UserApiService(
                HttpClient(engine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                },
            ),
        )

    @Test
    fun `isRegistered는 hasProfile로 위임한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY) { requested = it }

            val registered = dataSource(engine).isRegistered()

            assertEquals(HttpMethod.Get, requested?.method)
            assertEquals(USERS_ME_PATH, requested?.url?.encodedPath)
            assertTrue(registered)
        }

    @Test
    fun `getMe는 getMe로 위임하고 응답을 그대로 돌려준다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY) { requested = it }

            val profile = dataSource(engine).getMe()

            assertEquals(HttpMethod.Get, requested?.method)
            assertEquals(USERS_ME_PATH, requested?.url?.encodedPath)
            assertEquals("u-1", profile?.id)
            assertEquals("꾹이", profile?.nickname)
            assertEquals("red", profile?.avatar?.color)
        }

    @Test
    fun `register는 요청을 그대로 넘기고 응답을 그대로 돌려준다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY, HttpStatusCode.Created) { requested = it }

            val profile = dataSource(engine).register(profileRequest())

            assertEquals(HttpMethod.Post, requested?.method)
            assertEquals(USERS_PATH, requested?.url?.encodedPath)
            assertEquals(
                """{"nickname":"꾹이","avatar":{"color":"red"}}""",
                (requested?.body as TextContent).text,
            )
            assertEquals("u-1", profile.id)
        }

    @Test
    fun `updateMe는 요청을 그대로 넘기고 응답을 그대로 돌려준다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(PROFILE_BODY) { requested = it }

            val profile = dataSource(engine).updateMe(profileRequest())

            assertEquals(HttpMethod.Patch, requested?.method)
            assertEquals(USERS_ME_PATH, requested?.url?.encodedPath)
            assertEquals(
                """{"nickname":"꾹이","avatar":{"color":"red"}}""",
                (requested?.body as TextContent).text,
            )
            assertEquals("u-1", profile.id)
        }
}
