package team.mino.core.data.auth.extension

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

/**
 * 인증 제공자의 [Task]를 suspend로 변환하면서 실패를 도메인 예외로 매핑한다.
 *
 * 모든 인증 제공자 호출이 이 한 지점을 통과하므로 매핑 누락이 구조적으로 불가능하다 (research.md R-006).
 * 취소 전파·예외 원본 보존은 `kotlinx-coroutines-play-services`의 [await]에 맡긴다 (R-003).
 */
internal suspend fun <T> Task<T>.awaitDomain(): T =
    try {
        await()
    } catch (e: Throwable) {
        // 매핑에서 빠진 예외는 자기 자신이 돌아오므로 원본 그대로 rethrow된다
        throw e.toDomainExceptionOrSelf()
    }
