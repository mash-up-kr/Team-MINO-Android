package team.mino.feature.room.main.component

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.feature.room.R

/**
 * [RoomNudgeSheet] 치수 토큰. Figma `2661-157272`(방 리스트 화면 `2661-157259` 안의 넛지 카드) 대조.
 */
private object RoomNudgeSheetTokens {
    val HorizontalPadding = 20.dp
    val VerticalPadding = 32.dp
    val IllustrationTitleSpacing = 24.dp
    val TitleSubtitleSpacing = 8.dp
    val SubtitleButtonSpacing = 24.dp

    // Figma 일러스트(#5073:101188, "image 77") 실측 크기 — 이전엔 #2661:157273 기준 200×148.89dp로
    // 잘못 재고 있었다. 정사각 160×160이 맞다.
    val IllustrationSize = 160.dp
}

/**
 * 공동방 0개 사용자에게 첫 공동방 생성을 유도하는 Nudge(FR-008, [research.md D9]).
 *
 * Figma `2661-157272`는 일러스트 + 문구 + 버튼 1개(`공동방 만들기`)만 있고 별도 닫기 버튼이 없다.
 * 그래서 버튼은 `:core:design-system`의 [MinoButton]을 `SolidPrimary`·`Medium`으로 하나만 쓴다
 * (Figma `Button/Button` 인스턴스와 크기·패딩·타이포가 일치).
 *
 * 재노출 여부는 이 컴포저블이 아니라 호출부의 `showNudge`(=`groupRooms.isEmpty()` 파생값)가
 * 결정한다. 닫기 버튼이 Figma에 없으므로 별도 dismiss 콜백은 두지 않는다.
 *
 * @param onCreateClick [공동방 만들기] 클릭(FR-008) — `NavigateToRoomForm` 재사용.
 */
@Composable
internal fun RoomNudgeSheet(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal = RoomNudgeSheetTokens.HorizontalPadding,
                vertical = RoomNudgeSheetTokens.VerticalPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        // Figma 2661:157272 — Nudge 프레임 자체가 justifyContent: center. 시트 남은 높이를 채우는
        // 자리라 세로 가운데 정렬해야 그 안에서 콘텐츠가 위로 쏠리지 않는다.
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.room_nudge_illustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(RoomNudgeSheetTokens.IllustrationSize),
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = RoomNudgeSheetTokens.IllustrationTitleSpacing),
            text = "공동방을 생성해보세요!",
            color = MinoAndroidTheme.colors.primaryNormal,
            style = MinoAndroidTheme.typography.title3Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = RoomNudgeSheetTokens.TitleSubtitleSpacing),
            text = "\"저번에 말한 거기가 어디였지?\"\n더 이상 묻지 마세요.",
            color = MinoAndroidTheme.colors.labelAlternative,
            style = MinoAndroidTheme.typography.label1NormalRegular,
            textAlign = TextAlign.Center,
        )
        MinoButton(
            modifier = Modifier.padding(top = RoomNudgeSheetTokens.SubtitleButtonSpacing),
            text = "공동방 만들기",
            onClick = onCreateClick,
            size = ButtonSize.Medium,
            style = ButtonStyle.SolidPrimary,
            leadingIcon = { Icon(imageVector = MinoIcons.Plus, contentDescription = null) },
        )
    }
}

/** [RoomNudgeAutoSheet] 치수 토큰. Figma `2314-95482`("001-2-1 공동방 생성 유도") 실측. */
private object RoomNudgeAutoSheetTokens {
    val SheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val HandleSize = DpSize(38.dp, 4.dp)
    val HandleShape = RoundedCornerShape(4.dp)
    val HandleVerticalPadding = 12.dp
    val ContentPadding = 20.dp
    val ContentSpacing = 24.dp
    val TitleSubtitleSpacing = 8.dp
    val IllustrationSize = 160.dp

    // Material 표준 모션 지속시간 — 딤 페이드·시트 등장/소멸 모두 이 값 하나를 함께 쓴다.
    const val ANIMATION_DURATION_MILLIS = 300
}

