package team.mino.core.designsystem.component.topnavigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import team.mino.core.designsystem.component.topnavigation.token.TopNavigationTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ChevronLeft
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 화면 상단의 제목 표시줄(Figma `Top Navigation/Top Navigation`의 `Platform=iOS`).
 *
 * `Platform=Android`가 아니라 iOS 변형을 구현한다 — 화면 목업이 iOS 인스턴스를 쓰고 있어
 * 사용자가 그쪽에 맞추기로 결정했다.
 *
 * 좌측 뒤로가기·가운데 제목·우측 텍스트 액션을 갖는다. 검색 등 나머지 구성은 필요한 화면이 나올 때 축을 넓힌다.
 *
 * 우측을 아이콘으로 그리려면 [MinoTopNavigation] 아이콘 오버로드를 쓴다. 텍스트 액션과 아이콘 액션은
 * 오버로드로 갈라 두어 **한 호출이 둘을 함께 넘길 수 없다.** 다만 둘이 함께 놓인 구성이 디자인에
 * 없다고 확인한 것은 아니다 — 우측 슬롯의 구성을 정하는 하위 컴포넌트의 정의가 다른 라이브러리 파일에
 * 있어 열리지 않았고, 열리는 범위에서 그 슬롯은 항목을 여럿 담을 수 있는 행으로 되어 있다.
 * 둘을 함께 요구하는 화면이 나오면 이 갈래를 다시 판정한다.
 *
 * 제목은 뒤로가기 자리를 비켜 가지 않고 표시줄 전체 폭의 한가운데에 놓이며, 뒤로가기가 그 위에 겹친다.
 * 디자인이 그렇게 짜여 있어 제목이 길면 뒤로가기 아래로 흘러 들어간 뒤 말줄임된다.
 *
 * 상태 표시줄 인셋은 셸(`MinoScaffold`)이 처리하므로 이 컴포넌트는 콘텐츠 높이만 차지하고, 배경도 깔지 않는다.
 * 배경과 구분선은 컴포넌트셋의 `Scrolled` 축이 켜는 것이고 이 컴포넌트는 그 축이 꺼진 상태만 그린다 —
 * 만들지 않은 축과 그 이유는 프리뷰 파일(`TopNavigationPreview.kt`)의 KDoc에 모아 두었다.
 * 배경이 필요한 화면은 셸이나 호출자가 [modifier]로 준다.
 *
 * @param title 표시줄 한가운데에 그릴 제목. 빈 문자열이면 글자만 비고, 제목 자리와 나머지 배치는 그대로다.
 * @param onBackClick `null`이면 뒤로가기를 그리지 않는다. 자리는 그대로 비워 둔다.
 * @param actionLabel `null`이면 우측 텍스트 액션을 그리지 않는다. 자리는 그대로 비워 둔다.
 * @param onActionClick 우측 텍스트 액션을 눌렀을 때. [actionLabel]이 `null`이면 쓰이지 않는다.
 */
@Composable
fun MinoTopNavigation(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    TopNavigationLayout(
        title = title,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        if (actionLabel != null) {
            ActionButton(label = actionLabel, onClick = onActionClick)
        }
    }
}

/**
 * 우측이 아이콘 액션인 [MinoTopNavigation]. 나머지 구성은 텍스트 액션 오버로드와 같다.
 *
 * @param title 빈 문자열이면 글자만 빈다. 제목 없이 아이콘 액션만 두는 화면이 그렇게 쓴다.
 * @param actionIcon 우측에 그릴 아이콘. `MinoIcons`가 주는 것만 쓴다.
 * @param actionIconContentDescription 아이콘의 접근성 설명. 아이콘이 유일한 조작 수단이라 화면이 직접 준다.
 * @param onBackClick `null`이면 뒤로가기를 그리지 않는다. 자리는 그대로 비워 둔다.
 */
@Composable
fun MinoTopNavigation(
    title: String,
    actionIcon: ImageVector,
    actionIconContentDescription: String?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    TopNavigationLayout(
        title = title,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        ActionIconButton(
            icon = actionIcon,
            contentDescription = actionIconContentDescription,
            onClick = onActionClick,
        )
    }
}

