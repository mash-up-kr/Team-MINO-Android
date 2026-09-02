# 데이터 모델: 장소 상세 & 코멘트 (Place Detail & Comments)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](./plan.md)

현재 상태만 담는다. 과거 형태는 [research.md](./research.md)의 결정 이력이 갖는다.

---

## 0. 이번 개정에서 달라진 것

| 타입 | 변화 | 근거 |
|---|---|---|
| `PlaceLabel` | **삭제** | [D21](./research.md) — spec 4.0.0 FR-005가 라벨 노출을 제거 |
| `PlaceDetail.label` | **삭제** | 같은 항목 |
| `PlaceComment.createdAt` | **추가** | [D22](./research.md) — spec 4.0.0 FR-028 작성 시각 |
| `RoomSummary.hasPlace` | **추가** | [D24](./research.md) — FR-018 공유 시트 |
| `RoomSummary.matchedPinId` | **추가** | [D24](./research.md) — FR-024 저장된 방 전환 |

---

## 1. `PlaceDetail` — 핀 하나

`:core:domain/model/PlaceDetail.kt`. 이미 존재하며 `label` 필드만 걷어낸다.

| 필드 | 타입 | 근거 | 비고 |
|---|---|---|---|
| `pinId` | `String` | 모든 서버 호출의 키 | (장소, 방) 쌍의 식별자 |
| `roomId` | `String` | FR-027 「지금 보고 있는 방」 | 응답 `data.roomId` |
| `placeId` | `String` | FR-018·FR-024 | `showHasPlaceId=` 질의의 키 |
| `name` | `String` | FR-003 | 넘치면 한 줄 말줄임(FR-004) |
| `address` | `String` | FR-003 | 같음 |
| `location` | `GeoPoint` | FR-002 | 응답 `place.lat`/`place.lng` |
| `imageUrls` | `List<String>` | FR-007 | 비면 캐러셀 영역이 사라진다(EC-009) |
| `registrant` | `PlaceRegistrant?` | FR-003·**FR-005** | `null`이면 기본 아바타(EC-004) |
| `sourceUrl` | `String?` | FR-017 | `null`이면 [원문보기] 비활성(EC-017) |
| `mapUrl` | `String?` | FR-016 | `geo:` 실패 시의 웹 대체 후보 |

**`label`을 삭제한다.** spec 4.0.0이 장소 상세에서 라벨 노출을 없앴다. 필드가 남아 있으면 아무도 읽지 않는 값을 Mapper가 계속 채워야 한다.

**방 대표 색을 담지 않는다.** 핀 상세 응답에 없다. 편입 구조에서는 `RoomListViewModel`이 이미 들고 있는 방 목록에서 `roomId`로 찾는다([D23](./research.md)) — 별도 조회를 걸지 않는다.

카테고리·저장 경과일은 서버가 주더라도 담지 않는다(spec §3.2).

### 1.1 `PlaceRegistrant` — 등록자

| 필드 | 타입 | 근거 |
|---|---|---|
| `userId` | `String` | `createdBy.userId` |
| `nickname` | `String` | **FR-005** — 헤더 첫 줄. 서버 상한 2~15자 |
| `avatarColor` | `RoomColor?` | `createdBy.avatar.color`. 13색 팔레트를 `RoomColor`와 공유 |

**spec 4.0.0에서 이 타입의 역할이 커졌다.** 3.0.0까지 `nickname`은 담아만 두고 헤더에 쓰지 않았으나, 이제 헤더 첫 줄의 주인공이다. 타입 자체는 바뀌지 않는다 — 이미 필요한 값을 갖고 있었다.

**방과 무관한 장소 자체의 속성이다.** [저장된 방] 전환(FR-025)으로 보는 방이 바뀌어도 아바타·닉네임은 달라지지 않는다(spec §4 가정).

---

## 2. `PlaceComment` — 코멘트 하나

`:core:domain/model/PlaceComment.kt`. `createdAt`을 더한다.

| 필드 | 타입 | 근거 | 비고 |
|---|---|---|---|
| `id` | `String` | FR-015 | 삭제 키 |
| `content` | `String` | FR-010 | 서버 1~200자 |
| `createdAt` | `kotlin.time.Instant` | **FR-028** | **신규.** 응답 `createdAt`(`date-time`)을 `Instant.parse`로 옮긴다 |
| `author` | `PlaceCommentAuthor` | FR-010 | |
| `canDelete` | `Boolean` | FR-015 | [⋮] 노출 여부의 유일한 근거([D6](./research.md)) |

**`createdAt`은 시각이지 문구가 아니다.** `방금`·`N시간 전`·`N일 전`·`NNNN년 NN월 NN일`로 끊는 구간 판정과 문자열 조립은 feature의 UI 매핑이 한다([D22](./research.md), `core/domain/README.md` §5). 도메인이 표시 문구를 갖지 않는 것은 `PlaceLabel`을 없앤 것과 같은 규칙이다.

**정렬에 쓰지 않는다.** 나열 순서는 서버가 준 그대로다([D11](./research.md)) — 클라이언트가 `createdAt`으로 다시 정렬하지 않는다.

