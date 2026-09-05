package team.mino.feature.home.main.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.RoomSummary
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.home.main.model.HomePhase
import team.mino.feature.home.main.model.HomeTooltip
import team.mino.feature.home.main.model.SavePickerState

/**
 * 홈 탭 화면의 상태.
 *
 * 홈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은 콜백이 아니라 전부 여기 담긴다 —
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §1.
 *
 * @property phase 카드 자리에 무엇이 놓이는지. 상단(방 뱃지·인사 문구·정렬 칩)은 어느 값에서도 남는다.
 * @property room 지금 보고 있는 방. 첫 덱을 받기 전이라 아직 정해지지 않았으면 `null`이다.
 * @property rooms 방 시트가 늘어놓을 방 목록(spec FR-018). 순회 판정용 목록과 같은 값이지만 **시트가 그리는
 *  것**이라 상태로도 올라온다 — 홈에 머무는 동안 다시 받지 않으므로 첫 조회 뒤 바뀌지 않는다.
 * @property sort 방이 바뀌면 선언 순서상 첫 정렬로 되돌아간다(spec FR-013).
 * @property cards 남은 카드. 최상단이 첫 원소이며, 넘긴 카드는 여기서 덜어낸다.
 * @property isTransitioning 전환 애니메이션이 도는 중. 참인 동안 스와이프 의도는 큐에 쌓지 않고 버린다
 *  (spec UX-001, `research.md` R-007) — 그래서 진행 중인 애니메이션을 상태로 들고 있어야 한다.
 *  **[phase]가 바뀔 때는 완료 신호를 기다리지 않고 여기서 내린다.** 완료를 알리는 것은 카드 덱 컴포저블인데
 *  [HomePhase.DECK]를 벗어나면 그 컴포저블이 통째로 빠져 애니메이션 코루틴이 취소되고, 신호가 끝내 오지
 *  않아 이후 스와이프가 전부 버려진다.
 * @property tooltip 한 번에 하나만 뜬다. 사라진 상태가 `null`이다.
 * @property actionMenuTarget 액션 메뉴가 열린 카드의 pinId. 메뉴는 카드 앵커에 묶여 있어 대상 없이 열리지 않는다.
 * @property isRoomSheetOpen 「홈 방 시트」(방 변경)가 열렸는지(spec FR-017·018).
 * @property savePicker 「방 선택 시트」(`다른 방 저장`)의 상태. `null`이면 닫힌 것이고, `selectedRoomIds`가
 *  비어 있으면 `저장하기`가 비활성이다(spec FR-005, EC-018) — 시트가 서로 다른 컴포넌트라 [isRoomSheetOpen]과
 *  값을 공유하지 않는다.
 * @property isGuideVisible 참인 동안 [HomeIntent.DismissGuide]를 뺀 모든 의도를 버린다(spec FR-019).
 *  [phase]와 직교한다 — 볼 카드가 없어도 가이드를 먼저 띄운다(spec EC-016).
 * @property undoStack 우→좌 스와이프로 되돌릴 카드들. 넘긴 순서대로 쌓이고 **뒤에서부터** 꺼낸다 —
 *  이 덱에서 넘긴 카드가 남아 있는 한 연속으로 되돌아간다(spec FR-002). 덱이 바뀌면 통째로 비운다(spec EC-003).
 * @property loadError 주 데이터(방 목록·덱) 로드 실패. 문구가 아니라 리프를 담아 그리는 쪽이 매핑한다
 *  (`docs/conventions/error_handling.md` §5 1행). [HomePhase.ERROR]와 함께 세워지고 함께 걷힌다 —
 *  사용자 액션의 일회성 실패는 여기 오지 않고 `DomainErrorEmitter`로 나간다(같은 표 2행).
 */
@Immutable
internal data class HomeUiState(
    val phase: HomePhase = HomePhase.LOADING,
    val room: RoomSummary? = null,
    val rooms: ImmutableList<RoomSummary> = persistentListOf(),
    val sort: DeckSort = DeckSort.GGUK_PICK,
    val cards: ImmutableList<PlaceCard> = persistentListOf(),
    val isTransitioning: Boolean = false,
    val tooltip: HomeTooltip? = null,
    val actionMenuTarget: String? = null,
    val isRoomSheetOpen: Boolean = false,
    val savePicker: SavePickerState? = null,
    val isGuideVisible: Boolean = false,
    val undoStack: ImmutableList<PlaceCard> = persistentListOf(),
    val loadError: MinoDomainException? = null,
) : UiState
