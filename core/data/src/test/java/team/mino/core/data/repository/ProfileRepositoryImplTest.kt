package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.datasource.ProfileEntry
import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.data.network.dto.request.AvatarRequest
import team.mino.core.data.network.dto.request.ProfileRequest
import team.mino.core.data.network.dto.response.AvatarResponse
import team.mino.core.data.network.dto.response.ProfileResponse
import team.mino.core.domain.model.Profile
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * 원격이 원천이고 로컬은 캐시인 저장소의 계약을 판정한다
 * (contracts/profile-repository-contract.md §저장의 불변식 · research.md D36·D38·D42).
 *
 * 판정하는 것은 네 가지다.
 * ① 캐시 유무로 등록/수정이 갈린다 — 저장소 전체에서 분기는 여기 하나뿐이다(D38).
 * ② **원격 성공 → 캐시 갱신** 순서다. 원격이 실패하면 캐시는 손대지 않은 채로 남는다(FR-012·SC-006).
 * ③ `refreshProfile()`의 미등록은 실패가 아니다 — 캐시를 비우고 정상 종료한다.
 * ④ `409`는 저장 실패로 올라간다. 자동으로 `PATCH`로 갈아타지 않는다(D38).
 *
 * ②와 ④는 기기에서 재현할 수 없는 경로다(tasks.md §미결 7). 그 자리를 이 테스트가 대신한다.
 *
 * 저장 성공 뒤 캐시에 쓰이는 값이 **응답을 옮긴 것**인지 **저장한 프로필 그대로**인지는 계약이 정하지 않았다.
 * 그래서 Fake 원격은 요청을 그대로 되비추고, 둘 중 어느 구현이든 같은 캐시가 남는 것만 판정한다.
 */
