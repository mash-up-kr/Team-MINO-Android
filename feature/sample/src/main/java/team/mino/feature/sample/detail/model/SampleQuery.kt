package team.mino.feature.sample.detail.model

import kotlinx.serialization.Serializable

@Serializable
data class SampleQuery(
    val keyword: String,
    val page: Int,
)
