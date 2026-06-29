package team.mino.core.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRepoResponse(
    val id: Int,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String?,
    @SerialName("html_url") val htmlUrl: String,
    val language: String?,
    @SerialName("stargazers_count") val stargazersCount: Int,
    @SerialName("forks_count") val forksCount: Int,
)
