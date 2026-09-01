package team.mino.core.data.repository.mapper

import team.mino.core.data.datasource.ProfileEntry
import team.mino.core.data.network.dto.request.AvatarRequest
import team.mino.core.data.network.dto.request.ProfileRequest
import team.mino.core.data.network.dto.response.AvatarResponse
import team.mino.core.data.network.dto.response.ProfileResponse
import team.mino.core.domain.model.Profile
import team.mino.core.domain.model.ProfileAvatar

/**
 * 아바타의 서버 표현. 표의 소유자는 `docs/specs/profile/contracts/profile-api-contract.md` §2「아바타 값 표」다.
 *
 * 서버가 색 대응을 바꾸면 고칠 곳은 이 표 하나다 — 도메인·UI·로컬 캐시 어디에도 이 문자열이 새어 나가지 않는다.
 *
 * **선언 순서에서 파생하지 않는다.** 위 대응은 [ProfileAvatar]·`RoomColor`의 선언 순서와 어긋나므로
 * (`Person4`·`Person5`·`Person6`이 `green`·`purple`·`lime`), `ordinal`이나 `entries.zip(...)`으로 이으면
 * 컴파일도 되고 테스트도 도는 채로 서버에 틀린 색이 나간다(`docs/specs/profile/research.md` D44).
 * 그래서 12줄을 손으로 적는다. 도메인 이름이 바뀌었을 때 서버 계약이 따라 바뀌어서도 안 된다.
 *
 * 서버 `enum`의 13번째 값 `gray`는 여기에 없다 — 방에서 "색을 고르지 않음"을 뜻하는 값이고
 * 프로필에는 그 상태가 저장되지 않으므로 내보낼 일이 없다.
 */
private val AVATAR_COLORS: Map<ProfileAvatar, String> =
    mapOf(
        ProfileAvatar.Person1 to "red",
        ProfileAvatar.Person2 to "red_orange",
        ProfileAvatar.Person3 to "orange",
        ProfileAvatar.Person4 to "green",
        ProfileAvatar.Person5 to "purple",
        ProfileAvatar.Person6 to "lime",
        ProfileAvatar.Person7 to "cyan",
        ProfileAvatar.Person8 to "pink",
        ProfileAvatar.Person9 to "blue",
        ProfileAvatar.Person10 to "brown",
        ProfileAvatar.Person11 to "light_blue",
        ProfileAvatar.Person12 to "violet",
    )

/** [RoomMapper]도 같은 표를 쓴다 — 방 멤버 아바타도 같은 `{ color }` 서버 표현을 공유한다. */
internal val AVATARS_BY_COLOR: Map<String, ProfileAvatar> =
    AVATAR_COLORS.entries.associate { (avatar, color) -> color to avatar }

private val AVATARS_BY_NAME: Map<String, ProfileAvatar> = ProfileAvatar.entries.associateBy { it.name }

/**
 * `avatar`가 `null`이거나 표에 없는 색이면 기본 아바타로 읽는다. 서버가 색을 넓혔다거나 아바타를 비워
 * 보냈다는 이유로 프로필 조회가 실패하면 안 된다(`docs/specs/profile/research.md` D37 · API 계약 §2 협의 항목 ⑥).
 *
 * `id`·`createdAt`은 도메인에 오르지 않는다 — 어느 요구사항도 쓰지 않는다.
 */
internal fun ProfileResponse.toDomain(): Profile =
    Profile(
        nickname = nickname,
        avatar = avatar.toProfileAvatarOrNull() ?: ProfileAvatar.Default,
    )

/**
 * 서버가 준 아바타를 읽는다. **아바타가 없거나 표에 없는 색이면 `null`** — 무엇으로 메울지는 부르는 쪽이 정한다.
 * 프로필은 기본 아바타로 메우고(위), 홈 카드는 「고르지 않음」을 그대로 도메인에 싣는다(`DeckMapper`).
 *
 * 색 표를 이 파일 밖으로 내보내지 않기 위한 자리다 — 표가 하나여야 서버가 대응을 바꿀 때 고칠 곳도 하나다.
 */
internal fun AvatarResponse?.toProfileAvatarOrNull(): ProfileAvatar? = this?.let { AVATARS_BY_COLOR[it.color] }

/** 아바타 12종만 나간다 — `gray`를 내보내는 경로는 없다. */
internal fun Profile.toRequest(): ProfileRequest =
    ProfileRequest(
        nickname = nickname,
        avatar = AvatarRequest(color = AVATAR_COLORS.getValue(avatar)),
    )

/**
 * 로컬 캐시는 서버 색 문자열이 아니라 [ProfileAvatar]의 **이름**을 든다. 캐시가 서버 표현을 들면 서버가
 * 표현을 바꿀 때 고칠 곳이 이 파일 밖으로 하나 더 생긴다(`docs/specs/profile/data-model.md` §3 · D42).
 * 그래서 위 색 표를 여기서 쓰지 않는다.
 *
 * 이름이 열거에 없으면(항목 이름이 바뀐 뒤 남은 캐시 등) 기본 아바타로 읽는다.
 */
internal fun ProfileEntry.toDomain(): Profile =
    Profile(
        nickname = nickname,
        avatar = AVATARS_BY_NAME[avatarName] ?: ProfileAvatar.Default,
    )

internal fun Profile.toEntry(): ProfileEntry =
    ProfileEntry(
        nickname = nickname,
        avatarName = avatar.name,
    )
