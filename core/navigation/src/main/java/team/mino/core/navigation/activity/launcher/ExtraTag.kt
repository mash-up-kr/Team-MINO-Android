package team.mino.core.navigation.activity.launcher

/*
 * feature 간 Activity 전환에 싣는 Intent extra 키 모음.
 *
 * 전환 계약(`XLauncher`)과 같은 자리에 모아두고 `EXTRA_<대상 feature>_<이름>` 형태로 짓는다.
 * 최상위 상수라 스코프가 없으므로, 접두어로 어느 feature에 전달되는 값인지 드러낸다.
 */

const val EXTRA_SAMPLE_GREETING = "sample_greeting"
const val EXTRA_SAMPLE_FROM_HOME = "sample_from_home"
const val EXTRA_SAMPLE_RESULT_CONFIRMED = "sample_result_confirmed"

const val EXTRA_PROFILE_ENTRY_POINT = "profile_entry_point"

const val EXTRA_ROOM_FORM_ROOM_ID = "room_form_room_id"
const val EXTRA_ROOM_FORM_ONBOARDING = "room_form_onboarding"
const val EXTRA_ROOM_FORM_RESULT_OUTCOME = "room_form_result_outcome"
const val EXTRA_ROOM_FORM_RESULT_ROOM_ID = "room_form_result_room_id"

/** 초대 딥링크(SYS-010)로 참여까지 끝난 방으로 바로 들어가야 할 때. */
const val EXTRA_MAIN_ROOM_ID = "main_room_id"

/** 온보딩의 프로필 저장이 끝나는 시점에 이 코드로 자동 참여해야 할 때. */
const val EXTRA_ONBOARDING_INVITE_CODE = "onboarding_invite_code"

// EXTRA_ROOM_FORM_RESULT_OUTCOME에 실리는 값. 폼과 호출자가 같은 문자열을 보도록 계약 자리에서 공유한다.
const val ROOM_FORM_OUTCOME_CREATED = "created"
const val ROOM_FORM_OUTCOME_UPDATED = "updated"
const val ROOM_FORM_OUTCOME_SKIPPED = "skipped"
