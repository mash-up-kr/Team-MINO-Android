package team.mino.core.designsystem.util.modifier.selectable.node

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.semantics.Role

// Modifier 체인에 들어가는 가벼운 "설계도". DebouncedClickableElement와 같은 역할이며,
// selected가 추가된 것 외에는 동일하다 — 실제 상태를 갖지 않고 create/update 판단만 담당한다.
internal class DebouncedSelectableElement(
    private val selected: Boolean,
    private val enabled: Boolean,
    private val debounceIntervalMillis: Long,
    private val onClickLabel: String?,
    private val role: Role?,
    private val indication: IndicationNodeFactory?,
    private val onClick: () -> Unit,
) : ModifierNodeElement<DebouncedSelectableNode>() {
    // 해당 자리에 노드가 없을 때 한 번 호출돼 실제 상태를 가진 노드를 만든다.
    override fun create(): DebouncedSelectableNode =
        DebouncedSelectableNode(
            selected = selected,
            enabled = enabled,
            debounceIntervalMillis = debounceIntervalMillis,
            onClickLabel = onClickLabel,
            role = role,
            indication = indication,
            onClick = onClick,
        )

    // equals가 false라 갱신이 필요할 때 호출된다. 기존 노드를 재사용하며 새 값만 흘려보낸다.
    override fun update(node: DebouncedSelectableNode) {
        node.update(
            selected = selected,
            enabled = enabled,
            debounceIntervalMillis = debounceIntervalMillis,
            onClickLabel = onClickLabel,
            role = role,
            indication = indication,
            onClick = onClick,
        )
    }

    // Layout Inspector에 노출할 정보. 디버깅 시 어떤 확장·파라미터인지 식별하게 한다.
    override fun InspectorInfo.inspectableProperties() {
        name = if (indication != null) "rippleSingleSelectable" else "singleSelectable"
        properties["selected"] = selected
        properties["enabled"] = enabled
        properties["debounceIntervalMillis"] = debounceIntervalMillis
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }

    // Compose는 이전 Element와 equals를 비교해 같으면 노드를 건드리지 않고,
    // 다르면 update를 호출한다. selected가 빠지면 탭 전환이 시맨틱에 반영되지 않는다.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DebouncedSelectableElement) return false
        return selected == other.selected &&
            enabled == other.enabled &&
            debounceIntervalMillis == other.debounceIntervalMillis &&
            onClickLabel == other.onClickLabel &&
            role == other.role &&
            indication == other.indication &&
            onClick === other.onClick
    }

    // equals와 짝을 이뤄야 하는 hashCode. 동일 기준의 필드로 계산한다.
    override fun hashCode(): Int {
        var result = selected.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + debounceIntervalMillis.hashCode()
        result = 31 * result + (onClickLabel?.hashCode() ?: 0)
        result = 31 * result + (role?.hashCode() ?: 0)
        result = 31 * result + (indication?.hashCode() ?: 0)
        result = 31 * result + onClick.hashCode()
        return result
    }
}
