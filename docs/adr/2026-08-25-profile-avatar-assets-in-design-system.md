# 프로필 아바타 12종의 에셋과 컴포넌트는 `:core:design-system`이 소유한다

- **상태**: Accepted
- **작성일**: 2026-08-25
- **작성자**: Jaesung Lee

## 컨텍스트

프로필 설정 화면이 앱에 번들된 아바타 12종 중 하나를 고르는 3×4 그리드를 요구한다. 이 12종을 코드 어디에 두느냐를 정해야 했고, 세 가지가 위치를 좁혔다.

**첫째, 명세가 소유를 이미 지정했다.** [프로필 스펙](../specs/profile/spec.md) §3.2가 "아바타 12종의 이미지 에셋과 시각 규격은 디자인 시스템이 정의한다"를 비목표 항목으로 못박아 두었다.

**둘째, 소비자가 프로필 설정 화면 하나가 아니다.** 프로필은 앱 전체에서 하나이고(FR-007), 마이페이지 헤더·코멘트 작성자 표기·방 멤버 아바타가 같은 12종을 그린다(SC-003). 그 화면들은 앞으로 서로 다른 feature 모듈에 생긴다.

**셋째, 그리고 이것이 실제로 배치를 결정했다 — 에셋과 컴포넌트를 분리할 수 없다.** 아바타를 그리는 `MinoProfileAvatarImage`는 Figma 디자인 시스템에 컴포넌트로 존재하므로 [`component-asset-placement.md`](../conventions/component-asset-placement.md) §1.2에 따라 `:core:design-system`에 있어야 한다. 그런데 같은 문서 §1은 **"`:core:design-system`은 이미지 에셋을 받지 않는다"**고 정하고, [공유 래스터 이미지 ADR](2026-08-19-raster-image-placement-and-format.md)이 공유 이미지의 자리를 `:core:common:ui`로 지정한다. 둘을 그대로 따르면 에셋만 `:core:common:ui`로 가야 하는데, [모듈 의존 그래프](../architecture/modularization.md)가 `ui --> design` 방향이라 **`:core:design-system`은 `:core:common:ui`의 `R`을 참조할 수 없다.** 컴포넌트가 자기가 그릴 그림에 닿지 못한다.

## 결정

