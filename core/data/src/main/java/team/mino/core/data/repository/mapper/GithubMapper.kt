package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.GithubRepoResponse
import team.mino.core.domain.model.GithubRepo

internal fun GithubRepoResponse.toDomain(): GithubRepo =
    GithubRepo(
        id = id,
        name = name,
        fullName = fullName,
        description = description.orEmpty(),
        htmlUrl = htmlUrl,
        language = language.orEmpty(),
        stargazersCount = stargazersCount,
        forksCount = forksCount,
    )