/**
 * 두 오버로드가 공유하는 표시줄 골격. 우측 자리에 무엇이 오는지만 오버로드가 정한다.
 *
 * [content]는 공개 슬롯이 아니다 — 우측에 올 수 있는 구성은 이 파일 안의 두 액션뿐이고,
 * 호출부는 오버로드로만 그중 하나를 고른다.
 */
@Composable
private fun TopNavigationLayout(
    title: String,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TopNavigationTokens.BarHeight)
            .padding(horizontal = TopNavigationTokens.BarHorizontalPadding),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = TopNavigationTokens.TitleHorizontalPadding),
            text = title,
            color = MinoTopNavigationDefaults.titleColor,
            style = TopNavigationTokens.TitleFont.value,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(TopNavigationTokens.LeadingSlotSize),
            contentAlignment = Alignment.Center,
        ) {
            if (onBackClick != null) {
                BackButton(onClick = onBackClick)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(TopNavigationTokens.TrailingSlotHeight),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

/**
 * 뒤로가기 버튼. 터치·리플 영역이 아이콘 자리보다 커서 사방으로 넘치는데,
 * 감싸는 자리 프레임이 넘치는 부분을 잘라내지 않아 디자인의 원형 인터랙션 영역이 그대로 남는다.
 */
@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier
            .requiredSize(TopNavigationTokens.BackInteractionSize)
            .clip(CircleShape)
            .rippleSingleClickable(
                role = Role.Button,
                onClick = onClick,
            ).padding(TopNavigationTokens.BackIconPadding),
        imageVector = MinoIcons.ChevronLeft,
        contentDescription = "뒤로 가기",
        tint = MinoTopNavigationDefaults.backIconColor,
    )
}

/**
 * 우측 텍스트 액션. 뒤로가기와 마찬가지로 터치·리플 영역이 글자 자리보다 커서 넘치는데,
 * 좌우로 넘친 만큼을 되돌려 놓아 글자는 자리 오른쪽 끝에 붙은 채 인터랙션 영역만 표시줄 여백 쪽으로 번진다.
 */
@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .offset(x = TopNavigationTokens.ActionInteractionOverhang)
            .requiredHeight(TopNavigationTokens.ActionInteractionHeight)
            .clip(TopNavigationTokens.ActionInteractionShape)
            .rippleSingleClickable(
                role = Role.Button,
                onClick = onClick,
            ).padding(horizontal = TopNavigationTokens.ActionInteractionOverhang),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MinoTopNavigationDefaults.actionLabelColor,
            style = TopNavigationTokens.ActionLabelFont.value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 우측 아이콘 액션. 뒤로가기와 같은 아이콘 버튼 기하를 쓰지만 자리가 달라 자기 토큰을 갖는다.
 *
 * 아이콘 자리를 [TopNavigationTokens.ActionIconSize]로 고정해 아이콘이 표시줄 오른쪽 끝에 붙게 하고,
 * 그보다 큰 터치·리플 영역은 그 자리를 사방으로 넘치게 둔다 — 넘치는 부분을 잘라내는 프레임이 없다.
 *
 * 디자인이 눌림 오버레이 색을 아이콘 색과 별개로 정해 두어, 리플이 주변 콘텐츠 색을 물려받게 두지 않고
 * [MinoTopNavigationDefaults.actionIconOverlayColor]를 `LocalContentColor`로 깔아 그 색으로 그린다 —
 * 리플의 색 파라미터를 여는 클릭 유틸이 없어 M3가 쓰는 이 경로로 색을 넘긴다.
 */
@Composable
private fun ActionIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(TopNavigationTokens.ActionIconSize),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MinoTopNavigationDefaults.actionIconOverlayColor) {
            Icon(
                modifier = Modifier
                    .requiredSize(TopNavigationTokens.ActionIconInteractionSize)
                    .clip(CircleShape)
                    .rippleSingleClickable(
                        role = Role.Button,
                        onClick = onClick,
                    ).padding(TopNavigationTokens.ActionIconPadding),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MinoTopNavigationDefaults.actionIconColor,
            )
        }
    }
}
