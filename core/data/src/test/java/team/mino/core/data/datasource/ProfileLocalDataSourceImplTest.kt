package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import team.mino.core.domain.model.Profile
import java.io.File

/**
 * 프로필의 로컬 저장 계약을 판정한다.
 * 키 이름·미저장 판정·갱신 시점은 data-model.md §3이 소유하고, 표면은 contracts/profile-repository-contract.md §저장 계층이 소유한다.
 *
 * 공유 DataStore(`storage/DataStoreModule`)는 Context 위에 서므로 JVM 단위 테스트에서 띄울 수 없다.
 * 구현체가 인스턴스를 만들지 않고 주입받는 덕분에, 여기서는 임시 파일 위의 DataStore를 대신 넣어 같은 계약을 본다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileLocalDataSourceImplTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var localDataSource: ProfileLocalDataSource

    @Before
    fun setUp() {
        val file = File(temporaryFolder.root, "profile_test.preferences_pb")
        dataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope.backgroundScope,
                produceFile = { file },
            )
        localDataSource = ProfileLocalDataSourceImpl(dataStore)
    }

    @Test
    fun `저장된 적이 없으면 null을 흘린다`() =
        testScope.runTest {
            assertNull(localDataSource.observeProfile().first())
        }

    @Test
    fun `저장한 프로필이 그대로 돌아온다`() =
        testScope.runTest {
            val profile = Profile(nickname = "미노", avatarId = 3)

            localDataSource.saveProfile(profile)

            assertEquals(profile, localDataSource.observeProfile().first())
        }

    @Test
    fun `저장은 data-model이 정한 두 키를 쓴다`() =
        testScope.runTest {
            localDataSource.saveProfile(Profile(nickname = "미노", avatarId = 3))

            val preferences = dataStore.data.first()
            assertEquals("미노", preferences[NICKNAME_KEY])
            assertEquals(3, preferences[AVATAR_ID_KEY])
        }

    @Test
    fun `닉네임 키만 있으면 프로필이 없는 것으로 본다`() =
        testScope.runTest {
            dataStore.edit { it[NICKNAME_KEY] = "미노" }

            assertNull(localDataSource.observeProfile().first())
        }

    @Test
    fun `아바타 키만 있으면 프로필이 없는 것으로 본다`() =
        testScope.runTest {
            dataStore.edit { it[AVATAR_ID_KEY] = 3 }

            assertNull(localDataSource.observeProfile().first())
        }

    @Test
    fun `다시 저장하면 닉네임과 아바타가 함께 덮어써진다`() =
        testScope.runTest {
            localDataSource.saveProfile(Profile(nickname = "미노", avatarId = 3))

            localDataSource.saveProfile(Profile(nickname = "미노둘", avatarId = 7))

            assertEquals(Profile(nickname = "미노둘", avatarId = 7), localDataSource.observeProfile().first())
        }

    @Test
    fun `두 키를 한 번에 쓰므로 한쪽만 바뀐 중간 상태가 관측되지 않는다`() =
        testScope.runTest {
            // 키를 따로 쓰면 닉네임만 바뀌고 아바타가 이전 값으로 남은 프로필이 한 번 더 흘러나온다 (data-model.md §3)
            val before = Profile(nickname = "미노", avatarId = 3)
            val after = Profile(nickname = "미노둘", avatarId = 7)
            localDataSource.saveProfile(before)

            val observed = mutableListOf<Profile?>()
            val collectJob = backgroundScope.launch { localDataSource.observeProfile().toList(observed) }
            localDataSource.saveProfile(after)
            runCurrent()
            collectJob.cancel()

            assertEquals(listOf(before, after), observed)
        }

    private companion object {
        val NICKNAME_KEY = stringPreferencesKey("profile_nickname")
        val AVATAR_ID_KEY = intPreferencesKey("profile_avatar_id")
    }
}
