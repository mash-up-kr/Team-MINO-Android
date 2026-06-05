package team.mino.core.designsystem.util.modifier.clickable

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.util.modifier.clickable.node.DebouncedClickableElement

private val DefaultRipple: IndicationNodeFactory = ripple()

// 클릭은 기본 Modifier.clickable 대신 아래 두 확장을 사용한다. 기본 clickable은 연타 방지가 없어
// 중복 내비게이션/이벤트가 발생할 수 있다. 추후 커스텀 Lint 룰로 기본 clickable 사용 시 경고를
// 띄워 이 확장으로 유도할 예정이다. (린트 도입은 별도 논의)
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
