package team.mino.feature.room.detail.model

import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.domain.model.ProfileAvatar

/**
 * 도메인 아바타 [ProfileAvatar]와 그림 [MinoProfileAvatar]의 대응.
 *
 * `feature:profile`의 `ProfileAvatarMapping`과 같은 표다 — 도메인은 그림을 모르고 디자인 시스템은
 * 도메인을 모르므로, 양쪽을 모두 아는 feature가 각자 이 대응을 소유한다
 * (`docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md`).
 *
 * **선언 순서에서 파생하지 않는다** — 이유는 `ProfileAvatarMapping`과 같다.
 */
internal val ProfileAvatar.image: MinoProfileAvatar
    get() =
        when (this) {
            ProfileAvatar.Person1 -> MinoProfileAvatar.Person1
            ProfileAvatar.Person2 -> MinoProfileAvatar.Person2
            ProfileAvatar.Person3 -> MinoProfileAvatar.Person3
            ProfileAvatar.Person4 -> MinoProfileAvatar.Person4
            ProfileAvatar.Person5 -> MinoProfileAvatar.Person5
            ProfileAvatar.Person6 -> MinoProfileAvatar.Person6
            ProfileAvatar.Person7 -> MinoProfileAvatar.Person7
            ProfileAvatar.Person8 -> MinoProfileAvatar.Person8
            ProfileAvatar.Person9 -> MinoProfileAvatar.Person9
            ProfileAvatar.Person10 -> MinoProfileAvatar.Person10
            ProfileAvatar.Person11 -> MinoProfileAvatar.Person11
            ProfileAvatar.Person12 -> MinoProfileAvatar.Person12
        }
