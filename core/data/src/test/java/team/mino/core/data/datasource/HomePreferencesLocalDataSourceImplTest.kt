package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * 홈이 기기에 남기는 두 값의 저장 계약을 판정한다. 무엇을 영속하고 무엇을 하지 않는지는
 * `docs/specs/home-deck-exploration/research.md` R-004가 소유한다.
 *
 * 공유 DataStore(`storage/DataStoreModule`)는 Context 위에 서므로 JVM 단위 테스트에서 띄울 수 없다.
 * 구현체가 인스턴스를 만들지 않고 주입받는 덕분에, 여기서는 임시 파일 위의 DataStore를 대신 넣어 같은 계약을 본다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomePreferencesLocalDataSourceImplTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var localDataSource: HomePreferencesLocalDataSource

    @Before
    fun setUp() {
        val file = File(temporaryFolder.root, "home_test.preferences_pb")
        dataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope.backgroundScope,
                produceFile = { file },
            )
        localDataSource = HomePreferencesLocalDataSourceImpl(dataStore)
    }

    @Test
    fun `저장된 적이 없으면 마지막 방은 null이고 가이드는 닫히지 않은 것으로 본다`() =
        testScope.runTest {
            assertNull(localDataSource.getLastRoomId())
            assertFalse(localDataSource.isGuideDismissed())
        }

    @Test
    fun `마지막 방은 마지막에 저장한 값으로 덮어써진다`() =
        testScope.runTest {
            localDataSource.setLastRoomId("room-1")
            localDataSource.setLastRoomId("room-2")

            assertEquals("room-2", localDataSource.getLastRoomId())
        }

    @Test
    fun `가이드를 닫으면 닫힌 이력이 남는다`() =
        testScope.runTest {
            localDataSource.dismissGuide()

            assertTrue(localDataSource.isGuideDismissed())
        }

    @Test
    fun `두 값은 서로를 건드리지 않는다`() =
        testScope.runTest {
            localDataSource.dismissGuide()

            assertNull(localDataSource.getLastRoomId())
        }
}
