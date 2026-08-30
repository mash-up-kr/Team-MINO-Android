package team.mino.core.navigation.screen

/**
 * feature 내부 type-safe Screen Navigation의 라우트 공통 상위 타입.
 *
 * 구현체는 androidx Navigation의 type-safe 라우트로 쓰이도록 `@Serializable` 이어야 한다.
 * 각 feature는 자신의 화면 라우트를 자기 모듈 안에 정의한다(화면 전환은 feature 내부 관심사).
 */
interface Route
