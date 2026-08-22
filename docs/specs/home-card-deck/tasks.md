# 작업 목록: 홈 탭 카드덱(Card Deck)

**대상 스펙 경로**: `docs/specs/home-card-deck`

**기준 plan 버전**: 1.0.0

**최초 작성일**: 2026-08-19

**최종 수정일**: 2026-08-22

**사전 조건**: [plan.md](./plan.md) (필수), [spec.md](./spec.md) (사용자 스토리), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**테스트**: 테스트 작업을 포함하지 않는다. spec이 테스트를 요청하지 않았고, plan §기술 컨텍스트가 "이 저장소에 CI가 없고 Compose 테스트 관행이 정립되어 있지 않다 — 검증은 [quickstart.md](./quickstart.md)의 수동 시나리오 + `@Preview`로 수행한다"로 정했다.

**구성 방식**: spec의 유저 플로우 2개를 사용자 스토리로 삼아 독립 구현·검증이 가능하도록 묶는다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. **한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.**
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: US1(카드덱 스와이프 탐색 & 덱 보충) · US2(카드 액션)

## 경로 규칙

plan.md §프로젝트 구조를 따른다. 실제 소스 루트는 모듈마다 다르다.

- `:core:domain` → `core/domain/src/main/kotlin/team/mino/core/domain/`
- `:feature:home` → `feature/home/src/main/java/team/mino/feature/home/`
- `:feature:sample` → `feature/sample/src/main/java/team/mino/feature/sample/`

---

## Phase 1: 셋업 (공통 인프라)

**목적**: 카드 컴포넌트를 프로덕션 모듈로 옮겨 헌법 원칙 II 위반을 먼저 해소한다.

- [X] T001 `feature/sample/src/main/java/team/mino/feature/sample/main/component/HomeCard.kt`를 `feature/home/src/main/java/team/mino/feature/home/main/component/HomeCard.kt`로 이동하고 패키지 선언을 `team.mino.feature.home.main.component`로 바꾼다. **내부 구현은 손대지 않는다** (research.md D3, spec §3.2 — 카드 시각 스타일은 비목표)
- [X] T002 `feature/sample/`에서 이동으로 깨진 참조를 정리한다 — `HomeCard`를 쓰던 sample 화면의 호출부와 `@Preview`를 제거하거나 sample 자체 컴포넌트로 대체한다
- [X] T003 `:feature:home`이 카드 렌더링에 필요한 의존(`:core:design-system` 등)을 갖는지 `feature/home/build.gradle.kts`에서 확인하고, 빠진 것만 추가한다 (헌법 원칙 II — `implementation` 기본)

**체크포인트**: `./gradlew :app:assembleQaDebug` 성공. `:feature:home`이 `:feature:sample`을 의존하지 않는다.

---

## Phase 2: 기반 작업 (선행 필수 - 차단 요소)

**목적**: 두 스토리가 공통으로 쓰는 도메인 타입과 덱 상태 홀더를 만든다.

**⚠️ 중요**: 이 단계가 끝나기 전에는 어떤 사용자 스토리 작업도 시작할 수 없다

- [X] T004 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceCategoryLabel.kt`에 장소분류 라벨 4종 enum을 만든다 — `FRIENDS_MOST_VIEWED`·`MOST_TALKED`·`MOST_SAVED`·`WORTH_VISITING` (data-model.md §1.2). **표시 문구는 갖지 않는다**
- [X] T005 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/Registrant.kt`에 등록자 모델을 만든다 — `userId`·`avatarUrl?` (data-model.md §1.3). 닉네임은 두지 않는다
- [X] T006 `core/domain/src/main/kotlin/team/mino/core/domain/model/PlaceCard.kt`에 카드 도메인 모델을 만든다 — `pinId`·`placeName`·`address`·`imageUrls`·`label`·`registrant?` (data-model.md §1.1). **저장 경과일 필드를 두지 않는다** (T004·T005에 의존)
- [X] T007 [P] `core/domain/src/main/kotlin/team/mino/core/domain/repository/CardFeedRepository.kt`에 인터페이스를 만든다 — `getCards(roomId)`·`recordAccess(pinId)` (contracts/card-feed-repository.md). **구현은 만들지 않는다** — API가 TBD다 (T006에 의존)
- [X] T008 `feature/home/src/main/java/team/mino/feature/home/main/component/CardDeckState.kt`에 상태 홀더와 `rememberCardDeckState()`를 만든다 — 프로퍼티·파생값·전이 규칙은 data-model.md §3.1, 공개 표면은 contracts/card-deck-component.md §2를 따른다. `rememberSaveable` 복원 시 `pinId`만 저장한다
- [X] T009 `feature/home/src/main/java/team/mino/feature/home/main/component/PlaceCardMapper.kt`에 `PlaceCategoryLabel` → `HomeCardCategory` 1:1 매핑을 만든다 (contracts/card-deck-component.md §4). 4종이 정확히 대응하므로 분기 누락이 없어야 한다 (T001·T004에 의존)

