package team.mino.feature.profile.main.model

import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar

/**
 * 프로필 저장에 쓰이는 아바타 식별자와 [MinoProfileAvatar]를 잇는 매핑.
 *
 * **이 값은 서버와 맞춰지지 않았다.** 12종과 서버 `Avatar { id: integer }`의 대응표가 아직 없어,
 * enum 선언 순서를 1부터 매긴 값을 임시로 쓴다. 대응표가 나오면 고칠 곳은 이 파일 하나다.
 * 임시 매핑인 만큼 `MinoProfileAvatar`에 항목이 끼어들면 이미 저장된 식별자의 의미가 어긋난다.
 */
internal val MinoProfileAvatar.avatarId: Int
    get() = ordinal + 1

/** 미선택 상태의 상단 썸네일과 미선택 저장이 함께 쓰는 값. */
internal val DefaultProfileAvatar: MinoProfileAvatar = MinoProfileAvatar.entries.first()

/** 목록에 없는 식별자는 [DefaultProfileAvatar]로 대체한다 — 저장된 값이 목록을 벗어나도 화면이 비지 않는다. */
internal fun profileAvatarOf(avatarId: Int): MinoProfileAvatar =
    MinoProfileAvatar.entries.getOrNull(avatarId - 1) ?: DefaultProfileAvatar
