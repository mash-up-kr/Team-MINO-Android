package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.GithubRepoResponse

internal interface GithubRemoteDataSource {
    suspend fun getUserRepos(user: String): List<GithubRepoResponse>
}
