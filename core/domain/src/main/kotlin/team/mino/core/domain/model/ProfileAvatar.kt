package team.mino.core.domain.model

/**
 * 프로필이 가리키는 아바타. 앱이 제공하는 고정 12종 중 하나이며, 목록을 서버에서 내려받지 않는다.
 *
 * 그림(에셋)도 서버 문자열도 갖지 않는다. **무엇인지**만 안다. **어떻게 보이는지**는 `:core:design-system`의
 * `MinoProfileAvatar`가 소유한다 — `docs/adr/2026-08-25-profile-avatar-assets-in-design-system.md`.
 * 서버 표현(`avatar.color`)과의 대응표는 `:core:data`의 `ProfileMapper`가 소유하며, 그 표의 값은
 * `docs/specs/profile/contracts/profile-api-contract.md` §2 아바타 값 표가 정한다.
 *
 * 선언 순서는 디자인 목록의 배치 순서(좌→우, 상→하)다. 다만 그림 대응(`:feature:profile`)도 서버 문자열
 * 표(`:core:data`)도 이 순서에서 파생하지 않는다 — 둘 다 항목을 하나씩 적어 두므로, 목록이 늘면 그쪽
 * 컴파일이 깨져 함께 고치도록 강제된다.
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
    ;

    companion object {
        /** 사용자가 고르지 않았거나 서버 값을 알아볼 수 없을 때 채워지는 값. */
        val Default: ProfileAvatar = Person1
    }
}
