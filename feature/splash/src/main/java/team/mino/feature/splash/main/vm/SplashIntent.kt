package team.mino.feature.splash.main.vm

import team.mino.core.common.android.architecture.Intent

internal sealed interface SplashIntent : Intent {
    /**
     * 화면 최초 진입(콜드 스타트) 1회. 스플래시는 사용자 입력을 소비하지 않는다.
     *
     * @param inviteCode App Links(`gguk.org/r/{code}`)로 들어왔을 때의 초대 코드 후보. 일반 진입이면
     *  `null`이다 — 해석(미리보기·참여)은 [team.mino.feature.splash.main.vm.SplashViewModel]이 한다.
     */
    data class Start(val inviteCode: String? = null) : SplashIntent
}
