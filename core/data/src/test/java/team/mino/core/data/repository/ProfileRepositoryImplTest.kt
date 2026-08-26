package team.mino.core.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository
import java.io.IOException

/**
 * 이번 범위의 Repository는 원격도 매퍼도 없어 로컬 DataSource 위임이 전부다
 * (contracts/profile-repository-contract.md §저장 계층). 위임에 무엇도 끼어들지 않는 것을 판정한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryImplTest {
    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val localDataSource = FakeProfileLocalDataSource()
    private val repository: ProfileRepository = ProfileRepositoryImpl(localDataSource)

    @Test
    fun `DataSource에 값이 없으면 null을 그대로 흘린다`() =
        testScope.runTest {
            assertNull(repository.observeProfile().first())
        }

    @Test
    fun `DataSource가 흘리는 값을 바꾸지 않고 그대로 흘린다`() =
        testScope.runTest {
            val stored = Profile(nickname = "미노", avatarId = 3)
            localDataSource.storedProfile.value = stored

            assertSame(stored, repository.observeProfile().first())
        }

    @Test
    fun `DataSource가 값을 바꿀 때마다 새 값이 이어서 흘러나온다`() =
        testScope.runTest {
            val first = Profile(nickname = "미노", avatarId = 3)
            val second = Profile(nickname = "미노둘", avatarId = 7)

            val observed = mutableListOf<Profile?>()
            val collectJob = backgroundScope.launch { repository.observeProfile().toList(observed) }
            localDataSource.storedProfile.value = first
            localDataSource.storedProfile.value = second
            runCurrent()
            collectJob.cancel()

            assertEquals(listOf(null, first, second), observed)
        }

    @Test
    fun `saveProfile은 받은 프로필을 그대로 DataSource에 넘긴다`() =
        testScope.runTest {
            val profile = Profile(nickname = "미노", avatarId = 3)

            repository.saveProfile(profile)

            assertEquals(1, localDataSource.savedProfiles.size)
            assertSame(profile, localDataSource.savedProfiles.single())
        }

    @Test
    fun `저장한 값은 곧바로 observeProfile로 흘러나온다`() =
        testScope.runTest {
            val profile = Profile(nickname = "미노", avatarId = 3)

            repository.saveProfile(profile)

            assertSame(profile, repository.observeProfile().first())
        }

    @Test
    fun `DataSource가 던진 실패를 잡지 않고 그대로 전파한다`() =
        testScope.runTest {
            // 예외를 Result로 바꾸지 않는다 — 소비는 ViewModel의 runCatchingDomain이 한다 (repository 계약 §Repository)
            val origin = IOException("disk failure")
            localDataSource.saveError = origin

            val result = runCatching { repository.saveProfile(Profile(nickname = "미노", avatarId = 3)) }

            assertSame(origin, result.exceptionOrNull())
        }
}

private class FakeProfileLocalDataSource : ProfileLocalDataSource {
    val storedProfile = MutableStateFlow<Profile?>(null)
    val savedProfiles = mutableListOf<Profile>()
    var saveError: Throwable? = null

    override fun observeProfile(): Flow<Profile?> = storedProfile

    override suspend fun saveProfile(profile: Profile) {
        savedProfiles += profile
        saveError?.let { throw it }
        storedProfile.value = profile
    }
}
