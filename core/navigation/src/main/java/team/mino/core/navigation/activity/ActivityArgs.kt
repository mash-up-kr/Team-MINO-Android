package team.mino.core.navigation.activity

import android.content.Intent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal const val EXTRA_ACTIVITY_ARGS = "team.mino.core.navigation.ACTIVITY_ARGS"

internal val activityArgsJson = Json { ignoreUnknownKeys = true }

/**
 * [BaseActivityLauncher]가 [Intent]에 실어 보낸 진입 인자를 역직렬화한다.
 *
 * 대상 Activity(`:impl`)가 자신의 `:api`에 정의된 인자 타입과 serializer로 호출한다.
 * 인자가 없으면 `null`을 반환한다.
 */
fun <ARGS> Intent.getArgsOrNull(serializer: KSerializer<ARGS>): ARGS? =
    getStringExtra(EXTRA_ACTIVITY_ARGS)?.let { activityArgsJson.decodeFromString(serializer, it) }
