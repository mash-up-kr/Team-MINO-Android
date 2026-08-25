package team.mino.core.data.auth

import com.google.firebase.auth.FirebaseAuth
import team.mino.core.data.auth.extension.awaitDomain
import javax.inject.Inject

internal class AnonymousAuthProviderImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AnonymousAuthProvider {
    // currentUser는 SDK가 초기화 때 복원해 메모리에 들고 있는 값이라 조회에 네트워크 왕복이 없다.
    override suspend fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override suspend fun signInAnonymously(): String =
        // 성공한 익명 로그인에 사용자가 없는 것은 SDK 계약 위반이다.
        // 도메인 예외로 감싸지 않고 버그로 전파한다 — docs/conventions/error_handling.md §3
        checkNotNull(firebaseAuth.signInAnonymously().awaitDomain().user) {
            "익명 로그인이 성공했으나 사용자가 없다"
        }.uid
}
