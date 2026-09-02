package team.mino.feature.room.placedetail.component

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.flow.drop
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/*
 * 이 화면의 시트들이 함께 쓰는 조각들.
 *
 * 손잡이·구분선·모서리는 어느 시트냐와 무관하게 같은 값이어야 한다. 시트마다 한 벌씩 두면 디자인이 손잡이
 * 치수나 모서리를 바꿀 때 한쪽만 따라가고, 같은 화면 안에서 두 시트가 서로 다른 모양이 된다. 아래로 끌어
 * 닫는 동작도 같다 — 되돌아오는 거리와 닫혔다고 보는 지점이 시트마다 다르면 같은 손짓이 다르게 먹는다.
 *
 * 이 모듈 밖으로는 올리지 않는다 — 세 번째 소비자가 다른 feature에서 나오면 그때
 * [`component-asset-placement.md`](../../../../../../../../../../docs/conventions/component-asset-placement.md)
 * §2.1이 승격 자리를 정한다.
 */

/** 시트 컨테이너의 위쪽 모서리. 두 시트가 같은 곡률로 올라온다. */
internal val SheetContainerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

/**
 * 컨테이너를 위아래로 [top]·[bottom]만큼 부풀려 실제 화면 가장자리까지 그리게 한다.
 *
 * **왜 필요한가.** 이 화면이 놓이는 자리는 `MinoScaffold`가 상태바·내비게이션 바 높이만큼 이미 물러난
 * 뒤의 영역이다. 시트는 그 안에서 하단 정렬되므로 `fillMaxSize`만으로는 아랫변이 내비게이션 바 위에서
 * 끊기고, 딤도 상태바 자리에 닿지 못한다. 그 자리를 되찾는 방법은 측정 높이 자체를 늘리는 것뿐이다 —
 * 안쪽에서 음수 패딩을 흉내 내면 자식 배치가 함께 밀린다.
 *
 * **부풀린 안에서 인셋을 다시 얹지 않는다.** 늘어난 컨테이너는 이미 화면 가장자리에 닿아 있어, 안에서
 * `navigationBarsPadding()`을 한 번 더 걸면 같은 인셋이 두 번 들어간다. 시스템 바를 피해야 하는 요소가
 * 있다면 그 요소 하나만 인셋을 든다(`MinoActionArea` KDoc).
 *
 * 같은 셸에서 `RoomNudgeAutoSheet`가 실기기로 확인하고 같은 형태로 해결해 둔 결함이다.
 */
internal fun Modifier.systemBarBleed(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
): Modifier =
    layout { measurable, constraints ->
        // 높이가 무계인 자리(프리뷰)에는 되찾을 가장자리가 없다.
        val isBounded = constraints.hasBoundedHeight
        val topBleedPx = if (isBounded) top.roundToPx() else 0
        val bottomBleedPx = if (isBounded) bottom.roundToPx() else 0
        val placeable = if (isBounded) {
            val targetHeight = constraints.maxHeight + topBleedPx + bottomBleedPx
            measurable.measure(constraints.copy(minHeight = targetHeight, maxHeight = targetHeight))
        } else {
            measurable.measure(constraints)
        }
        layout(placeable.width, placeable.height) { placeable.place(0, -topBleedPx) }
    }

/**
 * 딤을 깔고 그 위에 시트 하나를 세우는 컨테이너. 딤 위에 뜨는 두 시트가 함께 쓴다.
 *
 * **딤은 화면 전체를 덮는다.** 상태바 자리까지 닿지 않으면 화면 위쪽만 딤이 걷힌 것처럼 보이고, 시트도
 * 내비게이션 바 위에서 끊긴다 — 그 자리를 되찾는 것은 [systemBarBleed]가 한다.
 *
 * **상태바 아이콘을 밝은 쪽으로 돌린다.** 딤은 테마와 무관하게 어두워서, 라이트 테마가 정한 어두운
 * 글리프를 그대로 두면 대비가 무너진다. 시트가 컴포지션에서 빠지면 시스템 다크 테마 여부가 정한
 * 기본값으로 되돌린다. 내비게이션 바는 시트의 밝은 배경 위에 있으므로 건드리지 않는다.
 *
 * **동시에 둘이 서지 않는다.** 어느 한쪽이 서면 그 딤이 나머지를 여는 입구를 덮으므로, 이 컨테이너의
 * 상태바 처리가 서로 엇갈릴 일이 없다.
 *
 * @param onDismissRequest 딤 영역 탭. 끌어 내려 닫는 경로는 시트 자신이 갖는다.
 * @param content 딤 위에 하단 정렬로 서는 시트. 아랫변이 화면 바닥에 닿는다.
 */
