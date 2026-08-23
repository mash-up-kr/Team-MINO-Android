package team.mino.core.domain.model

/**
 * 인증 제공자가 발급한 익명 세션.
 *
 * [userId]는 인증 제공자가 이 세션에 부여한 고유 값이며, 서버가 데이터 소유자를 판정하는 키다.
 * 값의 형식·고유성은 인증 제공자의 계약이므로 앱이 다시 검증하지 않는다.
 */
data class AnonymousSession(
    val userId: String,
)
