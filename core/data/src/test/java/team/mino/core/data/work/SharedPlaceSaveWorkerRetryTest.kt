package team.mino.core.data.work

import android.content.Context
import android.content.ContextWrapper
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.datasource.PinRemoteDataSourceImpl
import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.dto.request.PinDuplicateRequest
import team.mino.core.data.network.dto.response.PinDetailResponse
import team.mino.core.data.network.extension.convertDomainException
import team.mino.core.data.network.service.PinApiService
import java.io.IOException

/**
 * 워커의 재시도 판정을 고정한다.
 *
 * `4xx`는 실서버로 결정적으로 재현할 수 없다 — 시트에는 내가 속한 방만 뜨므로 `403`을 만들 수 없고,
 * `400`을 유도할 입력도 서버 판정에 달려 있다. 이 판정은 실기기가 아니라 이 테스트가 소유한다
 * (`docs/specs/shared-link-receiver/quickstart.md` §5.5).
 *
 * 응답 코드별 처리는 `contracts/shared-place-save-api.md` §1.2가, 재시도 정책은 `research.md` R-005가,
 * 도메인 예외가 아닌 실패의 처리는 같은 문서 R-016이 소유한다.
 *
 * 판정이 실제 앱과 같은 예외 위에서 서도록 테스트 클라이언트에도 `convertDomainException`을 설치한다.
 * 이 플러그인이 없으면 워커는 `MinoDomainException`이 아니라 ktor의 `ResponseException`을 보게 되어
 * 판정 대상이 달라진다.
 */
class SharedPlaceSaveWorkerRetryTest {
    @Test
    fun `202 접수 응답은 성공으로 확정한다`() =
        runTest {
            val worker = worker(respondingWith(HttpStatusCode.Accepted))

            assertEquals(ListenableWorker.Result.success(), worker.doWork())
        }

    @Test
    fun `4xx는 재시도하지 않고 실패로 확정한다`() =
        runTest {
            listOf(
                HttpStatusCode.BadRequest,
                HttpStatusCode.Unauthorized,
                HttpStatusCode.Forbidden,
            ).forEach { status ->
                val worker = worker(respondingWith(status))

                assertEquals(
                    "$status 는 같은 입력으로 다시 보내도 결과가 같다 — retry가 아니라 failure다",
                    ListenableWorker.Result.failure(),
                    worker.doWork(),
                )
            }
        }

    @Test
    fun `5xx는 지수 백오프로 재시도한다`() =
        runTest {
            listOf(
                HttpStatusCode.BadGateway,
                HttpStatusCode.InternalServerError,
                HttpStatusCode.ServiceUnavailable,
            ).forEach { status ->
                val worker = worker(respondingWith(status))

                assertEquals(
                    "$status 는 서버 사정이므로 재시도로 넘긴다",
                    ListenableWorker.Result.retry(),
                    worker.doWork(),
                )
            }
        }

    @Test
    fun `네트워크 오류는 재시도한다`() =
        runTest {
            val worker = worker(MockEngine { throw IOException("no network") })

            assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        }

    // --- T047: 도메인 예외가 아닌 예외는 전파한다 (research.md R-016) ---

    @Test
    fun `도메인 예외가 아닌 예외는 삼키지 않고 전파한다`() =
        runTest {
            // MinoIdentityProofPlugin이 신원 증명 없이 요청이 나가면 checkNotNull로 던지는 경로다.
            // convertDomainException이 매핑하지 않으므로 워커에도 도메인 예외가 아닌 채로 도착한다.
            val worker = worker(ThrowingPinRemoteDataSource(IllegalStateException("신원 증명이 없다")))

            val thrown =
                try {
                    worker.doWork()
                    null
                } catch (e: Throwable) {
                    e
                }

            assertTrue(
                "프로그래머 버그를 failure로 흡수하면 조용한 저장 실패로 위장된다 — 실제로는 ${thrown ?: "예외 없음"}",
                thrown is IllegalStateException,
            )
        }

    @Test
    fun `입력 키 없이 예약된 워커는 실패로 흡수하지 않고 던진다`() =
        runTest {
            val worker = worker(ThrowingPinRemoteDataSource(AssertionError("요청까지 가면 안 된다")), Data.EMPTY)

            val thrown =
                try {
                    worker.doWork()
                    null
                } catch (e: Throwable) {
                    e
                }

            assertTrue(
                "입력 누락은 예약한 쪽의 버그다 — 전파해야 WorkManager가 이 실행만 FAILED로 확정한다. 실제로는 ${thrown ?: "예외 없음"}",
                thrown is IllegalArgumentException,
            )
        }

    private fun respondingWith(status: HttpStatusCode): MockEngine =
        MockEngine { respond(content = "", status = status) }

    private fun worker(
        engine: MockEngine,
        inputData: Data = defaultInputData,
    ): SharedPlaceSaveWorker = worker(dataSource(engine), inputData)

    private fun worker(
        dataSource: PinRemoteDataSource,
        inputData: Data = defaultInputData,
    ): SharedPlaceSaveWorker =
        TestListenableWorkerBuilder
            .from(ContextWrapper(null), SharedPlaceSaveWorker::class.java)
            .setInputData(inputData)
            .setWorkerFactory(
                // 워커가 @AssistedInject 생성자를 쓰므로 기본 팩토리의 리플렉션 생성이 성립하지 않는다.
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ): ListenableWorker = SharedPlaceSaveWorker(appContext, workerParameters, dataSource)
                },
            ).build()

    private fun dataSource(engine: MockEngine): PinRemoteDataSource =
        PinRemoteDataSourceImpl(
            PinApiService(
                HttpClient(engine) {
                    expectSuccess = true
                    convertDomainException()
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                },
            ),
        )

    /** 워커는 [createPin]만 부른다. 나머지는 홈·장소 상세가 쓰는 함수라 여기 닿으면 그것 자체가 실패다. */
    private class ThrowingPinRemoteDataSource(private val error: Throwable) : PinRemoteDataSource {
        override suspend fun createPin(request: PinCreateRequest): Unit = throw error

        override suspend fun getPinDetail(pinId: String): PinDetailResponse = throw UNCALLED

        override suspend fun recordAccess(pinId: String): Unit = throw UNCALLED

        override suspend fun duplicatePin(
            pinId: String,
            request: PinDuplicateRequest,
        ): Unit = throw UNCALLED
    }

    private companion object {
        val UNCALLED = IllegalStateException("워커가 부르지 않는 함수다")

        const val INSTAGRAM_URL = "https://www.instagram.com/p/XXXX/"

        val defaultInputData: Data =
            workDataOf(
                SharedPlaceSaveWorker.KEY_URL to INSTAGRAM_URL,
                SharedPlaceSaveWorker.KEY_ROOM_IDS to arrayOf("room-1"),
            )
    }
}
