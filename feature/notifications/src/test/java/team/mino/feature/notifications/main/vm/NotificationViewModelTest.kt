@file:OptIn(ExperimentalTime::class)

package team.mino.feature.notifications.main.vm

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.domain.model.Notification
import team.mino.core.domain.model.NotificationTarget
import team.mino.core.domain.model.NotificationType
import team.mino.core.domain.usecase.ResolveNotificationDestinationUseCase
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.notifications.fake.FakeNotificationRepository
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 알림 목록이 실리고, 이어 붙고, 실패하는 전부를 판정한다.
 *
 * 다루는 범위는 FR-001·FR-018(최신순·페이징), UX-001(로딩과 빈 상태의 구분), UX-002·EC-001(첫 페이지 실패),
 * UX-012·EC-016(추가 페이지 실패), EC-018(끝 도달 가드), FR-015·TS-011·TS-043(탭 복귀 시 재조회 금지),
 * 그리고 행을 눌렀을 때의 도착지 세 갈래(FR-005·FR-010·FR-022, TS-020~TS-025)와 그 방출 횟수(EC-011·EC-012)다.
 * 규칙의 원문은 `spec.md`·`data-model.md` §2.3·§3과 `contracts/notification-ui.md` §2·§3·§4.2가 소유한다.
 *
 * **여기서 보지 않는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 경과 시간이 어느 구간으로 끊기는가(FR-003·SC-005) | `ElapsedTimeFormatterTest` — 「지금」을 주입하는 통로가 없어 여기서는 값을 단정할 수 없다 |
 * | 어떤 대상이 어떤 도착지로 갈리는가 | `ResolveNotificationDestinationUseCaseTest` — 여기서는 **누른 알림의 대상이 그 판정에 실려 신호가 되는가**만 본다 |
 * | 올라간 신호를 어디로 흘리는가(§1의 콜백·`NavController`) | `NotificationRoute` — 저장 오류 안내로의 전환은 그래프 안에서 끝난다 |
 * | 각 상태가 무엇을 그리는가(`contracts/notification-ui.md` §4.2) | 화면 — 상태를 그림으로 옮기는 것은 그리는 쪽의 몫이다 |
 * | 담긴 리프를 어떤 문구로 보여주는가 | `NotificationRoute` (`docs/conventions/error_handling.md` §5) |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {
    private val notificationRepository = FakeNotificationRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 인텐트 처리가 즉시 실행되도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 서버가 준 순서를 **그대로** 싣는다(FR-001, TS-036).
     *
     * 최신순으로 주는 것은 서버의 몫이고 정렬 책임은 어디에도 없다
     * (`contracts/notification-repository.md` §1). 그래서 판정하는 것은 "최신순인가"가 아니라 **받은 순서를
     * 다시 뒤집거나 자르지 않는가**다 — 그러지 않으면 이 케이스는 서버 계약을 두 번 검사하는 테스트가 된다.
     *
     * 발생 시각을 일부러 **오래된 것부터** 실어 둔다. `createdAt`으로 스스로 정렬하는 구현은 여기서 순서가
     * 뒤집혀 걸린다.
     */
    @Test
    fun `첫 묶음은 서버가 준 순서 그대로 실린다`() =
        runTest {
            val items = notifications(count = 3, elapsedGrowsWithIndex = false)
            notificationRepository.setPage(page = 0, items = items, hasNext = false)
            val viewModel = createViewModel()

            viewModel.processIntent(NotificationIntent.Load)

            val state = viewModel.state.value
            assertEquals(NotificationPhase.Content, state.phase)
            assertEquals(items.map { it.id }, state.items.map { it.id })
            assertEquals(listOf(0), notificationRepository.requestedPages)
        }

    /**
     * 끝에 도달하면 다음 묶음이 **뒤에** 붙는다(FR-018, TS-037, UX-011).
     *
     * 이미 보던 행이 앞자리에 그대로 남는 것까지 본다 — 새 묶음으로 갈아 끼우는 구현은 상태의 건수만 보면
     * 통과하지만 사용자가 읽던 자리를 잃는다.
     */
    @Test
    fun `끝에 도달하면 다음 묶음이 뒤에 이어 붙는다`() =
        runTest {
            val first = notifications(count = 2, prefix = "first")
            val second = notifications(count = 2, prefix = "second")
            notificationRepository.setPage(page = 0, items = first, hasNext = true)
            notificationRepository.setPage(page = 1, items = second, hasNext = false)
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.ReachedEnd)

            val state = viewModel.state.value
            assertEquals((first + second).map { it.id }, state.items.map { it.id })
            assertEquals(NotificationPhase.Content, state.phase)
            assertFalse("더 받을 것이 없다고 응답했으면 그것이 상태에 남아야 한다", state.hasNext)
            assertFalse(state.isAppending)
            assertEquals("다음 묶음은 0이 아니라 1이다", listOf(0, 1), notificationRepository.requestedPages)
        }

    /**
     * 더 받을 것이 없으면 끝에 도달해도 **요청이 나가지 않는다**(EC-018, TS-038).
     *
     * 상태가 그대로인 것만으로는 "다시 받아 같은 값이 된 것"과 구별되지 않아 요청 횟수를 함께 본다. 빈 응답을
     * 받아 목록을 지우거나 빈 상태로 뒤집히지 않는 것도 여기서 갈린다.
     */
    @Test
    fun `더 받을 것이 없으면 끝에 도달해도 요청이 나가지 않는다`() =
        runTest {
            val items = notifications(count = 2)
            notificationRepository.setPage(page = 0, items = items, hasNext = false)
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.ReachedEnd)
            viewModel.processIntent(NotificationIntent.ReachedEnd)

            val state = viewModel.state.value
            assertEquals(listOf(0), notificationRepository.requestedPages)
            assertEquals(items.map { it.id }, state.items.map { it.id })
            assertEquals(NotificationPhase.Content, state.phase)
        }

    /**
     * 이어 붙이는 중에 다시 끝에 도달해도 같은 묶음을 두 번 부르지 않는다(EC-018).
     *
     * 끝 감지는 사용자가 조작하지 않아도 계속 일어나므로(UX-011) 이 가드가 없으면 스크롤 한 번에 같은 요청이
     * 여러 번 나가고, 같은 알림이 목록에 두 번 붙는다.
     */
    @Test
    fun `이어 붙이는 중에 다시 끝에 도달해도 요청이 겹치지 않는다`() =
        runTest {
            notificationRepository.setPage(page = 0, items = notifications(count = 2, prefix = "first"), hasNext = true)
            notificationRepository.setPage(
                page = 1,
                items = notifications(count = 2, prefix = "second"),
                hasNext = false,
            )
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)

            val gate = CompletableDeferred<Unit>()
            notificationRepository.gate = gate
            viewModel.processIntent(NotificationIntent.ReachedEnd)
            assertTrue("응답을 기다리는 동안임이 상태에 서야 뒤따르는 감지를 막을 수 있다", viewModel.state.value.isAppending)

            viewModel.processIntent(NotificationIntent.ReachedEnd)
            gate.complete(Unit)

            assertEquals(listOf(0, 1), notificationRepository.requestedPages)
            assertEquals(4, viewModel.state.value.items.size)
        }

    /**
     * 첫 묶음을 못 받으면 오류 상태가 되고(UX-002·EC-001), 무엇 때문에 못 받았는지는 리프 그대로 나간다.
     *
     * **빈 상태로 수렴시키지 않는 것**이 UX-002다 — 사용자가 "알림이 없다"와 "못 불러왔다"를 구별할 수 있어야
     * 한다. 상태가 리프를 담지 않으므로(`data-model.md` §2.3) 실패의 종류는 `DomainErrorEmitter`로만 화면에
     * 닿는다(`contracts/notification-ui.md` §4.1). 통로 하나만 서면 재시도 화면이나 사유 중 하나가 사라진다.
     */
    @Test
    fun `첫 묶음을 못 받으면 오류 상태가 되고 리프가 채널로 나간다`() =
        runTest {
            val failure = MinoDomainException.Network(IOException("offline"))
            notificationRepository.failPage(page = 0, error = failure)
            val viewModel = createViewModel()
            val domainErrors = collectDomainErrors(viewModel)

            viewModel.processIntent(NotificationIntent.Load)

            val state = viewModel.state.value
            assertEquals("빈 상태로 수렴시키면 못 불러온 것과 0건이 같아진다", NotificationPhase.Error, state.phase)
            assertTrue(state.items.isEmpty())
            assertEquals(1, domainErrors.size)
            assertSame("문구가 아니라 리프를 흘린다", failure, domainErrors.first())
        }

    /** 오류 상태의 재시도는 첫 묶음을 다시 부르고 목록을 세운다(UX-002, `data-model.md` §3). */
    @Test
    fun `오류 상태에서 재시도하면 목록이 실린다`() =
        runTest {
            val items = notifications(count = 2)
            notificationRepository.failPage(page = 0, error = MinoDomainException.Network(IOException("offline")))
            notificationRepository.setPage(page = 0, items = items, hasNext = false)
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)
            assertEquals("회복을 보려면 먼저 실패해 있어야 한다", NotificationPhase.Error, viewModel.state.value.phase)

            notificationRepository.succeedPage(page = 0)
            viewModel.processIntent(NotificationIntent.Retry)

            val state = viewModel.state.value
            assertEquals(NotificationPhase.Content, state.phase)
            assertEquals(items.map { it.id }, state.items.map { it.id })
            assertEquals(listOf(0, 0), notificationRepository.requestedPages)
        }

    /**
     * 추가 묶음 실패는 **이미 그린 목록을 건드리지 않는다**(UX-012·EC-016, TS-039).
     *
     * 첫 페이지 실패와 갈리는 지점이 여기다 — `phase`가 `Error`로 넘어가면 사용자가 보고 있던 알림이 오류
     * 화면에 덮인다. 채널로도 나가지 않는다: 이 실패를 알리는 자리는 목록 끝 하나이며, 통로가 둘이 되면
     * 스낵바와 재시도 표시가 같은 실패를 두 번 말한다(`research.md` D11).
     */
    @Test
    fun `추가 묶음 실패는 목록을 남기고 끝의 재시도 표시만 세운다`() =
        runTest {
            val first = notifications(count = 2, prefix = "first")
            notificationRepository.setPage(page = 0, items = first, hasNext = true)
            notificationRepository.failPage(page = 1, error = MinoDomainException.Network(IOException("offline")))
            val viewModel = createViewModel()
            val domainErrors = collectDomainErrors(viewModel)
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.ReachedEnd)

            val state = viewModel.state.value
            assertEquals("이미 본 알림을 오류 화면으로 덮지 않는다", NotificationPhase.Content, state.phase)
            assertEquals("이미 그린 목록은 그대로 남는다", first.map { it.id }, state.items.map { it.id })
            assertTrue(state.appendError)
            assertFalse(state.isAppending)
            assertTrue("추가 실패를 알리는 자리는 목록 끝 하나다", domainErrors.isEmpty())
        }

    /**
     * 재시도 표시가 서 있는 동안의 끝 도달은 **버려진다**.
     *
     * 끝 감지가 자동이라(UX-011) 막지 않으면 실패한 요청이 곧바로 다시 나가고, 사용자는 재시도 표시를 누를
     * 틈을 얻지 못한다(EC-016). 회복 경로는 `RetryAppend` 하나다.
     */
    @Test
    fun `재시도 표시가 서 있는 동안 끝에 도달해도 요청이 나가지 않는다`() =
        runTest {
            notificationRepository.setPage(page = 0, items = notifications(count = 2), hasNext = true)
            notificationRepository.failPage(page = 1, error = MinoDomainException.Network(IOException("offline")))
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)
            viewModel.processIntent(NotificationIntent.ReachedEnd)
            assertTrue("가드를 보려면 먼저 실패 표시가 서 있어야 한다", viewModel.state.value.appendError)

            viewModel.processIntent(NotificationIntent.ReachedEnd)
            viewModel.processIntent(NotificationIntent.ReachedEnd)

            assertEquals(listOf(0, 1), notificationRepository.requestedPages)
            assertTrue(viewModel.state.value.appendError)
        }

    /**
     * 재시도는 **같은 묶음**을 다시 부른다(`contracts/notification-ui.md` §2).
     *
     * 실패한 번호를 건너뛰고 다음으로 넘어가면 그 묶음의 알림이 통째로 사라진다. 성공하면 실패 표시가 걷히고
     * 새 묶음이 뒤에 붙는다.
     */
    @Test
    fun `추가 실패 뒤 재시도는 같은 묶음을 다시 부른다`() =
        runTest {
            val first = notifications(count = 2, prefix = "first")
            val second = notifications(count = 2, prefix = "second")
            notificationRepository.setPage(page = 0, items = first, hasNext = true)
            notificationRepository.setPage(page = 1, items = second, hasNext = false)
            notificationRepository.failPage(page = 1, error = MinoDomainException.Network(IOException("offline")))
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)
            viewModel.processIntent(NotificationIntent.ReachedEnd)

            notificationRepository.succeedPage(page = 1)
            viewModel.processIntent(NotificationIntent.RetryAppend)

            val state = viewModel.state.value
            assertEquals(
                "실패한 번호를 건너뛰면 그 묶음이 통째로 사라진다",
                listOf(0, 1, 1),
                notificationRepository.requestedPages,
            )
            assertEquals((first + second).map { it.id }, state.items.map { it.id })
            assertFalse("성공했으면 재시도 표시는 걷힌다", state.appendError)
        }

    /**
     * 조회가 도는 동안에는 **빈 상태가 아니다**(UX-001).
     *
     * 응답이 오기 전에 `Empty`로 두는 구현은 목록이 실릴 때마다 `받은 알림이 없어요`가 잠깐 스쳐 지나간다.
     * 게이트로 응답을 붙잡아 그 구간을 만든다.
     */
    @Test
    fun `첫 조회가 끝나기 전에는 빈 상태가 아니라 로딩이다`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            notificationRepository.gate = gate
            notificationRepository.setPage(page = 0, items = notifications(count = 1), hasNext = false)
            val viewModel = createViewModel()

            viewModel.processIntent(NotificationIntent.Load)

            assertEquals(NotificationPhase.Loading, viewModel.state.value.phase)

            gate.complete(Unit)

            assertEquals(NotificationPhase.Content, viewModel.state.value.phase)
        }

    /** 조회가 끝나 0건임이 확정된 뒤에만 빈 상태다(UX-001·FR-006, `data-model.md` §3). */
    @Test
    fun `조회가 끝나고 0건이면 빈 상태다`() =
        runTest {
            notificationRepository.setPage(page = 0, items = emptyList(), hasNext = false)
            val viewModel = createViewModel()

            viewModel.processIntent(NotificationIntent.Load)

            val state = viewModel.state.value
            assertEquals(NotificationPhase.Empty, state.phase)
            assertTrue(state.items.isEmpty())
        }

    /**
     * 0건이어도 **더 받을 것이 남았으면** 빈 상태가 아니다(`data-model.md` §3).
     *
     * `Loading → Empty`는 첫 응답이 0건이고 `hasNext`가 `false`일 때만이다. 건수만 보고 판정하는 구현은
     * 이어 붙일 것이 남은 화면에 `받은 알림이 없어요`를 띄운다.
     */
    @Test
    fun `0건이어도 더 받을 것이 있으면 빈 상태가 아니다`() =
        runTest {
            notificationRepository.setPage(page = 0, items = emptyList(), hasNext = true)
            val viewModel = createViewModel()

            viewModel.processIntent(NotificationIntent.Load)

            assertEquals(NotificationPhase.Content, viewModel.state.value.phase)
        }

    /**
     * 탭을 다녀와 다시 들어온 진입은 목록을 **다시 세우지 않는다**(FR-015·TS-011·TS-043).
     *
     * 재구성마다 첫 묶음을 다시 받으면 이어 붙여 둔 것이 사라지고 스크롤 위치도 잃는다. 재시도는 이 가드에
     * 걸리지 않는다 — 오류 상태에서 나갈 길이 그것뿐이라, 둘이 같은 취급을 받으면 화면이 오류에 갇힌다.
     */
    @Test
    fun `두 번째 진입은 재조회하지 않지만 재시도는 재조회한다`() =
        runTest {
            val first = notifications(count = 2, prefix = "first")
            notificationRepository.setPage(page = 0, items = first, hasNext = true)
            notificationRepository.setPage(
                page = 1,
                items = notifications(count = 2, prefix = "second"),
                hasNext = false,
            )
            val viewModel = createViewModel()
            viewModel.processIntent(NotificationIntent.Load)
            viewModel.processIntent(NotificationIntent.ReachedEnd)
            val afterAppend = viewModel.state.value
            val appended = afterAppend.items.map { it.id }

            viewModel.processIntent(NotificationIntent.Load)

            assertEquals(
                "두 번째 진입이 통과하면 이어 붙인 것이 첫 묶음으로 되돌아간다",
                listOf(0, 1),
                notificationRepository.requestedPages,
            )
            val afterSecondLoad = viewModel.state.value
            assertEquals(appended, afterSecondLoad.items.map { it.id })

            viewModel.processIntent(NotificationIntent.Retry)

            val afterRetry = viewModel.state.value
            assertEquals(listOf(0, 1, 0), notificationRepository.requestedPages)
            assertEquals(first.map { it.id }, afterRetry.items.map { it.id })
        }

    /**
     * 장소 대상 알림을 누르면 **그 알림이 실어 온 핀**으로 장소 상세 신호가 나간다
     * (FR-005·FR-022, TS-020~TS-022, `contracts/notification-ui.md` §3).
     *
     * 다른 핀을 가리키는 알림을 목록에 함께 두어 **누른 행에서 값을 꺼내는지**를 가른다 — 첫 행이나 마지막
     * 행을 집는 구현은 신호 타입만 보면 통과한다. 방을 고르는 단계가 없고 되묻지도 않으므로(FR-022,
     * TS-048) 신호는 곧바로, 한 번에 나간다.
     */
    @Test
    fun `장소 대상 알림을 누르면 그 핀의 장소 상세 신호가 나간다`() =
        runTest {
            val clicked = notification(id = "clicked", target = NotificationTarget.Pin("pin-clicked"))
            val other = notification(id = "other", target = NotificationTarget.Pin("pin-other"))
            notificationRepository.setPage(page = 0, items = listOf(other, clicked), hasNext = false)
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))

            assertEquals(
                "누른 행의 핀이어야 한다 — 목록의 다른 핀을 집으면 엉뚱한 장소가 열린다",
                listOf(NotificationSideEffect.NavigateToPlaceDetail("pin-clicked")),
                sideEffects,
            )
        }

    /** 공동방 참가 알림을 누르면 그 방으로 방 상세 신호가 나간다(FR-005, TS-023·TS-024). */
    @Test
    fun `공동방 참가 알림을 누르면 그 방의 방 상세 신호가 나간다`() =
        runTest {
            val clicked =
                notification(
                    id = "clicked",
                    target = NotificationTarget.Room("room-clicked"),
                    type = NotificationType.ROOM_MEMBER_JOINED,
                )
            val other = notification(id = "other", target = NotificationTarget.Pin("pin-other"))
            notificationRepository.setPage(page = 0, items = listOf(other, clicked), hasNext = false)
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))

            assertEquals(
                listOf(NotificationSideEffect.NavigateToRoomDetail("room-clicked")),
                sideEffects,
            )
        }

    /**
     * 저장 오류 알림을 누르면 안내 화면 신호가 나간다(FR-010, TS-025).
     *
     * 열 대상이 따로 없는(`NotificationTarget.None`) 유일한 갈래다 — 대상이 없다고 아무 신호도 내지 않으면
     * 사용자는 누른 알림에 반응이 없는 화면을 본다. 어느 건을 눌렀든 같은 화면이라 신호는 값을 싣지
     * 않는다(EC-013).
     */
    @Test
    fun `저장 오류 알림을 누르면 안내 화면 신호가 나간다`() =
        runTest {
            val clicked =
                notification(
                    id = "clicked",
                    target = NotificationTarget.None,
                    type = NotificationType.SAVE_FAILED,
                )
            notificationRepository.setPage(page = 0, items = listOf(clicked), hasNext = false)
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))

            assertEquals(listOf(NotificationSideEffect.NavigateToSaveErrorGuide), sideEffects)
        }

    /**
     * 빠르게 두 번 누르면 이동 신호는 **한 번만** 나간다(EC-011, `contracts/notification-ui.md` §2).
     *
     * 도착지 해석에 조회가 없어 「해석 중」이라는 구간이 없으므로(`contracts/notification-repository.md` §2)
     * 막을 것은 첫 신호가 나간 **직후** 들어오는 탭이다 — 그래서 두 번째 탭을 첫 번째와 같은 흐름에서, 아무
     * 것도 기다리지 않고 곧바로 넣는다. 신호가 두 번 나가면 홀더 적재와 탭 전환이 두 번 나가 어긋난다.
     */
    @Test
    fun `빠르게 두 번 누르면 이동 신호가 한 번만 나간다`() =
        runTest {
            val clicked = notification(id = "clicked", target = NotificationTarget.Pin("pin-clicked"))
            notificationRepository.setPage(page = 0, items = listOf(clicked), hasNext = false)
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(NotificationIntent.Load)

            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))
            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))

            assertEquals(
                "두 번째 탭까지 나가면 이동이 두 번 일어난다",
                listOf(NotificationSideEffect.NavigateToPlaceDetail("pin-clicked")),
                sideEffects,
            )
        }

    /**
     * 눌러 본 알림을 목록으로 돌아와 다시 누르면 **처음과 똑같이** 이동한다(EC-012).
     *
     * EC-011의 자물쇠가 한 번 이동한 알림을 영영 막아 버리면 사용자는 같은 알림을 두 번 열 수 없다. 목록으로
     * 돌아오는 것이 `Load`이므로(FR-015·TS-043) 그 진입이 자물쇠를 푸는 자리다 — 재조회는 여전히 없다.
     *
     * 행의 표시가 달라지지 않는 것도 함께 본다(FR-016·TS-034). 읽음 여부는 알림의 속성이 아니라, 누른 뒤의
     * 행은 누르기 전의 행과 같은 값이어야 한다.
     */
    @Test
    fun `목록으로 돌아와 다시 누르면 같은 이동 신호가 다시 나간다`() =
        runTest {
            val clicked = notification(id = "clicked", target = NotificationTarget.Pin("pin-clicked"))
            notificationRepository.setPage(page = 0, items = listOf(clicked), hasNext = false)
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)
            viewModel.processIntent(NotificationIntent.Load)
            val beforeClick = viewModel.state.value.items
            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))

            viewModel.processIntent(NotificationIntent.Load)
            viewModel.processIntent(NotificationIntent.NotificationClicked(clicked.id))

            val expected = NotificationSideEffect.NavigateToPlaceDetail("pin-clicked")
            assertEquals("한 번 눌러 본 알림이 영영 막히면 안 된다", listOf(expected, expected), sideEffects)
            assertEquals("돌아온 진입은 목록을 다시 받지 않는다", listOf(0), notificationRepository.requestedPages)
            assertEquals(
                "눌러 본 알림의 행이 누르기 전과 달라지지 않는다",
                beforeClick,
                viewModel.state.value.items,
            )
        }

    /**
     * 케이스마다 새 인스턴스를 만든다 — 이동 자물쇠(EC-011)가 인스턴스에 남으므로 재사용하면 뒤 케이스의
     * 클릭이 앞 케이스에 막힌다.
     *
     * 도착지 해석은 더블을 두지 않고 **실물**을 넘긴다. 조회가 없는 순수 매핑이라
     * (`contracts/notification-repository.md` §2) 가짜로 바꿀 협력자가 아니고, 가짜를 두면 대상 갈래와
     * 도착지의 대응을 이 테스트가 다시 적게 되어 `ResolveNotificationDestinationUseCaseTest`와 두 벌이 된다.
     */
    private fun createViewModel(): NotificationViewModel =
        NotificationViewModel(
            notificationRepository = notificationRepository,
            resolveNotificationDestination = ResolveNotificationDestinationUseCase(),
        )

    /** 수집을 인텐트보다 먼저 걸어 둔다 — 채널로 나가는 일회성 신호는 놓치면 되돌릴 수 없다. */
    private fun TestScope.collectDomainErrors(viewModel: NotificationViewModel): List<MinoDomainException> {
        val collected = mutableListOf<MinoDomainException>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.domainErrors.toList(collected) }
        return collected
    }

    /** [collectDomainErrors]와 같은 이유로 인텐트보다 먼저 건다 — 방출 **횟수**가 판정 대상이다. */
    private fun TestScope.collectSideEffects(viewModel: NotificationViewModel): List<NotificationSideEffect> {
        val collected = mutableListOf<NotificationSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(collected) }
        return collected
    }

    /**
     * 도착지를 가르는 값만 세운 알림 한 건. 판정에 쓰이는 것은 [target]뿐이며 [type]은 썸네일 갈래를 위해
     * 함께 맞춰 둔다 — 도착지는 유형이 아니라 대상으로 갈린다
     * (`ResolveNotificationDestinationUseCase`).
     */
    private fun notification(
        id: String,
        target: NotificationTarget,
        type: NotificationType = NotificationType.PLACE_DUPLICATED,
    ): Notification =
        Notification(
            id = id,
            type = type,
            typeLabel = "유형 문구",
            targetName = "대상 이름",
            thumbnailUrl = null,
            target = target,
            createdAt = NOW,
        )

    /**
     * 순서를 눈으로 구별할 수 있게 id에 번호를 매긴 알림 [count]건.
     *
     * [elapsedGrowsWithIndex]가 `false`면 **오래된 것이 앞에** 온다 — 서버 순서와 발생 시각 순서를 어긋나게
     * 만들어, 목록을 스스로 정렬하는 구현을 드러내는 데 쓴다. 나머지 필드는 판정에 쓰이지 않는다.
     */
    private fun notifications(
        count: Int,
        prefix: String = "notification",
        elapsedGrowsWithIndex: Boolean = true,
    ): List<Notification> =
        List(count) { index ->
            val offset = if (elapsedGrowsWithIndex) index else count - index
            Notification(
                id = "$prefix-$index",
                type = NotificationType.PLACE_DUPLICATED,
                typeLabel = "이미 저장한 장소예요",
                targetName = "장소 $index",
                thumbnailUrl = null,
                target = NotificationTarget.Pin("pin-$prefix-$index"),
                createdAt = NOW - (offset * 10).minutes,
            )
        }

    private companion object {
        /** 발생 시각의 기준점. 경과 시간 자체는 판정 대상이 아니라 순서를 만드는 데만 쓴다. */
        val NOW: Instant = Instant.fromEpochSeconds(1_756_000_000)
    }
}
