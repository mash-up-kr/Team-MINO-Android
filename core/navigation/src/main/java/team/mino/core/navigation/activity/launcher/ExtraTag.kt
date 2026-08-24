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

const val EXTRA_ROOM_DETAIL_ROOM_ID = "room_detail_room_id"
