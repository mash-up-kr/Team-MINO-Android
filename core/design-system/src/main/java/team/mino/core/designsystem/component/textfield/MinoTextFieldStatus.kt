package team.mino.core.designsystem.component.textfield

/**
 * TextInput/TextArea의 검증 상태.
 *
 * - [Normal] 기본 상태.
 * - [Positive] 성공(체크 아이콘 + 헬퍼).
 * - [Negative] 에러(빨강 테두리·아이콘·헬퍼).
 *
 * Figma `Status` variant 축에 대응한다. (TextArea는 [Normal]/[Negative]만 사용)
 */
enum class MinoTextFieldStatus {
    Normal,
    Positive,
    Negative,
}
