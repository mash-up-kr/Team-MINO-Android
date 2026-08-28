package team.mino.core.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.errorhandling.MinoDomainException

/**
 * 공유받은 링크를 사용자가 고른 방들에 저장하는 워커.
 *
 * **워커 하나가 요청 하나를 담당하며, 방 개수와 무관하게 하나다.** 서버가 `roomIds` 배열을 받으므로 방 단위
 * 분해가 서버 몫으로 돌아갔고, 그래서 재시도·실패 판정도 요청 단위로 내려진다
 * (`docs/specs/shared-link-receiver/research.md` R-021).
 *
 * 도메인 계약을 거치지 않고 같은 모듈의 [PinRemoteDataSource]를 직접 호출한다 — 전송용 함수를
 * 도메인 표면에 두지 않기 위해서다(같은 문서 R-017).
 */
@HiltWorker
internal class SharedPlaceSaveWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val pinRemoteDataSource: PinRemoteDataSource,
) : CoroutineWorker(appContext, params) {
    /**
     * 서버가 `202`로 접수만 하고 결과는 비동기로 확정하므로, 요청이 나가면 그대로 성공이다
     * (`docs/specs/shared-link-receiver/contracts/shared-place-save-api.md` §1.2).
     *
     * 실패 판정은 HTTP 상태 코드만 본다. `errorCode` 값에는 분기하지 않는다 — swagger가 `400`·`403`·`502`에
     * 같은 example을 달아 두어 값으로는 상황을 가를 수 없다(같은 문서 §1.2).
     */
    override suspend fun doWork(): Result {
        // 입력이 없으면 예약한 쪽의 버그다. 도메인 예외로 감싸거나 실패로 흡수하지 않고 전파한다
        // (data-model.md §4.1, research.md R-016). WorkManager가 이 실행만 FAILED로 확정한다.
        val url = requireNotNull(inputData.getString(KEY_URL)) { "$KEY_URL 없이 예약된 워커다" }
        val roomIds = inputData.getStringArray(KEY_ROOM_IDS)?.toList().orEmpty()
        require(roomIds.isNotEmpty()) { "$KEY_ROOM_IDS 없이 예약된 워커다" }

        return try {
            pinRemoteDataSource.createPin(PinCreateRequest(url = url, roomIds = roomIds))
            Result.success()
        } catch (networkFailure: MinoDomainException.Network) {
            // 연결이 돌아오면 같은 요청이 그대로 성립한다 (EC-009, research.md R-005).
            Result.retry()
        } catch (httpFailure: MinoDomainException.Http) {
            if (httpFailure.code in SERVER_ERROR_CODES) {
                // 서버 사정이므로 지수 백오프에 맡긴다.
                Result.retry()
            } else {
                // 4xx는 같은 입력으로 다시 보내도 결과가 같다 — 재시도는 배터리만 쓴다.
                Result.failure()
            }
        }
        // 그 밖의 예외는 잡지 않는다. 신원 증명 없이 나간 요청의 IllegalStateException 같은 프로그래머 버그를
        // failure로 흡수하면 조용한 저장 실패로 위장된다(research.md R-016, conventions/error_handling.md §1).
        // MinoDomainException.Auth도 판정하지 않는다 — §1.2에 대응하는 행이 없고, 세션은 설치가 살아 있는 동안
        // 만료되지 않는다는 전제(spec §4)에서 이 워커가 예약되는 경로에는 도달하지 않는다(R-016).
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_ROOM_IDS = "roomIds"

        private val SERVER_ERROR_CODES = 500..599
    }
}