@Composable
internal fun DimmedSheetContainer(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    LightStatusBarIcons()

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarBleed(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.materialDimmer)
                .singleClickable(onClick = onDismissRequest),
        )
        content()
    }
}

/**
 * 컴포지션에 남아 있는 동안 상태바 아이콘을 밝은 쪽으로 고정한다.
 *
 * 시스템이 대비 확보용으로 얹는 스크림은 함께 끈다 — 이미 깔린 딤 위에 한 번 더 덮여 상태바 자리만
 * 유독 시커멓게 보인다(`RoomNudgeAutoSheet`가 실기기로 확인).
 */
@Composable
private fun LightStatusBarIcons() {
    val activity = LocalActivity.current
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()
    DisposableEffect(isDarkTheme, activity) {
        val window = activity?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        window?.isStatusBarContrastEnforced = false
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = !isDarkTheme
            window?.isStatusBarContrastEnforced = true
        }
    }
}

/** 시트 안 구분선의 굵기. 코멘트 목록의 항목 사이 선도 같은 값을 쓴다. */
internal val SheetDividerThickness: Dp = 1.dp

/** 시트 맨 위의 손잡이. 끄는 것을 받는 것은 시트 전체이고, 이 표식은 그 자리를 알린다. */
@Composable
internal fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HandleContainerHeight),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = Modifier
                .size(width = HandleWidth, height = HandleHeight)
                .surface(shape = HandleShape, containerColor = MinoAndroidTheme.colors.fillNormal),
        )
    }
}

/**
 * 시트 안 구역을 가르는 선. 선은 한 줄이고 위아래 여백은 그것을 담은 띠가 낸다.
 *
 * [horizontalPadding]은 선을 좌우로 물리는 폭이다. 콘텐츠 폭에 맞춰 선을 줄이는 시트가 있어 열어 둔다.
 */
@Composable
internal fun SheetSectionDivider(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DividerBandHeight)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            thickness = SheetDividerThickness,
            color = MinoAndroidTheme.colors.lineNormalNormal,
        )
    }
}

private val HandleContainerHeight = 30.dp

private val HandleWidth = 38.dp

private val HandleHeight = 4.dp

private val HandleShape = RoundedCornerShape(4.dp)

private val DividerBandHeight = 12.dp

/**
 * 아래로 끌어 닫는 시트의 드래그 상태.
 *
 * 멈춰 서는 자리가 열린 자리와 화면 밖 둘뿐이라, 조금 끌었다 놓으면 제자리로 돌아오고 끝까지 끌면
 * [onDismissRequest]가 한 번 불린다. **닫는 판단은 여기서 하지 않는다** — 끌어 내렸다는 사실만 알리고
 * 시트를 치우는 것은 상태를 든 쪽의 몫이다.
 *
 * 멈춰 설 자리가 여럿인 시트는 이것을 쓰지 않는다. 그런 시트는 자기 단계를 앵커로 갖고, 그 단계가 곧 화면
 * 상태라 여기 담을 수 없다.
 *
 * @param sheetHeight 화면 밖 자리를 정하는 값. 시트가 자기 높이만큼 내려가면 완전히 가려진다.
 */
@Composable
internal fun rememberSheetDismissDragState(
    sheetHeight: Dp,
    onDismissRequest: () -> Unit,
): AnchoredDraggableState<SheetDismissAnchor> {
    val heightPx = with(LocalDensity.current) { sheetHeight.toPx() }
    val anchors = remember(heightPx) {
        DraggableAnchors {
            SheetDismissAnchor.OPEN at 0f
            SheetDismissAnchor.GONE at heightPx
        }
    }
    val dragState = remember { AnchoredDraggableState(initialValue = SheetDismissAnchor.OPEN, anchors = anchors) }
    // 앵커가 고정 높이에서 나오므로 밀도가 바뀌어도 선 자리를 지킨 채 좌표만 옮긴다.
    SideEffect { dragState.updateAnchors(anchors, dragState.targetValue) }

    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    LaunchedEffect(dragState) {
        snapshotFlow { dragState.settledValue }
            // 처음 선 자리는 열자마자 닫는 것이 아니다.
            .drop(1)
            .collect { anchor ->
                if (anchor == SheetDismissAnchor.GONE) currentOnDismissRequest()
            }
    }

    return dragState
}

/** [rememberSheetDismissDragState]를 쓰는 시트가 멈춰 서는 자리. 사이에 머무는 단계가 없다. */
internal enum class SheetDismissAnchor {
    OPEN,
    GONE,
}
