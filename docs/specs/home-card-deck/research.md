# 리서치: 홈 탭 카드덱(Card Deck)

**대상 스펙 경로**: `docs/specs/home-card-deck`

**계획서**: [plan.md](./plan.md)

> 이 문서는 **누적**된다. 결정이 뒤집히면 항목을 지우지 않고 취소선과 `재검토됨(plan X.Y.Z)`을 붙인 뒤 새 항목을 덧붙인다.

---

## D1. 카드덱을 무엇으로 만들 것인가 — 화면인가, 상태 호이스팅 컴포넌트인가 *(plan 1.0.0)*

**Decision**: `:feature:home`의 **상태 호이스팅 컴포넌트**로 만든다. `CardDeck` 컴포저블 + `CardDeckState`(`rememberCardDeckState`) 쌍이며, **자체 ViewModel을 갖지 않는다.**

**Rationale**:
- spec §3.2가 "카드덱은 장소 목록을 **입력으로 받고**, 새 목록이 필요하면 요청 신호를 밖으로 보내는 데서 끝난다"고 못박는다. 목록을 스스로 불러오는 주체가 아니므로 ViewModel을 쥘 근거가 없다.
- spec SC-005("홈 화면 셸 없이 목록만 주입해 단독 검증 가능")를 만족하려면 데이터 출처와 분리되어야 한다.
- 덱은 **자기 상태**를 갖는다 — 현재 카드 위치, 되돌리기 이력(1단계), 가려진 장소, 애니메이션 진행 여부. 이는 UI 상태이지 화면 상태가 아니므로 `rememberSaveable` 기반 상태 홀더가 맞다.
- Compose 표준 관용구(`PagerState`·`LazyListState`)와 같은 형태라 학습 비용이 없다.

**Alternatives considered**:
- **ViewModel 보유 화면**: 목록 조회를 카드덱이 하게 되어 spec §3.2 위반. 기각.
- **완전 무상태 컴포저블**(모든 상태를 호출자가 관리): 되돌리기 이력·애니메이션 잠금까지 홈 셸이 떠안게 되어 캡슐화가 깨진다. 호출자가 덱의 내부 규칙(FR-002·UX-001)을 재구현해야 한다. 기각.

---

## D2. 카드덱을 어느 모듈에 둘 것인가 *(plan 1.0.0)*

**Decision**: `:feature:home`에 둔다. `:core:design-system`으로 올리지 않는다.

**Rationale**:
- [`core/common/ui/README.md`](../../../core/common/ui/README.md) §5의 승격 기준은 **둘 이상의 feature가 실제로 쓸 때**다. 카드덱을 쓰는 화면은 [SCR-003] 홈 하나뿐이다.
- 헌법 원칙 II — feature는 다른 feature를 의존하지 않는다. 홈 전용 컴포넌트를 공용 모듈에 두면 공용 모듈이 홈의 도메인 개념(장소분류 라벨·덱 구성 규칙)을 알게 된다.
- 승격이 필요해지면 그때 기준을 만족하는지 판정한다.

**Alternatives considered**:
- `:core:design-system`: 위 이유로 기각. 다만 **카드 한 장**(`HomeCard`)은 성격이 다르다 → D3.

---

## D3. 이미 구현된 `HomeCard`를 어떻게 가져올 것인가 *(plan 1.0.0)*

**Decision**: `:feature:sample`의 `HomeCard`·`HomeCardCategory`를 **`:feature:home`으로 이동**한다. 공용화(`:core:design-system` 승격)는 하지 않는다.

**Rationale**:
- 현재 위치는 `feature/sample/.../main/component/HomeCard.kt`이고, KDoc이 스스로 "design-system 공용 컴포넌트가 아니라 sample 화면 내부 컴포넌트다"라고 밝히고 있다. sample은 샘플 모듈이므로 프로덕션 화면이 의존해서는 안 된다(헌법 원칙 II — feature 간 의존 금지).
- 이슈 #145 본문도 "`feature/sample`의 `HomeCard`를 `feature/home`으로 이식해 사용한다"로 같은 방향을 지목한다.
- spec §3.2가 카드 시각 스타일을 비목표로 두었으므로 **이동만 하고 내부는 손대지 않는다.**

**확인된 사실**: `HomeCardCategory` enum 4종(`FriendsMostViewed`·`MostTalked`·`MostSaved`·`WorthVisiting`)이 「장소분류 라벨」 4종과 정확히 일치한다(spec §5 2026-08-18 실물 대조). 라벨 표기 변경 작업은 필요 없다.

