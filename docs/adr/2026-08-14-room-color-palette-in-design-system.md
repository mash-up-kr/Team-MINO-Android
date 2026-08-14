# 방 대표 색상 12종 팔레트와 색상 칩은 `:core:design-system`이 소유한다

- **상태**: Accepted
- **작성일**: 2026-08-14
- **작성자**: Jaesung Lee

## 컨텍스트

공동방 생성·편집 폼([spec](../specs/group-room-form/spec.md) FR-006)이 대표 색상 12종(red / red orange / orange / lime / green / cyan / violet / pink / blue / brown / light blue / purple) 중 하나를 고르는 칩 그리드를 요구한다. 이 팔레트를 코드 어디에 두느냐를 정해야 했고, 두 가지 제약이 위치를 좁혔다.

**첫째, 값이 `internal`인 토큰에 걸려 있다.** 칩의 채움색·테두리색은 Figma에서 `Atomic/Red/60`·`Atomic/Red/40` 같은 원시 팔레트 변수를 참조한다. 실사 결과 12색 24개 슬롯 중 **22개가 이미 `AtomicColorToken`에 존재**한다(없는 것은 brown 2개인데, Figma에도 brown만 변수가 붙어 있지 않다). 그런데 [`core:design-system` README §4.5](../../core/design-system/README.md#45-토큰-규칙)가 `*Token` 오브젝트를 `internal`로 못박아 두었으므로, **feature 모듈에서는 `AtomicColorToken.Red60`이 보이지 않는다.** 팔레트를 쓰는 컴포넌트는 디자인 시스템 안에서만 만들 수 있다.

**둘째, 이 팔레트의 소비자가 폼 하나가 아니다.** 대표 색상은 방 썸네일·지도 마커·방 뱃지·툴팁 색의 기준이 되고(spec §2.3, FR-016), 그 화면들은 앞으로 서로 다른 feature 모듈에 생긴다. 어느 한 feature가 팔레트를 들면 나머지가 그것을 볼 수 없어 값이 복제된다.

배경 조사와 대안 검토의 전문은 [`docs/specs/group-room-form/research.md` R-006](../specs/group-room-form/research.md)에 있다.

## 결정

**12색 팔레트와 그것을 그리는 칩 컴포넌트를 `:core:design-system`이 소유한다.** `component/roomcolorchip/` 아래에 `MinoRoomColor`(12항목 enum)와 `MinoRoomColorChip`을 두고, 컴포넌트 구조는 [M3 컴포넌트 패턴 ADR](2026-07-25-design-system-component-m3-pattern.md)을 따른다.

**enum은 팔레트일 뿐 도메인 규칙을 갖지 않는다.** 다음 셋은 `MinoRoomColor`에 넣지 않는다.

- **회색 기본값** — "고르지 않으면 회색"은 방을 만드는 시점의 도메인 규칙이다. 미선택은 소비처가 `MinoRoomColor?`의 `null`로 표현한다.
- **표시 이름·도메인 식별자** — 서버가 쓰는 색 식별자와 이 enum의 매핑은 그것을 소비하는 feature가 갖는다.
- **배치** — 3×4 그리드와 레이블은 화면의 구성이다. 칩은 자기 한 칸만 안다.

**brown 두 색은 실측 raw 값을 그대로 쓰고 주석을 남기지 않는다.** Figma에도 변수가 붙어 있지 않아 [디자인 토큰 판정 ADR](2026-08-13-design-token-when-value-matches.md)의 "변수 자체가 없음"에 해당한다.

## 근거

**가시성 제약이 위치를 결정한다.** 팔레트 값에 닿을 수 있는 곳이 디자인 시스템 안뿐이다. 이걸 우회하려면 `AtomicColorToken`을 public으로 열어야 하는데, 그러면 README §4.3이 정한 "외부는 홀더 프로퍼티로만 접근한다"가 무너지고 원시값이 feature로 새어 나간다. 내부 리팩터가 외부로 새지 않게 하려고 만든 캡슐화를 팔레트 하나 때문에 깨는 것은 비용이 맞지 않는다.

**소비자가 여럿이라 SSOT가 필요하다.** 방 목록 카드·지도 마커·뱃지가 각자 같은 12색을 들면, 디자이너가 색 하나를 바꿨을 때 고쳐야 할 자리를 코드가 알려주지 못한다. enum 하나면 `when`이 망라적이라 항목 추가·변경이 컴파일 타임에 드러난다.

**도메인을 모르게 두는 것이 레이어 경계를 지킨다.** `:core:design-system`은 UI 레이어의 자산이고 `:core:domain`을 의존하지 않는다([헌법 원칙 II](../constitution.md)). enum이 회색 기본값이나 서버 식별자를 알게 되는 순간 디자인 시스템이 공동방이라는 도메인 개념을 알게 되고, 그 방향의 의존이 한 번 열리면 되돌리기 어렵다. 팔레트는 "이 앱이 쓰는 12개의 색"까지만 안다.

## 결과

- **feature 모듈은 팔레트 hex를 갖지 않는다.** 대표 색상을 그리는 화면은 `MinoRoomColor`를 받아 디자인 시스템 컴포넌트에 넘기고, 자체 색 상수를 두지 않는다.
- **도메인 모델이 생기면 매핑은 feature가 소유한다.** 서버의 색 식별자 ↔ `MinoRoomColor` 변환을 디자인 시스템이나 도메인에 두지 않는다.
- **미선택은 nullable로 표현한다.** 팔레트에 "없음"이나 "회색" 항목을 추가하지 않는다. 회색 기본값이 필요한 자리는 도메인 레이어가 채운다.
- **brown 슬롯은 임시가 아니다.** 디자이너가 Figma에 변수를 붙이면 그때 `AtomicColorToken`에 추가하고 raw를 걷어낸다. 그전까지는 마커 주석 대상이 아니다.
- **칩의 접근성 시맨틱은 선택 상태를 노출한다.** `Modifier.rippleSingleSelectable`을 쓴다([README §6.3](../../core/design-system/README.md#63-클릭선택-modifier-유틸)).
- 첫 적용은 `:feature:roomform`이며, 공개 API는 [`contracts/design-system-components.md`](../specs/group-room-form/contracts/design-system-components.md)가 소유한다.

## 고려한 대안

**feature에 칩을 만들고 Atomic 팔레트를 public으로 연다** — 기각. 팔레트 하나를 쓰자고 원시 토큰 전체의 가시성을 열게 된다. README §4.3의 두 갈래 접근(외부는 홀더 프로퍼티, 내부는 AccessKey)이 무의미해지고, 이후 어떤 feature든 원시값에 직접 손댈 수 있게 된다.

**색을 `Color` 파라미터로 받는 무지성 칩을 만든다** — 기각. 컴포넌트는 디자인 시스템에 남지만 12색의 목록과 값은 호출부가 들게 되어, 팔레트의 SSOT가 결국 feature로 옮겨간다. 소비자가 늘면 그 목록이 복제된다.

**팔레트를 시맨틱 색 토큰 슬롯으로 추가한다**(`ColorScheme`에 `RoomColorRedFill` 등 24개) — 기각. 시맨틱 토큰은 **용도**를 이름으로 갖고 라이트/다크 쌍으로 존재하는 값인데([README §4.4](../../core/design-system/README.md#44-토큰-추가하기)), 이 팔레트는 모드에 따라 바뀌지 않는 원시 색의 나열이다. 홀더에 24개 슬롯을 밀어 넣으면 `equals`·`copy`·`fromToken`이 그만큼 불어나면서 얻는 것이 없다. [셰이프 foundation 폐기 ADR](2026-08-13-no-shape-token-foundation.md)이 정한 판단 기준 — "Figma가 이름 붙은 토큰 세트로 배포하는 축일 때만 foundation을 만든다" — 과도 어긋난다.

**`:core:domain`에 `RoomColor`를 두고 디자인 시스템이 그것을 참조한다** — 기각. `:core:design-system` → `:core:domain` 의존은 레이어 역행이다([modularization.md](../architecture/modularization.md) 금지 규칙). 도메인 모델은 나중에 별도로 생기고, 둘 사이의 매핑은 feature가 갖는다.