**체크포인트**: 도메인 타입과 덱 상태가 준비되어 두 스토리를 병렬로 시작할 수 있다.

---

## Phase 3: 사용자 스토리 1 - 카드덱 스와이프 탐색 & 덱 보충

**목표**: 목록을 주입받아 스택으로 그리고, 우측 영역 스와이프로 넘기고 되돌리며, 덱이 줄면 보충 요청을 밖으로 보낸다.

**독립 테스트**: quickstart.md 시나리오 **A·B·C·E·F**를 고정 목록 주입으로 수행한다. 홈 셸·서버 없이 `@Preview`만으로 검증 가능해야 한다 (spec SC-005).

### 사용자 스토리 1 구현

- [X] T010 [US1] `feature/home/src/main/java/team/mino/feature/home/main/component/CardDeck.kt`에 컴포저블 뼈대와 시그니처를 만든다 — `cards`·`state`·`onCardConfirmed`·`onLoadMore`·`onSaveToOtherRoom`·`modifier` (contracts/card-deck-component.md §1). 이 시점에는 최상단 카드 1장만 그린다 (T008에 의존)
- [X] T011 [US1] `CardDeck.kt`에 덱 구성 규칙을 넣는다 — 목록 10개 초과 시 앞에서 10장, 미만이면 있는 만큼, 0개면 덱을 그리지 않음. 중복 `pinId`는 앞의 것만 남긴다 (계약 C-07, FR-006, SC-003)
- [X] T012 [US1] `CardDeck.kt`에 남은 카드가 뒤로 겹쳐 보이는 **스택 배치**를 구현한다 (spec 유저 플로우 1 step 1)
- [X] T013 [US1] `CardDeck.kt`에 **화면 우측 영역 한정** 드래그 제스처를 붙인다. 좌측 영역 입력은 카드 전환·복구에 반영하지 않는다 (계약 C-01·C-03, FR-001·FR-003, SC-001)
- [X] T014 [US1] `CardDeck.kt`에 전환 임계값 판정을 넣는다 — **카드 폭 25% 이상 또는 충분한 가로 플릭 속도**면 넘기고, 둘 다 아니면 원위치하며 아무 신호도 보내지 않는다 (계약 C-04, EC-002)
  - 25%는 **잠정 구현 기준**이다. spec·plan에 수치 근거가 없어 이 작업에서 정한다 — Material `SwipeToDismissBox` 기본값 50%는 10장 연속 탐색에 무겁고, 속도 조건을 병행해야 짧고 빠른 플릭이 EC-002와 충돌하지 않는다. 디자인에서 수치가 확정되면 교체한다
