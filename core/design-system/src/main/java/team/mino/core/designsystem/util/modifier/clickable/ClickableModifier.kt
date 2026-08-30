package team.mino.core.designsystem.util.modifier.clickable

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import team.mino.core.designsystem.util.modifier.clickable.node.DebouncedClickableElement

// clickable·selectable 계열이 같은 리플 정책을 쓰도록 공유하는 단일 인스턴스.
internal val DefaultRipple: IndicationNodeFactory = ripple()

// 반경이 지정되면 요소 경계에 갇히지 않는 원형 리플을 쓴다. 리플이 번지는 범위만 넓히므로
// 요소의 크기·탭 영역은 그대로다.
internal fun rippleOf(radius: Dp): IndicationNodeFactory =
    if (radius.isSpecified) ripple(bounded = false, radius = radius) else DefaultRipple

fun Modifier.rippleClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    clickable(
        interactionSource = null,
        indication = DefaultRipple,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick,
    )

fun Modifier.singleClickable(
    enabled: Boolean = true,
    debounceIntervalMillis: Long = MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    debouncedClickable(
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = null,
        onClick = onClick,
    )

fun Modifier.rippleSingleClickable(
    enabled: Boolean = true,
    debounceIntervalMillis: Long = MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    debouncedClickable(
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = DefaultRipple,
        onClick = onClick,
    )

private fun Modifier.debouncedClickable(
    enabled: Boolean = true,
    debounceIntervalMillis: Long = MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS,
    onClickLabel: String? = null,
    role: Role? = null,
    indication: IndicationNodeFactory? = null,
    onClick: () -> Unit,
): Modifier =
    this then DebouncedClickableElement(
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = indication,
        onClick = onClick,
    )
