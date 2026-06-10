package team.mino.feature.sample

import androidx.navigation.NavType
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.serializableNavType
import team.mino.feature.sample.detail.model.SampleQuery
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
internal data object SampleMain : Route

@Serializable
internal data class SampleDetail(
    val query: SampleQuery,
) : Route {
    companion object {
        val typeMap: Map<KType, NavType<*>> =
            mapOf(typeOf<SampleQuery>() to serializableNavType<SampleQuery>())
    }
}
