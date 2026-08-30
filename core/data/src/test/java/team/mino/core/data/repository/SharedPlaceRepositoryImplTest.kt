package team.mino.core.data.repository

import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.work.SharedPlaceSaveWorker
import team.mino.core.domain.model.SharedPlaceSaveRequest
import java.util.UUID

/**
 * 예약이 **요청 하나**로 묶이는지를 고정한다.
 *
 * 서버가 `roomIds` 배열을 받으므로 방 단위 분해가 서버 몫으로 돌아갔다. spec §4 가정("한 방의 실패가
 * 다른 방의 저장을 되돌리지 않는다")과 TS-019(부분 실패)는 이제 서버가 지키며, 클라이언트가 지키는 것은
 * 고른 방이 하나도 빠지지 않고 한 요청에 실리는 것이다
 * (`docs/specs/shared-link-receiver/research.md` R-021).
 *
 * `WorkManager`는 실제 인스턴스를 띄우지 않고 더블로 세워 `enqueue`에 넘어온 요청을 붙잡는다.
 */
class SharedPlaceRepositoryImplTest {
    private val workManager = RecordingWorkManager()
    private val repository = SharedPlaceRepositoryImpl(workManager)

    @Test
    fun `방을 여러 개 골라도 워커는 하나다`() {
        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = listOf("r1", "r2", "r3")))

        assertEquals(
            "방마다 쪼개 예약하면 요청도 방마다 나간다 — 서버가 배열을 받으므로 분해는 서버 몫이다",
            1,
            workManager.enqueued.size,
        )
    }

    @Test
    fun `예약된 워커에 고른 방이 모두 실린다`() {
        val roomIds = listOf("r1", "r2", "r3")

        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = roomIds))

        assertEquals(
            roomIds,
            workManager.enqueued
                .single()
                .inputStringArray(SharedPlaceSaveWorker.KEY_ROOM_IDS)
                ?.toList(),
        )
    }

    @Test
    fun `예약된 워커에 공유받은 url이 실린다`() {
        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = listOf("r1", "r2")))

        assertEquals(INSTAGRAM_URL, workManager.enqueued.single().inputString(SharedPlaceSaveWorker.KEY_URL))
    }

    @Test
    fun `방이 하나여도 워커는 하나다`() {
        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = listOf("r1")))

        assertEquals(1, workManager.enqueued.size)
        assertEquals(
            listOf("r1"),
            workManager.enqueued
                .single()
                .inputStringArray(SharedPlaceSaveWorker.KEY_ROOM_IDS)
                ?.toList(),
        )
    }

    // --- T050: 실행 조건과 백오프 (EC-009 · research.md R-005) ---
    // 워커가 하나로 줄어도 제약과 백오프는 그 하나에 그대로 걸려야 한다.

    @Test
    fun `예약된 워커는 연결된 네트워크를 실행 조건으로 갖는다`() {
        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = listOf("r1", "r2")))

        assertEquals(
            "제약이 없으면 오프라인에서 워커가 그대로 실행돼 실패로 확정된다 — EC-009는 대기여야 한다",
            NetworkType.CONNECTED,
            workManager.enqueued.single().requiredNetworkType,
        )
    }

    @Test
    fun `예약된 워커는 지수 백오프로 재시도한다`() {
        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = listOf("r1", "r2")))

        assertEquals(
            "5xx 재시도가 선형으로 되풀이되면 장애 중인 서버를 그대로 두드린다",
            BackoffPolicy.EXPONENTIAL,
            workManager.enqueued.single().backoffPolicy,
        )
    }

    @Test
    fun `첫 재시도 지연은 WorkManager 기본값이 아니라 명시한 값이다`() {
        repository.scheduleSave(SharedPlaceSaveRequest(url = INSTAGRAM_URL, roomIds = listOf("r1")))

        assertEquals(
            "계약이 초기 지연을 정하지 않아 구현이 택한 값이다 — 바꾸려면 SharedPlaceRepositoryImpl의 근거부터 고쳐라",
            WorkRequest.MIN_BACKOFF_MILLIS,
            workManager.enqueued.single().backoffDelayMillis,
        )
    }

    @Suppress("RestrictedApi")
    private fun WorkRequest.inputString(key: String): String? = workSpec.input.getString(key)

    @Suppress("RestrictedApi")
    private fun WorkRequest.inputStringArray(key: String): Array<String>? = workSpec.input.getStringArray(key)

    @Suppress("RestrictedApi")
    private val WorkRequest.requiredNetworkType: NetworkType get() = workSpec.constraints.requiredNetworkType

    @Suppress("RestrictedApi")
    private val WorkRequest.backoffPolicy: BackoffPolicy get() = workSpec.backoffPolicy

    @Suppress("RestrictedApi")
    private val WorkRequest.backoffDelayMillis: Long get() = workSpec.backoffDelayDuration

    private companion object {
        const val INSTAGRAM_URL = "https://www.instagram.com/p/XXXX/"
    }
}

