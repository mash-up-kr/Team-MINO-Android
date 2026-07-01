package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.GithubRepoResponse
import team.mino.core.data.network.service.GithubApiService
import javax.inject.Inject

internal class GithubRemoteDataSourceImpl @Inject constructor(
    private val service: GithubApiService,
) : GithubRemoteDataSource {
    override suspend fun getUserRepos(user: String): List<GithubRepoResponse> = service.getUserRepos(user)
}
