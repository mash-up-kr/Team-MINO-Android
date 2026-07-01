package team.mino.core.domain.model

data class GithubRepo(
    val id: Int,
    val name: String,
    val fullName: String,
    val description: String,
    val htmlUrl: String,
    val language: String,
    val stargazersCount: Int,
    val forksCount: Int,
)
