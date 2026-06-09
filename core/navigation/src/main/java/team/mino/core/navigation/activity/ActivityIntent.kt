package team.mino.core.navigation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * 대상 Activity [T]로의 [Intent]를 생성한다. [builder]로 extra 등을 덧붙일 수 있다.
 */
inline fun <reified T : Activity> Context.intentOf(builder: Intent.() -> Intent = { this }): Intent =
    Intent(this, T::class.java).builder()
