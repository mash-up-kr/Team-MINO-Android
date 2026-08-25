package team.mino.core.navigation.activity.launcher

import team.mino.core.navigation.activity.ActivityLauncher

interface ProfileLauncher : ActivityLauncher

// EXTRA_PROFILE_ENTRY_POINT에 실리는 값. 호출자와 화면이 같은 문자열을 보도록 계약 자리에서 공유한다.
const val PROFILE_ENTRY_POINT_ONBOARDING = "onboarding"
const val PROFILE_ENTRY_POINT_EDIT = "edit"