**Alternatives considered**:
- `:core:design-system` 승격: 쓰는 곳이 하나뿐이라 D2와 같은 이유로 기각.
- `:feature:sample` 의존: 헌법 원칙 II 정면 위반. 기각.

---

## D4. 되돌리기(FR-002)를 어디까지 지원할 것인가 *(plan 1.0.0)*

**Decision**: **1단계만** 지원한다. 되돌린 직후 다시 되돌리기를 시도하면 아무 변화가 없다(EC-001).

**Rationale**: spec §4 가정이 "되돌리기는 1단계만 지원한다고 가정한다. 여러 장을 연속으로 되돌리는 동작은 디자인에 근거가 없다"로 이미 확정했다. PRD 「카드 덱」도 "바로 이전에 넘겼던 카드를 되돌린다(Undo)"로 단수다.

**Alternatives considered**: 전체 스택 되돌리기 — 근거 없음. 기각.

---

## D5. 서버 API를 지금 어디까지 확정할 것인가 *(plan 1.0.0)*

**Decision**: **`:core:domain`의 모델과 Repository 인터페이스까지만** 확정한다. DTO·DataSource·Mapper·Repository 구현(= `:core:data`)은 이번 plan에서 설계하지 않는다.

**Rationale**:
- 사용자 결정(2026-08-19): plan 범위를 "도메인 모델 + Repository 인터페이스까지"로 한정.
- 근거로 삼은 [스웨거](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)의 `GET /api/v1/rooms/{roomId}/cards`가 **`[TBD] 홈 카드 피드 조회`** 로 표시되어 있다. 설명에 "홈 카드 큐레이션 기획 변경 진행 중 … 확정 전이라 파라미터·응답 구성은 잠정"이라고 적혀 있다.
- `Card` 스키마도 `{ pin: Pin }` 하나뿐이며 "큐레이션 확정 후 라벨 그룹 등 필드 확정"이 TBD로 남아 있다.
- 잠정 스키마에 DTO·Mapper를 맞추면 큐레이션 확정 시점에 전부 다시 짜야 한다. 도메인 모델은 화면 요구(spec)에서 나오므로 API 변동에 덜 흔들린다.

**Alternatives considered**:
- 지금 data layer까지 확정: 재작업 위험이 크고, spec §3.2가 조회를 비목표로 둔 것과도 어긋난다. 기각.
- 도메인도 미루기: 카드덱이 "무엇을 입력으로 받는지"가 정해지지 않아 UI 설계를 시작할 수 없다. 기각.

---

## D6. 「장소분류 라벨」을 어떻게 받을 것인가 — **미해결 갭** *(plan 1.0.0)*

**Decision**: 도메인 모델 `PlaceCard`에 `label: PlaceCategoryLabel`을 **필수 필드로 둔다.** 서버가 이 값을 주기 전까지는 채울 수단이 없으므로 **API 갭으로 기록**하고 `[TBD]`로 남긴다.

**Rationale**:
- spec **FR-009**는 "각 카드에 장소분류 라벨 1종을 표시해야 한다. 라벨 값은 장소 목록과 함께 전달받으며, 카드덱은 그 값을 표시하는 책임만 진다"이다. 도메인 모델에 필드가 없으면 FR-009를 만족할 수 없다.
- 그런데 스웨거의 `Card` 스키마에는 **라벨 필드가 없다.** `pin` 하나뿐이다.
- spec §3.2가 라벨 **판정 로직**을 비목표로 두었으므로 클라이언트가 클릭수·코멘트수를 집계해 스스로 정할 수는 없다. 서버가 주어야 한다.

**갭의 성격**: 스펙 요구(FR-009) ↔ 현재 API 사이의 **미충족**이다. 설계로 봉합할 수 없고 백엔드 필드 추가가 필요하다.

**[TBD]**: `GET /rooms/{roomId}/cards` 응답의 `Card`에 라벨 필드가 추가되는가? 필드명·타입(enum 4종 문자열 여부)은 무엇인가? — 백엔드 큐레이션 기획 확정 후 회신 필요. 확정 전까지 `:core:data` 구현을 시작할 수 없다.

**Alternatives considered**:
- 클라이언트 판정: spec §3.2 비목표 위반이자, 클릭수·중복 저장 수 같은 전역 집계값을 클라이언트가 알 수 없다. 기각.
- 라벨을 nullable로 두고 없으면 미표시: FR-009가 "표시해야 한다"이므로 요구사항 미달. 기각.