/**
 * 공동방 0개 사용자에게 탭 진입마다 자동으로 표출되는 딤 배경 팝업 바텀시트(FR-008,
 * [docs/specs/room-list/spec.md] 유저 플로우4). [RoomNudgeSheet]는 시트를 `Full`까지 끌어올렸을 때만
 * 보이는 정적 인라인 카드라 이것과 별개다 — 이 컴포저블은 딤 위에 떠서 [onDismissRequest]로 닫을 수 있는
 * 독립 오버레이다(Figma `2314-95482` 대조 — 드래그 핸들 + 일러스트 + 메인/서브 2버튼).
 *
 * 재노출 여부(FR-008, EC-005)는 이 컴포저블이 아니라 호출부가 관리한다 — 탭에 진입할 때마다 dismiss
 * 상태를 초기화해 다시 그리게 하는 판단은 `RoomListViewModel`의 책임이다.
 *
 * [visible]로 표출·소멸 애니메이션을 직접 몬다 — 호출부가 `if`로 이 컴포저블 자체를 넣고 뺐다면 등장 시
 * 애니메이션 없이 즉시 나타나고(레이아웃이 그 순간 바텀 네비게이션 소멸과 겹쳐 순간 점프로 보인다),
 * 소멸 애니메이션은 재생될 기회조차 없다(실기기 확인된 결함). 딤은 [fadeIn]/[fadeOut]으로, 시트 본체는
 * 바텀시트답게 아래에서 올라오고 아래로 내려가야 하므로 [slideInVertically]/[slideOutVertically]로 각각
 * 움직인다.
 *
 * 딤은 상태바 영역까지 덮어야 한다 — 이 컴포저블은 `MinoScaffold`가 상태바 높이만큼 소비한 뒤의
 * 영역만 받으므로, [fillMaxSize]만으로는 상태바 자리가 배경색 그대로 비쳐 절반만 딤 처리된 것처럼
 * 보인다(실기기 확인된 결함). 시트 쪽은 반대로 내비게이션 바까지 **흰 배경**으로 이어져야 한다 —
 * 시스템 3버튼 내비게이션 자리에 딤 회색이 비치면 시트가 거기서 끊긴 것처럼 보인다(실기기 확인된
 * 결함). 그래서 이 컴포저블 전체(딤 + 시트)를 위아래로 [WindowInsets.statusBars]·
 * [WindowInsets.navigationBars]만큼 부풀려([Modifier.layout]) 실제 화면 가장자리까지 그리고,
 * 딤은 그 늘어난 영역 전체를 채우되 시트 카드는 원래 자리(내비게이션 바 위, 기존과 동일한 안전
 * 위치)에 그대로 두고 카드 바로 아래에 내비게이션 바 높이만큼 같은 흰색 스트립([RoomNudgeAutoSheetTokens]
 * 참고)을 이어 붙인다 — 시트 자체를 늘리면(`Box(contentAlignment = BottomCenter)` 안에서 높이를
 * 늘리는 방식은 정렬 하단이 항상 원래 경계에 고정돼 아래로는 못 번진다) 버튼 위치까지 내비게이션 바
 * 쪽으로 밀려 터치 영역이 위험해지므로 배경만 따로 늘린다.
 *
 * 시스템 상태바 아이콘 색도 딤에 맞춰 바꾼다 — 딤 배경은 라이트/다크 테마와 무관하게 항상 어두워서,
 * 테마가 정한 기본 아이콘 색(라이트 테마의 어두운 아이콘 등)을 그대로 두면 대비가 무너진다(실기기
 * 확인된 결함). [visible]인 동안은 상태바만 밝은 아이콘으로 강제하고, 사라지면 시스템 다크 테마
 * 여부가 정한 기본값으로 되돌린다. 내비게이션 바는 항상 흰 배경 위에 있으므로 아이콘 색을 건드리지
 * 않는다.
 *
 * @param onCreateClick [공동방 만들기] 클릭 — `NavigateToRoomForm` 재사용.
 * @param onDismissRequest [나중에 만들래요] 클릭 또는 딤 영역 탭.
 */
