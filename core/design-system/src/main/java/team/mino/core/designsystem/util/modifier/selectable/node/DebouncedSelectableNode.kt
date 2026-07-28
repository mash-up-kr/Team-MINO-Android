package team.mino.core.designsystem.util.modifier.selectable.node

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.ui.node.invalidateSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.selected
import team.mino.core.designsystem.util.modifier.clickable.node.DebouncedClickableNode

// DebouncedClickableNode 위에 selected 시맨틱만 얹는 노드
// (foundation의 SelectableNode : ClickableNode와 같은 구조).
// 제스처·debounce·인디케이션 처리는 전부 부모가 담당하고, 여기서는 복제하지 않는다.
internal class DebouncedSelectableNode(
    private var selected: Boolean,
    enabled: Boolean,
    debounceIntervalMillis: Long,
    onClickLabel: String?,
    role: Role?,
    indication: IndicationNodeFactory?,
    onClick: () -> Unit,
) : DebouncedClickableNode(
        enabled = enabled,
        debounceIntervalMillis = debounceIntervalMillis,
        onClickLabel = onClickLabel,
        role = role,
        indication = indication,
        onClick = onClick,
    ) {
    // 탭 전환 등으로 selected가 바뀌면 시맨틱을 무효화해 보조도구(TalkBack)가 다시 읽게 하고,
    // 나머지 파라미터는 부모 update에 그대로 위임한다.
    fun update(
        selected: Boolean,
        enabled: Boolean,
        debounceIntervalMillis: Long,
        onClickLabel: String?,
        role: Role?,
        indication: IndicationNodeFactory?,
        onClick: () -> Unit,
    ) {
        if (this.selected != selected) {
            this.selected = selected
            invalidateSemantics()
        }
        update(
            enabled = enabled,
            debounceIntervalMillis = debounceIntervalMillis,
            onClickLabel = onClickLabel,
            role = role,
            indication = indication,
            onClick = onClick,
        )
    }

    // 한정자 없는 selected는 SemanticsPropertyReceiver.selected 세터로 해석되므로,
    // 노드 필드는 반드시 this@ 한정으로 읽는다. (DebouncedClickableNode의 role과 동일한 함정)
    override fun SemanticsPropertyReceiver.applyAdditionalSemantics() {
        selected = this@DebouncedSelectableNode.selected
    }
}
