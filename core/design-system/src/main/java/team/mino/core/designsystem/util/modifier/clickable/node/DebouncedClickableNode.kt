package team.mino.core.designsystem.util.modifier.clickable.node

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.invalidateSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import kotlinx.coroutines.launch
import team.mino.core.designsystem.util.modifier.clickable.MultipleEventsCutter

// 컴포지션이 아니라 레이아웃 트리에 한 번 붙어 재사용되는 Modifier.Node.
// cutter / interactionSource 같은 상태가 이 인스턴스에 살기 때문에 리컴포지션 너머로 보존된다.
internal class DebouncedClickableNode(
    private var enabled: Boolean,
    debounceIntervalMillis: Long,
    private var onClickLabel: String?,
    private var role: Role?,
    indication: IndicationNodeFactory?,
    private var onClick: () -> Unit,
) : DelegatingNode(), SemanticsModifierNode {
    private var debounceIntervalMillis: Long = debounceIntervalMillis
    private var cutter = MultipleEventsCutter(debounceIntervalMillis)

    private var indication: IndicationNodeFactory? = indication
    private var interactionSource: MutableInteractionSource? = null
    private var indicationNode: DelegatableNode? = null

    // 탭 제스처 처리를 위임받는 pointerInput 노드. 람다는 노드 필드를 직접 읽으므로
    // update()로 onClick/enabled가 바뀌어도 최신 값으로 동작한다.
    private val pointerNode = delegate(
        SuspendingPointerInputModifierNode {
            detectTapGestures(
                // onPress: 누르는 동안 InteractionSource에 Press/Release/Cancel을 흘려
                // 인디케이션(ripple)이 눌림 상태를 그릴 수 있게 한다. 인디케이션이 없으면 생략.
                onPress = { offset ->
                    if (!enabled) return@detectTapGestures
                    val source = interactionSource
                    if (source == null) {
                        tryAwaitRelease()
                        return@detectTapGestures
                    }
                    val press = PressInteraction.Press(offset)
                    coroutineScope.launch { source.emit(press) }
                    val finish = if (tryAwaitRelease()) {
                        PressInteraction.Release(press)
                    } else {
                        PressInteraction.Cancel(press)
                    }
                    coroutineScope.launch { source.emit(finish) }
                },
                // onTap: 실제 클릭. cutter가 통과시킨 경우에만 onClick을 호출해 연타를 막는다.
                onTap = { if (enabled && cutter.processEvent()) onClick() },
            )
        },
    )

    // 노드가 트리에 붙는 시점. 인디케이션이 지정돼 있으면 이때 위임을 건다.
    override fun onAttach() {
        indication?.let(::attachIndication)
    }

    // 인디케이션이 그릴 눌림 정보를 받을 InteractionSource를 만들어 두고,
    // IndicationNodeFactory가 만든 노드를 자식으로 위임한다. (ripple 등)
    private fun attachIndication(factory: IndicationNodeFactory) {
        if (indicationNode != null) return
        val source = interactionSource ?: MutableInteractionSource().also { interactionSource = it }
        indicationNode = delegate(factory.create(source))
    }

    // 위임했던 인디케이션 노드를 떼어내 트리에서 제거한다.
    private fun detachIndication() {
        indicationNode?.let(::undelegate)
        indicationNode = null
    }

    // 같은 자리에 같은 종류의 Modifier가 다시 적용될 때 create 대신 호출된다.
    // 바뀐 파라미터만 반영하고, 변화가 없으면 비싼 재설정을 피한다.
    fun update(
        enabled: Boolean,
        debounceIntervalMillis: Long,
        onClickLabel: String?,
        role: Role?,
        indication: IndicationNodeFactory?,
        onClick: () -> Unit,
    ) {
        this.onClick = onClick

        // enabled가 바뀌면 진행 중이던 제스처 감지를 끊고 새 조건으로 다시 시작시킨다.
        if (this.enabled != enabled) {
            this.enabled = enabled
            pointerNode.resetPointerInputHandler()
        }

        // 간격이 바뀌면 새 cutter로 교체한다. (직전 클릭 시각은 초기화됨)
        if (this.debounceIntervalMillis != debounceIntervalMillis) {
            this.debounceIntervalMillis = debounceIntervalMillis
            cutter = MultipleEventsCutter(debounceIntervalMillis)
        }

        // 접근성 정보가 바뀌면 시맨틱 트리를 무효화해 다시 읽게 한다.
        if (this.onClickLabel != onClickLabel || this.role != role) {
            this.onClickLabel = onClickLabel
            this.role = role
            invalidateSemantics()
        }

        // 인디케이션이 교체되면 기존 노드를 떼고 새로 위임한다.
        if (this.indication != indication) {
            this.indication = indication
            detachIndication()
            indication?.let(::attachIndication)
        }
    }

    // 접근성/테스트가 읽는 시맨틱. TalkBack 등 보조도구의 클릭도 동일하게 cutter로 디바운스한다.
    override fun SemanticsPropertyReceiver.applySemantics() {
        role?.let { this.role = it }
        onClick(label = onClickLabel) {
            if (enabled && cutter.processEvent()) onClick()
            true
        }
        if (!enabled) disabled()
    }
}
