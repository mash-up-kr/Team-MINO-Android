package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.datasource.ProfileEntry
import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.data.network.dto.request.ProfileRequest
import team.mino.core.data.network.dto.response.ProfileResponse
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * 등록 여부 게이트는 값을 돌려주는 것 말고 **캐시를 무효화하는 일**도 한다.
 *
 * 서버가 모르는 세션의 프로필 캐시는 정의상 맞지 않는 값이다. 이것을 비우지 않으면 온보딩 화면이 낡은 값을
 * 프리필하고, 더 나쁘게는 `ProfileRepositoryImpl.saveProfile()`의 등록/수정 분기가 캐시를 보고 `PATCH`로
 * 갈라져 서버가 모르는 유저에게 수정 요청이 나간다(D38 · D50).
 */
class ProfileRegistrationRepositoryImplTest {
    private val remoteDataSource = FakeRegistrationRemoteDataSource()
    private val localDataSource = FakeRegistrationLocalDataSource()
    private val repository = ProfileRegistrationRepositoryImpl(remoteDataSource, localDataSource)

    @Test
    fun `미등록이면 프로필 캐시를 비운다`() =
        runTest {
            localDataSource.storedEntry.value = ProfileEntry(nickname = "민호", avatarName = "Person3")
            remoteDataSource.registered = false

            val registered = repository.isRegistered()

            assertFalse(registered)
            assertEquals(1, localDataSource.clearCount)
            assertNull("서버가 모르는 세션의 캐시는 남아 있으면 안 된다", localDataSource.storedEntry.value)
        }

    /** 값을 채우는 것은 `ProfileRepository.refreshProfile()`의 몫이다 — 이 판정은 값을 다루지 않는다. */
    @Test
    fun `등록되어 있으면 캐시를 건드리지 않는다`() =
        runTest {
            val cached = ProfileEntry(nickname = "민호", avatarName = "Person3")
            localDataSource.storedEntry.value = cached
            remoteDataSource.registered = true

            val registered = repository.isRegistered()

            assertTrue(registered)
            assertEquals(0, localDataSource.clearCount)
            assertEquals(cached, localDataSource.storedEntry.value)
        }

    /**
     * 실패는 미등록이 아니다. `false`로 뭉개지 않으므로 캐시를 비울 이유도 없다 — 네트워크가 끊겼다고 캐시를
     * 지우면 다음 진입에서 프리필이 사라진다.
     */
    @Test
    fun `조회가 실패하면 캐시를 비우지 않고 예외를 전파한다`() =
        runTest {
            val origin = MinoDomainException.Network(cause = IOException("offline"))
            localDataSource.storedEntry.value = ProfileEntry(nickname = "민호", avatarName = "Person3")
            remoteDataSource.error = origin

            val result = runCatching { repository.isRegistered() }

            assertSame(origin, result.exceptionOrNull())
            assertEquals(0, localDataSource.clearCount)
        }
}

private class FakeRegistrationRemoteDataSource : UserRemoteDataSource {
    var registered: Boolean = false
    var error: Throwable? = null

    override suspend fun isRegistered(): Boolean {
        error?.let { throw it }
        return registered
    }

    override suspend fun getMe(): ProfileResponse? = error("이 저장소는 프로필 값을 다루지 않는다")

    override suspend fun register(request: ProfileRequest): ProfileResponse = error("이 저장소는 저장하지 않는다")

    override suspend fun updateMe(request: ProfileRequest): ProfileResponse = error("이 저장소는 저장하지 않는다")

    override suspend fun putPushToken(
        token: String,
        platform: String,
    ) = error("푸시 토큰 등록은 PushRegistrationRepositoryImpl의 몫이다 — 이 저장소는 부르지 않는다")
}

private class FakeRegistrationLocalDataSource : ProfileLocalDataSource {
    val storedEntry = MutableStateFlow<ProfileEntry?>(null)
    var clearCount = 0

    override fun observeProfile(): Flow<ProfileEntry?> = storedEntry

    override suspend fun saveProfile(entry: ProfileEntry) {
        storedEntry.value = entry
    }

    override suspend fun clearProfile() {
        clearCount++
        storedEntry.value = null
    }
}
