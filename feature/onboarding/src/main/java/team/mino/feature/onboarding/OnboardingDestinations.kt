package team.mino.feature.onboarding

import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route

/**
 * 위임 스텝(프로필·공동방) 동안 머무르는 화면. 배경만 그리며 진입 인자를 갖지 않는다.
 *
 * 온보딩 그래프의 시작 목적지이기도 하다 — 첫 스텝이 다른 Activity로 위임되므로 그 아래에
 * 깔려 있어야 할 화면이 필요하다.
 */
@Serializable
internal data object OnboardingRelay : Route

/**
 * 친구 초대 스텝.
 *
 * @param roomId 이 온보딩에서 만든 공동방 ID. `String`이라 `typeMap`이 필요 없다.
 */
@Serializable
internal data class OnboardingInvite(
    val roomId: String,
) : Route

/** 공유 방법 튜토리얼 스텝. 스텝 위치를 복원하지 않으므로 진입 인자가 없다. */
@Serializable
internal data object OnboardingTutorial : Route
