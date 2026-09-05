package team.mino.core.data.push.extension

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * FCM Messaging SDK의 [Task]를 suspend로 변환하면서 실패를 도메인 예외로 매핑한다.
 *
 * 모든 Messaging SDK 호출이 이 한 지점을 통과하므로 매핑 누락이 구조적으로 불가능하다.
 * 인증 제공자용(`auth/extension/Task.kt`)과 형태는 같지만 재사용하지 않는다 — 그쪽 화이트리스트는
 * `FirebaseAuthException` 전용이라 Messaging 예외가 전부 열거 밖으로 새기 때문이다
 * (docs/specs/push-notification/research.md D10).
 * 매핑의 성질(화이트리스트 열거·열거 밖 rethrow·`CancellationException` 보존)은
 * docs/conventions/error_handling.md §3이 소유한다.
 * 취소 전파·예외 원본 보존은 `kotlinx-coroutines-play-services`의 [await]에 맡긴다.
 */
internal suspend fun <T> Task<T>.awaitDomain(): T =
    try {
        await()
    } catch (e: Throwable) {
        // 매핑에서 빠진 예외는 자기 자신이 돌아오므로 원본 그대로 rethrow된다
        throw e.toDomainExceptionOrSelf()
    }

private fun Throwable.toDomainExceptionOrSelf(): Throwable =
    when (this) {
        // 연결 실패
        is FirebaseNetworkException -> MinoDomainException.Network(this)
        // 토큰 조회 실패 — Messaging SDK는 "서비스 불가"·"전달자 서비스 없음" 같은 예상 가능한 실패를
        // IOException으로 던진다. HTTP 원천이 IOException 계열을 Network로 분류하는 것과 같은 갈래다
        // (docs/conventions/error_handling.md §3 표). CancellationException은 IOException이 아니라 여기에 걸리지 않는다
        is IOException -> MinoDomainException.Network(this)
        // CancellationException과 열거 밖 예외는 여기로 와 원본 그대로 돌아가야 한다
        // — 공통 상위 타입(FirebaseException·RuntimeException 등) 분기를 추가해 위에서 가로채지 말 것
        else -> this
    }
