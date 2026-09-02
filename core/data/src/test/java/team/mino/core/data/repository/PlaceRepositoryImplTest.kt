package team.mino.core.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.dto.request.PinDuplicateRequest
import team.mino.core.data.network.dto.response.PinDetailResponse
import team.mino.core.data.network.dto.response.PlaceResponse
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * `PlaceRepositoryImpl`이 **더하는 규칙**만 판정한다. 위임뿐인 함수와 DTO→도메인 변환은 컴파일과
 * `PlaceDetailMapperTest`가 이미 보증하므로 여기서는 계약이 코드 모양으로 드러나지 않는 것들을 본다.
 *
 * 1. [PlaceRepositoryImpl.recordAccess]는 `MinoDomainException`을 밖으로 내보내지 않고 재시도하지 않는다
 *    (EC-022·EC-023, `docs/specs/place-detail/contracts/place-repository.md` §1).
 * 2. 그 삼키기가 취소와 버그까지 먹지 않는다 — `CancellationException`은 코루틴 취소를 보존하려고,
 *    그 밖의 예외는 CEH에 닿아야 해서 그대로 올라가야 한다(`docs/conventions/error_handling.md` §3).
 * 3. `duplicatePin`은 받은 방 목록을 그대로 싣고 `409`를 흡수하지 않는다(D14).
 */
class PlaceRepositoryImplTest {
    private val pinRemoteDataSource = RecordingPinRemoteDataSource()
    private val repository = PlaceRepositoryImpl(pinRemoteDataSource)

    @Test
    fun `접근 기록의 도메인 예외는 밖으로 나가지 않는다`() =
        runTest {
            pinRemoteDataSource.recordAccessFailure = MinoDomainException.Http(code = 500, cause = IOException())

            // 예외가 새어 나가면 호출부가 try로 감싸야 하고, 그러면 EC-022가 호출부마다 새어 나간다.
            repository.recordAccess("pin-1")

            assertEquals(1, pinRemoteDataSource.recordAccessCallCount)
        }

    @Test
    fun `접근 기록이 실패해도 재시도하지 않는다`() =
        runTest {
            pinRemoteDataSource.recordAccessFailure = MinoDomainException.Network(cause = IOException())

            repository.recordAccess("pin-1")

            assertEquals("EC-023 — 실패한 기록을 다시 보내지 않는다", 1, pinRemoteDataSource.recordAccessCallCount)
        }

    @Test
    fun `접근 기록의 취소는 삼키지 않고 전파한다`() =
        runTest {
            assertRecordAccessRethrows(
                message = "취소를 삼키면 코루틴 취소가 깨진다",
                failure = CancellationException("cancelled"),
            )
        }

    @Test
    fun `접근 기록의 버그는 삼키지 않고 전파한다`() =
        runTest {
            assertRecordAccessRethrows(
                message = "도메인 예외가 아닌 것은 CEH에 닿아야 한다",
                failure = IllegalStateException("서버 계약 위반"),
            )
        }

    /**
     * [failure]가 `recordAccess`를 그대로 통과해 나오는지 본다.
     *
     * `assertThrows`가 아니라 던져진 것 자체를 비교하는 것은, 타입이 맞는 다른 예외로 바꿔치기되지 않았음까지
     * 확인해야 하기 때문이다. `Throwable`로 받는 것은 삼켜졌을 때 [thrown]이 `null`로 남아 그대로 실패하므로
     * 타입을 좁히지 않아도 판정이 무뎌지지 않는다.
     */
    private suspend fun assertRecordAccessRethrows(
        message: String,
        failure: Throwable,
    ) {
        pinRemoteDataSource.recordAccessFailure = failure

        val thrown =
            try {
                repository.recordAccess("pin-1")
                null
            } catch (e: Throwable) {
                e
            }

        assertSame(message, failure, thrown)
    }

    @Test
    fun `복제는 받은 방 목록을 그대로 싣는다`() =
        runTest {
            repository.duplicatePin(pinId = "pin-1", roomIds = listOf("room-2", "room-1"))

            assertEquals("pin-1", pinRemoteDataSource.lastDuplicatePinId)
            assertEquals(
                "정렬·중복 제거를 하지 않는다 — 화면이 고른 그대로가 서버에 간다",
                PinDuplicateRequest(roomIds = listOf("room-2", "room-1")),
                pinRemoteDataSource.lastDuplicateRequest,
            )
        }

    @Test
    fun `이미 저장된 방의 409는 그대로 올라온다`() =
        runTest {
            val conflict = MinoDomainException.Http(code = 409, cause = IOException())
            pinRemoteDataSource.duplicateFailure = conflict

            val thrown =
                try {
                    repository.duplicatePin(pinId = "pin-1", roomIds = listOf("room-2"))
                    null
                } catch (e: MinoDomainException) {
                    e
                }

            assertSame("409를 흡수하면 저장되지 않은 것이 저장된 것으로 보인다", conflict, thrown)
        }

    @Test
    fun `상세 조회는 응답을 도메인으로 옮겨 돌려준다`() =
        runTest {
            pinRemoteDataSource.pinDetail =
                PinDetailResponse(
                    id = "pin-1",
                    roomId = "room-1",
                    place =
                        PlaceResponse(
                            id = "place-1",
                            provider = "kakao",
                            providerPlaceId = "kakao-1",
                            name = "장소",
                            address = "서울시",
                            lat = 37.5,
                            lng = 127.0,
                            createdAt = "2026-09-01T00:00:00Z",
                            updatedAt = "2026-09-01T00:00:00Z",
                        ),
                    createdAt = "2026-09-01T00:00:00Z",
                )

            val detail = repository.getPlaceDetail("pin-1")

            // 두 식별자가 서로 다른 자리에서 오는 것이 이 경계의 핵심이다(data-model.md §1).
            assertEquals("pin-1", detail.pinId)
            assertEquals("place-1", detail.placeId)
            assertEquals("room-1", detail.roomId)
        }

    private class RecordingPinRemoteDataSource : PinRemoteDataSource {
        var pinDetail: PinDetailResponse? = null
        var recordAccessFailure: Throwable? = null
        var duplicateFailure: Throwable? = null

        var recordAccessCallCount: Int = 0
            private set

        var lastDuplicatePinId: String? = null
            private set

        var lastDuplicateRequest: PinDuplicateRequest? = null
            private set

        override suspend fun getPinDetail(pinId: String): PinDetailResponse =
            pinDetail ?: throw IllegalStateException("상세를 준비하지 않았다")

        override suspend fun createPin(request: PinCreateRequest) = throw IllegalStateException("부르지 않는다")

        override suspend fun recordAccess(pinId: String) {
            recordAccessCallCount++
            recordAccessFailure?.let { throw it }
        }

        override suspend fun duplicatePin(
            pinId: String,
            request: PinDuplicateRequest,
        ) {
            lastDuplicatePinId = pinId
            lastDuplicateRequest = request
            duplicateFailure?.let { throw it }
        }
    }
}
