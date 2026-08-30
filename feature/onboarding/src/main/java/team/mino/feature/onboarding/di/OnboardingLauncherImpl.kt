package team.mino.feature.onboarding.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.activity.launcher.OnboardingLauncher
import team.mino.feature.onboarding.OnboardingActivity
import javax.inject.Inject

/**
 * 진입 인자를 싣지 않는다 — 어느 스텝부터 시작할지는 호출자가 아니라 저장된 진행 상태가 정한다
 * (`contracts/onboarding-launcher.md` §2).
 */
internal class OnboardingLauncherImpl @Inject constructor() : BaseActivityLauncher(), OnboardingLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<OnboardingActivity>()
}
