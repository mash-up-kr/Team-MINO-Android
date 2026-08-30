package team.mino.feature.home.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.home.R

/**
 * 홈 탭 최초 진입에서 한 번 뜨는 사용 가이드(spec FR-019).
 *
 * **화면 전체(상태바 포함)를 덮는 자리에 놓는다.** 딤과 하단 액션 영역이 인셋 바깥까지 닿는다.
 *
 * 노출 여부·닫은 이력의 영속 저장은 `HomeViewModel`이 판정하므로 여기서 다시 세우지 않는다.
 * 가이드가 떠 있는 동안의 조작 차단도 ViewModel이 의도 단위로 이미 버리며(spec TS-030),
 * 이 컴포저블은 딤이 뒤쪽으로 터치를 흘리지 않게만 막는다.
 *
 * 딤 위에서 원색으로 되살아나는 카드·방 뱃지·캐릭터는 [content]로 받는다. 무엇을 그릴지는 홈의 상태를
 * 아는 화면 조립부가 정하고, 이 컴포저블은 그 **자리**만 정한다.
 *
 * **딤의 backdrop blur도 여기서 걸지 않는다.** 딤 자신의 속성이지만 Compose에서 뒤 콘텐츠를
 * 흐리려면 그 콘텐츠 계층을 잡아야 해서, 하이라이트 재렌더와 같이 화면 조립부가 건다.
 *
 * @param onDismiss 하단 `시작하기`를 눌렀을 때. 호출부가 [team.mino.feature.home.main.vm.HomeIntent.DismissGuide]로 잇는다.
 * @param statusBarHeight 안내 요소(화살표·손 그림·문구)를 상태바 아래를 원점으로 놓기 위한 값. 시안은 상태바를
 *  포함한 좌표를 주지만 그대로 옮기면 상태바 높이가 시안과 다른 기기에서 가리키는 대상과 어긋난다. **호출부가 이
 *  컴포저블 바깥에서 읽어 넘긴다** — 가이드는 제 창에 떠 인셋 계층이 갈리므로 여기서 읽은 값은 화면이 놓인
 *  자리를 말해 주지 않는다. 하이라이트([content])가 쓰는 값과 같아야 서로 어긋나지 않는다.
 * @param content 딤 위에 원색으로 다시 그릴 요소(하이라이트). **딤 바로 다음**에 놓여 안내 요소(화살표·손
 *  그림·문구·액션 영역)가 전부 그 위로 깔린다. 시안의 쌓임 순서가 그 자리를 요구한다 — 손 그림이 카드 위에
 *  얹히고 화살표가 캐릭터를 가로지른다.
 */
@Composable
internal fun HomeGuideOverlay(
    onDismiss: () -> Unit,
    statusBarHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GuideScrimColor)
            .pointerInput(GUIDE_SCRIM_POINTER_KEY) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach(PointerInputChange::consume)
                    }
                }
            },
    ) {
        content()

        Image(
            painter = painterResource(R.drawable.home_guide_arrow_room_badge),
            contentDescription = null,
            modifier = Modifier.padding(start = 45.dp, top = statusBarHeight + BadgeArrowTop),
        )
        Image(
            painter = painterResource(R.drawable.home_guide_arrow_character),
            contentDescription = null,
            modifier = Modifier.padding(start = 233.86.dp, top = statusBarHeight + CharacterArrowTop),
        )
        Text(
            text = stringResource(R.string.home_guide_room_change_hint),
            // 줄바꿈이 문구 안에 박혀 있어 너비를 묶지 않는다. 묶으면 글자 폭이 조금만 넓어져도
            // 세 줄로 접힌다.
            modifier = Modifier.padding(start = 156.dp, top = statusBarHeight + RoomChangeHintTop),
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.body1NormalBold,
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusBarHeight + SwipeHandTop),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.home_guide_swipe_hand),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.home_guide_swipe_hint),
                color = MinoAndroidTheme.colors.staticBlack,
                style = MinoAndroidTheme.typography.body1NormalBold,
                textAlign = TextAlign.Center,
            )
        }

        MinoActionArea(
            mainAction = ActionAreaAction(
                text = stringResource(R.string.home_guide_dismiss),
                onClick = onDismiss,
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

private val GuideScrimColor = Color.White.copy(alpha = 0.8f)

/**
 * 안내 요소의 세로 자리. **상태바 아래를 원점으로 잡는다** — 가리키는 대상(방 뱃지·캐릭터·카드)이 전부
 * 상태바 아래에서 시작하므로, 상태바를 포함한 좌표를 그대로 쓰면 그 높이가 시안과 다른 기기에서 화살표와
 * 손 그림이 대상을 빗나간다.
 */
private val BadgeArrowTop = 70.2.dp

private val CharacterArrowTop = 119.52.dp

private val RoomChangeHintTop = 180.dp

private val SwipeHandTop = 442.dp

/** 딤이 뒤로 터치를 흘리지 않게만 하는 고정 제스처라 키가 바뀔 일이 없다. */
private const val GUIDE_SCRIM_POINTER_KEY = "home-guide-scrim"

@UiModePreviews
@Composable
private fun HomeGuideOverlayPreview() {
    MinoAndroidAppTheme {
        HomeGuideOverlay(
            onDismiss = {},
            statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        )
    }
}
