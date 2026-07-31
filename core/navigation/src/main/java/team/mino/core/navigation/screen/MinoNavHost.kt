package team.mino.core.navigation.screen

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlin.reflect.KType

/**
 * 프로젝트 표준 [NavHost]. feature 내부 화면 그래프의 진입점으로 사용한다.
 *
 * type-safe 라우트([Route])를 시작 목적지로 받아, 화면 정의는 [screen]으로 등록한다.
 */
@Composable
fun MinoNavHost(
    navController: NavHostController,
    startDestination: Route,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        builder = builder,
    )
}

/**
 * [Route] 타입 [T]를 목적지로 하는 화면을 등록한다.
 *
 * androidx Navigation의 type-safe `composable<T>`를 [Route]로 제약해, 모든 화면이
 * 동일한 라우트 계약을 따르도록 강제한다.
 *
 * 라우트가 custom `@Serializable` 인자를 들면 [serializableNavType]로 만든 [typeMap]을 넘긴다.
 */
inline fun <reified T : Route> NavGraphBuilder.screen(
    typeMap: Map<KType, NavType<*>> = emptyMap(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T>(typeMap = typeMap, content = content)
}

/**
 * [Route] 타입 [T]를 진입점으로 하는 중첩 그래프를 등록한다.
 *
 * androidx Navigation의 `navigation<T>`를 [Route]로 제약해, 그래프 진입점도 화면([screen])과
 * 동일한 라우트 계약을 따르도록 강제한다.
 *
 * 화면 묶음을 상위 그래프에 편입시킬 때 쓴다(사용 규약 → `docs/architecture/feature-navigation.md`).
 */
inline fun <reified T : Route> NavGraphBuilder.graph(
    startDestination: Route,
    typeMap: Map<KType, NavType<*>> = emptyMap(),
    noinline builder: NavGraphBuilder.() -> Unit,
) {
    navigation<T>(startDestination = startDestination, typeMap = typeMap, builder = builder)
}