- [X] T015 [US1] `CardDeck.kt`에 좌→우 넘김을 구현하고 `onCardConfirmed(pinId)`를 **1회만** 발생시킨다 (계약 C-01, FR-001)
- [X] T016 [US1] `CardDeck.kt`에 우→좌 되돌리기(1단계)를 구현한다. 되돌릴 카드가 없으면 무동작이며, 복구해도 이미 나간 `onCardConfirmed`는 취소하지 않는다 (계약 C-02·C-05, FR-002·EC-001)
- [X] T017 [US1] `CardDeck.kt`에 전환 애니메이션을 넣고, 애니메이션 진행 중 추가 스와이프 입력을 무시한다 (계약 C-06, UX-001, SC-004)
- [X] T018 [P] [US1] `feature/home/src/main/java/team/mino/feature/home/main/component/LoadMoreButton.kt`에 `장소 더 보기` Floating Button을 만든다 (계약 C-08)
- [X] T019 [US1] `CardDeck.kt`에 잔여 2장 이하 노출 조건과 클릭 시 `onLoadMore()` 발생을 배선한다. **덱은 스스로 목록을 가져오지 않는다** (계약 C-08·C-09, FR-007, SC-002) (T018에 의존)
- [X] T020 [US1] `CardDeck.kt`에 새 `cards` 주입 시 진행 상태 초기화와 덱 재구성을 구현한다 (계약 C-10, FR-008)
- [X] T021 [US1] `CardDeck.kt`가 각 카드를 `HomeCard`로 그리도록 배선하고 장소분류 라벨을 표시한다. `imageCount = 2` 고정, 등록자는 아바타만 (계약 C-14·§4, FR-009) (T009에 의존)
- [X] T022 [US1] 덱이 비어도 `장소 더 보기` 노출을 유지하고 **빈 상태 안내를 하지 않는다**. 제외 후 남는 카드가 0장이면 임의 카드를 채우지 않는다 (계약 C-17, EC-003·EC-004)
- [X] T023 [P] [US1] `CardDeck.kt`에 `@Preview`를 추가한다 — 12장/4장/0장, 라벨 4종 각각. quickstart 시나리오 B·E를 눈으로 확인할 수 있어야 한다

**체크포인트**: 고정 목록만으로 스와이프 탐색·덱 구성·보충 요청·라벨 표시가 동작한다. quickstart A·B·C·E·F 통과.

---

## Phase 4: 사용자 스토리 2 - 카드 액션(다른 방 저장 / 장소 가리기)

**목표**: 카드 `[...]`로 액션 메뉴를 열어, 현재 카드를 덱에서만 숨기거나 다른 방 저장 요청을 밖으로 보낸다.

**독립 테스트**: quickstart.md 시나리오 **D**를 수행한다. 「홈 방 시트」 없이 콜백 발생만으로 검증한다.

### 사용자 스토리 2 구현

- [X] T024 [P] [US2] `feature/home/src/main/java/team/mino/feature/home/main/component/CardActionMenu.kt`에 `다른 방 저장`·`장소 가리기` 두 항목 메뉴를 만든다. **Figma 시안이 없는 신규 UI**이며 항목 순서는 PRD 「카드 덱」 표기 순서를 따른다 (계약 C-11, FR-004)
- [X] T025 [US2] `CardDeck.kt`에서 카드 `[...]` 클릭 시 메뉴를 **그 카드 근처에** 열도록 배선한다 (계약 C-11, UX-002) (T024에 의존)
- [X] T026 [US2] `장소 가리기`를 구현한다 — 현재 덱에서만 제거하고 다음 카드를 노출하며, `CardDeckState.hidePlace(pinId)`를 통한다. 새 목록에 다시 들어오면 정상 노출된다 (계약 C-12, FR-005)
- [X] T027 [US2] `다른 방 저장` 선택 시 메뉴를 닫고 `onSaveToOtherRoom(pinId)`만 발생시킨다. **덱의 현재 카드와 되돌리기 이력이 변하지 않아야 한다** (계약 C-13, FR-004)
- [X] T028 [US2] 메뉴가 열린 상태의 스와이프는 메뉴만 닫고 카드 전환에 반영하지 않으며, 바깥 탭은 아무 액션 없이 메뉴만 닫는다 (계약 C-15·C-16, EC-005·EC-007)
- [X] T029 [US2] 마지막 1장을 `장소 가리기`로 제거했을 때 덱이 비고 `장소 더 보기` 노출이 유지되는지 배선을 확인한다 (계약 C-17, EC-006) (T019·T026에 의존)

**체크포인트**: US1과 US2가 모두 독립적으로 동작한다. quickstart A~F 전부 통과.

---

## Phase 5: 마무리 및 공통 관심사

**목적**: 검증 실행과 규약 확인

