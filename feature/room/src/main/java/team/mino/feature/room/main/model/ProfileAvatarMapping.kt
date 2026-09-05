package team.mino.feature.room.main.model

import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.domain.model.ProfileAvatar

/**
 * 도메인 아바타 [ProfileAvatar]와 그림 [MinoProfileAvatar]의 대응.
 *
 * 도메인은 그림을 모르고 디자인 시스템은 도메인을 모르므로, 둘의 대응은 양쪽을 모두 아는 feature가
 * 소유한다(`docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md`) — `:feature:profile`의
 * `ProfileAvatarMapping.kt`와 같은 표를 이 모듈에도 둔다. 방 멤버 아바타(`GET /rooms/{roomId}/members`)를
 * 그리는 데 쓰며, 방 목록·방 상세가 이 한 벌을 함께 본다.
 *
 * **선언 순서에서 파생하지 않는다.** 전수 `when`으로 적어 두면 어느 목록이 늘어나든 컴파일이 깨져
 * 두 목록을 함께 고치도록 강제된다.
 *
 * 아바타를 고르지 않은 멤버(`ProfileAvatar.Basic`)는 `null`로 간다(`docs/specs/profile/research.md` D53).
 */
internal val ProfileAvatar.image: MinoProfileAvatar?
    get() =
        when (this) {
            ProfileAvatar.Basic -> null
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
