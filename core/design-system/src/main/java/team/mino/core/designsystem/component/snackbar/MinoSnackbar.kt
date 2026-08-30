package team.mino.core.designsystem.component.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import team.mino.core.designsystem.component.snackbar.token.SnackbarTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * 간단한 메시지와 상호작용(액션)을 일시적으로 안내하는 Snackbar.
 *
 * Figma(MU_Wanted / Montage)의 `Snackbar/Snackbar` 스펙을 따른다. 배경·콘텐츠 모두 Inverse 계열
 * 토큰을 써서 라이트/다크에 따라 함께 반전된다(라이트=어두운 pill+밝은 글자, 다크=밝은 pill+어두운 글자).
 * 메시지 **맨 앞**에는 [leadingIcon], **맨 뒤**에는 닫기(x) 버튼을 선택적으로 둘 수 있다.
 *
 * 글줄은 [message]·[description] 두 개이고 각각 독립적으로 켜고 끈다(Figma `heading`·`description`
 * 속성). **[message]와 [description] 중 적어도 하나는 non-null이어야 한다** — 둘 다 null이면
 * 글자 없는 빈 pill이 그려진다.
 *
 * @param message 제목 글줄(Figma `heading`). Body2 Bold. `null`이면 제목 없이 [description] 한 줄만
 *   그린다(Figma `heading=false`) — 제목으로 쓰기엔 긴 메시지에 쓰는 형태다.
 * @param description 보조 설명 글줄(Figma `description`). Label2 Regular, 한 줄 말줄임.
 * @param leadingIcon 메시지 맨 앞에 붙는 아이콘(Painter). null이면 표시하지 않는다.
 * @param actionLabel 오른쪽 액션 버튼 라벨. null이면 버튼을 숨긴다.
 * @param onActionClick 액션 버튼 클릭 콜백.
 * @param onCloseClick 맨 뒤 닫기(x) 버튼 콜백. null이면 닫기 버튼을 숨긴다.
 *
 * 액션은 Figma가 `Button/Text` 인스턴스를 쓰지만 글자색(Static/White)과 글자 크기(Body2 15sp)를
 * 모두 인스턴스에서 덮어써, [team.mino.core.designsystem.component.button.MinoTextButton]의
 * 어느 조합과도 맞지 않는다. 그래서 재사용하지 않고 여기서 직접 그린다.
 *
 * TODO(#77): Figma는 배경에 backdrop-blur(radius 64)를 지정하나, 임의 배경을 블러하는
 *  유틸이 아직 없어 반투명 레이어(Inverse 52% + Black 5%)로 근사했다.
 */
@Composable
fun MinoSnackbar(
    message: String?,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: Painter? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null,
) {
    val contentColor = MinoSnackbarDefaults.contentColor

    Row(
        modifier = modifier
            .widthIn(max = MinoSnackbarDefaults.maxWidth)
            .clip(MinoSnackbarDefaults.shape)
            .background(MinoSnackbarDefaults.containerColor)
            .background(MinoSnackbarDefaults.overlayColor)
            .padding(
                horizontal = SnackbarTokens.HorizontalPadding,
                vertical = SnackbarTokens.VerticalPadding,
            ).heightIn(min = SnackbarTokens.MinContentHeight),
        horizontalArrangement = Arrangement.spacedBy(SnackbarTokens.ContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 아이콘과 메시지는 나머지 슬롯보다 좁은 간격으로 묶인다(Figma `Content` 프레임).
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(SnackbarTokens.LeadingIconSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                    .padding(
                        horizontal = SnackbarTokens.MessageHorizontalPadding,
                        vertical = SnackbarTokens.MessageVerticalPadding,
                    ),
            ) {
                if (message != null) {
                    Text(
                        text = message,
                        style = SnackbarTokens.MessageFont.value,
                        color = contentColor,
                    )
                }
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
        }

        if (actionLabel != null) {
            Text(
                text = actionLabel,
                modifier = Modifier
                    .clip(MinoSnackbarDefaults.actionShape)
                    .rippleSingleClickable(onClick = onActionClick)
                    .padding(
                        horizontal = SnackbarTokens.ActionHorizontalPadding,
                        vertical = SnackbarTokens.ActionVerticalPadding,
                    ),
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
                    .padding(SnackbarTokens.CloseButtonPadding)
                    .size(SnackbarTokens.CloseIconSize),
            )
        }
    }
}
