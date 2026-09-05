package team.mino.feature.notifications.main.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.scrollbar.MinoScrollBar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.notifications.R
import team.mino.feature.notifications.main.component.NotificationEmptyContent
import team.mino.feature.notifications.main.component.NotificationErrorContent
import team.mino.feature.notifications.main.component.NotificationListFooter
import team.mino.feature.notifications.main.component.NotificationRow
import team.mino.feature.notifications.main.model.NotificationItemUiModel
import team.mino.feature.notifications.main.model.NotificationThumbnail
import team.mino.feature.notifications.main.util.ElapsedTime
import team.mino.feature.notifications.main.vm.NotificationIntent
import team.mino.feature.notifications.main.vm.NotificationPhase
import team.mino.feature.notifications.main.vm.NotificationUiState

/**
 * 알림 탭 목록 화면. 상태와 콜백만으로 그린다 — ViewModel도 `NavController`도 모른다.
 *
 * **제목 아래가 곧 목록이다**(spec FR-017·UX-010). 권한을 켜라고 권하는 배너도, 안 읽은 건수를 세는 배지도
 * 그 사이에 두지 않는다 — 시안의 유도 배너 레이어도 이 상태에서는 꺼져 있다.
 *
 * [NotificationUiState.phase]가 **제목 아래 자리 하나만** 가른다. 제목은 어느 값에서도 그대로 남는다.
 *
 * - [NotificationPhase.Loading]은 아무것도 그리지 않는다. 조회가 끝나기 전에 빈 상태 문구가 보이면 「알림이
 *   없다」로 읽히기 때문이며(spec UX-001), 이 구간의 화면은 시안에 없어 자리를 비워 둔다.
 * - [NotificationPhase.Empty]와 [NotificationPhase.Error]를 갈라 그린다 — 못 불러온 것과 받은 것이 없는
 *   것은 서로 다른 화면이다(spec UX-002·EC-001).
 */
@Composable
internal fun NotificationScreen(
    state: NotificationUiState,
    onIntent: (NotificationIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        NotificationTitle()

        when (state.phase) {
            NotificationPhase.Loading -> Unit

            NotificationPhase.Content ->
                NotificationList(
                    items = state.items,
                    isAppending = state.isAppending,
                    appendError = state.appendError,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )

            NotificationPhase.Empty -> NotificationEmptyContent(modifier = Modifier.weight(1f))

            NotificationPhase.Error ->
                NotificationErrorContent(
                    onRetryClick = { onIntent(NotificationIntent.Retry) },
                    modifier = Modifier.weight(1f),
                )
        }
    }
}

/**
 * 화면 제목. 시안의 표시줄에는 우측 액션 자리가 있으나 이 화면에서는 꺼져 있어 제목 하나만 그린다.
 *
 * `MinoTopNavigation`을 쓰지 않는다 — 그쪽은 제목을 가운데 놓고 뒤로가기를 여는 다른 컴포넌트이고,
 * 이 표시줄은 그 컴포넌트의 인스턴스가 아니라 화면이 직접 든 프레임이다.
 *
 * 배경을 칠하지 않는다. 상태 표시줄 인셋도 다루지 않는다 — 둘 다 셸(`MinoScaffold`)이 이미 처리해,
 * 이 표시줄은 그 아래에서 자기 높이만 차지한다.
 */
@Composable
private fun NotificationTitle(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = TitleHorizontalPadding,
                end = TitleHorizontalPadding,
                top = TitleTopPadding,
                bottom = TitleBottomPadding,
            ),
        text = stringResource(R.string.notification_title),
        color = MinoAndroidTheme.colors.labelStrong,
        style = MinoAndroidTheme.typography.title3Bold,
    )
}

/**
 * 받은 알림을 순서대로 잇는 목록. 행 사이에 여백도 구분선도 두지 않아 행 높이가 그대로 목록의 리듬이 된다.
 *
 * **끝에 닿는 것이 곧 다음 묶음 요청이다**(spec UX-011) — `더 보기` 버튼도 당겨서 새로고침도 없다.
 * 더 받을 것이 있는지, 이미 받는 중인지의 판정은 ViewModel이 들고 있으므로 여기서는 닿았다는 사실만 올린다.
 * 끝 도달을 [derivedStateOf] 안에서 읽는 것은 스크롤할 때마다 바뀌는 값이라, 컴포지션 본문에서 읽으면 프레임
 * 마다 이 목록이 다시 그려지기 때문이다.
 *
 * 목록이 뷰포트를 채우지 못할 때도 끝에 닿은 것으로 본다. 그 경우 사용자가 스크롤로 끝에 닿을 방법이 없어
 * 자동 요청을 걸지 않으면 다음 묶음이 영영 오지 않는다.
 *
 * 맨 끝 자리는 [NotificationListFooter]가 스스로 그릴지 말지를 정한다 — 조건을 여기서 한 번 더 두면 판정
 * 출처가 둘로 갈린다.
 *
 * 오른쪽 끝에 겹쳐 놓인 [MinoScrollBar]는 목록과 같은 [listState]를 보며 스크롤 위치만 표시한다. 목록 위에
 * 얹히므로 행의 여백을 건드리지 않고, 그리기만 할 뿐 입력을 받지 않아 행 탭도 가로채지 않는다.
 */
