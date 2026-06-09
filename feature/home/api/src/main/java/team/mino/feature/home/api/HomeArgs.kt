package team.mino.feature.home.api

import kotlinx.serialization.Serializable

/**
 * `HomeActivity` 진입 인자.
 *
 * @property greeting 전환을 시작한 feature가 전달하는 인사 문구.
 */
@Serializable
data class HomeArgs(
    val greeting: String,
)
