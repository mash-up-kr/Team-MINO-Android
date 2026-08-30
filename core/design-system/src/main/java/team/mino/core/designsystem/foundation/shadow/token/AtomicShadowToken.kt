package team.mino.core.designsystem.foundation.shadow.token

import team.mino.core.designsystem.foundation.color.token.AtomicColorToken

internal object AtomicShadowToken {
    // 그림자 색은 원시 팔레트를 공유하지만, 알파는 색상 불투명도 스케일(AtomicOpacityToken)과
    // 별개인 그림자 전용 스케일이라 자체 정의한다.
    val ShadowColor = AtomicColorToken.Neutral10

    val Alpha6 = 0.06f
    val Alpha7 = 0.07f
    val Alpha8 = 0.08f
    val Alpha10 = 0.1f
    val Alpha12 = 0.12f
    val Alpha16 = 0.16f
}