class ProfileRepositoryImplTest {
    private val remoteDataSource = FakeUserRemoteDataSource()
    private val localDataSource = FakeProfileLocalDataSource()
    private val repository: ProfileRepository =
        ProfileRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
        )

    // --- observeProfile: 캐시가 흘리는 것을 도메인으로 옮긴다 ---

    @Test
    fun `캐시가 비어 있으면 null을 흘린다`() =
        runTest {
            assertNull(repository.observeProfile().first())
        }

    @Test
    fun `캐시에 담긴 항목을 도메인 프로필로 옮겨 흘린다`() =
        runTest {
            localDataSource.storedEntry.value = ProfileEntry(nickname = "미노", avatarName = "Person3")

            assertEquals(Profile(nickname = "미노", avatar = ProfileAvatar.Person3), repository.observeProfile().first())
        }

    // --- ① 저장의 분기: 캐시가 비었으면 등록, 있으면 수정 (불변식 2 · D38) ---

    @Test
    fun `캐시가 비어 있으면 등록으로 나간다`() =
        runTest {
            repository.saveProfile(Profile(nickname = "미노", avatar = ProfileAvatar.Person3))

            assertEquals(
                "캐시가 비었다는 것은 아직 서버에 유저가 없다는 뜻이다 — 수정으로 나가면 없는 유저를 고치려 든다",
                1,
                remoteDataSource.registerRequests.size,
            )
            assertTrue(remoteDataSource.updateRequests.isEmpty())
        }

    @Test
    fun `캐시에 프로필이 있으면 수정으로 나간다`() =
        runTest {
            localDataSource.storedEntry.value = ProfileEntry(nickname = "미노", avatarName = "Person1")

            repository.saveProfile(Profile(nickname = "미노둘", avatar = ProfileAvatar.Person10))

            assertEquals(
                "이미 등록된 유저에 등록을 다시 보내면 409로 되돌아온다",
                1,
                remoteDataSource.updateRequests.size,
            )
            assertTrue(remoteDataSource.registerRequests.isEmpty())
        }

    @Test
    fun `요청에는 닉네임과 아바타 색이 함께 실린다`() =
        runTest {
            repository.saveProfile(Profile(nickname = "미노", avatar = ProfileAvatar.Person3))

            assertEquals(
                "부분 전송을 하지 않는다 — 두 값은 언제나 함께 나간다(불변식 4)",
                ProfileRequest(nickname = "미노", avatar = AvatarRequest(color = "orange")),
                remoteDataSource.registerRequests.single(),
            )
        }

    @Test
    fun `수정 요청에도 두 값이 함께 실린다`() =
        runTest {
            localDataSource.storedEntry.value = ProfileEntry(nickname = "미노", avatarName = "Person1")

            repository.saveProfile(Profile(nickname = "미노둘", avatar = ProfileAvatar.Person10))

            assertEquals(
                ProfileRequest(nickname = "미노둘", avatar = AvatarRequest(color = "brown")),
                remoteDataSource.updateRequests.single(),
            )
        }

    @Test
    fun `등록에 성공하면 캐시가 갱신된다`() =
        runTest {
            repository.saveProfile(Profile(nickname = "미노", avatar = ProfileAvatar.Person3))

            assertEquals(
                ProfileEntry(nickname = "미노", avatarName = "Person3"),
                localDataSource.savedEntries.single(),
            )
        }

    @Test
    fun `수정에 성공하면 캐시가 새 값으로 바뀐다`() =
        runTest {
            localDataSource.storedEntry.value = ProfileEntry(nickname = "미노", avatarName = "Person1")

            repository.saveProfile(Profile(nickname = "미노둘", avatar = ProfileAvatar.Person10))

            assertEquals(
                Profile(nickname = "미노둘", avatar = ProfileAvatar.Person10),
                repository.observeProfile().first(),
            )
        }

    // --- ② 원격 실패 시 캐시 불변 (불변식 1 · FR-012 · SC-006) ---

    @Test
    fun `등록이 실패하면 캐시를 건드리지 않는다`() =
        runTest {
            remoteDataSource.registerError = MinoDomainException.Http(code = 500, cause = IOException("boom"))

            runCatching { repository.saveProfile(Profile(nickname = "미노", avatar = ProfileAvatar.Person3)) }

            assertTrue(
                "원격이 실패했는데 캐시가 쓰이면 화면을 다시 열었을 때 저장되지 않은 값이 프리필된다(FR-012·SC-006)",
                localDataSource.savedEntries.isEmpty(),
            )
            assertNull(repository.observeProfile().first())
        }

    @Test
    fun `수정이 실패하면 이전 캐시가 그대로 남는다`() =
        runTest {
            val cached = ProfileEntry(nickname = "미노", avatarName = "Person1")
            localDataSource.storedEntry.value = cached
            remoteDataSource.updateError = MinoDomainException.Http(code = 500, cause = IOException("boom"))

            runCatching { repository.saveProfile(Profile(nickname = "미노둘", avatar = ProfileAvatar.Person10)) }

            assertTrue(localDataSource.savedEntries.isEmpty())
            assertEquals(
                "실패한 저장의 값이 캐시에 남으면 재진입 시 저장되지 않은 닉네임이 프리필된다",
                Profile(nickname = "미노", avatar = ProfileAvatar.Person1),
                repository.observeProfile().first(),
            )
        }

    @Test
    fun `저장 실패는 잡지 않고 그대로 전파한다`() =
        runTest {
            // 예외를 Result로 바꾸지 않는다 — 소비는 ViewModel의 runCatchingDomain이 한다(계약 §Repository)
            val origin = MinoDomainException.Network(IOException("no connection"))
            remoteDataSource.registerError = origin

            val profile = Profile(nickname = "미노", avatar = ProfileAvatar.Person3)

            val result = runCatching { repository.saveProfile(profile) }

            assertSame(origin, result.exceptionOrNull())
        }

    // --- ④ 409의 전파 (불변식 3 · D38) ---

    @Test
    fun `등록이 409로 거절되면 수정으로 갈아타지 않는다`() =
        runTest {
            remoteDataSource.registerError = MinoDomainException.Http(code = 409, cause = IOException("already"))

            runCatching { repository.saveProfile(Profile(nickname = "미노", avatar = ProfileAvatar.Person3)) }

            assertTrue(
                "자동 폴백을 두면 캐시와 서버가 어긋난 상태가 조용히 덮인다 — 복구는 다음 진입의 refreshProfile이 맡는다",
                remoteDataSource.updateRequests.isEmpty(),
            )
        }

    @Test
    fun `409는 저장 실패로 올라간다`() =
        runTest {
            val origin = MinoDomainException.Http(code = 409, cause = IOException("already"))
            remoteDataSource.registerError = origin

            val profile = Profile(nickname = "미노", avatar = ProfileAvatar.Person3)

            val result = runCatching { repository.saveProfile(profile) }

            assertSame(origin, result.exceptionOrNull())
        }

    @Test
    fun `409로 끝난 저장은 캐시를 남기지 않는다`() =
        runTest {
            remoteDataSource.registerError = MinoDomainException.Http(code = 409, cause = IOException("already"))

            runCatching { repository.saveProfile(Profile(nickname = "미노", avatar = ProfileAvatar.Person3)) }

            assertTrue(localDataSource.savedEntries.isEmpty())
            assertNull(repository.observeProfile().first())
        }

    // --- ③ refreshProfile: 미등록은 실패가 아니다 ---

    @Test
    fun `미등록이면 캐시를 비운다`() =
        runTest {
            localDataSource.storedEntry.value = ProfileEntry(nickname = "미노", avatarName = "Person1")
            remoteDataSource.profileOnServer = null

            repository.refreshProfile()

            assertEquals(1, localDataSource.clearCount)
            assertNull(repository.observeProfile().first())
        }

    @Test
    fun `미등록이어도 예외를 던지지 않는다`() =
        runTest {
            remoteDataSource.profileOnServer = null

            val result = runCatching { repository.refreshProfile() }

            assertNull(
                "온보딩 진입에서 미등록은 정상 상태다 — 던지면 처음 들어온 사용자가 오류를 본다",
                result.exceptionOrNull(),
            )
        }

    @Test
    fun `서버에 프로필이 있으면 캐시에 옮겨 담는다`() =
        runTest {
            remoteDataSource.profileOnServer = profileResponse(nickname = "미노", color = "orange")

            repository.refreshProfile()

            assertEquals(0, localDataSource.clearCount)
            assertEquals(
                Profile(nickname = "미노", avatar = ProfileAvatar.Person3),
                repository.observeProfile().first(),
            )
        }

    @Test
    fun `조회 실패는 그대로 전파하고 캐시를 비우지 않는다`() =
        runTest {
            val cached = ProfileEntry(nickname = "미노", avatarName = "Person1")
            localDataSource.storedEntry.value = cached
            val origin = MinoDomainException.Network(IOException("no connection"))
            remoteDataSource.getError = origin

            val result = runCatching { repository.refreshProfile() }

            assertSame(origin, result.exceptionOrNull())
            assertEquals(
                "미등록만 캐시를 비운다 — 네트워크가 끊겼다고 캐시를 지우면 오프라인에서 프리필이 사라진다",
                0,
                localDataSource.clearCount,
            )
        }
}

