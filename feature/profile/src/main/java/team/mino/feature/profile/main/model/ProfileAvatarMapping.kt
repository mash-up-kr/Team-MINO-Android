package team.mino.feature.profile.main.model

import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.domain.model.ProfileAvatar

/**
 * 도메인 아바타 [ProfileAvatar]와 그림 [MinoProfileAvatar]의 대응.
 *
 * 도메인은 그림을 모르고 디자인 시스템은 도메인을 모르므로, 둘의 대응은 양쪽을 모두 아는 feature가 소유한다
 * (`docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md`). 서버 표현은 여기까지 오지 않는다 —
 * `avatar.color` 문자열 표는 `:core:data`의 `ProfileMapper`에 갇혀 있다.
 *
 * **선언 순서에서 파생하지 않는다.** `ordinal`로 이으면 어느 한쪽에 항목이 끼어들어도 컴파일이 통과해,
 * 그 지점부터 뒤의 모든 항목이 조용히 어긋난 그림으로 이어진다. 전수 `when`으로 적어 두면 어느 목록이
 * 늘어나든 컴파일이 깨져 두 목록을 함께 고치도록 강제된다 — `ProfileMapper`의 색 표와 같은 이유다.
 *
 * 기본 아바타의 그림은 [MinoProfileAvatar] 항목이 아니라 `MinoProfileAvatarImage`가 `null`에서 그리는
 * 별개의 그림이라, 이 방향은 기본 아바타에서만 `null`을 낸다. 그래서 두 목록의 항목 수가 갈린다
 * (`docs/specs/profile/research.md` D53).
 */
internal val ProfileAvatar.image: MinoProfileAvatar?
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
            ProfileAvatar.Basic -> null
        }

/**
 * [image]의 반대 방향. 고를 수 있는 12종만 출발점이라 기본 아바타로 가는 항목이 없다 —
 * 고르지 않은 상태를 도메인 값으로 옮기는 것은 이 표가 아니라 [ProfileAvatar.Default]다.
 */
internal val MinoProfileAvatar.profileAvatar: ProfileAvatar
    get() =
        when (this) {
            MinoProfileAvatar.Person1 -> ProfileAvatar.Person1
            MinoProfileAvatar.Person2 -> ProfileAvatar.Person2
            MinoProfileAvatar.Person3 -> ProfileAvatar.Person3
            MinoProfileAvatar.Person4 -> ProfileAvatar.Person4
            MinoProfileAvatar.Person5 -> ProfileAvatar.Person5
            MinoProfileAvatar.Person6 -> ProfileAvatar.Person6
            MinoProfileAvatar.Person7 -> ProfileAvatar.Person7
            MinoProfileAvatar.Person8 -> ProfileAvatar.Person8
            MinoProfileAvatar.Person9 -> ProfileAvatar.Person9
            MinoProfileAvatar.Person10 -> ProfileAvatar.Person10
            MinoProfileAvatar.Person11 -> ProfileAvatar.Person11
            MinoProfileAvatar.Person12 -> ProfileAvatar.Person12
        }