@Composable
private fun NotificationList(
    items: ImmutableList<NotificationItemUiModel>,
    isAppending: Boolean,
    appendError: Boolean,
    onIntent: (NotificationIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // 행마다 만들면 살아 있는 행 수만큼 벡터 서브컴포지션이 선다 — 목록에서 한 번 만들어 내려보낸다.
    val imageFallback = rememberVectorPainter(MinoIcons.Image)
    val reachedEnd by remember(listState) { derivedStateOf { !listState.canScrollForward } }

    LaunchedEffect(reachedEnd) {
        if (reachedEnd) onIntent(NotificationIntent.ReachedEnd)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
            items(items = items, key = { it.id }, contentType = { ROW_CONTENT_TYPE }) { item ->
                NotificationRow(
                    item = item,
                    imageFallback = imageFallback,
                    onClick = { onIntent(NotificationIntent.NotificationClicked(item.id)) },
                )
            }
            item(key = FOOTER_KEY, contentType = FOOTER_CONTENT_TYPE) {
                NotificationListFooter(
                    isAppending = isAppending,
                    appendError = appendError,
                    onRetryClick = { onIntent(NotificationIntent.RetryAppend) },
                )
            }
        }
        MinoScrollBar(scrollState = listState, modifier = Modifier.align(Alignment.TopEnd))
    }
}

private val TitleHorizontalPadding = 20.dp

private val TitleTopPadding = 10.dp

private val TitleBottomPadding = 18.dp

/** 행 키가 서버 UUID라 겹치지 않는다. */
private const val FOOTER_KEY = "footer"

/**
 * 행과 목록 끝 표시는 생김새가 달라 노드를 나눠 쓴다. 지정하지 않으면 lazy 레이아웃이 둘을 서로 재활용하려다
 * 실패해 매번 새로 컴포즈한다.
 */
private const val ROW_CONTENT_TYPE = "row"

private const val FOOTER_CONTENT_TYPE = "footer"

/**
 * 미리보기 표본. 컴포저블 밖에 두는 것은 [NotificationThumbnail.Image]를 부르는 자리를 컴포지션에서 빼기
 * 위해서다 — 이름이 같은 컴포저블이 있어 컴포지션 안에서는 Compose Lint가 UI를 그리는 호출로 읽는다.
 */
private val SampleItems = persistentListOf(
    NotificationItemUiModel(
        id = "1",
        typeLabel = "이미 저장해둔 곳이에요",
        targetName = "연남동 스탠딩 커피",
        elapsed = ElapsedTime.JustNow,
        thumbnail = NotificationThumbnail.Image(url = null),
    ),
    NotificationItemUiModel(
        id = "2",
        typeLabel = "장소를 저장하지 못했어요.",
        targetName = "잠시 후 다시 시도해주세요",
        elapsed = ElapsedTime.HoursAgo(1),
        thumbnail = NotificationThumbnail.SaveError,
    ),
    NotificationItemUiModel(
        id = "3",
        typeLabel = "지은님이 들어왔어요",
        targetName = "언젠가 가야지 방",
        elapsed = ElapsedTime.AbsoluteDate(month = 8, day = 10),
        thumbnail = NotificationThumbnail.Image(url = null),
    ),
)

/** 목록이 있는 화면 — 제목 바로 아래에서 첫 행이 시작한다. */
@UiModePreviews
@Composable
private fun NotificationScreenPreview() {
    MinoAndroidAppTheme {
        NotificationScreen(
            state = NotificationUiState(items = SampleItems, phase = NotificationPhase.Content),
            onIntent = {},
        )
    }
}

/** 받은 알림이 0건인 화면. 조회가 끝난 뒤에만 이 얼굴이 된다. */
@UiModePreviews
@Composable
private fun NotificationScreenEmptyPreview() {
    MinoAndroidAppTheme {
        NotificationScreen(
            state = NotificationUiState(phase = NotificationPhase.Empty),
            onIntent = {},
        )
    }
}

/** 첫 묶음을 못 받은 화면 — 빈 상태와 다른 얼굴이어야 한다. */
@UiModePreviews
@Composable
private fun NotificationScreenErrorPreview() {
    MinoAndroidAppTheme {
        NotificationScreen(
            state = NotificationUiState(phase = NotificationPhase.Error),
            onIntent = {},
        )
    }
}
