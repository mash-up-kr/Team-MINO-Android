package team.mino.core.data.network

import team.mino.core.data.network.dto.request.AvatarRequest
import team.mino.core.data.network.dto.request.ProfileRequest

/**
 * `user` 태그 테스트가 공유하는 경로·본문. 두 벌로 두면 한쪽만 갱신됐을 때 두 테스트가 서로 다른 서버 계약을
 * 가정한 채 통과한다.
 */
internal const val USERS_PATH = "/api/v1/users"

internal const val USERS_ME_PATH = "/api/v1/users/me"

internal const val PROFILE_BODY =
    """{"data":{"id":"u-1","nickname":"꾹이","avatar":{"color":"red"},"createdAt":"2026-08-28T00:00:00Z"}}"""

internal fun profileRequest(): ProfileRequest = ProfileRequest(nickname = "꾹이", avatar = AvatarRequest("red"))
