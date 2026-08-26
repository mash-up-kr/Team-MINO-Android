package team.mino.core.designsystem.component.textinput

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.maxTextLength
import team.mino.core.designsystem.util.text.graphemeLength

/**
 * [maxLength] grapheme을 넘기는 입력을 되돌린다.
 *
 * `InputTransformation.maxLength`가 코드 유닛으로 세는 것을 대신한다. 되돌리는 동작과
 * 접근성 시맨틱(`maxTextLength`)은 그것과 같게 유지한다.
 */
internal data class MaxGraphemeLengthTransformation(
    private val maxLength: Int,
) : InputTransformation {
    init {
        require(maxLength >= 0) { "maxLength must be at least zero" }
    }

    override fun SemanticsPropertyReceiver.applySemantics() {
        maxTextLength = maxLength
    }

    // 이미 상한을 넘은 값이 밖에서 들어왔을 때 지우는 편집까지 막지 않도록, 길이가 늘어난 경우만 되돌린다.
    override fun TextFieldBuffer.transformInput() {
        val length = asCharSequence().graphemeLength()
        if (length > maxLength && length > originalText.graphemeLength()) {
            revertAllChanges()
        }
    }
}
