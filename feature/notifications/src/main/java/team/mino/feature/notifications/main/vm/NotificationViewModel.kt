@file:OptIn(ExperimentalTime::class)

package team.mino.feature.notifications.main.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.Notification
import team.mino.core.domain.model.NotificationDestination
import team.mino.core.domain.model.NotificationType
import team.mino.core.domain.repository.NotificationRepository
import team.mino.core.domain.usecase.ResolveNotificationDestinationUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.notifications.main.model.NotificationItemUiModel
import team.mino.feature.notifications.main.model.NotificationThumbnail
import team.mino.feature.notifications.main.util.elapsedTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 알림 탭 목록 화면의 ViewModel.
 *
 * 사용자 조작 하나가 [NotificationIntent] 하나로 들어와 [processIntent]의 한 분기로 간다 —
 * 목록은 `docs/specs/notifications/contracts/notification-ui.md` §2가 소유한다.
 *
 * **페이징을 직접 든다.** 다음에 어느 묶음을 요청할지는 호출자가 정한다는 계약이라
 * (`docs/specs/notifications/contracts/notification-repository.md` §1) 여기서 [nextPage]를 세고 받은 묶음을
 * 이어 붙인다. 한 묶음의 크기는 서버가 정하므로 요청에 싣지 않는다(spec §4).
 *
 * **실패는 첫 페이지와 추가 페이지를 갈라 담는다**(`docs/specs/notifications/research.md` D11). 첫 페이지
 * 실패만 [NotificationPhase.Error]로 화면을 덮고, 추가 페이지 실패는 [NotificationUiState.appendError]만
 * 세워 이미 그린 목록을 남긴다(spec UX-012·EC-016).
 *
 * **문구를 만들지 않는다.** 경과 시간은 [elapsedTime]이 끊어 준 갈래로, 썸네일은 어느 그림을 그릴지 정해진
 * 갈래로 올라가고 문자열은 행이 리소스에서 꺼낸다(`docs/specs/notifications/data-model.md` §2.1.1).
 */
