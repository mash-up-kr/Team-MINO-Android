package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import team.mino.core.data.network.dto.response.GithubRepoResponse
import javax.inject.Inject

internal class GithubApiService @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getUserRepos(
        user: String,
        perPage: Int = 30,
    ): List<GithubRepoResponse> =
        client
            .get("users/$user/repos") {
                parameter("per_page", perPage)
            }.body()
}
