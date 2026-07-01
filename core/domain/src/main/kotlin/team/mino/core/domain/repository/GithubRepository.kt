package team.mino.core.domain.repository

import team.mino.core.domain.model.GithubRepo

interface GithubRepository {
    suspend fun getUserRepos(user: String): List<GithubRepo>
}
