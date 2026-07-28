package team.mino.core.designsystem.util.modifier.selectable

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.util.modifier.clickable.DefaultRipple
import team.mino.core.designsystem.util.modifier.clickable.MultipleEventsCutter
import team.mino.core.designsystem.util.modifier.selectable.node.DebouncedSelectableElement

fun Modifier.rippleSelectable(
    selected: Boolean,
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    selectable(
        selected = selected,
        interactionSource = null,
        indication = DefaultRipple,
        enabled = enabled,
        role = role,
        onClick = onClick,
    )

fun Modifier.singleSelectable(
    selected: Boolean,
    enabled: Boolean = true,
    debounceIntervalMillis: Long = MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    debouncedSelectable(
        selected = selected,
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = null,
        onClick = onClick,
    )

fun Modifier.rippleSingleSelectable(
    selected: Boolean,
    enabled: Boolean = true,
    debounceIntervalMillis: Long = MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    debouncedSelectable(
        selected = selected,
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = DefaultRipple,
        onClick = onClick,
    )

private fun Modifier.debouncedSelectable(
    selected: Boolean,
    enabled: Boolean = true,
    debounceIntervalMillis: Long = MultipleEventsCutter.DEFAULT_INTERVAL_MILLIS,
    onClickLabel: String? = null,
    role: Role? = null,
    indication: IndicationNodeFactory? = null,
    onClick: () -> Unit,
): Modifier =
    this then DebouncedSelectableElement(
        selected = selected,
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = indication,
        onClick = onClick,
    )
