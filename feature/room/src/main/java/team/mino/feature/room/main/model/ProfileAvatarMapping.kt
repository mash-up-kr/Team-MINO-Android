package team.mino.feature.room.main.model

import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.ProfileAvatar

/**
 * 도메인 아바타 [ProfileAvatar]와 그림 [MinoProfileAvatar]의 대응.
 *
 * 도메인은 그림을 모르고 디자인 시스템은 도메인을 모르므로, 둘의 대응은 양쪽을 모두 아는 feature가
 * 소유한다(`docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md`) — `:feature:profile`의
 * `ProfileAvatarMapping.kt`와 같은 표를 이 모듈에도 둔다. 방 멤버 아바타(`GET /rooms/{roomId}/members`)를
 * 그리는 데 쓴다.
 *
 * **선언 순서에서 파생하지 않는다.** 전수 `when`으로 적어 두면 어느 목록이 늘어나든 컴파일이 깨져
 * 두 목록을 함께 고치도록 강제된다.
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

/**
 * 프로필 아바타와 방 대표 색의 대응 — 둘 다 서버 표현이 같은 색 식별자(`red`, `cyan`...)를 공유한다
 * (`:core:data`의 `ProfileMapper.AVATAR_COLORS`·`RoomMapper.COLOR_IDENTIFIERS` 참고, 각각 `internal`이라
 * 이 feature에서 직접 재사용할 수 없어 표를 다시 적는다).
 *
 * 개인 방 지도 핀(`RoomListMap.PersonalPlacePin`)에 "내 프로필 색" 핀을 얹는 데 쓴다 — 개인 방은
 * `RoomColor.GRAY`(색 미선택)라 방 색을 그대로 쓸 수 없다.
 */
internal val ProfileAvatar.roomColor: MinoRoomColor
    get() =
        when (this) {
            ProfileAvatar.Person1 -> MinoRoomColor.Red
            ProfileAvatar.Person2 -> MinoRoomColor.RedOrange
            ProfileAvatar.Person3 -> MinoRoomColor.Orange
            ProfileAvatar.Person4 -> MinoRoomColor.Green
            ProfileAvatar.Person5 -> MinoRoomColor.Purple
            ProfileAvatar.Person6 -> MinoRoomColor.Lime
            ProfileAvatar.Person7 -> MinoRoomColor.Cyan
            ProfileAvatar.Person8 -> MinoRoomColor.Pink
            ProfileAvatar.Person9 -> MinoRoomColor.Blue
            ProfileAvatar.Person10 -> MinoRoomColor.Brown
            ProfileAvatar.Person11 -> MinoRoomColor.LightBlue
            ProfileAvatar.Person12 -> MinoRoomColor.Violet
        }