**아바타 12종의 에셋과 그것을 다루는 표면 전부를 `:core:design-system`이 소유한다.** `component/profileavatar/` 아래에 `MinoProfileAvatar`(12항목 enum)·`MinoProfileAvatarImage`·`MinoProfileAvatarDefaults`·`token/ProfileAvatarTokens`를 두고, 에셋은 `src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 WebP로 둔다. 컴포넌트 구조는 [M3 컴포넌트 패턴 ADR](2026-07-25-design-system-component-m3-pattern.md)을 따른다.

**이것은 [`component-asset-placement.md`](../conventions/component-asset-placement.md) §1과 [공유 래스터 이미지 ADR](2026-08-19-raster-image-placement-and-format.md)의 문면에 대한 예외다.** 적용 범위는 아래 "결과"가 좁힌다.

**enum은 그림과 크기만 안다.** 다음 셋은 `MinoProfileAvatar`에 넣지 않는다.

- **저장 식별자** — 서버 계약(`Avatar { id: integer }`)의 `Int`와 enum의 매핑은 소비 feature가 갖는다.
- **"미선택"** — 고르지 않은 상태는 소비처가 `MinoProfileAvatar?`의 `null`로 표현한다. 기본 아바타가 무엇인지도 도메인 규칙이다.
- **그리드 배치** — 4열 × 3행과 섹션 제목은 화면의 구성이다. 컴포넌트는 자기 한 칸만 안다.

**포맷·밀도 규칙은 예외가 아니다.** WebP 무손실 변환과 밀도별 디렉터리 배치는 [`component-asset-placement.md`](../conventions/component-asset-placement.md) §1.1을 그대로 따른다. 바뀌는 것은 **어느 모듈에 두는가**뿐이다.

## 근거

**컴포넌트와 에셋의 불가분성이 1순위 근거다.** 규약을 문자 그대로 따르면 컴포넌트는 `:core:design-system`(§1.2), 에셋은 `:core:common:ui`(§1)에 나뉘어야 하는데 의존 방향이 `ui --> design`이라 그 조합이 **성립하지 않는다.** 남는 선택지는 둘 중 하나를 어기는 것뿐이고, 컴포넌트를 `:core:common:ui`로 내리면 "Figma 디자인 시스템 컴포넌트는 `:core:design-system`이 소유한다"는 §1.2의 강한 규칙과 그 IMPORTANT 블록을 정면으로 어긴다. 에셋 쪽 규칙을 굽히는 편이 무너뜨리는 것이 적다.

**§1 경계표가 이 경우를 이미 받고 있다.** 같은 문서의 경계표는 "**특정 모듈의 기능에 종속된 리소스** — `:core:*`가 자기 기능에 쓰는 리소스. 판정 대상이 아니다"를 두고 있다. §1이 배제하려는 것은 "화면용 사진·일러스트"인데, 아바타 12종은 화면이 쓰는 삽화가 아니라 **디자인 시스템 컴포넌트가 렌더하는 재료**다. 컴포넌트 없이는 쓰일 일이 없고, 컴포넌트는 이 그림 없이는 그릴 것이 없다.

**명세와 SSOT 요구가 이를 뒷받침한다 — 다만 단독 근거는 아니다.** spec §3.2의 지정과 소비자가 여럿이라는 사실은 "공용 모듈에 둔다"까지만 말한다. **어느** 공용 모듈이냐는 위 불가분성이 정한다. [방 색상 팔레트 ADR](2026-08-14-room-color-palette-in-design-system.md)이 가시성 제약으로 같은 자리에 도달한 것과 구조가 같고, 근거의 종류만 다르다.

**도메인을 모르게 두는 것이 레이어 경계를 지킨다.** `:core:design-system`은 `:core:domain`을 의존하지 않는다([헌법 원칙 II](../constitution.md)). enum이 저장 식별자를 알게 되는 순간 디자인 시스템이 프로필이라는 도메인 개념을 알게 되고, 그 방향의 의존은 한 번 열리면 되돌리기 어렵다.

## 결과

- **이 예외의 적용 범위는 "Figma 디자인 시스템 컴포넌트가 렌더하는 전용 에셋"에 한정한다.** 여러 feature가 공유하더라도 컴포넌트에 묶이지 않은 사진·일러스트는 [공유 래스터 이미지 ADR](2026-08-19-raster-image-placement-and-format.md)대로 `:core:common:ui`에 둔다. 이 ADR을 "design-system에 이미지를 둬도 된다"로 일반화하지 않는다.
- **[`component-asset-placement.md`](../conventions/component-asset-placement.md) §1의 문면과 충돌이 남는다.** 규약 본문이 이 갈래를 다루지 않아 생긴 충돌이며, 다음 규약 개정에서 §1에 이 경우를 명시하는 것이 이 ADR의 후속 과제다.
- **feature 모듈은 아바타 그림을 갖지 않는다.** 프로필을 표기하는 화면은 `MinoProfileAvatar`를 받아 컴포넌트에 넘기고 자체 drawable을 두지 않는다.
- **enum ↔ 저장 식별자 매핑은 feature가 소유한다.** 첫 구현은 `:feature:profile`의 `main/model/ProfileAvatarId.kt`이며, 서버 대응표가 없어 **선언 순서를 1부터 매긴 임시값**을 쓴다. 그 사실은 매핑을 소유한 파일이 주석으로 든다.
- **기본 아바타는 목록의 첫 항목이라는 규칙도 feature가 갖는다.** enum에 "기본" 항목이나 플래그를 추가하지 않는다.
- **선택 상태의 시각 표시는 컴포넌트가 그리지 않는다.** Figma 원본에 선택된 칸을 구별하는 표현이 없다는 것을 디자인 검수가 확인했고, `selected`는 `Modifier.rippleSingleSelectable`의 접근성 시맨틱으로만 나간다. 디자인이 생기면 컴포넌트에서 그린다.
- **테두리는 에셋이 아니라 컴포넌트가 그린다.** 그림에는 배경 원과 캐릭터만 구워져 있고 테두리는 Figma에서 Container 프레임의 stroke다.

## 고려한 대안

**에셋만 `:core:common:ui`에 두고 컴포넌트는 `:core:design-system`에 남긴다** — 기각. [모듈 의존 그래프](../architecture/modularization.md)가 `ui --> design`이라 `:core:design-system`이 `:core:common:ui`의 `R`을 참조할 수 없다. 규약 문면은 지키지만 **컴파일되지 않는다.**

**에셋과 컴포넌트를 통째로 `:core:common:ui`로 옮긴다** — 기각. [`component-asset-placement.md`](../conventions/component-asset-placement.md) §1.2가 "Figma 디자인 시스템에 컴포넌트로 존재하면 `:core:design-system`에 만든다"를 사용처 개수와 무관하게 못박고, IMPORTANT 블록이 "재사용성이 높아 보인다"를 근거로 인정하지 않는다. 에셋 규칙 하나를 지키려고 컴포넌트 배치 규칙을 어기는 교환이다.

**`:feature:profile`이 에셋과 목록을 소유한다** — 기각. 프로필 표기 지점이 마이페이지·코멘트·방 멤버로 늘어나는 순간 12종이 모듈마다 복제된다. spec §3.2의 지정과도 어긋난다.

**`:core:domain`에 아바타 enum을 두고 디자인 시스템이 참조한다** — 기각. `:core:design-system` → `:core:domain` 의존은 레이어 역행이다([modularization.md](../architecture/modularization.md) 금지 규칙).

**규약을 어긴 채 기록을 남기지 않는다** — 기각. [헌법](../constitution.md) Governance가 "예외를 ADR로 기록한다. 기록 없는 예외는 없다"로 정한다. 기록이 없으면 다음 세션이 이 배치를 단순 위반으로 보고 되돌리려 하거나, 같은 충돌을 처음부터 다시 판단한다.
