package team.mino.core.data.network.dto.response

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
