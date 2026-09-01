package team.mino.core.domain.model

/**
 * 프로필이 가리키는 아바타. 앱이 제공하는 **선택 12종 + 기본 1종** 중 하나이며, 목록을 서버에서 내려받지 않는다.
 *
 * 그림(에셋)도 서버 문자열도 갖지 않는다. **무엇인지**만 안다. **어떻게 보이는지**는 `:core:design-system`의
 * `MinoProfileAvatar`가 소유한다 — `docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md`.
 * 서버 표현(`avatar.color`)과의 대응표는 `:core:data`의 `ProfileMapper`가 소유하며, 그 표의 값은
 * `docs/specs/profile/contracts/profile-api-contract.md` §2 아바타 값 표가 정한다.
 *
 * 선언 순서는 디자인 목록의 배치 순서(좌→우, 상→하)이고 [Basic]이 그 뒤에 온다. 다만 그림 대응(`:feature:profile`)도
 * 서버 문자열 표(`:core:data`)도 이 순서에서 파생하지 않는다 — 둘 다 항목을 하나씩 적어 두므로, 목록이 늘면 그쪽
 * 컴파일이 깨져 함께 고치도록 강제된다.
 *
 * [Basic]은 "값 없음"이 아니라 **아바타를 고르지 않은 프로필이 갖게 되는 값**이다 — `RoomColor.GRAY`와 같은
 * 성격이며, 화면에만 있는 자리 표시가 아니라 저장되고 서버로 나가는 값이다(`docs/specs/profile/spec.md` FR-015 · EC-002).
 *
 * 선택 12종을 순회할 목록은 여기 두지 않는다. 그 순회를 하는 곳은 아바타 그리드 하나인데 그리드가 도는 것은
 * `MinoProfileAvatar.entries`(12종)여서, 목록을 두면 아무도 쓰지 않는다. `RoomColor`가 `selectable`을 가진 것과
 * 갈리는 지점이다(`docs/specs/profile/research.md` D53).
 *
 * 미선택 저장·모르는 서버 값의 대체값은 모두 [Default]다 — 어느 레이어도 그 값을 다시 유도하지 않는다.
 */
enum class ProfileAvatar {
    Person1,
    Person2,
    Person3,
    Person4,
    Person5,
    Person6,
    Person7,
    Person8,
    Person9,
    Person10,
    Person11,
    Person12,
    Basic,
    ;

    companion object {
        /**
         * 사용자가 고르지 않았거나 서버 값을 알아볼 수 없을 때 채워지는 값.
         *
         * 항목을 이름이 아니라 **역할**로 부르는 자리다 — "고르지 않았다"를 값으로 옮기는 곳은 이 값을 쓰고
         * [Basic]을 직접 적지 않는다. 항목을 이름으로 적는 곳은 두 목록뿐이며(`:core:data`의 색 표,
         * `:feature:profile`의 그림 대응) 둘 다 전수로 적어야 목록이 늘 때 컴파일이 깨진다.
         */
        val Default: ProfileAvatar = Basic
    }
}
