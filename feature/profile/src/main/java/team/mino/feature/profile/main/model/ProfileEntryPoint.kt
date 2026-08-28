package team.mino.feature.profile.main.model

import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_EDIT
import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_ONBOARDING

internal enum class ProfileEntryPoint(val extraValue: String) {
    Onboarding(PROFILE_ENTRY_POINT_ONBOARDING),
    MyPage(PROFILE_ENTRY_POINT_EDIT),
    ;

    /**
     * 진입 시 서버 갱신을 걸어야 하는 진입점인지.
     *
     * [Onboarding]은 스플래시가 같은 `GET /users/me`로 **미등록을 방금 확정해서** 연 화면이다
     * (`ResolveSplashEntryUseCase`). 갱신을 걸면 같은 401을 한 번 더 받고 이미 빈 캐시를 다시 비우는 것으로
     * 끝나, 앱 시작 경로에 결과가 0인 왕복이 얹힌다.
     *
     * **캐시가 비어 있다는 것은 추정이 아니라 보장이다** — 그 판정이 미등록일 때 캐시를 비우기 때문이다
     * (`ProfileRegistrationRepository.isRegistered`). 그 보장이 깨지면 저장의 등록/수정 분기가 캐시를 보고
     * `PATCH`로 갈라진다.
     *
     * [MyPage]는 다르다 — 스플래시는 콜드 스타트 때 등록 여부만 봤을 뿐이고, 그 사이 다른 기기에서 프로필이
     * 바뀌었을 수 있다.
     */
    val needsRefresh: Boolean get() = this == MyPage

    companion object {
        /* 알 수 없는 값·값 없음은 MyPage로 읽는다. 뒤로가기를 막는 쪽이 더 강한 제약이라,
         * 잘못된 값 때문에 사용자가 화면에 갇히는 것을 피한다. */
        fun from(extraValue: String?): ProfileEntryPoint = entries.firstOrNull { it.extraValue == extraValue } ?: MyPage
    }
}
