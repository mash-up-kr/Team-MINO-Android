package team.mino.feature.profile.main.model

import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_EDIT
import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_ONBOARDING

internal enum class ProfileEntryPoint(val extraValue: String) {
    Onboarding(PROFILE_ENTRY_POINT_ONBOARDING),
    MyPage(PROFILE_ENTRY_POINT_EDIT),
    ;

    companion object {
        /* 알 수 없는 값·값 없음은 MyPage로 읽는다. 뒤로가기를 막는 쪽이 더 강한 제약이라,
         * 잘못된 값 때문에 사용자가 화면에 갇히는 것을 피한다. */
        fun from(extraValue: String?): ProfileEntryPoint = entries.firstOrNull { it.extraValue == extraValue } ?: MyPage
    }
}
