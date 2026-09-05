package team.mino.feature.onboarding.flow.vm

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.domain.model.InvitationPreview
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep
import team.mino.core.domain.repository.RoomInvitationRepository
import team.mino.core.domain.usecase.JoinRoomByInviteCodeUseCase
import team.mino.core.domain.usecase.ResolveOnboardingStepUseCase
import team.mino.feature.onboarding.fake.FakeOnboardingProgressRepository
import team.mino.feature.onboarding.fake.FakeOnboardingProgressRepository.Call

/**
 * 온보딩 스텝 전이를 판정한다. 전이 표의 단일 출처는 `contracts/onboarding-flow-ui.md` §2.4이며,
 * 이 테스트는 그 표의 일곱 줄을 한 줄씩 그대로 옮긴다.
 *
 * 표의 각 줄에서 판정하는 것은 세 가지다 — **무엇을 저장하는가 · 새 스텝은 무엇인가 · 어떤 SideEffect가
 * 나가는가.** 셋 중 하나만 어긋나도 전이가 어긋난 것이므로 한 테스트에서 셋을 함께 본다.
 *
 * 그 위에 표가 따로 못박은 두 규칙이 있다.
 *
 * **저장이 전환보다 앞선다**(EC-019·SC-008) — 순서를 뒤집으면 기록 직전에 죽은 프로세스가 같은 스텝을
 * 두 번 실행한다. 호출 횟수로는 이것을 볼 수 없어, 쓰기를 [FakeOnboardingProgressRepository.writeGate]로
 * 붙잡아 둔 채 스텝과 SideEffect가 아직 움직이지 않았는지를 본다.
 *
 * **중복 조작 가드**(UX-005·EC-003) — 각 Intent는 현재 스텝이 표의 왼쪽 칸과 같을 때만 처리된다.
 * 같은 Intent를 두 번 보내면 전이는 한 번만 일어난다. 다만 `RoomFormCanceled` 한 줄은 예외이며
 * 그 근거는 해당 테스트의 주석에 있다.
 *
 * **여기서 판정하지 않는 것**: `popUpTo(inclusive = true)`가 붙는지(FR-006·TS-007), 바텀 네비가
 * 비노출인지(FR-005·TS-006)는 SideEffect를 받는 Activity·셸의 몫이라 ViewModel에 닿지 않는다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingFlowViewModelTest {
    private val progressRepository = FakeOnboardingProgressRepository()
    private val resolveOnboardingStep = ResolveOnboardingStepUseCase()
    private val roomInvitationRepository = FakeRoomInvitationRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 인텐트 처리가 즉시 실행되도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Start — 표 1행: 저장 없음(읽기만) · ResolveOnboardingStepUseCase(progress) · 그 스텝의 Launch/Navigate

    /**
     * `Start`는 읽기만 한다. 진입 자체가 진행 상태를 바꾸지 않는다는 것이 표 1행의 "저장 없음"이며,
     * 그래서 [Call.GetProgress] 한 건 말고는 아무 호출도 없어야 한다.
     */
    @Test
    fun `Start는 저장된 스텝을 열고 아무것도 저장하지 않는다`() =
        runTest {
            progressRepository.progress = OnboardingProgress(lastStep = OnboardingStep.PROFILE)
            val viewModel = createViewModel()
            val effects = collectSideEffects(viewModel)

            viewModel.processIntent(OnboardingFlowIntent.Start())

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertEquals(OnboardingStep.PROFILE, state.step)
            assertNull(state.createdRoomId)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchProfile), effects)
            assertEquals(listOf(Call.GetProgress), progressRepository.calls)
        }

    @Test
    fun `공동방 폼에서 중단한 설치는 공동방 폼을 다시 연다`() =
        runTest {
            progressRepository.progress = OnboardingProgress(lastStep = OnboardingStep.ROOM_FORM)
            val viewModel = createViewModel()
            val effects = collectSideEffects(viewModel)

            viewModel.processIntent(OnboardingFlowIntent.Start())

            assertEquals(OnboardingStep.ROOM_FORM, viewModel.state.value.step)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchRoomForm), effects)
            assertEquals(listOf(Call.GetProgress), progressRepository.calls)
        }

    /**
     * 재개 경로에는 폼의 결과 인텐트가 없다. 초대 화면을 다시 열려면 방 id가 저장된 진행 상태에서
     * 와야 하므로, `Start`가 그것을 상태와 SideEffect 양쪽에 실어야 한다.
     */
    @Test
    fun `친구 초대에서 중단한 설치는 저장된 방 id로 초대 화면을 연다`() =
        runTest {
            progressRepository.progress =
                OnboardingProgress(lastStep = OnboardingStep.INVITE, createdRoomId = ROOM_ID)
            val viewModel = createViewModel()
            val effects = collectSideEffects(viewModel)

            viewModel.processIntent(OnboardingFlowIntent.Start())

            val state = viewModel.state.value
            assertEquals(OnboardingStep.INVITE, state.step)
            assertEquals(ROOM_ID, state.createdRoomId)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToInvite(ROOM_ID)), effects)
        }

    /**
     * 재개 스텝을 ViewModel이 직접 정하지 않고 [ResolveOnboardingStepUseCase]에 맡긴다는 것의 확인이다 —
     * 방 없이 `INVITE`가 저장된 어긋난 조합을 떨어뜨리는 규칙은 그 UseCase가 소유하고
     * (`ResolveOnboardingStepUseCaseTest`), ViewModel은 그 판정 결과대로 화면을 여는지만 본다.
     */
    @Test
    fun `방 없이 초대 스텝이 저장돼 있으면 튜토리얼을 연다`() =
        runTest {
            progressRepository.progress =
                OnboardingProgress(lastStep = OnboardingStep.INVITE, createdRoomId = null)
            val viewModel = createViewModel()
            val effects = collectSideEffects(viewModel)

            viewModel.processIntent(OnboardingFlowIntent.Start())

            assertEquals(OnboardingStep.TUTORIAL, viewModel.state.value.step)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), effects)
        }

    @Test
    fun `튜토리얼에서 중단한 설치는 튜토리얼을 연다`() =
        runTest {
            progressRepository.progress = OnboardingProgress(lastStep = OnboardingStep.TUTORIAL)
            val viewModel = createViewModel()
            val effects = collectSideEffects(viewModel)

            viewModel.processIntent(OnboardingFlowIntent.Start())

            assertEquals(OnboardingStep.TUTORIAL, viewModel.state.value.step)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), effects)
        }

    /** 읽는 동안이 `isLoading = true`의 정의다(FR-023). 그 사이에는 아직 어떤 스텝도 열리지 않는다. */
    @Test
    fun `진행 상태를 읽는 동안에는 로딩이고 아무 스텝도 열지 않는다`() =
        runTest {
            val readGate = CompletableDeferred<Unit>()
            progressRepository.readGate = readGate
            progressRepository.progress = OnboardingProgress(lastStep = OnboardingStep.TUTORIAL)
            val viewModel = createViewModel()
            val effects = collectSideEffects(viewModel)

            viewModel.processIntent(OnboardingFlowIntent.Start())

            assertTrue(viewModel.state.value.isLoading)
            assertTrue(effects.isEmpty())

            readGate.complete(Unit)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isLoading)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), effects)
        }

    // endregion

    // region PROFILE — 표 2행: ProfileSaved · setCurrentStep(ROOM_FORM) · ROOM_FORM · LaunchRoomForm

    @Test
    fun `프로필을 저장하면 공동방 폼 스텝을 기록하고 폼을 연다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.PROFILE)

            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)

            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchRoomForm), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.ROOM_FORM)), progressRepository.calls)
        }

    /** 기록이 끝나기 전에 스텝이 먼저 바뀌면, 기록 직전에 죽은 프로세스가 프로필 스텝을 다시 연다. */
    @Test
    fun `프로필 저장은 기록이 끝난 뒤에야 스텝을 넘긴다`() =
        runTest {
            val writeGate = CompletableDeferred<Unit>()
            progressRepository.writeGate = writeGate
            val fixture = startedAt(OnboardingStep.PROFILE)

            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)

            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.ROOM_FORM)), progressRepository.calls)
            assertEquals(OnboardingStep.PROFILE, fixture.step)
            assertTrue(fixture.effects.isEmpty())

            writeGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchRoomForm), fixture.effects)
        }

    @Test
    fun `프로필 저장이 두 번 들어와도 전이는 한 번만 일어난다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.PROFILE)

            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)
            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)

            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchRoomForm), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.ROOM_FORM)), progressRepository.calls)
        }

    /**
     * SYS-010 Flow A(신규 유저) — 프로필 저장 직후 대기 중이던 초대 코드로 자동 참여가 되면 공동방
     * 생성 유도(Flow B)로 가지 않고 나머지 스텝을 전부 건너뛴다.
     */
    @Test
    fun `대기 중인 초대 코드가 있으면 프로필 저장 직후 자동 참여하고 나머지 스텝을 건너뛴다`() =
        runTest {
            roomInvitationRepository.previewRoomId = ROOM_ID
            val fixture = startedAt(OnboardingStep.PROFILE, pendingInviteCode = INVITE_CODE)

            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)

            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToHomeWithRoom(ROOM_ID)), fixture.effects)
            assertEquals(listOf(INVITE_CODE), roomInvitationRepository.previewedCodes)
            assertEquals(listOf(ROOM_ID to INVITE_CODE), roomInvitationRepository.joinedRooms)
            assertEquals(listOf(Call.MarkCompleted), progressRepository.calls)
        }

    /** 자동 참여가 실패(만료·잘못된 코드 등)하면 조용히 정상 흐름(Flow B)으로 폴백한다. */
    @Test
    fun `대기 중인 초대 코드로 자동 참여가 실패하면 정상 흐름으로 폴백한다`() =
        runTest {
            roomInvitationRepository.previewFailure = IllegalStateException("만료된 코드")
            val fixture = startedAt(OnboardingStep.PROFILE, pendingInviteCode = INVITE_CODE)

            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)

            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchRoomForm), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.ROOM_FORM)), progressRepository.calls)
        }

    // endregion

    // region ROOM_FORM — 표 3~5행

    /** 방 id를 먼저 기록하고 그다음 스텝을 기록한다. 순서가 표에 적혀 있으므로 순서까지 본다. */
    @Test
    fun `공동방을 만들면 방 id와 초대 스텝을 차례로 기록하고 초대 화면으로 간다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomCreated(ROOM_ID))

            val state = fixture.viewModel.state.value
            assertEquals(OnboardingStep.INVITE, state.step)
            assertEquals(ROOM_ID, state.createdRoomId)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToInvite(ROOM_ID)), fixture.effects)
            assertEquals(
                listOf(
                    Call.SetCreatedRoomId(ROOM_ID),
                    Call.SetCurrentStep(OnboardingStep.INVITE),
                ),
                progressRepository.calls,
            )
        }

    /**
     * 두 쓰기가 **모두** 끝난 뒤에 전이가 일어난다. 방 id만 기록된 채 스텝이 넘어가면, 그 사이에 죽은
     * 프로세스가 방을 만들어 둔 채로 공동방 폼을 다시 연다.
     */
    @Test
    fun `공동방 생성은 두 기록이 끝난 뒤에야 스텝을 넘긴다`() =
        runTest {
            val writeGate = CompletableDeferred<Unit>()
            progressRepository.writeGate = writeGate
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomCreated(ROOM_ID))

            assertEquals(listOf(Call.SetCreatedRoomId(ROOM_ID)), progressRepository.calls)
            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertTrue(fixture.effects.isEmpty())

            writeGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(
                listOf(
                    Call.SetCreatedRoomId(ROOM_ID),
                    Call.SetCurrentStep(OnboardingStep.INVITE),
                ),
                progressRepository.calls,
            )
            assertEquals(OnboardingStep.INVITE, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToInvite(ROOM_ID)), fixture.effects)
        }

    /** 뒤늦게 들어온 두 번째 결과가 이미 확정된 방 id를 덮어쓰지 않는다. */
    @Test
    fun `공동방 생성 결과가 두 번 들어와도 첫 방 id로 한 번만 전이한다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomCreated(ROOM_ID))
            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomCreated(ANOTHER_ROOM_ID))

            assertEquals(ROOM_ID, fixture.viewModel.state.value.createdRoomId)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToInvite(ROOM_ID)), fixture.effects)
            assertEquals(
                listOf(
                    Call.SetCreatedRoomId(ROOM_ID),
                    Call.SetCurrentStep(OnboardingStep.INVITE),
                ),
                progressRepository.calls,
            )
        }

    /** 건너뛰면 초대 스텝을 통째로 지나간다. 방을 만들지 않았으므로 방 id 기록도 없다. */
    @Test
    fun `공동방 폼을 건너뛰면 튜토리얼 스텝을 기록하고 튜토리얼로 간다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormSkipped)

            val state = fixture.viewModel.state.value
            assertEquals(OnboardingStep.TUTORIAL, state.step)
            assertNull(state.createdRoomId)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.TUTORIAL)), progressRepository.calls)
        }

    @Test
    fun `건너뛰기가 두 번 들어와도 전이는 한 번만 일어난다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormSkipped)
            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormSkipped)

            assertEquals(OnboardingStep.TUTORIAL, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.TUTORIAL)), progressRepository.calls)
        }

    /** 취소는 전이가 아니다. 스텝도 저장도 그대로 두고 폼만 다시 연다. */
    @Test
    fun `공동방 폼이 취소되면 아무것도 저장하지 않고 폼을 다시 연다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormCanceled)

            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.LaunchRoomForm), fixture.effects)
            assertTrue(progressRepository.calls.isEmpty())
        }

    /**
     * **이 한 줄만 중복 가드의 예외다.** 가드의 정의는 "현재 스텝이 표의 왼쪽 칸과 같을 때만 처리한다"이고,
     * 이 줄은 전이 후에도 스텝이 `ROOM_FORM` 그대로라 두 번째도 조건을 만족한다. 그래야 맞기도 하다 —
     * `RoomFormCanceled`는 버튼 연타가 아니라 공동방 폼 Activity가 실제로 한 번 더 닫혔다는 결과이고,
     * 그때 폼을 다시 열지 않으면 온보딩이 빈 화면에 멈춘다.
     *
     * 그래서 "SideEffect가 나가면 스텝이 바뀐다"는 단순화로 가드를 구현하면 이 줄에서 깨진다.
     */
    @Test
    fun `공동방 폼 취소가 두 번 들어오면 폼도 두 번 열린다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.ROOM_FORM)

            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormCanceled)
            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormCanceled)

            assertEquals(OnboardingStep.ROOM_FORM, fixture.step)
            assertEquals(
                listOf(
                    OnboardingFlowSideEffect.LaunchRoomForm,
                    OnboardingFlowSideEffect.LaunchRoomForm,
                ),
                fixture.effects,
            )
            assertTrue(progressRepository.calls.isEmpty())
        }

    // endregion

    // region INVITE — 표 6행: InviteClosed · setCurrentStep(TUTORIAL) · TUTORIAL · NavigateToTutorial

    /** 초대를 닫아도 만든 방은 사라지지 않는다 — 스텝만 넘어가고 방 id는 상태에 남는다. */
    @Test
    fun `친구 초대를 닫으면 튜토리얼 스텝을 기록하고 튜토리얼로 간다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.INVITE, createdRoomId = ROOM_ID)

            fixture.viewModel.processIntent(OnboardingFlowIntent.InviteClosed)

            val state = fixture.viewModel.state.value
            assertEquals(OnboardingStep.TUTORIAL, state.step)
            assertEquals(ROOM_ID, state.createdRoomId)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.TUTORIAL)), progressRepository.calls)
        }

    @Test
    fun `초대 닫기가 두 번 들어와도 전이는 한 번만 일어난다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.INVITE, createdRoomId = ROOM_ID)

            fixture.viewModel.processIntent(OnboardingFlowIntent.InviteClosed)
            fixture.viewModel.processIntent(OnboardingFlowIntent.InviteClosed)

            assertEquals(OnboardingStep.TUTORIAL, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToTutorial), fixture.effects)
            assertEquals(listOf(Call.SetCurrentStep(OnboardingStep.TUTORIAL)), progressRepository.calls)
        }

    // endregion

    // region TUTORIAL — 표 7행: TutorialFinished · markCompleted() · 스텝 변화 없음 · NavigateToHome

    /** 완료는 스텝이 아니라 완료 표시가 든다. 그래서 마지막 줄에는 `setCurrentStep`이 없다. */
    @Test
    fun `튜토리얼을 끝내면 완료를 기록하고 홈으로 간다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.TUTORIAL)

            fixture.viewModel.processIntent(OnboardingFlowIntent.TutorialFinished)

            assertEquals(OnboardingStep.TUTORIAL, fixture.step)
            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToHome), fixture.effects)
            assertEquals(listOf(Call.MarkCompleted), progressRepository.calls)
        }

    /**
     * 홈으로 보낸 뒤에 완료를 기록하면, 그 사이에 죽은 프로세스가 다음 실행에서 온보딩을 처음부터 다시 연다.
     */
    @Test
    fun `튜토리얼 완료는 기록이 끝난 뒤에야 홈으로 보낸다`() =
        runTest {
            val writeGate = CompletableDeferred<Unit>()
            progressRepository.writeGate = writeGate
            val fixture = startedAt(OnboardingStep.TUTORIAL)

            fixture.viewModel.processIntent(OnboardingFlowIntent.TutorialFinished)

            assertEquals(listOf(Call.MarkCompleted), progressRepository.calls)
            assertTrue(fixture.effects.isEmpty())

            writeGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToHome), fixture.effects)
        }

    /**
     * 마지막 줄은 전이 후에도 스텝이 `TUTORIAL` 그대로라, 왼쪽 칸 비교만으로는 두 번째 Intent를 걸러내지
     * 못한다. 그래도 걸러야 한다 — [건너뛰기]와 `꾹 시작하기`는 사용자가 연타할 수 있는 버튼이고,
     * `NavigateToHome`이 두 번 나가면 홈 Activity를 두 번 띄운다(UX-005·EC-003).
     *
     * 어떻게 거를지는 정하지 않는다. 다만 [OnboardingFlowUiState]에는 완료를 담을 필드가 없으므로
     * (계약 §2.1의 세 필드가 전부다) 상태가 아닌 곳에서 걸러야 한다.
     */
    @Test
    fun `튜토리얼 완료가 두 번 들어와도 완료 기록과 홈 이동은 한 번뿐이다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.TUTORIAL)

            fixture.viewModel.processIntent(OnboardingFlowIntent.TutorialFinished)
            fixture.viewModel.processIntent(OnboardingFlowIntent.TutorialFinished)

            assertEquals(listOf(OnboardingFlowSideEffect.NavigateToHome), fixture.effects)
            assertEquals(listOf(Call.MarkCompleted), progressRepository.calls)
        }

    // endregion

    /**
     * 가드는 중복만이 아니라 **순서가 어긋난 Intent** 전체를 막는다. 뒤늦게 도착한 앞 스텝의 결과가
     * 진행을 되돌리지 못한다는 것이 표의 왼쪽 칸이 존재하는 이유다.
     */
    @Test
    fun `현재 스텝과 맞지 않는 Intent는 모두 버려진다`() =
        runTest {
            val fixture = startedAt(OnboardingStep.TUTORIAL)

            fixture.viewModel.processIntent(OnboardingFlowIntent.ProfileSaved)
            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomCreated(ROOM_ID))
            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormSkipped)
            fixture.viewModel.processIntent(OnboardingFlowIntent.RoomFormCanceled)
            fixture.viewModel.processIntent(OnboardingFlowIntent.InviteClosed)

            val state = fixture.viewModel.state.value
            assertEquals(OnboardingStep.TUTORIAL, state.step)
            assertNull(state.createdRoomId)
            assertTrue(fixture.effects.isEmpty())
            assertTrue(progressRepository.calls.isEmpty())
        }

    private fun createViewModel() =
        OnboardingFlowViewModel(
            onboardingProgressRepository = progressRepository,
            resolveOnboardingStep = resolveOnboardingStep,
            joinRoomByInviteCode = JoinRoomByInviteCodeUseCase(roomInvitationRepository),
        )

    /**
     * [step]에서 재개한 직후의 플로우를 세운다. `Start`가 남긴 SideEffect와 호출 기록은 지워, 뒤이어
     * 보내는 Intent 하나가 만든 것만 보이게 한다.
     *
     * @param pendingInviteCode SYS-010 자동 참여 시나리오만 채운다. 나머지 표의 일곱 줄은 전부
     *  `null`로 시작해 `roomInvitationRepository`가 호출되지 않아야 한다.
     */
    private fun TestScope.startedAt(
        step: OnboardingStep,
        createdRoomId: String? = null,
        pendingInviteCode: String? = null,
    ): Fixture {
        progressRepository.progress = OnboardingProgress(lastStep = step, createdRoomId = createdRoomId)
        val viewModel = createViewModel()
        val effects = collectSideEffects(viewModel)
        viewModel.processIntent(OnboardingFlowIntent.Start(pendingInviteCode))
        check(viewModel.state.value.step == step) {
            "재개 스텝이 $step 이어야 준비가 끝난 것이다. 실제: ${viewModel.state.value.step}"
        }
        effects.clear()
        progressRepository.clearCalls()
        return Fixture(viewModel, effects)
    }

    private class Fixture(
        val viewModel: OnboardingFlowViewModel,
        val effects: MutableList<OnboardingFlowSideEffect>,
    ) {
        val step: OnboardingStep get() = viewModel.state.value.step
    }

    private fun TestScope.collectSideEffects(
        viewModel: OnboardingFlowViewModel,
    ): MutableList<OnboardingFlowSideEffect> {
        val collected = mutableListOf<OnboardingFlowSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(collected) }
        return collected
    }

    /**
     * SYS-010 자동 참여 시나리오 전용 스텁. `previewFailure`가 있으면 미리보기에서 던지고, 없으면
     * `previewRoomId`를 돌려준 뒤 참여까지 성공한다 — 이 테스트 파일이 보는 것은 전이 표뿐이라
     * 참여 성공·실패 두 갈래만 있으면 충분하다.
     */
    private class FakeRoomInvitationRepository : RoomInvitationRepository {
        var previewRoomId: String = ""
        var previewFailure: Throwable? = null
        val previewedCodes = mutableListOf<String>()
        val joinedRooms = mutableListOf<Pair<String, String>>()

        override suspend fun issueInviteCode(roomId: String): String = error("이 테스트는 발급을 부르지 않는다")

        override suspend fun previewInvitation(inviteCode: String): InvitationPreview {
            previewedCodes += inviteCode
            previewFailure?.let { throw it }
            return InvitationPreview(roomId = previewRoomId)
        }

        override suspend fun joinRoom(
            roomId: String,
            inviteCode: String,
        ) {
            joinedRooms += roomId to inviteCode
        }
    }

    private companion object {
        /** 온보딩에서 만든 공동방의 id. */
        const val ROOM_ID = "room-1"

        /** 중복으로 들어온 두 번째 생성 결과. 첫 결과를 덮어쓰지 않는 것을 보려고 값을 다르게 둔다. */
        const val ANOTHER_ROOM_ID = "room-2"

        /** SYS-010 딥링크 초대 코드. */
        const val INVITE_CODE = "invite-code"
    }
}
