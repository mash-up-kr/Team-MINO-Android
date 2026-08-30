package team.mino.feature.roomform

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_CREATED
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_SKIPPED
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_UPDATED
import team.mino.feature.roomform.form.vm.RoomFormOutcome

/**
 * 방 생성·편집 폼의 진입 Activity.
 *
 * 진입 인자는 해석하지 않고 시작 라우트에 그대로 싣는다 — 편집이냐 생성이냐는 `roomId`의
 * 유무 하나로 갈리고(계약 §2), 그 판정은 ViewModel이 한다.
 *
 * 폼이 끝났다는 신호를 호출자가 읽는 결과로 옮기는 것은 이 Activity 한 곳이다.
 */
@AndroidEntryPoint
class RoomFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* RoomForm의 두 인자는 기본값을 갖는다(테스트에서 ViewModel을 만들기 위한 대가).
         * 기본값이 인자 누락을 컴파일 시점에 감추므로 이 자리에서 둘 다 명시한다. */
        val startDestination =
            RoomForm(
                roomId = intent.getStringExtra(EXTRA_ROOM_FORM_ROOM_ID),
                isOnboarding = intent.getBooleanExtra(EXTRA_ROOM_FORM_ONBOARDING, false),
            )

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                RoomFormShell(
                    startDestination = startDestination,
                    onFinish = ::finishWith,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    /* 네 갈래를 한 자리에서 결과 코드와 extra로 옮긴다. setResult 호출은 이 함수 하나뿐이어야
     * 결과 계약이 네 갈래로 닫힌다(계약 §3·§5). */
    private fun finishWith(outcome: RoomFormOutcome) {
        val (resultCode, data) =
            when (outcome) {
                is RoomFormOutcome.Created -> RESULT_OK to resultOf(ROOM_FORM_OUTCOME_CREATED, outcome.roomId)
                is RoomFormOutcome.Updated -> RESULT_OK to resultOf(ROOM_FORM_OUTCOME_UPDATED, outcome.roomId)
                RoomFormOutcome.Skipped -> RESULT_OK to resultOf(ROOM_FORM_OUTCOME_SKIPPED)
                RoomFormOutcome.Cancelled -> RESULT_CANCELED to null
            }
        setResult(resultCode, data)
        finish()
    }

    private fun resultOf(
        outcome: String,
        roomId: String? = null,
    ): Intent =
        Intent()
            .putExtra(EXTRA_ROOM_FORM_RESULT_OUTCOME, outcome)
            .apply { roomId?.let { putExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID, it) } }
}