---

## D7. FR-008 "방금 본 카드 제외"를 누가 책임지는가 *(plan 1.0.0)*

**Decision**: **서버가 책임진다.** 카드덱은 새로 받은 목록을 그대로 덱으로 만들고, 클라이언트에서 별도 제외 필터를 돌리지 않는다.

**Rationale**:
- 스웨거 `GET /rooms/{roomId}/cards` 설명의 확정분: "**재생성 시 사용자별 접근 기록으로 이미 본 카드 제외**, 개인별 큐레이션".
- `POST /api/v1/pins/{pinId}/accesses`가 그 접근 기록을 남기는 엔드포인트이며, "카드 탐색/장소 상세 진입 시 요청 유저 기준으로 접근 행을 남긴다 … 개인별 큐레이션의 재생성 제외 조건"이라고 명시한다.
- 즉 제외는 서버 큐레이션의 일부다. 클라이언트가 중복 구현하면 두 개의 진실이 생긴다(헌법 원칙 I).

**카드덱 경계에 미치는 영향**: 접근 기록 호출(`POST /pins/{pinId}/accesses`)은 **네트워크 호출이므로 카드덱이 직접 하지 않는다.** 카드덱은 "카드가 확인되었다"는 신호를 콜백으로 밖에 알리고, 호출은 홈 화면 셸이 한다. spec §3.2의 "요청 신호를 밖으로 보내는 데서 끝난다"와 같은 형태다.

**spec과의 관계**: spec §4 가정은 "`장소 더 보기`가 제외하는 '방금 본 카드'는 직전 덱에서 이미 넘긴 카드들을 뜻하며, 그 제외는 다음 덱 1회에만 적용된다"고 적었다. 서버 구현은 **접근 기록 누적** 기반이라 "1회 한정"이 아닐 수 있다. 요구 판정(TS-008: "직전 덱에서 이미 넘긴 카드가 제외된 새 덱이 노출된다")은 양쪽 모두 만족하므로 설계 충돌은 아니지만, 가정의 정밀도가 실제와 다를 수 있음을 기록해 둔다.

**Alternatives considered**:
- 클라이언트 제외 필터: 서버가 이미 제외한 목록을 다시 거르는 이중 처리. 기각.

---

## D8. 「홈 방 시트」·복제 저장을 plan이 다룰 것인가 *(plan 1.0.0)*

**Decision**: **다루지 않는다.** 카드덱은 `다른 방 저장` 선택을 콜백으로 밖에 전달하는 데서 끝난다.

**Rationale**: spec §3.2가 "카드덱은 메뉴 항목 노출과 선택 전달까지만 다룬다. 이 경로가 쓰는 시트는 「홈 방 시트」이며 **홈 화면 셸이 소유**한다"로 확정했다. FR-004도 "그 요청을 덱 밖으로 전달하는 데서 끝나야 하며, 덱의 진행 상태를 바꾸지 않아야 한다"이다.

**참고로 기록해 두는 API 사실** (홈 셸 설계 시 근거로 쓸 것):
- `POST /api/v1/pins/{pinId}/duplicate` — body `{ roomIds: [uuid] }`, 복수 방 동시 복제.
- **`409`: 대상 방 중 하나라도 같은 장소가 이미 저장돼 있으면 전체 거절.** PRD [SCR-003] Flow C는 "이미 그 장소가 있는 방 카드는 체크된 채 비활성이라 애초에 고를 수 없다"로 UI에서 막는 방식이라, 서버의 409는 UI가 막지 못한 경합 상황의 방어선이다. 이 어긋남의 처리는 **홈 셸 spec/plan의 몫**이다.

---

## D9. 에러 처리 경로 *(plan 1.0.0)*

**Decision**: 카드덱 컴포넌트는 **에러를 소비하지 않는다.** 목록 로딩 실패·복제 실패는 모두 호출자(홈 셸)의 ViewModel에서 [`conventions/error_handling.md`](../../conventions/error_handling.md)의 `launchSafely`·`runCatchingDomain` 경로로 처리한다.

**Rationale**: 카드덱은 네트워크를 호출하지 않으므로(D5·D7·D8) 도메인 예외가 발생할 지점이 없다. 덱이 표현하는 유일한 비정상 상태는 "카드가 없음"이며, 그때 무엇을 안내할지는 spec EC-003이 명시적으로 범위 밖(홈 셸 소관)으로 넘겼다.

**Alternatives considered**: 덱 내부에 에러 상태 추가 — spec에 근거 없음. 기각.