**타입은 `kotlin.time.Instant`다.** 도메인이 이미 그 타입으로 정착해 있다 — `Place.savedAt`·`Room.lastPlaceSavedAt`·`RoomMember.joinedAt`이 모두 같고, `PlaceMapper`가 `Instant.parse(createdAt)`로 서버 문자열을 옮기는 경로까지 선례가 있다. 버전 카탈로그의 `kotlinx-datetime`은 `:core:common:kotlin`의 `implementation`이라 이 모듈로 새지 않는다.

**`@OptIn(ExperimentalTime::class)`이 필요하다.** Kotlin 2.2.10 stdlib에서 `kotlin.time.Instant`가 아직 실험적이고 `build-logic`에 전역 opt-in 설정이 없다. `PlaceComment.kt`에 opt-in을 새로 붙여야 하며(`Place.kt`가 클래스에 `@OptIn`을 붙인 형태를 따른다), 이 타입에 닿는 Mapper·ViewModel도 같다. 기준 시각을 공급하는 `kotlin.time.Clock`도 같은 opt-in 대상이다([D26](./research.md)).

### 2.1 `PlaceCommentAuthor` — 작성자

`PlaceRegistrant`와 필드가 같지만 별개 타입으로 둔다. 두 값이 서로 다른 응답에서 오고, 한쪽이 늘 때 다른 쪽이 끌려가지 않게 한다.

| 필드 | 타입 |
|---|---|
| `userId` | `String` |
| `nickname` | `String` |
| `avatarColor` | `RoomColor?` |

### 2.2 `PlaceCommentPage` — 코멘트 한 페이지

| 필드 | 타입 | 비고 |
|---|---|---|
| `comments` | `List<PlaceComment>` | 페이지 안 순서(오래된 것이 먼저)를 서버가 준 그대로 유지 |
| `page` | `Int` | 0이 최신 페이지 |
| `hasOlder` | `Boolean` | 서버 `pagination`에서 Mapper가 도출 |

역방향 페이징이다 — `page`가 커질수록 더 오래된 코멘트가 온다([D11](./research.md)).

---

## 3. `RoomSummary` — 방 목록 항목 (기존 타입 확장)

`:core:domain/model/RoomSummary.kt`. 두 필드를 늘린다.

| 필드 | 타입 | 상태 | 근거 |
|---|---|---|---|
| `id`·`name`·`description`·`type`·`color`·`placeCount`·`thumbnailImageUrls` | — | 기존 | |
| `hasPlace` | `Boolean?` | **신규** | FR-018 — 공유 시트의 이미 저장된 방 표시 |
| `matchedPinId` | `String?` | **신규** | FR-024 — [저장된 방] 전환 대상 핀 |

**둘 다 nullable이며 `null`은 "물어보지 않았다"를 뜻한다.** `getRooms()`를 `placeId` 없이 부른 호출자(방 리스트 탭·기존 공유 시트)가 `false`를 사실로 오해하지 않게 하는 구분이다([D24](./research.md)). 서버도 `?showHasPlaceId=`를 지정했을 때만 두 필드를 포함한다.

**`placeCount`와 혼동하지 않는다.** `placeCount`는 그 방에 저장된 장소의 수일 뿐, 지금 보고 있는 장소가 그 방에 있는지를 뜻하지 않는다.

---

## 4. 화면이 쥐는 값 — 편입 구조에서의 소유권

편입([D17](./research.md)) 이후 상태가 두 ViewModel에 나뉜다. **어느 쪽이 무엇을 갖는지가 이 표다.**

| 값 | 소유자 | 이유 |
|---|---|---|
| `selectedPinId` | `RoomListViewModel` | 시트 분기를 가르는 값. `selectedRoomId`와 같은 자리에 있어야 세 갈래가 한 곳에서 판정된다 |
| `mapCenter`·`mapCenterRequestId` | `RoomListViewModel` | 지도를 실제로 그리는 주체. 장소 상세가 따로 들면 [현재 위치]가 안 먹는 결함이 재현된다([D25](./research.md)) |
| 방 목록(`List<RoomSummary>`) | `RoomListViewModel` | 이미 들고 있다. 마커 색·[저장된 방] 시트·공유 시트가 함께 쓴다 |
| `PlaceDetail`·코멘트·시트 단계·헤더 밀도·코멘트 초안 | `PlaceDetailViewModel` | `pinId` key의 화면 상태 |

`PlaceDetailViewModel`은 `pinId`를 `@AssistedInject`로 받는다 — `RoomDetailViewModel`이 `roomId`를 받는 방식 그대로다. `pinId`가 바뀌면(= [저장된 방] 전환) 새 인스턴스가 서고, 코멘트 초안이 함께 사라지는 것이 FR-025가 정한 동작이다.

---

## 5. 삭제되는 타입

| 타입 | 사유 |
|---|---|
| `PlaceLabel` (`:core:domain/model/PlaceLabel.kt`) | 소비자가 장소 상세뿐이었고 spec 4.0.0이 그 소비를 없앴다([D21](./research.md)). 홈 카드는 `PlaceCard`가 자기 라벨 표현을 따로 가져 영향이 없다 |
