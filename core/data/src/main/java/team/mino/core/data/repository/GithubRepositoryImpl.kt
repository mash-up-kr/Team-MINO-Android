package team.mino.core.data.repository

import team.mino.core.data.datasource.GithubRemoteDataSource
import team.mino.core.domain.model.GithubRepo
import team.mino.core.domain.repository.GithubRepository
import javax.inject.Inject

internal class GithubRepositoryImpl @Inject constructor(
    private val dataSource: GithubRemoteDataSource,
) : GithubRepository {
    override suspend fun getUserRepos(user: String): List<GithubRepo> =
        dataSource.getUserRepos(user).map { it.toDomain() }
}
