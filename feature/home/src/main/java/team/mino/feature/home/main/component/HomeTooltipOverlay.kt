package team.mino.feature.home.main.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.tooltip.MinoTooltip
import team.mino.core.designsystem.component.tooltip.TooltipAlign
import team.mino.core.designsystem.component.tooltip.TooltipPosition
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.DeckSort
import team.mino.feature.home.R
import team.mino.feature.home.main.model.HomeTooltip

/**
 * 방 캐릭터 옆에 잠깐 떴다 사라지는 안내 2종(spec FR-015·FR-016).
 *
 * 노출 시간(3초)은 `HomeViewModel`이 소유하고 여기서는 [tooltip]이 `null`이 되는 것으로만 안다 —
 * 이 컴포저블은 타이머를 돌리지 않고 들어온 상태를 그리며 페이드만 얹는다.
 *
 * 어디에 놓을지는 호출부가 [modifier]로 정한다. **클릭·제스처 모디파이어를 붙이지 않아 아래 카드의
 * 조작을 가로채지 않는다**(spec UX-003).
 *
 * @param tooltip 지금 띄울 안내. `null`이면 페이드아웃한다.
 */
@Composable
internal fun HomeTooltipOverlay(
    tooltip: HomeTooltip?,
    modifier: Modifier = Modifier,
) {
    // 페이드아웃이 도는 동안에도 그릴 문구가 필요해 마지막으로 띄운 값을 남긴다.
    var lastShown by remember { mutableStateOf<HomeTooltip?>(null) }
    LaunchedEffect(tooltip) { lastShown = tooltip ?: lastShown }

    AnimatedVisibility(
        visible = tooltip != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        lastShown?.let {
            MinoTooltip(
                text = it.message(),
                position = TooltipPosition.Bottom,
                align = TooltipAlign.End,
            )
        }
    }
}

/** 갈래에 담긴 값으로 문구를 조립한다. ViewModel은 이름·정렬만 담고 문구를 만들지 않는다. */
@Composable
private fun HomeTooltip.message(): String =
    when (this) {
        is HomeTooltip.RoomChanged -> stringResource(R.string.home_tooltip_room_changed, roomName)
        is HomeTooltip.DeckAhead.NextRoom -> stringResource(R.string.home_tooltip_next_room, roomName)
        is HomeTooltip.DeckAhead.NextSort -> stringResource(sort.tooltipMessageRes)
    }

private val DeckSort.tooltipMessageRes: Int
    get() =
        when (this) {
            DeckSort.GGUK_PICK -> R.string.home_tooltip_next_sort_gguk_pick
            DeckSort.LATEST -> R.string.home_tooltip_next_sort_latest
            DeckSort.NEAREST -> R.string.home_tooltip_next_sort_nearest
        }

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeTooltipOverlayPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HomeTooltipOverlay(tooltip = HomeTooltip.RoomChanged(roomName = "민호야 잘하자"))
            HomeTooltipOverlay(tooltip = HomeTooltip.DeckAhead.NextRoom(roomName = "성수 맛집"))
            HomeTooltipOverlay(tooltip = HomeTooltip.DeckAhead.NextSort(sort = DeckSort.LATEST))
        }
    }
}
