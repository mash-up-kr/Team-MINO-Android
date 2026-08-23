package team.mino.core.designsystem.util.text

import android.icu.text.BreakIterator

// 입력 컴포넌트가 타자마다 호출하므로 iterator를 재사용한다. BreakIterator는 스레드 안전하지 않다.
private val characterIterator = ThreadLocal.withInitial { BreakIterator.getCharacterInstance() }

/**
 * 사용자가 화면에서 세는 글자 수. 서로게이트 쌍·ZWJ 시퀀스·결합 문자를 각각 한 글자로 센다.
 *
 * `length`는 UTF-16 코드 유닛이라 이모지를 2 이상으로 세고, 코드포인트로 세도 ZWJ 시퀀스를
 * 놓친다. 화면과 맞추려면 UAX #29 grapheme cluster여야 한다.
 */
internal fun CharSequence.graphemeLength(): Int {
    if (isEmpty()) return 0
    val iterator = requireNotNull(characterIterator.get())
    iterator.setText(toString())
    var count = 0
    while (iterator.next() != BreakIterator.DONE) {
        count++
    }
    return count
}
