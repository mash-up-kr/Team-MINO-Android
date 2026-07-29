package team.mino.core.designsystem.component.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.snackbar.token.SnackbarTokens
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CircleCheckFill
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 간단한 메시지와 상호작용(액션)을 일시적으로 안내하는 Snackbar.
 *
 * Figma(MU_Wanted / Montage)의 `Snackbar/Snackbar` 스펙을 따른다. 배경·콘텐츠 모두 Inverse 계열
 * 토큰을 써서 라이트/다크에 따라 함께 반전된다(라이트=어두운 pill+밝은 글자, 다크=밝은 pill+어두운 글자).
 * 메시지 **맨 앞**에는 [leadingIcon], **맨 뒤**에는 닫기(x) 버튼을 선택적으로 둘 수 있다.
 *
 * @param message 필수 메시지(제목). Body2 Bold.
 * @param description 선택적 보조 설명. 있으면 메시지 아래 한 줄로 표시(말줄임).
 * @param leadingIcon 메시지 맨 앞에 붙는 아이콘(Painter). null이면 표시하지 않는다.
 * @param actionLabel 오른쪽 액션 버튼 라벨. null이면 버튼을 숨긴다.
 * @param onActionClick 액션 버튼 클릭 콜백.
 * @param onCloseClick 맨 뒤 닫기(x) 버튼 콜백. null이면 닫기 버튼을 숨긴다.
 *
 * TODO(#77): Figma는 배경에 backdrop-blur(radius 64)를 지정하나, 임의 배경을 블러하는
 *  유틸이 아직 없어 반투명 레이어(Inverse 52% + Black 5%)로 근사했다.
 */
@Composable
fun MinoSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: Painter? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .widthIn(max = MinoSnackbarDefaults.maxWidth)
            .clip(MinoSnackbarDefaults.shape)
            .background(MinoSnackbarDefaults.containerColor)
            .background(MinoSnackbarDefaults.overlayColor)
            .padding(
                horizontal = SnackbarTokens.HorizontalPadding,
                vertical = SnackbarTokens.VerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(SnackbarTokens.ContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val contentColor = MinoSnackbarDefaults.contentColor

        if (leadingIcon != null) {
            Icon(
                painter = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(SnackbarTokens.LeadingIconSize),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = SnackbarTokens.MessagePadding, vertical = 5.dp),
        ) {
            Text(
                text = message,
                style = SnackbarTokens.MessageFont.value,
                color = contentColor,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = SnackbarTokens.DescriptionFont.value,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (actionLabel != null) {
            Text(
                text = actionLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .rippleSingleClickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = SnackbarTokens.ActionFont.value,
                color = MinoSnackbarDefaults.actionColor,
            )
        }

        if (onCloseClick != null) {
            Icon(
                imageVector = MinoIcons.Close,
                contentDescription = "닫기",
                tint = MinoSnackbarDefaults.closeColor,
                modifier = Modifier
                    .clip(CircleShape)
                    .rippleSingleClickable(onClick = onCloseClick)
                    .padding(2.dp)
                    .size(SnackbarTokens.CloseIconSize),
            )
        }
    }
}

@UiModePreviews
@Composable
private fun MinoSnackbarPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
        ) {
            MinoSnackbar(
                message = "메시지에 마침표를 찍어요.",
                actionLabel = "텍스트",
            )
        }
    }
}

@UiModePreviews
@Composable
private fun MinoSnackbarWithDescriptionPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
        ) {
            MinoSnackbar(
                message = "메시지에 마침표를 찍어요.",
                description = "설명은 필요할 때만 써요.",
                actionLabel = "텍스트",
            )
        }
    }
}

@UiModePreviews
@Composable
private fun MinoSnackbarWithIconsPreview() {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
        ) {
            MinoSnackbar(
                message = "메시지에 마침표를 찍어요.",
                leadingIcon = rememberVectorPainter(MinoIcons.CircleCheckFill),
                actionLabel = "텍스트",
                onCloseClick = {},
            )
        }
    }
}
