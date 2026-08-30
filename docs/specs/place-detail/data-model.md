# 데이터 모델: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](./plan.md)

이 문서는 현재 설계 상태만 담는다. 서버 스키마의 원문과 대조 결과는 [contracts/place-api.md](./contracts/place-api.md)·[contracts/comment-api.md](./contracts/comment-api.md)가 소유한다.

---

## 1. `PlaceDetail` — `:core:domain`, 신규

장소 상세 화면이 그리는 핀 하나. **핀은 (장소, 방) 쌍**이므로 같은 장소가 여러 방에 있으면 서로 다른 `PlaceDetail`이 된다([research.md D4](./research.md)).

| 필드 | 타입 | 근거·비고 |
|---|---|---|
| `pinId` | `String` | 화면의 식별자이자 모든 서버 호출의 키 |
| `roomId` | `String` | **「지금 보고 있는 방」**(FR-027). 별도 필드로 두지 않고 이 값이 그 역할을 한다 |
| `placeId` | `String` | 방 목록의 `hasPlace` 판정(FR-018·FR-022)에 쓰는 키 |
| `name` | `String` | 장소명 (FR-003·FR-004) |
| `address` | `String` | 주소 (FR-003·FR-004) |
| `location` | `GeoPoint` | 지도 카메라·마커 (FR-002). `:core:common:kotlin` 소유 타입 |
| `imageUrls` | `List<String>` | 대표 이미지 캐러셀 (FR-007). 없으면 빈 목록 — 캐러셀 영역 자체를 감춘다(EC-009) |
| `registrant` | `PlaceRegistrant?` | 등록자 아바타·닉네임 (FR-003). 서버가 `nullable`이라 없을 수 있고, 그때 기본 아바타를 그린다(EC-004) |
| `sourceUrl` | `String?` | 원문 링크 (FR-017). `null`이면 [원문보기]를 비활성으로 둔다(EC-017) |
| `mapUrl` | `String?` | 외부 지도 앱·브라우저 목적지 (FR-016) |
| `label` | `PlaceLabel` | 장소분류 라벨 (FR-005). **서버가 주지 않아 항상 기본값이다** — [research.md D12](./research.md) |

**방 대표 색상을 담지 않는다.** 핀 상세 응답에 없어 이 모델이 만들어낼 수 없다([contracts/place-api.md §1](./contracts/place-api.md)). 마커 색은 방 목록에서 찾아 `PlaceDetailUiState.roomColor`가 들며, 그 조회 경로는 [contracts/place-detail-main-contract.md §5.1](./contracts/place-detail-main-contract.md)이 소유한다. `roomId`는 그 조회의 키로 남는다.

**저장 경과일·카테고리는 담지 않는다.** 서버 응답에 `place.category`가 있지만 FR-005가 "카테고리는 장소 상세 어디에도 노출하지 않는다"를 명시하므로 모델에 올리지 않는다. 저장 경과일은 [spec.md §3.2](./spec.md)대로 어느 화면에도 없다.

### 1.1 `PlaceRegistrant` — 중첩 값

| 필드 | 타입 | 비고 |
|---|---|---|
| `userId` | `String` | |
| `nickname` | `String` | |
| `avatarColor` | `RoomColor?` | 서버가 `avatar: { color }`로 준다. 기존 `RoomColor` enum이 같은 13색 팔레트라 재사용한다 |

> **서버 계약 불일치 주의**: 같은 사용자 아바타를 엔드포인트마다 다르게 표현한다 — 핀 상세·코멘트는 `avatar: { color }`, 홈 카드와 이 저장소의 `Profile.avatarId`는 `avatar: { id: integer }`다. 협의 항목으로 [contracts/place-api.md](./contracts/place-api.md) §4에 실었다.

## 2. `PlaceComment` — `:core:domain`, 신규

| 필드 | 타입 | 근거·비고 |
|---|---|---|
| `id` | `String` | 삭제 대상 지목 (FR-015) |
| `content` | `String` | 본문. 서버 스키마가 `minLength 1, maxLength 200`이라 FR-012의 200자 상한과 일치 |
| `author` | `PlaceCommentAuthor` | 프로필 이미지·닉네임 (FR-010) |
| `canDelete` | `Boolean` | **[⋮] 노출 여부의 유일한 근거**([research.md D6](./research.md)) |

**작성 시각을 담지 않는다.** 서버가 `createdAt`을 주지만 [spec.md §4](./spec.md)가 "코멘트에 작성 시각을 표기하지 않는다"를 가정으로 닫았다. 정렬은 서버가 준 순서를 그대로 쓰므로 클라이언트가 시각으로 다시 정렬할 일이 없다.