/**
 * `enqueue`에 넘어온 요청만 붙잡는 더블. 그 밖의 표면은 이 테스트가 쓰지 않는다.
 *
 * `WorkManager`의 생성자는 `internal`이라 Kotlin에서 상속이 막혀 있고, androidx가 대신 권하는
 * `WorkManagerTestInitHelper`는 실제 `Context`(Robolectric)를 요구한다. 이 모듈의 테스트 의존성에
 * Robolectric이 없으므로 가시성만 열고 더블을 세운다 — 예약 여부만 관측하면 되는 테스트다.
 */
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
private class RecordingWorkManager : WorkManager() {
    val enqueued = mutableListOf<WorkRequest>()

    override fun enqueue(requests: List<WorkRequest>): Operation {
        enqueued += requests
        // 예약은 즉시 반환하고 결과를 기다리지 않으므로(data-model.md §2.2) 이 Operation은 읽히지 않는다.
        return NoOpOperation
    }

    override fun beginWith(requests: List<OneTimeWorkRequest>): WorkContinuation = unused()

    override fun beginUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        requests: List<OneTimeWorkRequest>,
    ): WorkContinuation = unused()

    override fun enqueueUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        requests: List<OneTimeWorkRequest>,
    ): Operation = unused()

    override fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        request: PeriodicWorkRequest,
    ): Operation = unused()

    override fun cancelWorkById(id: UUID): Operation = unused()

    override fun cancelAllWorkByTag(tag: String): Operation = unused()

    override fun cancelUniqueWork(uniqueWorkName: String): Operation = unused()

    override fun cancelAllWork(): Operation = unused()

    override fun createCancelPendingIntent(id: UUID): PendingIntent = unused()

    override fun pruneWork(): Operation = unused()

    override fun getLastCancelAllTimeMillisLiveData(): LiveData<Long> = unused()

    override fun getLastCancelAllTimeMillis(): ListenableFuture<Long> = unused()

    override fun getWorkInfoByIdLiveData(id: UUID): LiveData<WorkInfo?> = unused()

    override fun getWorkInfoByIdFlow(id: UUID): Flow<WorkInfo?> = unused()

    override fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo?> = unused()

    override fun getWorkInfosByTagLiveData(tag: String): LiveData<List<WorkInfo>> = unused()

    override fun getWorkInfosByTagFlow(tag: String): Flow<List<WorkInfo>> = unused()

    override fun getWorkInfosByTag(tag: String): ListenableFuture<List<WorkInfo>> = unused()

    override fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String): LiveData<List<WorkInfo>> = unused()

    override fun getWorkInfosForUniqueWorkFlow(uniqueWorkName: String): Flow<List<WorkInfo>> = unused()

    override fun getWorkInfosForUniqueWork(uniqueWorkName: String): ListenableFuture<List<WorkInfo>> = unused()

    override fun getWorkInfosLiveData(workQuery: WorkQuery): LiveData<List<WorkInfo>> = unused()

    override fun getWorkInfosFlow(workQuery: WorkQuery): Flow<List<WorkInfo>> = unused()

    override fun getWorkInfos(workQuery: WorkQuery): ListenableFuture<List<WorkInfo>> = unused()

    override fun updateWork(request: WorkRequest): ListenableFuture<UpdateResult> = unused()

    override val configuration: Configuration get() = unused()
}

private object NoOpOperation : Operation {
    override fun getState(): LiveData<Operation.State> = unused()

    override fun getResult(): ListenableFuture<Operation.State.SUCCESS> = unused()
}

private fun unused(): Nothing = error("이 테스트가 쓰지 않는 표면이다")
