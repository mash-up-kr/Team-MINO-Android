package team.mino.core.data.auth

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.auth.extension.toDomainExceptionOrSelf
import team.mino.core.errorhandling.MinoDomainException

/**
 * 인증 제공자 예외의 화이트리스트 매핑을 `Task` 없이 판정한다.
 * 네 갈래는 quickstart.md §2 표가 소유하고, 분류 기준은 contracts/identity-proof-attachment.md §2가 소유한다.
 */
class FirebaseAuthExceptionMappingTest {
    @Test
    fun `연결 실패는 Network 리프로 매핑한다`() {
        val origin = FirebaseNetworkException("no connection")

        val mapped = origin.toDomainExceptionOrSelf()

        assertTrue(mapped is MinoDomainException.Network)
        assertSame(origin, mapped.cause)
    }

    @Test
    fun `발급 실패 - 호출 한도 초과는 Auth 리프로 매핑한다`() {
        val origin = FirebaseTooManyRequestsException("quota exceeded")

        val mapped = origin.toDomainExceptionOrSelf()

        assertTrue(mapped is MinoDomainException.Auth)
        assertSame(origin, mapped.cause)
    }

    @Test
    fun `발급 실패 - 인증 구성 오류는 Auth 리프로 매핑한다`() {
        // 익명 인증 제공자가 꺼져 있을 때의 실패 (quickstart.md §3 V-6)
        val origin = FirebaseAuthException("ERROR_OPERATION_NOT_ALLOWED", "anonymous sign-in is disabled")

        val mapped = origin.toDomainExceptionOrSelf()

        assertTrue(mapped is MinoDomainException.Auth)
        assertSame(origin, mapped.cause)
    }

    @Test
    fun `열거 밖 Firebase 예외는 매핑하지 않고 원본을 그대로 돌려준다`() {
        // 매핑된 두 갈래의 상위 타입이다. 여기가 매핑되면 열거를 넓히는 분기가 들어간 것이다
        val origin = FirebaseException("unclassified failure")

        assertSame(origin, origin.toDomainExceptionOrSelf())
    }

    @Test
    fun `열거 밖 일반 예외는 매핑하지 않고 원본을 그대로 돌려준다`() {
        val origin = IllegalStateException("programmer error")

        assertSame(origin, origin.toDomainExceptionOrSelf())
    }

    @Test
    fun `CancellationException은 매핑하지 않고 원본을 그대로 돌려준다`() {
        val origin = CancellationException("cancelled")

        assertSame(origin, origin.toDomainExceptionOrSelf())
    }
}
