package team.mino.feature.room.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
 * @param onCreateClick [공동방 만들기] 클릭 — `NavigateToRoomForm` 재사용.
 * @param onDismissRequest [나중에 만들래요] 클릭 또는 딤 영역 탭.
 */
@Composable
internal fun RoomNudgeAutoSheet(
    onCreateClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.materialDimmer)
                .singleClickable(onClick = onDismissRequest),
        )
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
            // navigationBarsPadding()을 여기서 또 얹지 않는다 — 이 시트는 `MainShell`의 `MinoScaffold`
            // 아래(RoomListScreen)에 있고, 바텀 네비게이션이 숨겨진 동안(RoomListRoute의
            // `isNudgeSheetVisible` 배선) Scaffold의 `innerPadding`이 이미 네비게이션 바 인셋을
            // 하단 여백으로 흘려보낸다 — 여기서 다시 적용하면 인셋이 두 번 들어가 하단 간격이
            // Figma보다 훨씬 크게 보인다(실기기 확인된 결함).
            MinoActionArea(
                mainAction = ActionAreaAction(text = "공동방 만들기", onClick = onCreateClick),
                subAction = ActionAreaAction(text = "나중에 만들래요", onClick = onDismissRequest),
            )
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
