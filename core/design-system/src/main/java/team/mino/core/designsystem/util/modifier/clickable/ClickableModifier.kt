package team.mino.core.designsystem.util.modifier.clickable

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.util.modifier.clickable.node.DebouncedClickableElement

private val DefaultRipple: IndicationNodeFactory = ripple()

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
