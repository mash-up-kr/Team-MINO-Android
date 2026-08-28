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

/** [image]의 반대 방향. */
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

/** 미선택 상태의 상단 썸네일이 쓰는 값. 기본값 자체는 [ProfileAvatar.Default]가 소유한다. */
internal val DefaultProfileAvatar: MinoProfileAvatar = ProfileAvatar.Default.image
