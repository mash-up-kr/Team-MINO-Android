package team.mino.core.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.auth.FakeAnonymousAuthProvider
import team.mino.core.errorhandling.MinoDomainException

/**
 * `ensureSession()`의 동작·실패 계약을 판정한다.
 * 계약은 contracts/anonymous-auth-repository.md §2가 소유하고, 멱등 구조의 근거는 research.md R-004다.
 */
class AnonymousAuthRepositoryImplTest {
    private val provider = FakeAnonymousAuthProvider()
    private val repository = AnonymousAuthRepositoryImpl(anonymousAuthProvider = provider)

    @Test
    fun `세션이 없으면 익명 로그인을 1회 수행하고 발급된 식별자를 반환한다`() =
        runTest {
            val session = repository.ensureSession()

            assertEquals(1, provider.signInCount)
            assertEquals(provider.issuedUserId, session.userId)
        }

    @Test
    fun `이미 확보된 세션이 있으면 로그인 없이 기존 식별자를 반환한다`() =
        runTest {
            provider.existingUserId = "restored-user-id"

            val session = repository.ensureSession()

            assertEquals(0, provider.signInCount)
            assertEquals("restored-user-id", session.userId)
        }

    @Test
    fun `재호출은 인증 제공자와 왕복하지 않고 같은 식별자를 돌려준다`() =
        runTest {
            val first = repository.ensureSession()
            val second = repository.ensureSession()

            assertEquals(1, provider.signInCount)
            assertEquals(first.userId, second.userId)
        }

    @Test
    fun `동시에 호출해도 식별자는 1개만 발급된다`() =
        runTest {
            val sessions =
                List(CONCURRENT_CALLS) { async { repository.ensureSession() } }.awaitAll()

            assertEquals(1, provider.signInCount)
            assertEquals(1, sessions.map { it.userId }.distinct().size)
        }

    @Test
    fun `연결 실패는 Network 리프 그대로 전파한다`() =
        runTest {
            val origin = MinoDomainException.Network(IllegalStateException("no connection"))
            provider.signInError = origin

            val result = runCatching { repository.ensureSession() }

            assertSame(origin, result.exceptionOrNull())
        }

    @Test
    fun `발급 실패는 Auth 리프 그대로 전파한다`() =
        runTest {
            val origin = MinoDomainException.Auth(IllegalStateException("issue failed"))
            provider.signInError = origin

            val result = runCatching { repository.ensureSession() }

            assertSame(origin, result.exceptionOrNull())
        }

    @Test
    fun `실패한 뒤 다시 호출하면 새로 확보를 시도한다`() =
        runTest {
            // 실패 결과를 캐시하면 호출자의 재시도(FR-005)가 영구히 같은 실패를 재생한다 — research.md R-004 기각 대안
            provider.signInError = MinoDomainException.Auth(IllegalStateException("issue failed"))
            val failed = runCatching { repository.ensureSession() }
            provider.signInError = null

            val session = repository.ensureSession()

            assertTrue(failed.exceptionOrNull() is MinoDomainException.Auth)
            assertEquals(2, provider.signInCount)
            assertEquals(provider.issuedUserId, session.userId)
        }

    private companion object {
        const val CONCURRENT_CALLS = 10
    }
}