@HiltViewModel
internal class NotificationViewModel
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
        private val resolveNotificationDestination: ResolveNotificationDestinationUseCase,
    ) :
    ViewModel(),
        MviContainer<NotificationUiState, NotificationSideEffect> by mviContainer(NotificationUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        /**
         * 받은 그대로의 도메인 목록. 화면 모델이 유형·대상을 버리므로(`data-model.md` §2.1) 행을 눌렀을 때
         * 도착지를 정할 재료가 여기에만 남는다.
         *
         * 밖으로 나가지 않는 버퍼라 묶음을 이을 때 새 리스트를 만들지 않고 뒤에 덧붙인다.
         */
        private val notifications = mutableListOf<Notification>()

        /** 다음에 요청할 묶음의 번호. 0이 최신 묶음이다(FR-018). */
        private var nextPage: Int = FIRST_PAGE

        /**
         * 첫 페이지를 이미 요청했는가. 탭을 떠났다 돌아오면 화면이 다시 그려지지만 목록은 그대로여야 하므로
         * (FR-015·TS-011·TS-043) 그때 들어오는 [NotificationIntent.Load]를 여기서 막는다. 재시도는 이 값과
         * 무관하게 다시 세운다.
         */
        private var firstPageRequested = false

        /**
         * 이동 요청을 이미 냈는가. 두 번째 요청을 막는 자물쇠이며(spec EC-011) 화면에 다시 들어올 때 풀린다
         * ([enterScreen]).
         */
        private var navigationRequested = false

        fun processIntent(intent: NotificationIntent) {
            when (intent) {
                NotificationIntent.Load -> enterScreen()
                NotificationIntent.Retry -> loadFirstPage()
                NotificationIntent.ReachedEnd -> reachEnd()
                NotificationIntent.RetryAppend -> retryAppend()
                is NotificationIntent.NotificationClicked -> clickNotification(intent.id)
                NotificationIntent.SaveErrorGuideBackClicked -> launchSafely {
                    postSideEffect(NotificationSideEffect.NavigateBack)
                }
            }
        }

        /**
         * 화면에 들어왔다. 첫 묶음은 한 번만 부르고(FR-015·TS-011·TS-043) 이동 자물쇠는 여기서 푼다.
         *
         * 이 의도는 최초 진입뿐 아니라 **탭을 떠났다 돌아올 때마다** 들어온다 — 그래서 [firstPageRequested]가
         * 재조회를 막는 자리이자, 돌아온 사용자가 같은 알림을 다시 누를 수 있게 되는 자리다(spec EC-012).
         */
        private fun enterScreen() {
            navigationRequested = false
            if (!firstPageRequested) loadFirstPage()
        }

        /**
         * 첫 묶음을 받아 목록을 처음부터 세운다. 진입과 재시도가 같은 경로다.
         *
         * 실패는 상태와 채널 양쪽으로 나간다 — [NotificationPhase.Error]가 재시도 가능한 오류 화면을 세우고
         * (UX-002·EC-001), 무엇 때문에 못 받았는지는 리프 그대로 흘려 `NotificationRoute`가 문구로 옮긴다
         * (`contracts/notification-ui.md` §4.1, `docs/conventions/error_handling.md` §5). 상태가 리프를
         * 담지 않으므로(`data-model.md` §2.3) 이 통로가 없으면 실패의 종류가 화면에 닿지 않는다.
         */
        private fun loadFirstPage() {
            firstPageRequested = true
            launchSafely {
                updateState {
                    copy(phase = NotificationPhase.Loading, isAppending = false, appendError = false)
                }

                val page =
                    runCatchingDomain { notificationRepository.getNotifications(FIRST_PAGE) }
                        .onDomainFailure(::showLoadError)
                        .getOrNull() ?: return@launchSafely

                notifications.clear()
                notifications += page.items
                nextPage = FIRST_PAGE + 1

                val items = page.items.toUiModels(Clock.System.now())
                // 조회가 끝나 0건임이 확정된 뒤에만 빈 상태다(UX-001, `data-model.md` §3).
                val loadedPhase =
                    if (items.isEmpty() && !page.hasNext) NotificationPhase.Empty else NotificationPhase.Content
                updateState { copy(items = items, phase = loadedPhase, hasNext = page.hasNext) }
            }
        }

        private fun showLoadError(error: MinoDomainException) {
            updateState { copy(phase = NotificationPhase.Error) }
            emitDomainError(error)
        }

        /**
         * 목록 끝 도달(UX-011). 더 받을 것이 없거나 이미 받는 중이면 버린다(EC-018).
         *
         * 실패 표시가 떠 있는 동안에도 버린다. 끝 감지는 사용자가 조작하지 않아도 계속 일어나므로, 막지 않으면
         * 실패한 요청이 곧바로 다시 나가 재시도 표시(EC-016)를 누를 틈이 없다.
         */
        private fun reachEnd() {
            val current = state.value
            if (!current.hasNext || current.isAppending || current.appendError) return
            appendNextPage()
        }

        /** 목록 끝의 재시도 표시 탭. 표시를 내리고 **같은 묶음**을 다시 요청한다 — [nextPage]는 그대로다. */
        private fun retryAppend() {
            if (state.value.isAppending) return
            appendNextPage()
        }

        /**
         * 다음 묶음을 받아 뒤에 잇는다.
         *
         * 실패해도 [NotificationUiState.items]와 [NotificationUiState.phase]를 건드리지 않는다 — 이미 본
         * 알림을 지우거나 오류 화면으로 덮지 않는 것이 UX-012다. 방금 받은 묶음의 경과 시간만 지금 시각으로
         * 끊고 앞서 받은 행은 다시 계산하지 않는다(`research.md` D12).
         */
        private fun appendNextPage() =
            launchSafely {
                updateState { copy(isAppending = true, appendError = false) }

                val page =
                    runCatchingDomain { notificationRepository.getNotifications(nextPage) }
                        .onDomainFailure { updateState { copy(isAppending = false, appendError = true) } }
                        .getOrNull() ?: return@launchSafely

                notifications += page.items
                nextPage += 1

                val appended = page.items.toUiModels(Clock.System.now())
                updateState {
                    copy(
                        items = items.addAll(appended),
                        isAppending = false,
                        hasNext = page.hasNext,
                    )
                }
            }

        /**
         * 알림 행 탭. 유형·대상을 버린 화면 모델 대신 [notifications]에서 [id]로 찾아
         * [resolveNotificationDestination]에 넘긴다(`data-model.md` §2.1). 어느 방으로 열지 되묻지 않고 방을
         * 고르는 단계도 두지 않는다 — 도착지는 알림이 실어 온 대상 하나로 정해진다(spec FR-005·FR-022·SC-014).
         *
         * **이동 요청은 한 번만 나간다**(spec EC-011). 도착지 해석에 조회가 없어 「해석 중」이라는 구간 자체가
         * 없으므로(`contracts/notification-repository.md` §2), 막아야 하는 것은 방출과 실제 전환 사이에 들어오는
         * 두 번째 탭이다. 그래서 자물쇠는 코루틴이 끝날 때가 아니라 목록으로 돌아왔을 때 풀린다([enterScreen]).
         */
        private fun clickNotification(id: String) {
            if (navigationRequested) return
            val notification = notifications.firstOrNull { it.id == id } ?: return

            val destination = resolveNotificationDestination(notification)
            navigationRequested = true
            launchSafely { postSideEffect(destination.toSideEffect()) }
        }

        private companion object {
            /** 최신 묶음의 번호(`contracts/notification-repository.md` §1). */
            const val FIRST_PAGE = 0
        }
    }

/**
 * 받은 묶음을 화면 모델로 옮긴다. [observedAt]은 **묶음마다 한 번** 읽은 기준 시각이다 — 그리는 자리에서
 * 「지금」을 읽으면 리컴포지션마다 표기가 흔들린다(spec EC-005, `research.md` D12).
 */
private fun List<Notification>.toUiModels(observedAt: Instant): PersistentList<NotificationItemUiModel> =
    map { it.toUiModel(observedAt) }.toPersistentList()

/**
 * 도착지 하나가 신호 하나로 간다(`contracts/notification-ui.md` §3). 갈래를 남김없이 가르므로 도착지가 늘면
 * 여기서 컴파일이 멈춘다.
 */
private fun NotificationDestination.toSideEffect(): NotificationSideEffect =
    when (this) {
        is NotificationDestination.PlaceDetail -> NotificationSideEffect.NavigateToPlaceDetail(pinId)
        is NotificationDestination.RoomDetail -> NotificationSideEffect.NavigateToRoomDetail(roomId)
        NotificationDestination.SaveErrorGuide -> NotificationSideEffect.NavigateToSaveErrorGuide
    }

private fun Notification.toUiModel(observedAt: Instant): NotificationItemUiModel =
    NotificationItemUiModel(
        id = id,
        typeLabel = typeLabel,
        targetName = targetName,
        elapsed = elapsedTime(createdAt = createdAt, observedAt = observedAt),
        thumbnail =
            when (type) {
                // 저장 오류만 유형으로 갈린다 — 나머지 다섯은 서버가 고른 이미지 한 장이다(`data-model.md` §2.2).
                NotificationType.SAVE_FAILED -> NotificationThumbnail.SaveError
                else -> NotificationThumbnail.Image(thumbnailUrl)
            },
    )
