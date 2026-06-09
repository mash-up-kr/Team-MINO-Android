package team.mino.feature.sample.api

import kotlinx.serialization.Serializable

/**
 * `SampleActivity` 진입 인자. feature 간 전환 시 type-safe 하게 전달된다.
 *
 * @property fromHome `:feature:home`에서 되돌아온 진입인지 여부.
 */
@Serializable
data class SampleArgs(
    val fromHome: Boolean = false,
)
