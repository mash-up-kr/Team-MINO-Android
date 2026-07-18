package team.mino.core.analytics.screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import team.mino.core.analytics.AnalyticsTracker
import javax.inject.Inject

@HiltViewModel
internal class ScreenViewTrackerViewModel @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    fun trackScreenView(route: String) {
        // 라우트 문자열은 `FQCN` 뒤에 인자가 path(`/{arg}`) 또는 query(`?arg={arg}`) 형태로 붙는다.
        // 두 구분자 중 먼저 나오는 지점까지를 클래스명으로 본다.
        val screenClass = route.takeWhile { it != '/' && it != '?' }
        val screenName = screenClass.substringAfterLast(".")
        analyticsTracker.logScreenView(screenName = screenName, screenClass = screenClass)
    }
}
