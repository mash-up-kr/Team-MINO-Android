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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * 좌측 뒤로가기·가운데 제목·우측 텍스트 액션을 갖는다. 액션 아이콘·검색 등 나머지 구성은 필요한 화면이 나올 때 축을 넓힌다.
 *
 * 제목은 뒤로가기 자리를 비켜 가지 않고 표시줄 전체 폭의 한가운데에 놓이며, 뒤로가기가 그 위에 겹친다.
 * 디자인이 그렇게 짜여 있어 제목이 길면 뒤로가기 아래로 흘러 들어간 뒤 말줄임된다.
 *
 * 상태 표시줄 인셋은 셸(`MinoScaffold`)이 처리하므로 이 컴포넌트는 콘텐츠 높이만 차지하고,
 * 배경도 깔지 않는다(디자인에서 이 변형의 배경·구분선이 꺼져 있다). 배경이 필요한 화면은
 * 셸이나 호출자가 [modifier]로 준다.
 *
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
            if (actionLabel != null) {
                ActionButton(label = actionLabel, onClick = onActionClick)
            }
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
