package team.mino.feature.home.api

import kotlinx.serialization.Serializable

/**
 * `HomeActivity`가 호출부에 돌려주는 결과.
 *
 * @property confirmed 사용자가 확인하고 돌아왔는지 여부.
 */
@Serializable
data class HomeResult(
    val confirmed: Boolean,
)
