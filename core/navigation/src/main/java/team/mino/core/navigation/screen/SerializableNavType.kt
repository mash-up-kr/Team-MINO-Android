package team.mino.core.navigation.screen

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

/**
 * type-safe 라우트 인자 직렬화에 쓰는 단일 [Json].
 *
 * [serializableNavType]의 등록(`screen<T>`)과 복원(`SavedStateHandle.toRoute<T>`)이
 * 같은 설정을 공유해야 route 문자열 round-trip이 어긋나지 않으므로 한 곳에서만 정의한다.
 */
val MinoNavJson: Json = Json { ignoreUnknownKeys = true }

/**
 * custom `@Serializable` 타입 [T]를 라우트 인자로 쓰기 위한 [NavType].
 *
 * Navigation은 비프리미티브 타입의 NavType을 추론하지 못하므로, 화면 등록 시 typeMap으로 넘겨준다.
 * route 문자열에는 URL-safe 인코딩을 적용하고, Bundle에는 raw JSON 문자열을 그대로 담는다.
 */
inline fun <reified T> serializableNavType(
    isNullableAllowed: Boolean = false,
    json: Json = MinoNavJson,
): NavType<T> {
    val serializer = serializer<T>()
    return object : NavType<T>(isNullableAllowed) {
        override val name: String = typeOf<T>().toString()

        override fun put(
            bundle: Bundle,
            key: String,
            value: T,
        ) {
            bundle.putString(key, json.encodeToString(serializer, value))
        }

        override fun get(
            bundle: Bundle,
            key: String,
        ): T? = bundle.getString(key)?.let { json.decodeFromString(serializer, it) }

        override fun parseValue(value: String): T = json.decodeFromString(serializer, Uri.decode(value))

        override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(serializer, value))
    }
}