### 2.1 `PlaceCommentAuthor` — 중첩 값

| 필드 | 타입 |
|---|---|
| `userId` | `String` |
| `nickname` | `String` |
| `avatarColor` | `RoomColor?` |

### 2.2 `PlaceCommentPage` — 페이지네이션 결과

역방향 페이징([research.md D11](./research.md))을 화면이 다루려면 "더 오래된 페이지가 남았는지"가 필요하다.

| 필드 | 타입 | 비고 |
|---|---|---|
| `comments` | `List<PlaceComment>` | 페이지 안에서는 오래된 것이 먼저 — 서버 순서를 그대로 유지한다 |
| `page` | `Int` | 0이 최신 페이지 |
| `hasOlder` | `Boolean` | 더 받을 이전 페이지가 있는지. 서버 `pagination`에서 Mapper가 도출한다 |

## 3. `PlaceLabel` — `:core:domain`, 신규

장소분류 라벨 4종. 서버 `labelGroup` enum과 1:1로 대응시켜 두어, 서버가 핀 상세에 필드를 추가하면 Mapper만 고치면 되게 한다.

| 값 | 서버 `labelGroup` | 표시 문구 |
|---|---|---|
| `WORTH_VISITING` | `worthVisiting` | `가볼 만한 곳` |
| `MANY_COMMENTS` | `manyComments` | `이야기 많은 곳` |
| `MANY_SAVES` | `manySaves` | `여럿이 저장한 곳` |
| `MANY_VIEWS` | `manyViews` | `친구들이 많이 본 곳` |

**기본값은 `WORTH_VISITING`이다**(EC-005·TS-009). 표시 문구는 도메인이 갖지 않는다 — 문자열 리소스는 feature가 소유한다.

## 4. `RoomSummary` — `:core:domain`, **기존 타입 수정**

[research.md D9](./research.md)에 따라 필드 하나를 늘린다.

| 필드 | 변경 | 비고 |
|---|---|---|
| `hasPlace` | **추가** `Boolean?` | `null` = 판정하지 않음(기존 호출자), `true` = 그 방에 이 장소가 이미 있음. 기본값 `null`이라 `:feature:sharereceiver`와 `GetRoomPickerRoomsUseCase`는 고치지 않는다 |

기존 필드(`id`·`name`·`description`·`type`·`color`·`placeCount`·`thumbnailImageUrls`)는 그대로다. 이 타입이 두 시트를 모두 덮는다 — [다른방에 공유](FR-018)와 [저장된 방](FR-024, 이번 범위 보류).

## 5. 화면 상태 타입 — `:feature:placedetail`, 신규

도메인이 아니라 UI 상태다. `:core:domain`에 올리지 않는다([core/domain README](../../../core/domain/README.md) — 비즈니스 개념만 domain).

| 타입 | 값 | 근거 |
|---|---|---|
| `PlaceSheetLevel` | `HALF` · `FULL` | FR-001. `Peek`이 없다는 것이 [spec.md §4](./spec.md)의 명시적 가정 |
| `PlaceHeaderMode` | `EXPANDED` · `COLLAPSED` | FR-008. `sheetLevel`이 아니라 **콘텐츠 스크롤 위치**가 결정한다([research.md D5](./research.md)) |

## 6. 상태 전이

### 6.1 시트 단계

```text
(진입) ──> HALF ──위로 드래그──> FULL
             ^                    │
             └──아래로 드래그─────┘
             │
             └──아래로 드래그──> (닫힘 = 나가기, EC-003)
```

`Peek` 단계로 머무는 중간 상태가 없다(TS-015). 드래그를 놓으면 `HALF` 유지 또는 닫힘 중 하나로 귀결된다.

### 6.2 헤더 모드

```text
FULL & 스크롤 최상단  ──> EXPANDED
FULL & 스크롤 내려감  ──> COLLAPSED
콘텐츠가 화면보다 짧음 ──> EXPANDED 고정 (EC-007)
```

### 6.3 코멘트 목록

```text
(진입) ──> page 0 조회 ──> 목록 = [page 0]            (최신이 아래)
   위로 스크롤 & hasOlder ──> page N+1 조회 ──> 목록 = [page N+1] + 기존
   등록(FR-014) ──────────> 목록 = 기존 + [새 코멘트]  (맨 아래)
   삭제(FR-015) ──────────> 해당 항목 제거. 0건이 되면 빈 상태(EC-014)
```
