package team.mino.core.data.device

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class PushTokenProviderImpl @Inject constructor() : PushTokenProvider {
    // FirebaseMessaging.getInstance().token은 Task<String>을 반환한다. play-services-tasks의
    // Task.await() 확장을 새로 의존성 추가하지 않고, addOnCompleteListener를 suspend로 감싼다.
    override suspend fun getToken(): String =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    val exception = task.exception
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resumeWithException(
                            exception ?: IllegalStateException("FCM 토큰 발급 실패"),
                        )
                    }
                }
        }
}