private fun profileResponse(
    nickname: String,
    color: String,
): ProfileResponse =
    ProfileResponse(
        id = "user-1",
        nickname = nickname,
        avatar = AvatarResponse(color = color),
        createdAt = "2026-08-28T00:00:00Z",
    )

/**
 * 서버 상태를 값 하나로 들고, 나간 요청을 붙잡는 더블.
 *
 * 등록·수정 성공은 **요청을 그대로 되비춘다**. 성공 뒤 캐시에 쓰이는 값이 응답에서 온 것인지 저장한 프로필에서
 * 온 것인지는 계약이 정하지 않았고, 이 테스트가 그것을 임의로 못 박지 않기 위해서다.
 */
private class FakeUserRemoteDataSource : UserRemoteDataSource {
    var profileOnServer: ProfileResponse? = null
    var getError: Throwable? = null
    var registerError: Throwable? = null
    var updateError: Throwable? = null

    val registerRequests = mutableListOf<ProfileRequest>()
    val updateRequests = mutableListOf<ProfileRequest>()

    /**
     * 저장소는 등록 여부 게이트를 부르지 않는다 — 캐시 유무로 등록/수정을 가르기 때문이다(D38).
     * 호출되면 그 자체가 계약 위반이므로 값을 꾸며 주지 않고 터뜨린다.
     */
    override suspend fun isRegistered(): Boolean =
        error("ProfileRepositoryImpl은 isRegistered()를 쓰지 않는다 — 등록/수정 분기는 캐시 유무로만 갈린다")

    override suspend fun getMe(): ProfileResponse? {
        getError?.let { throw it }
        return profileOnServer
    }

    override suspend fun register(request: ProfileRequest): ProfileResponse {
        registerRequests += request
        registerError?.let { throw it }
        return request.echo()
    }

    override suspend fun updateMe(request: ProfileRequest): ProfileResponse {
        updateRequests += request
        updateError?.let { throw it }
        return request.echo()
    }

    private fun ProfileRequest.echo(): ProfileResponse = profileResponse(nickname = nickname, color = avatar.color)
}

private class FakeProfileLocalDataSource : ProfileLocalDataSource {
    val storedEntry = MutableStateFlow<ProfileEntry?>(null)
    val savedEntries = mutableListOf<ProfileEntry>()
    var clearCount = 0

    override fun observeProfile(): Flow<ProfileEntry?> = storedEntry

    override suspend fun saveProfile(entry: ProfileEntry) {
        savedEntries += entry
        storedEntry.value = entry
    }

    override suspend fun clearProfile() {
        clearCount++
        storedEntry.value = null
    }
}
