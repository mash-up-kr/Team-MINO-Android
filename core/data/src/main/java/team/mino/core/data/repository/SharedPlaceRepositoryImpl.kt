package team.mino.core.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import team.mino.core.data.work.SharedPlaceSaveWorker
import team.mino.core.domain.model.SharedPlaceSaveRequest
import team.mino.core.domain.repository.SharedPlaceRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 저장 요청을 워커 하나로 넘기는 경계다.
 *
 * DataSource를 직접 부르지 않는다 — 전송은 [SharedPlaceSaveWorker] 안에서 끝나고, 이 클래스가 확정하는 것은
 * "요청이 예약됐다"까지다 (`docs/specs/shared-link-receiver/research.md` R-017).
 */
internal class SharedPlaceRepositoryImpl @Inject constructor(
    private val workManager: WorkManager,
) : SharedPlaceRepository {
    /**
     * 고른 방 전부를 실은 워커 **하나**를 예약한다. 방마다 쪼개지 않는 것은 서버가 `roomIds` 배열을 받기
     * 때문이며, 한 방의 실패가 다른 방의 저장을 되돌리지 않는다는 보장(spec §4 가정·TS-019)은 이제 서버가
     * 방마다 갈라 처리하는 것으로 성립한다(research.md R-021).
     *
     * 고유 작업 이름을 쓰지 않는 것은 의도다. 같은 링크를 여러 번 공유하는 것은 정상 경로이므로 중복 예약을
     * 접거나 대체할 근거가 없다.
     *
     * 재시도 여부의 판정은 [SharedPlaceSaveWorker]가 소유하지만, 제약과 백오프는 `WorkRequest`에만 실을 수 있어
     * 여기서 건다(`docs/adr/2026-08-26-workmanager-for-detached-requests.md`).
     */
    override fun scheduleSave(request: SharedPlaceSaveRequest) {
        // enqueue는 Operation을 돌려주지만 기다리지 않는다. 계약이 즉시 반환을 요구한다(data-model.md §2.2).
        workManager.enqueue(
            OneTimeWorkRequestBuilder<SharedPlaceSaveWorker>()
                .setInputData(
                    workDataOf(
                        SharedPlaceSaveWorker.KEY_URL to request.url,
                        SharedPlaceSaveWorker.KEY_ROOM_IDS to request.roomIds.toTypedArray(),
                    ),
                ).setConstraints(NETWORK_CONNECTED)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    private companion object {
        /** 오프라인이면 실행되지 않고 연결이 돌아올 때까지 대기한다 — 재시도가 아니라 실행 조건이다(EC-009). */
        val NETWORK_CONNECTED: Constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /**
         * 워커가 `Result.retry()`를 낸 뒤 첫 재시도까지의 지연. WorkManager 기본값(30초)에 기대지 않고
         * 허용 최소치를 명시한다 — 계약(`contracts/shared-place-save-api.md` §1.2)이 "지수 백오프"까지만 정하고
         * 초기 지연은 정하지 않았으며, 사용자가 방금 요청한 저장이라 5xx 같은 일시 장애에서 가능한 한 빨리
         * 다시 보내는 편이 낫다. 이후 지연은 지수로 늘어나므로 되풀이 비용은 제한된다.
         */
        const val BACKOFF_DELAY_MILLIS = WorkRequest.MIN_BACKOFF_MILLIS
    }
}