- [X] T030 [P] `feature/home/.../component/`의 색·치수·타이포가 [`docs/conventions/figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md)의 토큰·실측값 판정 절차를 따르는지 대조한다. 카드 내부(`HomeCard`)는 비목표라 대상에서 제외한다
- [ ] T031 [quickstart.md](./quickstart.md) 시나리오 A~F를 순서대로 실행해 결과를 기록한다. `onCardConfirmed` 호출 횟수가 카드당 정확히 1회인지 로그로 확인한다
- [X] T032 `./gradlew :app:assembleQaDebug`로 빌드를 확인한다 (헌법 §품질 게이트 — 이 저장소의 빌드 확인 최소선)
- [X] T033 Compose Lint 위반을 확인하고 [`docs/conventions/compose-lint.md`](../../conventions/compose-lint.md)에 따라 처리한다. 로컬 `lintDebug`가 JBR JIT 이슈로 죽으면 검증이 수행되지 않은 것이므로 그 사실을 PR에 남긴다

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1 셋업**: 의존성 없음 — 즉시 시작. `HomeCard` 이동이 이후 모든 렌더링 작업의 전제다
- **Phase 2 기반**: Phase 1 완료에 의존. **두 스토리를 모두 차단한다**
- **Phase 3 US1 / Phase 4 US2**: 둘 다 Phase 2 완료에 의존. 서로는 **부분적으로만** 독립적이다 — 아래 참고
- **Phase 5 마무리**: US1·US2 완료에 의존

### 사용자 스토리 간 의존성

US2는 US1과 완전히 독립적이지 않다. 액션 메뉴가 `CardDeck.kt`의 카드 렌더링 위에 얹히기 때문이다.

- **T024(메뉴 컴포저블)는 US1과 무관하게 먼저 만들 수 있다** — 파일이 다르다
- **T025 이후는 T021(카드 렌더링 배선) 이후**에 붙는 것이 자연스럽다
- **T029는 T019(보충 버튼 배선)와 T026 양쪽에 의존**한다

인력이 하나면 US1 → US2 순차 진행이 가장 매끄럽다.

### 병렬 실행 예시

```text
Phase 2 시작 직후:  T004, T005, T007 을 병렬로
                    (T006 은 T004·T005 완료 후, T007 은 T006 후)

Phase 3 진행 중:    T018(LoadMoreButton), T023(Preview) 을 본체 작업과 병렬로
                    T024(CardActionMenu) 도 이 시점에 병렬 착수 가능

Phase 5:            T030(디자인 대조) 을 T031~T033 과 병렬로
```

`CardDeck.kt` 한 파일에 몰리는 T010~T022는 **병렬 불가**다. 같은 파일을 여러 사람이 동시에 고치면 충돌한다.

### 구현 전략

1. **Phase 1을 먼저 닫는다.** `HomeCard` 이동은 헌법 원칙 II 위반 해소이고 이후 모든 작업의 전제다.
2. **Phase 2에서 타입을 굳힌다.** 도메인 모델이 흔들리면 UI 작업이 전부 흔들린다.
3. **US1을 완주해 `@Preview`로 눈으로 확인한다.** 이 시점에 spec SC-005("홈 셸 없이 단독 검증 가능")가 실제로 성립하는지 판명된다.
4. **US2를 얹는다.** 액션 메뉴는 덱 위의 레이어이므로 마지막에 붙이는 것이 충돌이 적다.
5. **`:core:data`는 착수하지 않는다.** plan.md §미해결 사항의 TBD-1·TBD-2가 풀릴 때까지 `CardFeedRepository` 구현은 별도 작업으로 남긴다.

---

## 미결 사항

작업으로 만들지 못한 항목이다. plan.md §미해결 사항과 같은 출처를 가리킨다.

| # | 내용 | 왜 작업이 아닌가 |
|---|---|---|
| **TBD-1** | `Card` 응답에 장소분류 라벨 필드가 없어 spec FR-009를 서버 데이터로 충족할 수 없다 | 백엔드 필드 추가가 선행되어야 한다. T021은 **주입된 라벨을 표시**하는 데까지만 책임지며, 실제 값이 어디서 오는지는 `:core:data` 작업의 몫이다 |
| **TBD-2** | `GET /rooms/{roomId}/cards`가 `[TBD]`(큐레이션 기획 변경 중) | DTO·Mapper를 지금 만들면 재작업이다. T007은 인터페이스까지만 만든다 |
| **TBD-3** | `getCards`에 정렬 파라미터가 없다 | 정렬은 홈 셸 이관분이라 이 작업 목록의 범위 밖이다 |

**세 항목 모두 T001~T033의 착수를 막지 않는다.** 카드덱은 고정 목록 주입으로 완성·검증할 수 있다.
