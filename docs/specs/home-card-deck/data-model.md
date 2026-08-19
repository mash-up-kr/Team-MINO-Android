# 데이터 모델: 홈 탭 카드덱(Card Deck)

**대상 스펙 경로**: `docs/specs/home-card-deck`

**계획서**: [plan.md](./plan.md)

> 이 문서는 **현재 상태만** 담는다. 과거 형태를 남기지 않으며 개정 시 대체된다.
> 범위는 [research.md](./research.md) D5를 따른다 — `:core:domain`의 모델과 Repository 인터페이스까지이며, `:core:data`(DTO·DataSource·Mapper)는 이번 설계에 없다.

---

## 1. 도메인 모델 (`:core:domain`)

배치 규칙은 [`core/domain/README.md`](../../../core/domain/README.md) §3·§5를 따른다.

### 1.1 `PlaceCard`

카드 한 장이 담는 값. 카드덱이 **입력으로 받는 목록의 원소**다(spec §3.2).

| 필드 | 타입 | 필수 | 근거 |
|---|---|---|---|
| `pinId` | `String` | ✅ | 방에 저장된 장소의 식별자. 접근 기록·복제 저장의 대상 키 (스웨거 `Pin.id`) |
| `placeName` | `String` | ✅ | 카드 장소명 (spec 유저 플로우 1 step 1) |
| `address` | `String` | ✅ | 카드 주소 |
| `imageUrls` | `List<String>` | ✅ | 대표 이미지. 카드는 **2칸 그리드**로 그린다 (스웨거 `Pin.images`) |
| `label` | `PlaceCategoryLabel` | ✅ | 장소분류 라벨 (spec FR-009) — **현재 API 미제공, [research.md](./research.md) D6 갭** |
| `registrant` | `Registrant?` | ⭕ | 등록자. 카드에는 **아바타 이미지만** 쓰고 이름은 표시하지 않는다 (스웨거 `Pin.createdBy`가 nullable) |

**검증 규칙**
- `placeName`·`address`는 빈 문자열을 허용하지 않는다. 카드가 빈 칸으로 그려지면 안 된다.
- `imageUrls`는 비어 있을 수 있다(이미지 없는 장소). 카드는 이미지 슬롯을 placeholder로 그린다 — 현재 `HomeCard(imageCount = 2)` 구현이 이미 그렇게 동작한다.
- `pinId`는 한 덱 안에서 유일하다. 덱 구성 시 중복 `pinId`는 앞의 것만 남긴다.

> **저장 경과일 필드는 두지 않는다.** spec 4.2.0·PRD 4.2.0에서 카드 비표시로 확정되었고, 순위 판정값은 서버가 갖는다. 스웨거 `Pin.createdAt`은 도메인 모델에 올리지 않는다 — 쓰는 화면이 없다.

### 1.2 `PlaceCategoryLabel`

장소분류 라벨 4종. **고정 enum**이다(PRD 「장소분류 라벨」).

| 값 | 표기 | 판정 근거(서버 소관) |
|---|---|---|
| `FRIENDS_MOST_VIEWED` | `친구들이 많이 본 곳` | 클릭수 상위 |
| `MOST_TALKED` | `이야기 많은 곳` | 코멘트수 상위 |
| `MOST_SAVED` | `여럿이 저장한 곳` | 중복 저장 상위 |
| `WORTH_VISITING` | `가볼 만한 곳` | 위 셋에 걸리지 않은 장소의 기본값 |

- **판정 로직은 도메인에 두지 않는다.** spec §3.2 비목표.
- 표시 문구는 도메인이 갖지 않는다. UI 레이어의 기존 `HomeCardCategory`가 `label` 문자열을 갖고 있으므로, feature에서 `PlaceCategoryLabel` → `HomeCardCategory` 매핑만 한다(1:1).
- 「카테고리 필터」(`카페`·`음식점`)와 **다른 축**이다. 같은 타입으로 합치지 않는다.

### 1.3 `Registrant`

| 필드 | 타입 | 필수 |
|---|---|---|
| `userId` | `String` | ✅ |
| `avatarUrl` | `String?` | ⭕ |

닉네임은 두지 않는다 — 카드가 이름을 표시하지 않기 때문이다(spec §2.3). 다른 화면이 닉네임을 필요로 하면 그 화면의 모델이 갖는다.

---

## 2. Repository 인터페이스 (`:core:domain`)

UseCase 생성 여부의 판단 기준은 [`core/domain/README.md`](../../../core/domain/README.md) §4를 따른다. 아래 오퍼레이션은 **단일 Repository 호출로 끝나고 조합·가공이 없으므로 UseCase를 만들지 않는다.**

