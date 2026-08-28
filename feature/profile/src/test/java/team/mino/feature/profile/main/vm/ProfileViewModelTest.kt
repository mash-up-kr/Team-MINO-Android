package team.mino.feature.profile.main.vm

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.domain.model.Profile
import team.mino.core.domain.usecase.SaveProfileUseCase
import team.mino.core.domain.usecase.ValidateNicknameUseCase
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.profile.fake.FakeProfileRepository
import team.mino.feature.profile.main.model.DefaultProfileAvatar
import team.mino.feature.profile.main.model.ProfileEntryPoint
import team.mino.feature.profile.main.model.profileAvatar
import java.io.IOException

/**
 * 프로필 설정 화면의 인텐트 처리를 판정한다.
 *
 * 계약은 `contracts/profile-screen-contract.md` §Intent·§실패 통로가 소유하고,
 * 상태의 필드·파생 값은 `data-model.md` §5가 소유한다. 프리필과 갱신의 순서·가드는
 * `research.md` D45가 단일 출처다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val profileRepository = FakeProfileRepository()
    private val validateNickname = ValidateNicknameUseCase()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 인텐트 처리가 즉시 실행되도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `닉네임이 유효해지면 저장이 활성된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))

            val state = viewModel.state.value
            assertEquals("민호", state.nickname)
            assertTrue(state.isNicknameValid)
            assertTrue(state.isSaveEnabled)
        }

    @Test
    fun `아바타는 단일 선택이라 이전 선택을 교체한다`() =
        runTest {
            val viewModel = createViewModel()
            val first = MinoProfileAvatar.entries[2]
            val second = MinoProfileAvatar.entries[6]

            viewModel.processIntent(ProfileIntent.AvatarSelected(first))
            viewModel.processIntent(ProfileIntent.AvatarSelected(second))

            val state = viewModel.state.value
            assertEquals(second, state.selectedAvatar)
            assertEquals(second, state.displayedAvatar)
        }

    @Test
    fun `저장에 성공하면 SaveCompleted를 발행하고 저장 중 상태를 되돌린다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            val avatar = MinoProfileAvatar.entries[4]

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))
            viewModel.processIntent(ProfileIntent.AvatarSelected(avatar))
            viewModel.processIntent(ProfileIntent.SaveClicked)
            advanceUntilIdle()

            assertEquals(Profile(nickname = "민호", avatar = avatar.profileAvatar), profileRepository.savedProfile)
            assertEquals(listOf(ProfileSideEffect.SaveCompleted), sideEffects)
            assertFalse(viewModel.state.value.isSaving)
        }

    @Test
    fun `저장 중에 다시 누른 저장은 요청을 만들지 않는다`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            profileRepository.saveGate = gate
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))
            viewModel.processIntent(ProfileIntent.SaveClicked)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isSaving)

            viewModel.processIntent(ProfileIntent.SaveClicked)
            advanceUntilIdle()

            assertEquals(1, profileRepository.saveCallCount)
            gate.complete(Unit)
            advanceUntilIdle()
        }

    @Test
    fun `저장에 실패해도 입력한 닉네임과 아바타 선택이 남는다`() =
        runTest {
            val failure = MinoDomainException.Network(IOException("저장 실패"))
            profileRepository.saveFailure = failure
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            val domainErrors = collectDomainErrors(viewModel)
            val avatar = MinoProfileAvatar.entries[4]

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))
            viewModel.processIntent(ProfileIntent.AvatarSelected(avatar))
            viewModel.processIntent(ProfileIntent.SaveClicked)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("민호", state.nickname)
            assertEquals(avatar, state.selectedAvatar)
            assertFalse(state.isSaving)
            assertTrue(sideEffects.isEmpty())
            assertEquals(1, domainErrors.size)
            assertSame(failure, domainErrors.first())
        }

    @Test
    fun `저장된 프로필이 있으면 진입 시 닉네임과 아바타가 채워진다`() =
        runTest {
            val avatar = MinoProfileAvatar.entries[4]
            profileRepository.givenProfile(Profile(nickname = "민호", avatar = avatar.profileAvatar))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("민호", state.nickname)
            assertEquals(avatar, state.selectedAvatar)
            assertEquals(avatar, state.displayedAvatar)
            assertTrue(state.isNicknameValid)
            assertTrue(state.isSaveEnabled)
        }

    /** 프리필은 사용자의 입력이 아니므로 `isNicknameTouched`를 올리지 않는다 — 채워진 채로 오류 문구가 뜨면 안 된다. */
    @Test
    fun `프리필은 입력으로 치지 않아 오류 문구를 띄우지 않는다`() =
        runTest {
            profileRepository.givenProfile(
                Profile(nickname = "민호", avatar = MinoProfileAvatar.entries[4].profileAvatar),
            )

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isNicknameTouched)
            assertFalse(state.isNicknameErrorVisible)
        }

    @Test
    fun `저장된 프로필이 없으면 아무것도 채우지 않는다`() =
        runTest {
            profileRepository.givenProfile(null)

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("", state.nickname)
            assertNull(state.selectedAvatar)
            assertEquals(DefaultProfileAvatar, state.displayedAvatar)
            assertFalse(state.isNicknameValid)
            assertFalse(state.isSaveEnabled)
        }

    @Test
    fun `프리필된 닉네임을 모두 지우면 오류가 되고 저장된 프로필은 그대로다`() =
        runTest {
            val saved = Profile(nickname = "민호", avatar = MinoProfileAvatar.entries[4].profileAvatar)
            profileRepository.givenProfile(saved)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("민호", viewModel.state.value.nickname)

            viewModel.processIntent(ProfileIntent.NicknameChanged(""))

            val state = viewModel.state.value
            assertFalse(state.isNicknameValid)
            assertTrue(state.isNicknameErrorVisible)
            assertFalse(state.isSaveEnabled)
            assertEquals(saved, profileRepository.savedProfile)
        }

    /**
     * 진입 시 갱신은 한 번뿐이다 — 흐름을 계속 구독하지 않으므로 재요청도 없다
     * (screen 계약 §Intent, `research.md` D45).
     */
    @Test
    fun `진입하면 갱신을 정확히 한 번 요청한다`() =
        runTest {
            createViewModel()
            advanceUntilIdle()

            assertEquals(1, profileRepository.refreshCallCount)
        }

    /**
     * 미등록은 실패가 아니다. 갱신이 예외 없이 끝나므로 온보딩 사용자는 진입 직후 오류를 보지 않는다
     * (repository 계약 `refreshProfile`, screen 계약 §Intent).
     */
    @Test
    fun `미등록이라 갱신이 캐시를 비워도 오류를 방출하지 않는다`() =
        runTest {
            profileRepository.givenProfile(null)
            profileRepository.givenRefreshedProfile(null)

            val viewModel = createViewModel()
            val domainErrors = collectDomainErrors(viewModel)
            advanceUntilIdle()

            assertTrue(domainErrors.isEmpty())
            val state = viewModel.state.value
            assertEquals("", state.nickname)
            assertNull(state.selectedAvatar)
            assertFalse(state.isNicknameErrorVisible)
        }

    /**
     * 갱신이 실패해도 화면은 캐시 값으로 계속 선다 — 별도 로딩 상태가 없으므로 프리필이 되돌려지지도 않는다
     * (FR-012·SC-006, `data-model.md` §5). 실패 자체는 실패 통로로 나간다(screen 계약 §Intent).
     */
    @Test
    fun `진입 갱신이 실패해도 화면은 캐시 값으로 계속 선다`() =
        runTest {
            val avatar = MinoProfileAvatar.entries[4]
            profileRepository.givenProfile(Profile(nickname = "민호", avatar = avatar.profileAvatar))
            val failure = MinoDomainException.Network(IOException("갱신 실패"))
            profileRepository.refreshFailure = failure

            val viewModel = createViewModel()
            val domainErrors = collectDomainErrors(viewModel)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("민호", state.nickname)
            assertEquals(avatar, state.selectedAvatar)
            assertTrue(state.isNicknameValid)
            assertFalse(state.isNicknameTouched)
            assertEquals(1, domainErrors.size)
            assertSame(failure, domainErrors.first())
        }

    /**
     * 갱신이 캐시를 고치면 화면이 그 값으로 다시 채워진다. `observeProfile().first()`는 갱신된 값을
     * 저절로 흘리지 않으므로, 갱신이 성공한 그 시점에 한 번 더 읽어야만 성립한다(`research.md` D45 ②).
     */
    @Test
    fun `갱신이 새 값을 캐시에 쓰면 화면이 그 값으로 다시 채워진다`() =
        runTest {
            val cached = MinoProfileAvatar.entries[2]
            val refreshed = MinoProfileAvatar.entries[7]
            profileRepository.givenProfile(Profile(nickname = "민호", avatar = cached.profileAvatar))
            profileRepository.givenRefreshedProfile(Profile(nickname = "지민", avatar = refreshed.profileAvatar))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("지민", state.nickname)
            assertEquals(refreshed, state.selectedAvatar)
            assertTrue(state.isNicknameValid)
            assertFalse(state.isNicknameTouched)
        }

    /**
     * 사용자가 이미 타이핑을 시작했으면 갱신 값이 그것을 덮어쓰지 않는다 — `isNicknameTouched` 가드다(`research.md` D45).
     * 갱신 응답이 입력보다 늦게 도착하는 순서를 게이트로 고정한다.
     */
    @Test
    fun `사용자가 이미 입력했으면 갱신 값이 덮어쓰지 않는다`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            profileRepository.refreshGate = gate
            profileRepository.givenRefreshedProfile(
                Profile(nickname = "지민", avatar = MinoProfileAvatar.entries[7].profileAvatar),
            )
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))
            gate.complete(Unit)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("민호", state.nickname)
            assertNull(state.selectedAvatar)
            assertTrue(state.isNicknameTouched)
        }

    /**
     * 저장 왕복 중에 갱신 응답이 도착해도 화면을 건드리지 않는다 — `isSaving` 가드다(`research.md` D45).
     *
     * 캐시 프리필로 저장을 활성시켜 `isNicknameTouched`는 거짓으로 둔다. 그래야 이 케이스가
     * 위의 입력 가드가 아니라 저장 가드만 판정한다.
     */
    @Test
    fun `저장 중이면 갱신 값이 프리필을 덮어쓰지 않는다`() =
        runTest {
            val cached = MinoProfileAvatar.entries[2]
            profileRepository.givenProfile(Profile(nickname = "민호", avatar = cached.profileAvatar))
            profileRepository.givenRefreshedProfile(
                Profile(nickname = "지민", avatar = MinoProfileAvatar.entries[7].profileAvatar),
            )
            val refreshGate = CompletableDeferred<Unit>()
            val saveGate = CompletableDeferred<Unit>()
            profileRepository.refreshGate = refreshGate
            profileRepository.saveGate = saveGate

            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("민호", viewModel.state.value.nickname)
            assertFalse(viewModel.state.value.isNicknameTouched)

            viewModel.processIntent(ProfileIntent.SaveClicked)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isSaving)

            refreshGate.complete(Unit)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("민호", state.nickname)
            assertEquals(cached, state.selectedAvatar)

            saveGate.complete(Unit)
            advanceUntilIdle()
        }

    @Test
    fun `진입 직후에는 닉네임 오류를 표시하지 않는다`() =
        runTest {
            val viewModel = createViewModel()

            val state = viewModel.state.value
            assertFalse(state.isNicknameErrorVisible)
            assertFalse(state.isSaveEnabled)
            assertFalse(state.isClearEnabled)
        }

    @Test
    fun `한 글자 닉네임은 오류로 표시된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민"))

            val state = viewModel.state.value
            assertFalse(state.isNicknameValid)
            assertTrue(state.isNicknameErrorVisible)
            assertFalse(state.isSaveEnabled)
        }

    @Test
    fun `허용되지 않는 문자가 섞인 닉네임은 오류로 표시된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("abc1"))

            val state = viewModel.state.value
            assertFalse(state.isNicknameValid)
            assertTrue(state.isNicknameErrorVisible)
            assertFalse(state.isSaveEnabled)
        }

    @Test
    fun `닉네임을 고쳐 유효해지면 오류 표시가 사라진다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민"))
            assertTrue(viewModel.state.value.isNicknameErrorVisible)

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))

            val state = viewModel.state.value
            assertTrue(state.isNicknameValid)
            assertFalse(state.isNicknameErrorVisible)
            assertTrue(state.isSaveEnabled)
        }

    /** `isNicknameTouched`가 함께 거짓으로 돌아가야 지우기 직후에 오류 문구가 뜨지 않는다. */
    @Test
    fun `지우기는 닉네임과 아바타를 초기 상태로 되돌린다`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))
            viewModel.processIntent(ProfileIntent.AvatarSelected(MinoProfileAvatar.entries[4]))

            viewModel.processIntent(ProfileIntent.ClearClicked)

            val state = viewModel.state.value
            assertEquals("", state.nickname)
            assertNull(state.selectedAvatar)
            assertEquals(DefaultProfileAvatar, state.displayedAvatar)
            assertFalse(state.isNicknameValid)
            assertFalse(state.isNicknameTouched)
            assertFalse(state.isNicknameErrorVisible)
            assertFalse(state.isSaveEnabled)
            assertFalse(state.isClearEnabled)
        }

    @Test
    fun `닉네임만 입력하면 저장만 활성되고 지우기는 비활성이다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민호"))

            val state = viewModel.state.value
            assertTrue(state.isSaveEnabled)
            assertFalse(state.isClearEnabled)
        }

    @Test
    fun `아바타를 골라도 닉네임이 오류면 저장과 지우기가 모두 비활성이다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(ProfileIntent.NicknameChanged("민"))
            viewModel.processIntent(ProfileIntent.AvatarSelected(MinoProfileAvatar.entries[4]))

            val state = viewModel.state.value
            assertTrue(state.isNicknameErrorVisible)
            assertFalse(state.isSaveEnabled)
            assertFalse(state.isClearEnabled)
        }

    /**
     * 뒤로가기 노출은 진입점만 보는 순수 계산이라 상태를 직접 세워 본다 — ViewModel을 거치면
     * JVM 단위 테스트에서 라우트 인자가 복원되지 않아 진입점을 통제할 수 없다.
     */
    @Test
    fun `뒤로가기는 마이페이지 진입에서만 활성이다`() {
        assertFalse(ProfileUiState(entryPoint = ProfileEntryPoint.Onboarding).isBackEnabled)
        assertTrue(ProfileUiState(entryPoint = ProfileEntryPoint.MyPage).isBackEnabled)
    }

    /**
     * **진입점은 통제되지 않는다 — ViewModel이 보는 값은 언제나 [ProfileEntryPoint.MyPage]다.**
     *
     * 이 모듈은 `isReturnDefaultValues = true`라 `SavedStateHandle`에 넣은 인자가 라우트 복원용
     * Bundle까지 전달되지 않고, `NavType.StringType`이 빈 Bundle에서 `null`을 읽어
     * `ProfileEntryPoint.from(null)`이 기본값을 돌려준다. 그래서 진입점을 받는 파라미터를 두지 않았다.
     *
     * 진입점별 분기를 검증하려면 ViewModel을 거치지 말고 [ProfileUiState]를 직접 세워라 —
     * `뒤로가기는 마이페이지 진입에서만 활성이다`가 그 방식이다.
     */
    private fun createViewModel(): ProfileViewModel =
        ProfileViewModel(
            savedStateHandle = SavedStateHandle(mapOf(ENTRY_POINT_ARG to ProfileEntryPoint.MyPage.extraValue)),
            validateNickname = validateNickname,
            saveProfile =
                SaveProfileUseCase(
                    profileRepository = profileRepository,
                    validateNickname = validateNickname,
                ),
            profileRepository = profileRepository,
        )

    /** 수집을 인텐트보다 먼저 걸어 둔다 — 채널로 나가는 일회성 신호는 놓치면 되돌릴 수 없다. */
    private fun TestScope.collectSideEffects(viewModel: ProfileViewModel): List<ProfileSideEffect> {
        val collected = mutableListOf<ProfileSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(collected) }
        return collected
    }

    /**
     * 진입 시 갱신이 내는 오류는 ViewModel 생성 시점에 이미 나갔을 수 있다. 채널이 `BUFFERED`라
     * 뒤늦게 걸어도 버퍼에 쌓인 값을 받는다.
     */
    private fun TestScope.collectDomainErrors(viewModel: ProfileViewModel): List<MinoDomainException> {
        val collected = mutableListOf<MinoDomainException>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.domainErrors.toList(collected) }
        return collected
    }

    private companion object {
        /**
         * `ProfileMain(entryPoint)`의 인자 이름. 라우트 복원의 형태를 맞추기 위한 것일 뿐,
         * 여기 실린 값은 ViewModel에 도달하지 않는다 — `createViewModel` 참고.
         */
        const val ENTRY_POINT_ARG = "entryPoint"
    }
}
