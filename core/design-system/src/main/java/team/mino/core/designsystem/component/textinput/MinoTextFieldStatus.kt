package team.mino.core.designsystem.component.textinput

/**
 * [MinoTextField]의 검증 상태.
 *
 * - [Normal] 기본 상태.
 * - [Positive] 성공(체크 아이콘 + 헬퍼).
 * - [Negative] 에러(빨강 테두리·아이콘·헬퍼).
 *
 * Figma `Textinput/Textfield`의 `Status` variant 축에 대응한다.
 *
 * **TextArea와 공유하지 않는다.** Figma에서 Textfield와 Textarea는 서로를 인스턴스로 물지 않는
 * 형제 컴포넌트셋이고(공유하는 것은 배경·테두리·그림자 리소스뿐이다), `Status`도 각자 선언한
 * 별개의 축이다 — Textarea 쪽은 [Positive]가 없는 2값이라 [MinoTextAreaStatus]를 쓴다.
 */
enum class MinoTextFieldStatus {
    Normal,
    Positive,
    Negative,
}
