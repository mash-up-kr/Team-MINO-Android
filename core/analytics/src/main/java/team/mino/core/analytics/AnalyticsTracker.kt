package team.mino.core.analytics

/**
 * 이벤트·화면 조회 트래킹 공용 계약.
 *
 * feature는 이 인터페이스에만 의존한다 — Firebase 등 구체 SDK는 `:core:analytics` 내부 구현에 갇혀 밖으로 노출되지 않는다.
 */
interface AnalyticsTracker {
    /**
     * 커스텀 이벤트를 기록한다.
     *
     * @param name 이벤트 이름
     * @param params 이벤트에 함께 실을 파라미터. `String`/`Int`/`Long`/`Double`/`Float`/`Boolean` 값만 지원한다.
     */
    fun logEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )

    /**
     * 화면 조회 이벤트를 기록한다.
     *
     * `screen/TrackScreenViews.kt`가 [core:navigation]의 Route 전환마다 자동으로 호출하므로,
     * 화면 진입 로깅을 위해 직접 호출할 필요는 없다.
     */
    fun logScreenView(
        screenName: String,
        screenClass: String? = null,
    )
}