@Composable
internal fun RoomNudgeAutoSheet(
    visible: Boolean,
    onCreateClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusBarBleed = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarBleed = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // px 환산은 리컴포지션마다 다시 하지 않는다 — 이 컴포저블은 이제 `if`로 넣고 빼지 않고 항상
    // 컴포지션에 남아 있어(visible로 표출만 제어) 아래 layout 패스가 표출 여부와 무관하게 매번 돈다.
    val density = LocalDensity.current
    val topBleedPx = remember(statusBarBleed, density) { with(density) { statusBarBleed.roundToPx() } }
    val bottomBleedPx = remember(navigationBarBleed, density) { with(density) { navigationBarBleed.roundToPx() } }

    val activity = LocalActivity.current
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()
    DisposableEffect(visible, isDarkTheme, activity) {
        val window = activity?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        if (visible) {
            // 밝은(흰) 상태바 아이콘으로 바꾸면 시스템이 대비 확보용 스크림을 자동으로 얹어, 이미
            // 있는 우리 딤 위에 한 번 더 덮여 상태바 자리가 시커멓게 보인다(실기기 확인된 결함) —
            // 우리 딤 자체가 대비를 이미 확보하므로 시스템 스크림은 끈다.
            window?.isStatusBarContrastEnforced = false
            controller?.isAppearanceLightStatusBars = false
        }
        // visible이 true→false로 바뀌면 이 onDispose가 먼저 실행돼 원래 값으로 되돌리므로, else
        // 분기로 같은 두 줄을 한 번 더 반복할 필요가 없다.
        onDispose {
            controller?.isAppearanceLightStatusBars = !isDarkTheme
            window?.isStatusBarContrastEnforced = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // 이 컴포저블 전체를 위아래로 부풀려 실제 화면 가장자리(상태바 위·내비게이션 바 아래)까지
            // 그린다 — 안에서 그리는 딤·시트는 이제 늘어난 영역을 기준으로 배치된다.
            .layout { measurable, constraints ->
                val targetHeight = if (constraints.hasBoundedHeight) {
                    constraints.maxHeight + topBleedPx + bottomBleedPx
                } else {
                    constraints.maxHeight
                }
                val placeable = measurable.measure(
                    constraints.copy(minHeight = targetHeight, maxHeight = targetHeight),
                )
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, -topBleedPx)
                }
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(RoomNudgeAutoSheetTokens.ANIMATION_DURATION_MILLIS)),
            exit = fadeOut(tween(RoomNudgeAutoSheetTokens.ANIMATION_DURATION_MILLIS)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MinoAndroidTheme.colors.materialDimmer)
                    .singleClickable(onClick = onDismissRequest),
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(RoomNudgeAutoSheetTokens.ANIMATION_DURATION_MILLIS)) { it } +
                fadeIn(tween(RoomNudgeAutoSheetTokens.ANIMATION_DURATION_MILLIS)),
            exit = slideOutVertically(tween(RoomNudgeAutoSheetTokens.ANIMATION_DURATION_MILLIS)) { it } +
                fadeOut(tween(RoomNudgeAutoSheetTokens.ANIMATION_DURATION_MILLIS)),
        ) {
            // 부풀린 컨테이너 안에서 이 Column은 그대로 BottomCenter 정렬된다 — 카드(원래 안전
            // 위치)와 그 아래 내비게이션 바 높이만큼의 흰 연장 스트립을 합친 자연 높이가 정확히
            // 부풀린 하단(=실제 화면 하단)에 맞물려, 카드 자체는 원래 자리에서 한 치도 움직이지 않는다.
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .surface(
                            shape = RoomNudgeAutoSheetTokens.SheetShape,
                            containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
                        )
                        // 시트가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가 닫히지 않는다.
                        .pointerInput(Unit) {},
                ) {
                    RoomNudgeAutoSheetDragHandle()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(RoomNudgeAutoSheetTokens.ContentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.room_nudge_illustration),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(RoomNudgeAutoSheetTokens.IllustrationSize),
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = RoomNudgeAutoSheetTokens.ContentSpacing),
                            text = "공동방을 생성해보세요!",
                            color = MinoAndroidTheme.colors.primaryNormal,
                            style = MinoAndroidTheme.typography.title3Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = RoomNudgeAutoSheetTokens.TitleSubtitleSpacing),
                            text = "\"저번에 말한 거기가 어디였지?\"\n더 이상 묻지 마세요.",
                            color = MinoAndroidTheme.colors.labelAlternative,
                            style = MinoAndroidTheme.typography.label1NormalRegular,
                            textAlign = TextAlign.Center,
                        )
                    }
                    // navigationBarsPadding()을 여기서 또 얹지 않는다 — 부풀린 컨테이너 덕에 이
                    // Column은 이미 원래(늘리기 전) 안전 위치에 그대로 있고, 내비게이션 바 자리는
                    // 아래 흰 연장 스트립이 따로 채운다. 여기서 또 적용하면 인셋이 두 번 들어가
                    // 하단 간격이 Figma보다 훨씬 크게 보인다(실기기 확인된 결함).
                    MinoActionArea(
                        mainAction = ActionAreaAction(text = "공동방 만들기", onClick = onCreateClick),
                        subAction = ActionAreaAction(text = "나중에 만들래요", onClick = onDismissRequest),
                    )
                }
                // 카드의 흰 배경을 내비게이션 바 높이만큼 그대로 이어 붙인다 — 모서리를 둥글리지
                // 않는 평평한 스트립이라 카드와 이음매 없이 붙는다.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(navigationBarBleed)
                        .background(MinoAndroidTheme.colors.backgroundElevatedNormal),
                )
            }
        }
    }
}

@Composable
private fun RoomNudgeAutoSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = RoomNudgeAutoSheetTokens.HandleVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RoomNudgeAutoSheetTokens.HandleSize.width, RoomNudgeAutoSheetTokens.HandleSize.height)
                .background(
                    color = MinoAndroidTheme.colors.fillNormal,
                    shape = RoomNudgeAutoSheetTokens.HandleShape,
                ),
        )
    }
}
