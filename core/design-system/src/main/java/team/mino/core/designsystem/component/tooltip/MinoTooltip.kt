package team.mino.core.designsystem.component.tooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import team.mino.core.designsystem.component.tooltip.token.arrowInset
import team.mino.core.designsystem.component.tooltip.token.contentPadding
import team.mino.core.designsystem.component.tooltip.token.contentSpacing
import team.mino.core.designsystem.component.tooltip.token.font
import team.mino.core.designsystem.component.tooltip.token.minWidth
import team.mino.core.designsystem.component.tooltip.token.shape
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * 특정 UI 요소에 대한 짧은 안내를 덧붙이는 말풍선(Figma `Tooltip/Tooltip`).
 *
 * [MinoMenu][team.mino.core.designsystem.component.menu.MinoMenu]와 마찬가지로 **말풍선을 그리기만
 * 하는 컴포넌트**다. 어떤 요소에 붙일지, 언제 띄우고 닫을지, 화면 밖으로 나가면 어느 쪽으로 뒤집을지는
 * 호출부가 `Popup`·`Box` 등으로 정한다. Figma 컴포넌트셋도 배치 규칙 없이 모양만 정의한다.
 *
 * [position]은 **앵커를 기준으로 말풍선이 놓이는 방향**이다. 화살표는 그 반대편,
 * 즉 앵커를 향하는 변에 붙는다(`Bottom`이면 앵커 아래에 놓이고 화살표는 위를 향한다).
 *
 * @param text 말풍선 본문. 최대 너비([MinoTooltipDefaults.maxWidth])를 넘으면 줄바꿈된다.
 * @param size 크기. `Small`은 데스크톱 보조 정보용이라 모바일에서는 `Medium`을 쓴다.
 * @param position 앵커 기준 말풍선 방향.
 * @param align 화살표가 붙는 변에서의 화살표 위치. [position]이 `Top`·`Bottom`이면 좌/중앙/우,
 *   `Left`·`Right`면 상/중앙/하로 해석된다.
 * @param shortcut 라벨 뒤에 흐리게 붙는 단축키 표기. `null`이면 표시하지 않는다.
 *
 * TODO(#77): Figma는 배경에 backdrop-blur(radius 64)를 지정하나, 임의 배경을 블러하는
 *  유틸이 아직 없어 반투명 레이어(Inverse 88% + Black 5%)로 근사했다.
 */
@Composable
fun MinoTooltip(
    text: String,
    modifier: Modifier = Modifier,
    size: TooltipSize = TooltipSize.Medium,
    position: TooltipPosition = TooltipPosition.Bottom,
    align: TooltipAlign = TooltipAlign.Start,
    shortcut: String? = null,
) {
    if (position.isVertical) {
        Column(modifier = modifier) {
            val arrowModifier = Modifier
                .align(align.horizontal)
                .padding(horizontal = size.arrowInset)

            if (position == TooltipPosition.Bottom) {
                TooltipArrow(size = size, position = position, modifier = arrowModifier)
            }
            TooltipBubble(text = text, size = size, shortcut = shortcut)
            if (position == TooltipPosition.Top) {
                TooltipArrow(size = size, position = position, modifier = arrowModifier)
            }
        }
    } else {
        Row(modifier = modifier) {
            val arrowModifier = Modifier
                .align(align.vertical)
                .padding(vertical = size.arrowInset)

            if (position == TooltipPosition.Right) {
                TooltipArrow(size = size, position = position, modifier = arrowModifier)
            }
            TooltipBubble(text = text, size = size, shortcut = shortcut)
            if (position == TooltipPosition.Left) {
                TooltipArrow(size = size, position = position, modifier = arrowModifier)
            }
        }
    }
}

@Composable
private fun TooltipBubble(
    text: String,
    size: TooltipSize,
    shortcut: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(min = size.minWidth, max = MinoTooltipDefaults.maxWidth)
            .clip(size.shape())
            .background(MinoTooltipDefaults.containerColor)
            .background(MinoTooltipDefaults.overlayColor)
            .padding(size.contentPadding()),
        horizontalArrangement = Arrangement.spacedBy(size.contentSpacing),
    ) {
        Text(
            // 단축키를 먼저 재고 남은 폭에서 줄바꿈시킨다. fill = false라 짧은 본문은 그대로 좁게 둔다.
            modifier = Modifier.weight(1f, fill = false),
            text = text,
            color = MinoTooltipDefaults.labelColor,
            style = size.font.value,
        )
        if (shortcut != null) {
            Text(
                text = shortcut,
                color = MinoTooltipDefaults.shortcutColor,
                style = size.font.value,
                softWrap = false,
            )
        }
    }
}

/** [MinoTooltip]의 크기. Figma `Size` 속성(Medium·Small)에 대응. */
enum class TooltipSize {
    Medium,
    Small,
}

/** 앵커를 기준으로 [MinoTooltip]이 놓이는 방향. Figma `Position` 속성에 대응. */
enum class TooltipPosition {
    Top,
    Bottom,
    Left,
    Right,
}

/**
 * 화살표가 붙는 변에서의 화살표 위치. Figma `align` 속성에 대응하며,
 * 세로 배치(`Top`·`Bottom`)는 `leading`·`center`·`trailing`, 가로 배치(`Left`·`Right`)는
 * `top`·`center`·`bottom`으로 이름만 다르고 축만 바뀐다.
 */
enum class TooltipAlign {
    Start,
    Center,
    End,
}

private val TooltipAlign.horizontal: Alignment.Horizontal
    get() =
        when (this) {
            TooltipAlign.Start -> Alignment.Start
            TooltipAlign.Center -> Alignment.CenterHorizontally
            TooltipAlign.End -> Alignment.End
        }

private val TooltipAlign.vertical: Alignment.Vertical
    get() =
        when (this) {
            TooltipAlign.Start -> Alignment.Top
            TooltipAlign.Center -> Alignment.CenterVertically
            TooltipAlign.End -> Alignment.Bottom
        }