### 2.1 `CardFeedRepository`

```
suspend fun getCards(roomId: String): List<PlaceCard>
suspend fun recordAccess(pinId: String)
```

| 오퍼레이션 | 대응 API | 근거 |
|---|---|---|
| `getCards` | `GET /api/v1/rooms/{roomId}/cards` | 덱을 채울 목록. 서버가 최대 10개를 주고 이미 본 카드를 제외한다([research.md](./research.md) D7) |
| `recordAccess` | `POST /api/v1/pins/{pinId}/accesses` | 카드 확인 기록. 서버 큐레이션의 제외 조건이자 `친구들이 많이 본 곳` 집계 원천 |

**호출 주체는 카드덱이 아니라 홈 화면 셸이다.** 카드덱은 콜백으로 신호만 보낸다([contracts/card-deck-component.md](./contracts/card-deck-component.md)).

**[TBD]** `getCards`가 정렬 기준 파라미터를 받는가? 현재 스웨거는 `RoomId`만 받고 정렬 파라미터가 없다. 정렬 칩·자동 정렬 전환은 홈 셸 이관분이므로 **이 시그니처는 홈 셸 plan에서 확장될 수 있다.** 카드덱은 영향받지 않는다.

### 2.2 이번 설계에 포함하지 않는 것

| 오퍼레이션 | 대응 API | 소관 |
|---|---|---|
| 다른 방 복제 저장 | `POST /pins/{pinId}/duplicate` | 홈 화면 셸 ([research.md](./research.md) D8) |
| 방 목록 조회 | `GET /rooms` | 홈 화면 셸(「홈 방 시트」) |
| 정렬·방 전환 | — | 홈 화면 셸 |

---

## 3. UI 상태 모델 (`:feature:home`)

카드덱은 ViewModel을 갖지 않는다([research.md](./research.md) D1). 아래는 컴포넌트가 보유하는 **UI 상태**이며, 화면 상태(`UiState`)가 아니다.

### 3.1 `CardDeckState`

| 프로퍼티 | 타입 | 의미 | 근거 |
|---|---|---|---|
| `cards` | `List<PlaceCard>` | 현재 덱. 입력 목록에서 최대 10장 | FR-006 |
| `currentIndex` | `Int` | 최상단 카드 위치 | FR-001·002 |
| `undoneCard` | `PlaceCard?` | 직전에 넘긴 카드 1장. 되돌리기 대상 | FR-002, D4 |
| `hiddenPinIds` | `Set<String>` | `장소 가리기`로 현재 덱에서 뺀 장소 | FR-005 |
| `isAnimating` | `Boolean` | 전환 애니메이션 진행 여부. `true`면 입력을 무시 | UX-001 |

**파생값** (별도 저장하지 않는다)
- `remainingCount` = `cards.size - currentIndex - hiddenPinIds` 반영분 → **2 이하면 `장소 더 보기` 노출**(FR-007)
- `canUndo` = `undoneCard != null` → `false`면 우→좌 스와이프가 무동작(EC-001)

**상태 전이**

| 현재 | 사건 | 다음 | 비고 |
|---|---|---|---|
| 카드 노출 | 좌→우 스와이프(임계값 이상) | `currentIndex + 1`, `undoneCard` = 넘긴 카드 | FR-001. 확인 신호를 밖으로 보냄 |
| 카드 노출 | 좌→우 스와이프(임계값 미만) | 변화 없음 | EC-002 |
| 카드 노출 | 좌측 영역 스와이프 | 변화 없음 | FR-003 |
| `canUndo` | 우→좌 스와이프 | `currentIndex - 1`, `undoneCard` = null | FR-002 |
| `!canUndo` | 우→좌 스와이프 | 변화 없음 | EC-001 |
| `isAnimating` | 모든 스와이프 | 변화 없음 | UX-001 |
| 카드 노출 | `장소 가리기` | `hiddenPinIds + pinId` | FR-005 |
| 임의 | 새 목록 주입 | 전체 초기화(`currentIndex`=0, `undoneCard`=null, `hiddenPinIds`=∅) | FR-008 |

**영속성**: `rememberSaveable`로 프로세스 재생성에서 살아남되, 서버에 저장하지 않는다(spec §4 — "클라이언트 로컬 상태로만 관리"). `PlaceCard`가 `Parcelable`/`Serializable`이 아니어도 되도록 **`currentIndex`·`hiddenPinIds`·`undoneCard`의 `pinId`만** 저장하고 카드 본문은 재주입된 목록에서 복원한다.
