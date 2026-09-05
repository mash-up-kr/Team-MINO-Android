# 방 카드·방 칩·방 헤더 컴포넌트는 `:core:design-system`이 소유한다

- **상태**: Accepted
- **작성일**: 2026-08-18
- **작성자**: Chea-yunzi

## 컨텍스트

`room-list` spec([SCR-004])을 설계하며 `Full` 상태의 방 카드 목록(방 썸네일·이름·설명·장소 개수·멤버 아바타, [FR-004])을 구현할 컴포넌트가 필요했다. 검색해 보니 이미 `MinoRoomCard`·`MinoRoomCheckBoxCard`·`MinoChipRoom`·`MinoHeaderRoom`이 `:feature:sample/main/component/`에 프로토타입으로 존재했다 — `MinoRoomCard`는 room-list가 요구하는 필드(제목·장소 개수 라벨·참여자 아바타 그룹·커버 이미지·메모)를 이미 그대로 갖추고 있었다.

문제는 이 컴포넌트들의 소비자가 room-list 하나가 아니라는 점이다. `room-detail` spec([SCR-005])의 헤더, `home` feature의 방 변경 시트([SCR-003])·다른 방 저장 시트가 모두 같은 성격의 방 카드/칩/헤더를 필요로 한다. 그런데 `:feature:sample`은 헌법(`docs/constitution.md`)이 명시한 데모 모듈이라 "추후 제거될 수 있고", 다른 feature 모듈이 서로를 직접 의존할 수도 없다(`docs/architecture/modularization.md` 금지 규칙 — feature 간 직접 의존 금지). room-list가 이 컴포넌트를 그대로 가져다 쓰려면 지금 위치(`:feature:sample`)로는 불가능하다.

이미 같은 문제를 겪은 선례가 있다 — 방 대표 색상 12종 팔레트와 색상 칩을 `:core:design-system`으로 옮긴 [`2026-08-14-room-color-palette-in-design-system.md`](2026-08-14-room-color-palette-in-design-system.md)다. 그 ADR의 논거("소비자가 여럿이면 SSOT가 필요하다", "디자인 시스템은 UI 레이어 자산이고 도메인을 모른다")가 방 카드류 컴포넌트에도 그대로 적용된다.

## 결정

**`MinoRoomCard`·`MinoRoomCheckBoxCard`·`MinoChipRoom`·`MinoHeaderRoom`(과 그 `*Defaults`·`*Tokens`·`*Content` 보조 파일)을 `:feature:sample`에서 `:core:design-system`으로 승격한다.** 컴포넌트 구조는 [M3 컴포넌트 패턴 ADR](2026-07-25-design-system-component-m3-pattern.md)을 그대로 따른다(이미 `*Defaults` 형태로 만들어져 있어 구조 변경은 필요 없다).

**승격 대상은 순수 표현(stateless) 컴포넌트로 한정한다.** `MinoRoomCard`는 이미 `title`·`placeCountLabel`·`participantImageUrls`·`onClick`·`coverImageUrl`·`memo`만 받는 stateless 형태라 feature 도메인 모델(`Room` 등)을 모른다 — 이 경계를 승격 후에도 유지한다. 방 목록을 `Room` 도메인 모델에서 저 파라미터들로 변환하는 매핑은 각 소비 feature(room-list·room-detail·home)가 갖는다.

## 근거

**소비자가 여럿이면 SSOT가 필요하다.** room-list·room-detail·home이 각자 비슷한 방 카드를 따로 구현하면, 디자이너가 카드 레이아웃 하나를 바꿨을 때 고쳐야 할 자리를 코드가 알려주지 못한다 — [색상 팔레트 ADR](2026-08-14-room-color-palette-in-design-system.md)과 동일한 근거.

**`:core:common:ui`가 아니라 `:core:design-system`인 이유.** `core/common/ui/README.md` §4가 명확히 구분한다 — "색·타이포·그림자 토큰, 테마, 기본 디자인 컴포넌트"는 `core:design-system`, "동작/구조"는 `core:common:ui`. 방 카드는 시각적 표현(레이아웃·타이포·색)이 중심인 디자인 컴포넌트라 전자에 속한다.

**모듈 경계 위반을 피할 수 있는 유일한 길이다.** feature 모듈은 다른 feature 모듈을 직접 의존할 수 없다(`docs/architecture/modularization.md`). `:feature:sample`에 그대로 두면 room-list·room-detail·home 중 어느 것도 이 컴포넌트를 재사용할 수 없고, 결국 각자 복제하게 된다.

**이미 stateless라 승격 비용이 낮다.** `MinoRoomCard`를 실제로 읽어보면 이미 `core/common/ui/README.md` §5의 승격 기준("feature 도메인/네비게이션에 묶여 있지 않음", "상태를 인자로 받고 콜백을 올리는 stateless 형태")을 만족한다 — 파라미터로 원시 문자열·URL·콜백만 받고 `Room` 도메인 모델을 모른다. 리팩터링 없이 위치만 옮기면 된다.

## 결과

- room-list·room-detail·home은 각자 `Room`(또는 그에 상응하는 로컬 뷰 모델)을 `MinoRoomCard`의 파라미터로 변환하는 매핑만 작성한다. 카드 자체를 복제하지 않는다.
- `:feature:sample`의 원본 파일은 승격 후 삭제한다(데모 목적이 사라짐 — 실제 구현 위치는 `:core:design-system`).
- `MinoRoomCard`·`MinoRoomCheckBoxCard`·`MinoChipRoom`·`MinoHeaderRoom`은 `Room` 등 도메인 개념을 계속 모른다. 도메인이 새어 들어가는 변경(예: `Room` 타입을 직접 파라미터로 받기)은 이 결정을 재검토해야 한다.
- 첫 적용은 `room-list`([SCR-004])다. `room-detail`·`home`이 뒤이어 같은 컴포넌트를 재사용한다.
- 이 승격 작업 자체는 `room-list`의 `/mino-plan`이 아니라 `/mino-task`(구현 단계)가 수행한다 — Plan 단계는 소스 코드를 만들거나 고치지 않는다.

## 고려한 대안

**`:feature:sample`에 그대로 두고 room-list가 복제해서 새로 만든다** — 기각. 세 feature(room-list·room-detail·home)가 각자 복제하면 값이 갈라지고, `:feature:sample`이 언젠가 제거되면(헌법이 이미 그 가능성을 명시) 원본이 사라져 참조할 곳이 없어진다.

**`:core:common:ui`로 승격한다** — 기각. 위 근거에서 다뤘듯 방 카드는 동작/구조가 아니라 디자인 표현이 중심이라 `core:common:ui`의 책임 범위(§4)를 벗어난다.

**`Room` 도메인 모델을 파라미터로 직접 받게 만든다** — 기각. `:core:design-system`이 `:core:domain`을 의존하게 되어 레이어 역행이다(헌법 원칙 II, [색상 팔레트 ADR](2026-08-14-room-color-palette-in-design-system.md)과 동일 논거). 도메인 → 표현 매핑은 항상 소비하는 feature가 갖는다.
