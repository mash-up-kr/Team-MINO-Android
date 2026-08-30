package team.mino.feature.roomform.form.vm

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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomNameValidation
import team.mino.core.domain.usecase.CreateRoomUseCase
import team.mino.core.domain.usecase.ValidateRoomNameUseCase
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.roomform.fake.FakeRoomRepository
import team.mino.feature.roomform.form.model.RoomFormDialog
import team.mino.feature.roomform.form.model.RoomFormMode
import java.io.IOException

/**
 * 공동방 폼의 입력 처리와 진입 맥락 복원을 판정한다.
 *
 * 계약은 `contracts/room-form-ui.md` §5가, 상태의 필드·파생 값은 `data-model.md` §2·§5가 소유한다.
 *
 * **문자열은 판정 대상이 아니다.** 상단 타이틀·CTA 라벨(TS-044·TS-037)은 화면이 고르므로, ViewModel에서
 * 검증할 수 있는 것은 그 선택의 근거인 [RoomFormUiState.mode]와 [RoomFormUiState.isOnboarding]뿐이다.
 *
 * **편집 경로는 ViewModel을 거쳐 판정되지 않는다.** 라우트 인자가 복원되지 않아 ViewModel의 모드가 언제나
 * [RoomFormMode.Create]이기 때문이다([createViewModel] 주석). 그래서 편집은 [RoomFormUiState]를 직접 세워
 * 상태의 계약만 본다. 이 방식으로도 닿지 않아 **여기서 검증하지 못하는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 편집 진입 조회가 `values`·`initial`을 방의 현재 값으로 채운다(TS-018) | quickstart S-3 1 |
 * | 편집 CTA가 모달 없이 곧바로 제출하고 `Finish(Updated)`로 끝난다(TS-019·EC-011의 복귀) | quickstart S-3 4·6 |
 * | 편집 뒤로가기가 `ExitEdit` 모달로 갈리고 [나가기]가 방을 되돌리지 않는다(TS-039·TS-041) | quickstart S-5 2·3 |
 * | 편집 요청 실패가 고친 값을 남기고 도메인 에러를 방출한다(EC-014) | **없다** |
 *
 * 마지막 줄은 공백이 아니라 **위임의 공백**이다 — quickstart는 mock에 실패 주입이 없다는 이유로 EC-014를
 * 이 단위 테스트에 위임했는데(quickstart.md §5), 그 위임이 성립하지 않는다. 생성 실패(EC-009)는
 * `생성에 실패해도 입력값이 남고 저장 확인 모달은 다시 열리지 않는다`가 같은 실패 처리 통로를 지나므로,
 * 편집 실패에서 검증되지 않은 채 남는 것은 그 통로가 **편집 제출에도 걸려 있는지**다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RoomFormViewModelTest {
    private val roomRepository = FakeRoomRepository()
    private val validateRoomName = ValidateRoomNameUseCase()
    private val createRoom = CreateRoomUseCase(roomRepository)

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 인텐트 처리가 즉시 실행되도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 빈 필드는 오류가 아니라 [RoomNameValidation.Blank]다 — 막되 오류로 그리지 않는다(TS-001 vs TS-008). */
    @Test
    fun `빈 폼에서는 CTA가 비활성이다`() =
        runTest {
            val viewModel = createViewModel()

            val state = viewModel.state.value
            assertEquals(RoomNameValidation.Blank, state.nameValidation)
            assertFalse(state.canSubmit)
            assertTrue(state.isBlankForm)
        }

    /** 방 설명과 대표 색상은 선택 입력이라 활성 조건에 들어가지 않는다(FR-007). */
    @Test
    fun `방 이름만 입력해도 CTA가 활성된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomFormIntent.NameChanged(VALID_NAME))

            val state = viewModel.state.value
            assertEquals(VALID_NAME, state.values.name)
            assertEquals("", state.values.description)
            assertNull(state.values.color)
            assertEquals(RoomNameValidation.Valid, state.nameValidation)
            assertTrue(state.canSubmit)
        }

    /**
     * 상한은 판정이 아니라 입력 차단이다 — 16번째 글자는 오류가 되는 것이 아니라 아예 들어가지 않는다.
     * 자르는 주체는 ViewModel이다(방 설명은 `MinoTextArea`가 자른다).
     */
    @Test
    fun `방 이름 15자를 채운 뒤 들어온 16번째 글자는 반영되지 않는다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomFormIntent.NameChanged(NAME_AT_LIMIT))
            assertEquals(NAME_AT_LIMIT, viewModel.state.value.values.name)

            viewModel.processIntent(RoomFormIntent.NameChanged(NAME_AT_LIMIT + "호"))

            val state = viewModel.state.value
            assertEquals(NAME_AT_LIMIT, state.values.name)
            assertEquals(NAME_MAX_LENGTH, state.values.name.length)
            assertEquals(RoomNameValidation.Valid, state.nameValidation)
            assertTrue(state.canSubmit)
        }

    /** 붙여넣기는 상한을 한 번에 넘겨 온다. 요청 전체를 버리지 않고 앞 15자만 남긴다(EC-002). */
    @Test
    fun `상한을 넘겨 붙여넣은 방 이름은 앞 15자만 반영된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomFormIntent.NameChanged(PASTED_NAME))

            val state = viewModel.state.value
            assertEquals(PASTED_NAME.take(NAME_MAX_LENGTH), state.values.name)
            assertEquals(NAME_MAX_LENGTH, state.values.name.length)
        }

    /**
     * 선택 입력이 필수 입력의 오류를 덮지 못한다 — 방 설명과 대표 색상은 활성 조건에 들어가지 않으므로
     * 채워도 [RoomFormUiState.canSubmit]을 되살리지 못한다(TS-009 · FR-004·FR-007).
     *
     * **어떤 문자가 [RoomNameValidation.InvalidCharacter]인지는 여기서 판정하지 않는다** —
     * 그것은 `ValidateRoomNameUseCaseTest`가 소유하고, 여기서 보는 것은 판정 결과와 CTA의 연결뿐이다.
     */
    @Test
    fun `방 이름이 오류인 상태에서는 설명과 색상을 채워도 CTA가 비활성이다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomFormIntent.NameChanged(INVALID_NAME))
            assertEquals(RoomNameValidation.InvalidCharacter, viewModel.state.value.nameValidation)
            assertFalse(viewModel.state.value.canSubmit)

            viewModel.processIntent(RoomFormIntent.DescriptionChanged(DESCRIPTION))
            viewModel.processIntent(RoomFormIntent.ColorSelected(RoomColor.RED))

            val state = viewModel.state.value
            assertEquals(DESCRIPTION, state.values.description)
            assertEquals(RoomColor.RED, state.values.color)
            assertEquals(RoomNameValidation.InvalidCharacter, state.nameValidation)
            assertFalse(state.canSubmit)
        }

    /** 오류는 한 번 붙으면 남는 표식이 아니라 현재 값의 판정이다 — 문자를 지우면 CTA가 돌아온다(TS-010). */
    @Test
    fun `오류를 일으킨 문자를 지우면 CTA가 다시 활성된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomFormIntent.NameChanged(INVALID_NAME))
            assertFalse(viewModel.state.value.canSubmit)

            viewModel.processIntent(RoomFormIntent.NameChanged(VALID_NAME))

            val state = viewModel.state.value
            assertEquals(VALID_NAME, state.values.name)
            assertEquals(RoomNameValidation.Valid, state.nameValidation)
            assertTrue(state.canSubmit)
        }

    /**
     * 생성 진입만이 ViewModel을 거쳐 검증되는 진입 맥락이다 — 라우트 인자가 복원되지 않아 ViewModel이
     * 보는 값이 언제나 기본값(`roomId = null` · `isOnboarding = false`)이기 때문이다. 자세한 사정은
     * [createViewModel]의 주석에 있다. 편집·온보딩 진입은 [RoomFormUiState]를 직접 세워 본다.
     */
    @Test
    fun `생성 진입 상태는 Create 모드이고 온보딩이 아니다`() =
        runTest {
            val viewModel = createViewModel()

            val state = viewModel.state.value
            assertEquals(RoomFormMode.Create, state.mode)
            assertFalse(state.isOnboarding)
        }

    /**
     * **ViewModel을 거치지 않는다 — 이 모듈의 JVM 단위 테스트로는 편집 진입을 만들 수 없다.**
     * `SavedStateHandle`에 `roomId`를 넣어도 스텁 `Bundle`이 `null`을 돌려주므로 [RoomFormMode.Edit]에
     * 도달하지 못한다([createViewModel] 주석). 그래서 진입 인자에서 모드까지의 복원 경로는
     * **이 단위 테스트가 검증하지 못하는 구간**이고, quickstart S-3(방 상세 [편집] 진입)이 그 자리를 메운다.
     *
     * 여기서 남기는 계약은 모드가 `roomId`를 담은 채 생성과 구분된다는 것뿐이다 — 화면은 이 구분만 보고
     * 타이틀 `공동방 만들기`/`방 편집`과 CTA `방 생성하기`/`방 편집 완료`를 고른다(FR-025·FR-009).
     */
    @Test
    fun `편집 상태는 방 식별자를 담은 Edit 모드로 생성과 구분된다`() {
        val editing = RoomFormUiState(mode = RoomFormMode.Edit(EDIT_ROOM_ID))

        assertEquals(RoomFormMode.Edit(EDIT_ROOM_ID), editing.mode)
        assertNotEquals(RoomFormUiState().mode, editing.mode)
    }

    /**
     * **ViewModel을 거치지 않는다 — [editingState]가 조회가 끝난 뒤의 상태를 대신 세운다.**
     * 필수·상한 규칙은 진입 맥락을 타지 않는다 — 기존 값으로 열린 폼이라도 이름을 모두 지우면 CTA가
     * 닫힌다(TS-020 · FR-007). [RoomFormUiState.canSubmit]이 모드를 보지 않는다는 것이 그 계약이다.
     *
     * 지운 뒤의 판정을 손으로 고르지 않고 [validateRoomName]으로 다시 받는 것은 [withName]의 주석이 설명한다 —
     * 빈 이름이 [RoomNameValidation.Blank]인 것까지 여기서 선언하면 판정이 아니라 기대의 복사가 된다.
     */
    @Test
    fun `편집 상태에서 방 이름을 모두 지우면 CTA가 비활성이 된다`() {
        val loaded = editingState()
        assertEquals(RoomNameValidation.Valid, loaded.nameValidation)
        assertTrue(loaded.canSubmit)

        val cleared = loaded.withName("")

        assertEquals("", cleared.values.name)
        assertEquals(RoomNameValidation.Blank, cleared.nameValidation)
        assertFalse(cleared.canSubmit)
    }

    /**
     * **ViewModel을 거치지 않는다 — 위와 같은 이유다.**
     * 편집을 완료할 수 있는지는 무엇을 고쳤는지와 무관하다 — 아무것도 고치지 않은 폼도 완료 처리된다(EC-011).
     * [RoomFormUiState.canSubmit]이 [RoomFormUiState.isChanged]를 보지 않는다는 것이 그 계약이라,
     * 변경 여부를 양쪽으로 뒤집어 CTA가 따라 움직이지 않는지 본다.
     *
     * **CTA를 누른 뒤 복귀까지는 여기서 판정하지 못한다** — 제출은 ViewModel의 몫이라 quickstart S-3 6이 본다.
     * [RoomFormUiState.isChanged]가 이탈 확인으로 이어지는 경로는 이 케이스의 관심사가 아니다(TS-042·TS-043).
     */
    @Test
    fun `편집 상태는 아무것도 고치지 않아도 CTA가 활성이다`() {
        val untouched = editingState()
        assertEquals(untouched.initial, untouched.values)
        assertFalse(untouched.isChanged)
        assertTrue(untouched.canSubmit)

        val edited = untouched.withName(EDITED_NAME)

        assertTrue(edited.isChanged)
        assertTrue(edited.canSubmit)
    }

    /**
     * **ViewModel을 거치지 않는다 — [editingState]가 조회가 끝난 뒤의 상태를 대신 세운다.**
     * 열어만 보고 나가는 방장은 붙잡지 않는다(TS-042). 되돌린 값도 같다 — 기준은 고친 적이 있는지가 아니라
     * **지금 값이 진입 시점과 같은지**다(TS-043 · FR-024). 그래서 고친 상태를 한 번 거쳐서 되돌린다.
     * 거치지 않으면 되돌리기가 아니라 손대지 않은 폼을 한 번 더 확인하는 것에 지나지 않는다.
     *
     * [RoomFormUiState.isChanged] 자체의 계산은 `편집 상태는 아무것도 고치지 않아도 CTA가 활성이다`가
     * 이미 양쪽으로 뒤집어 봤다. 여기서 보는 것은 그 값이 이탈 확인으로 이어지는 연결뿐이다.
     */
    @Test
    fun `편집 상태는 고쳤다가 되돌리면 이탈 확인이 필요 없다`() {
        val untouched = editingState()
        assertFalse(untouched.needsExitConfirm)

        val edited = untouched.withName(EDITED_NAME)
        assertTrue(edited.needsExitConfirm)

        val reverted = edited.withName(LOADED_NAME)

        assertEquals(untouched.values, reverted.values)
        assertFalse(reverted.needsExitConfirm)
    }

    /**
     * **ViewModel을 거치지 않는다 — 위와 같은 이유다.**
     * 이탈 확인의 기준은 세 입력 전체라 이름을 손대지 않아도 색만 달라지면 잃을 것이 생긴다(EC-023 · FR-024).
     * 이름만 비교하는 구현은 이 케이스에서만 갈린다.
     */
    @Test
    fun `편집 상태는 대표 색상만 바뀌어도 이탈 확인이 필요하다`() {
        val recolored = editingState().withColor(EDITED_COLOR)

        assertEquals(LOADED_NAME, recolored.values.name)
        assertEquals(LOADED_DESCRIPTION, recolored.values.description)
        assertNotEquals(LOADED_COLOR, recolored.values.color)
        assertTrue(recolored.needsExitConfirm)
    }

    /**
     * **ViewModel을 거치지 않는다 — 위와 같은 이유다.**
     * 저장할 수 있는지와 잃을 것이 있는지는 다른 질문이다 — 이름을 모두 지운 편집 폼은 CTA가 닫히지만
     * 그대로 나가면 지운 이름을 잃는다(EC-024 · FR-024). [RoomFormUiState.needsExitConfirm]이
     * [RoomFormUiState.canSubmit]을 보지 않는다는 것이 그 계약이다.
     */
    @Test
    fun `편집 상태는 CTA가 비활성이어도 값이 다르면 이탈 확인이 필요하다`() {
        val cleared = editingState().withName("")

        assertFalse(cleared.canSubmit)
        assertTrue(cleared.needsExitConfirm)
    }

    /**
     * **ViewModel을 거치지 않는다 — `isOnboarding`은 기본값 `false`만 만들 수 있다.**
     * [createViewModel] 주석과 같은 이유다. 온보딩은 별도의 모드가 아니라 생성에 얹히는 표식이므로,
     * 여기서 확인하는 것은 그 표식이 모드와 독립이라는 점이다(온보딩 생성 · 비온보딩 생성이 모두 성립한다).
     * 실제 온보딩 진입은 quickstart S-6이 검증한다.
     */
    @Test
    fun `온보딩 표식은 생성 모드에 얹히는 값이라 모드를 바꾸지 않는다`() {
        val onboarding = RoomFormUiState(isOnboarding = true)

        assertTrue(onboarding.isOnboarding)
        assertEquals(RoomFormMode.Create, onboarding.mode)
    }

    /**
     * 생성 경로의 CTA는 저장이 아니라 확인 요청이다 — 모달이 뜰 뿐 이 시점에 방은 만들어지지 않는다(TS-030).
     * 모달을 거치는 것은 생성 경로뿐이라, 여기서 요청이 나가면 확인 자체가 의미를 잃는다.
     */
    @Test
    fun `생성 CTA는 방을 만들지 않고 저장 확인 모달만 띄운다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomFormIntent.NameChanged(VALID_NAME))
            viewModel.processIntent(RoomFormIntent.SubmitClicked)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(RoomFormDialog.Save, state.dialog)
            assertEquals(0, roomRepository.createCallCount)
            assertNull(roomRepository.createdDraft)
            assertFalse(state.isSubmitting)
            assertTrue(sideEffects.isEmpty())
        }

    /**
     * 모달의 [취소]는 모달만 닫는다 — 세 입력이 그대로 남고 방도 만들어지지 않는다(TS-031·TS-034).
     * `DialogDismissed`가 다른 상태를 건드리면 취소가 곧 입력 소실이 된다.
     */
    @Test
    fun `저장 확인 모달의 취소는 모달만 닫고 입력값을 남긴다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            fillForm(viewModel)
            viewModel.processIntent(RoomFormIntent.SubmitClicked)
            advanceUntilIdle()
            assertEquals(RoomFormDialog.Save, viewModel.state.value.dialog)

            viewModel.processIntent(RoomFormIntent.DialogDismissed)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertNull(state.dialog)
            assertEquals(VALID_NAME, state.values.name)
            assertEquals(DESCRIPTION, state.values.description)
            assertEquals(SELECTED_COLOR, state.values.color)
            assertTrue(state.canSubmit)
            assertEquals(0, roomRepository.createCallCount)
            assertTrue(sideEffects.isEmpty())
        }

    /**
     * 확인을 받은 뒤에야 방이 만들어지고, 폼은 결과만 알리고 끝난다(TS-012 · FR-010·FR-020).
     *
     * **색을 골라 둔 것은 의도적이다** — 미선택을 회색으로 확정하는 규칙은 `CreateRoomUseCaseTest`가 소유하므로
     * 여기서는 고른 값이 손실 없이 도달하는지만 본다.
     */
    @Test
    fun `저장을 확인하면 입력값 그대로 방이 만들어지고 Created로 끝난다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            fillForm(viewModel)

            viewModel.processIntent(RoomFormIntent.SubmitClicked)
            viewModel.processIntent(RoomFormIntent.SaveConfirmed)
            advanceUntilIdle()

            assertEquals(
                RoomDraft(name = VALID_NAME, description = DESCRIPTION, color = SELECTED_COLOR),
                roomRepository.createdDraft,
            )
            assertEquals(1, roomRepository.createCallCount)
            assertEquals(
                listOf(RoomFormSideEffect.Finish(RoomFormOutcome.Created(roomRepository.newRoomId))),
                sideEffects,
            )
            val state = viewModel.state.value
            assertNull(state.dialog)
            assertFalse(state.isSubmitting)
        }

    /**
     * 제출 중에는 확정 조작이 요청을 늘리지 않는다(UX-001·SC-005·EC-008). 관문으로 첫 요청을 붙잡은 채
     * 다시 눌러 본다 — [FakeRoomRepository]의 호출 횟수는 관문을 기다리기 전에 오르므로, 두 번째 요청이
     * 실제로 나갔다면 카운트가 2가 된다.
     *
     * 같은 게이트가 CTA에도 걸린다 — 제출 중의 `SubmitClicked`가 저장 확인 모달을 다시 띄우면
     * 사용자는 이미 나간 요청 위에 또 한 번 확인 창을 받는다.
     */
    @Test
    fun `제출 중에 다시 확인해도 생성 요청이 늘지 않는다`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            roomRepository.createGate = gate
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            fillForm(viewModel)

            viewModel.processIntent(RoomFormIntent.SubmitClicked)
            viewModel.processIntent(RoomFormIntent.SaveConfirmed)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isSubmitting)
            assertFalse(viewModel.state.value.canSubmit)

            viewModel.processIntent(RoomFormIntent.SaveConfirmed)
            viewModel.processIntent(RoomFormIntent.SubmitClicked)
            advanceUntilIdle()

            assertEquals(1, roomRepository.createCallCount)
            assertNull(viewModel.state.value.dialog)

            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, roomRepository.createCallCount)
            assertEquals(
                listOf(RoomFormSideEffect.Finish(RoomFormOutcome.Created(roomRepository.newRoomId))),
                sideEffects,
            )
        }

    /**
     * 실패는 폼을 되돌리지 않는다 — 입력값은 남고, 알림은 `DomainErrorEmitter`로 나가며,
     * **저장 확인 모달을 다시 열지 않는다**(UX-003·EC-009 · `contracts/room-repository.md` §5).
     * 모달이 다시 뜨면 사용자는 실패 안내를 모달 뒤에서 받고 같은 확인을 두 번 하게 된다.
     */
    @Test
    fun `생성에 실패해도 입력값이 남고 저장 확인 모달은 다시 열리지 않는다`() =
        runTest {
            val failure = MinoDomainException.Network(IOException("생성 실패"))
            roomRepository.createFailure = failure
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            val domainErrors = collectDomainErrors(viewModel)
            fillForm(viewModel)

            viewModel.processIntent(RoomFormIntent.SubmitClicked)
            viewModel.processIntent(RoomFormIntent.SaveConfirmed)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(VALID_NAME, state.values.name)
            assertEquals(DESCRIPTION, state.values.description)
            assertEquals(SELECTED_COLOR, state.values.color)
            assertNull(state.dialog)
            assertFalse(state.isSubmitting)
            assertTrue(state.canSubmit)
            assertEquals(1, roomRepository.createCallCount)
            assertTrue(sideEffects.isEmpty())
            assertEquals(1, domainErrors.size)
            assertSame(failure, domainErrors.first())
        }

    /**
     * 잃을 것이 없는 폼에는 확인 단계를 하나 더 두지 않는다 — 빈 생성 폼의 뒤로가기는 모달 없이 곧바로
     * 끝난다(TS-028 · FR-021).
     *
     * 모달이 뜨지 않는 것과 실제로 끝나는 것을 함께 본다. 둘 중 하나만 맞으면 뒤로가기가 먹통이 되거나
     * (모달도 없고 종료도 없다) 확인 없이 나가는 대신 사용자가 갇힌다.
     */
    @Test
    fun `빈 생성 폼의 뒤로가기는 모달 없이 즉시 Cancelled로 끝난다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            assertTrue(viewModel.state.value.isBlankForm)

            viewModel.processIntent(RoomFormIntent.BackClicked)
            advanceUntilIdle()

            assertNull(viewModel.state.value.dialog)
            assertEquals(listOf(RoomFormSideEffect.Finish(RoomFormOutcome.Cancelled)), sideEffects)
            assertEquals(0, roomRepository.createCallCount)
        }

    /**
     * 기준은 손댄 적이 있는지가 아니라 **지금 남은 값**이다 — 채웠다가 모두 지운 폼은 빈 폼과 같게 나간다
     * (EC-021 · FR-021). 손댔다는 사실을 따로 기억해 두는 구현은 이 케이스에서만 갈린다.
     *
     * 색상을 되돌리지 않는 것은 폼에 해제 수단이 없어서다 — `ColorSelected`는 교체만 하므로
     * 고른 색을 지우는 경로 자체가 존재하지 않는다.
     */
    @Test
    fun `채웠다가 모두 지운 생성 폼도 모달 없이 즉시 끝난다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(RoomFormIntent.NameChanged(VALID_NAME))
            viewModel.processIntent(RoomFormIntent.DescriptionChanged(DESCRIPTION))
            assertFalse(viewModel.state.value.isBlankForm)

            viewModel.processIntent(RoomFormIntent.NameChanged(""))
            viewModel.processIntent(RoomFormIntent.DescriptionChanged(""))
            assertTrue(viewModel.state.value.isBlankForm)
            viewModel.processIntent(RoomFormIntent.BackClicked)
            advanceUntilIdle()

            assertNull(viewModel.state.value.dialog)
            assertEquals(listOf(RoomFormSideEffect.Finish(RoomFormOutcome.Cancelled)), sideEffects)
        }

    /**
     * 이탈 확인의 기준은 저장할 수 있는 폼인지가 아니라 잃을 것이 있는 폼인지다 — 색만 골라 둔 폼은
     * CTA가 닫혀 있어도 손댄 폼이라 모달을 거친다(EC-020 · FR-021).
     *
     * 뜨는 모달이 [RoomFormDialog.ExitCreate]인지까지 보는 것은, 슬롯이 하나라 종류를 틀리면
     * 저장 확인 모달이 뒤로가기에 뜨기 때문이다.
     */
    @Test
    fun `대표 색상만 골라도 뒤로가기가 생성 이탈 모달을 띄운다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(RoomFormIntent.ColorSelected(SELECTED_COLOR))
            assertFalse(viewModel.state.value.canSubmit)

            viewModel.processIntent(RoomFormIntent.BackClicked)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(RoomFormDialog.ExitCreate, state.dialog)
            assertEquals(SELECTED_COLOR, state.values.color)
            assertTrue(sideEffects.isEmpty())
        }

    /**
     * 확인을 받은 이탈은 입력값을 버리고 끝낼 뿐 방을 만들지 않는다(TS-035 · FR-018).
     * 저장 경로와 종료 경로가 같은 신호(`Finish`)로 나가므로, 결과가 [RoomFormOutcome.Cancelled]인지가
     * 진입점이 스낵바를 띄울지 말지를 가른다.
     */
    @Test
    fun `이탈을 확인하면 방을 만들지 않고 Cancelled로 끝난다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            fillForm(viewModel)
            viewModel.processIntent(RoomFormIntent.BackClicked)

            viewModel.processIntent(RoomFormIntent.ExitConfirmed)
            advanceUntilIdle()

            assertEquals(listOf(RoomFormSideEffect.Finish(RoomFormOutcome.Cancelled)), sideEffects)
            assertNull(viewModel.state.value.dialog)
            assertEquals(0, roomRepository.createCallCount)
        }

    /**
     * [건너뛰기]는 확인을 거치지 않는다 — 채워 둔 값이 있어도 모달 없이 [RoomFormOutcome.Skipped]로 끝난다
     * (TS-024 · FR-017). 이탈과 갈라 두는 것은 진입점이 이 둘을 다르게 처리하기 때문이다.
     * 온보딩의 공유 방법 튜토리얼로 보내는 것은 `Skipped`를 받은 쪽의 몫이라 여기서 보지 않는다.
     *
     * **[건너뛰기]가 온보딩에서만 노출된다는 것은 여기서 판정하지 않는다** — 노출은 화면이 정하고,
     * `isOnboarding = true`인 ViewModel은 이 테스트에서 만들 수 없다([createViewModel] 주석).
     * 인텐트 처리 자체는 진입 맥락을 타지 않으므로 도달했을 때의 결과만 본다.
     */
    @Test
    fun `건너뛰기는 확인 없이 Skipped로 끝낸다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            fillForm(viewModel)

            viewModel.processIntent(RoomFormIntent.SkipClicked)
            advanceUntilIdle()

            assertEquals(listOf(RoomFormSideEffect.Finish(RoomFormOutcome.Skipped)), sideEffects)
            assertNull(viewModel.state.value.dialog)
            assertEquals(0, roomRepository.createCallCount)
        }

    /**
     * **진입 인자는 통제되지 않는다 — ViewModel이 보는 라우트는 언제나 `RoomForm()`의 기본값이다.**
     *
     * `savedStateHandle.toRoute<RoomForm>()`는 핸들의 값을 `Bundle`로 다시 싸서 `NavType`으로 읽는데,
     * 이 모듈은 `isReturnDefaultValues = true`라 그 `Bundle`이 스텁이다 — `putAll`이 아무것도 담지 않고
     * `containsKey`가 `false`를 돌려주므로 어떤 인자를 넣어도 `null`로 읽힌다. `roomId`·`isOnboarding`에
     * 기본값이 있는 것은 그래서다. 넣어 봐야 도달하지 않으므로 **빈 핸들을 넘긴다.**
     *
     * 진입 맥락별 분기를 검증하려면 ViewModel을 거치지 말고 [RoomFormUiState]를 직접 세워라 —
     * `편집 상태는 방 식별자를 담은 Edit 모드로 생성과 구분된다`가 그 방식이다.
     */
    private fun createViewModel(): RoomFormViewModel =
        RoomFormViewModel(
            savedStateHandle = SavedStateHandle(),
            roomRepository = roomRepository,
            validateRoomName = validateRoomName,
            createRoom = createRoom,
        )

    /** 세 입력을 모두 채워 저장할 수 있는 폼을 만든다. 생성 케이스의 공통 준비다. */
    private fun fillForm(viewModel: RoomFormViewModel) {
        viewModel.processIntent(RoomFormIntent.NameChanged(VALID_NAME))
        viewModel.processIntent(RoomFormIntent.DescriptionChanged(DESCRIPTION))
        viewModel.processIntent(RoomFormIntent.ColorSelected(SELECTED_COLOR))
    }

    /**
     * 편집 진입 조회가 끝난 직후의 상태. 세 입력이 방의 현재 값으로 차 있고 스냅샷이 같으므로 변경 없음이다.
     *
     * **이 상태에 도달하는 과정은 검증되지 않는다** — 라우트 인자도 조회도 여기서는 일어나지 않는다.
     * 도달 이후의 계약만 본다.
     */
    private fun editingState(): RoomFormUiState {
        val loaded =
            RoomFormValues(
                name = LOADED_NAME,
                description = LOADED_DESCRIPTION,
                color = LOADED_COLOR,
            )
        return RoomFormUiState(
            mode = RoomFormMode.Edit(EDIT_ROOM_ID),
            values = loaded,
            initial = loaded,
            nameValidation = validateRoomName(LOADED_NAME),
        )
    }

    /**
     * 이름만 고친 상태. 판정은 ViewModel이 그러듯 [validateRoomName]으로 다시 받는다 —
     * 손으로 골라 넣으면 기대값을 기대값으로 검증하는 셈이 된다.
     *
     * 상한을 자르지 않는 것은 의도적이다. 자르는 주체는 ViewModel이고 그 규칙은 생성 케이스가 검증한다.
     */
    private fun RoomFormUiState.withName(name: String): RoomFormUiState =
        copy(
            values = values.copy(name = name),
            nameValidation = validateRoomName(name),
        )

    /**
     * 색만 고친 상태. 이름 판정은 건드릴 일이 없어 그대로 둔다 —
     * 다시 받으면 색을 바꾼 것이 이름 판정을 스치는지까지 이 케이스가 떠안는다.
     */
    private fun RoomFormUiState.withColor(color: RoomColor): RoomFormUiState = copy(values = values.copy(color = color))

    /** 수집을 인텐트보다 먼저 걸어 둔다 — 채널로 나가는 일회성 신호는 놓치면 되돌릴 수 없다. */
    private fun TestScope.collectSideEffects(viewModel: RoomFormViewModel): List<RoomFormSideEffect> {
        val collected = mutableListOf<RoomFormSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(collected) }
        return collected
    }

    private fun TestScope.collectDomainErrors(viewModel: RoomFormViewModel): List<MinoDomainException> {
        val collected = mutableListOf<MinoDomainException>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.domainErrors.toList(collected) }
        return collected
    }

    private companion object {
        /** 편집 상태가 담는 방 식별자. */
        const val EDIT_ROOM_ID = "room-1"

        /** 편집 진입이 불러온 방의 값(TS-018 · quickstart S-3). 계약이 정한 시드가 아니라 이 테스트의 픽스처다. */
        const val LOADED_NAME = "야호"

        const val LOADED_DESCRIPTION = "야호호"

        val LOADED_COLOR = RoomColor.RED

        /** 편집 폼에서 이름만 고친 값(quickstart S-3 3). 유효한 값이라 CTA를 닫지 않는다. */
        const val EDITED_NAME = "야호야호"

        /** 편집 폼에서 색만 바꾼 값(EC-023). [LOADED_COLOR]와 다르기만 하면 되고 어느 색인지는 상관없다. */
        val EDITED_COLOR = RoomColor.CYAN

        /** 공백 포함 15자(FR-003). */
        const val NAME_MAX_LENGTH = 15

        /** TS-002가 지목한 값. 허용 문자(한글·공백)만 쓰므로 판정은 `Valid`다. */
        const val VALID_NAME = "민호야 잘하자"

        /** TS-008이 지목한 값. [VALID_NAME]에 허용되지 않는 문자만 덧붙여 오류 해소가 삭제 한 번이 되게 한다. */
        const val INVALID_NAME = VALID_NAME + "^^"

        /** 오류를 덮지 못하는 선택 입력(TS-009). 내용은 판정 대상이 아니다. */
        const val DESCRIPTION = "같이 달려요"

        /** TS-012가 지목한 대표 색상. 고른 색이 초안까지 그대로 도달하는지 보는 데 쓴다. */
        val SELECTED_COLOR = RoomColor.CYAN

        /** 정확히 [NAME_MAX_LENGTH]자. 조합 없이 한 글자가 한 자로 세어지는 완성형만 쓴다. */
        const val NAME_AT_LIMIT = "가나다라마바사아자차카타파하허"

        /** 한 번에 20자가 들어오는 붙여넣기(EC-002 · quickstart S-2). */
        const val PASTED_NAME = "가나다라마바사아자차카타파하허고노도로모"
    }
}
