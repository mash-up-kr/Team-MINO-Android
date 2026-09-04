package team.mino.feature.main.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.deeplink.MainDeepLinkIntentFactory
import team.mino.feature.main.MainActivity
import javax.inject.Inject

/**
 * 알림 `PendingIntent`가 겨냥하는 `MainActivity` Intent.
 *
 * `NEW_TASK`만 건다 — Service Context에서 `PendingIntent.getActivity`로 띄우기 위한 필수 플래그다.
 * `CLEAR_TOP`은 걸지 않는다. `MainActivity`가 `singleTask`라 인스턴스 재사용과 위에 쌓인 Activity 정리를
 * 구조적으로 하고, `CLEAR_TOP`을 얹으면 Main이 재생성돼 웜 경로의 상태 보존이 깨진다(research.md D14).
 */
internal class MainDeepLinkIntentFactoryImpl @Inject constructor() : MainDeepLinkIntentFactory {
    override fun create(context: Context): Intent =
        context.intentOf<MainActivity> { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
}
