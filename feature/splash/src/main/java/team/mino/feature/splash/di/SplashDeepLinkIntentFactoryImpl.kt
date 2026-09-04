package team.mino.feature.splash.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.deeplink.SplashDeepLinkIntentFactory
import team.mino.feature.splash.SplashActivity
import javax.inject.Inject

/**
 * 콜드 우회용 Intent — `MainActivity`가 게이트 미통과 상태에서 푸시 extra를 스플래시로 넘길 때 쓴다.
 *
 * `FLAG_ACTIVITY_CLEAR_TOP`만 건다. 스플래시가 이미 진행 중이면 그 위에 쌓인 우회용 `MainActivity`를
 * 정리하고, `singleTop`(매니페스트)과 맞물려 그 스플래시가 재생성 없이 `onNewIntent`로 extra를 이어받는다.
 * `NEW_TASK`는 걸지 않는다 — Activity 안에서 호출되므로 태스크가 이미 있다.
 * 계약: docs/specs/push-notification/contracts/push-deeplink-contract.md §2, research.md D16.
 */
internal class SplashDeepLinkIntentFactoryImpl @Inject constructor() : SplashDeepLinkIntentFactory {
    override fun create(context: Context): Intent =
        context.intentOf<SplashActivity> { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
}
