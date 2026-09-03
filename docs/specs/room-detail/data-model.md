# 데이터 모델: 방 상세 (Room Detail)

**대상 spec**: [spec.md](./spec.md) 2.1.3 · **대상 plan**: [plan.md](./plan.md) · 근거: [research.md](./research.md) D4·D5·D6·D7·D8·D14·D15·D16

이 문서는 현재 설계 상태만 담는다(개정 시 대체). 실제 필드 목록은 구현 단계에서 API 계약이 확정되면 좁혀질 수 있다.

---

## 1. `:core:domain` 도메인 모델

### `Place` (신규)

```kotlin
data class Place(
    val id: String,                          // 서버 Pin.id(핀 식별자) — Place.id(장소 식별자)가 아니다. 공유·삭제 호출은 이 값을 쓴다([research.md D14](./research.md))
    val placeId: String,                     // 서버 Pin.place.id(장소 식별자) — 방마다 다른 [id]와 달리 같은 장소면 어느 방에서든 같다
    val name: String,                        // 서버 Pin.place.name
    val address: String,                     // 서버 Pin.place.address
    val category: PlaceCategoryFilter,       // room-list가 정의(재사용) — 서버 Pin.place.category(자유 문자열)를 매핑. CAFE/RESTAURANT만 실값, ALL은 필터 전용
    val thumbnailUrl: String?,               // 서버 Pin.images.firstOrNull()
    val savedAt: Instant,                    // "최신순" 정렬 근거([FR-005]) — 서버 Pin.createdAt
    val commentCount: Int,                   // "코멘트순" 정렬 근거([FR-005]) — 서버 응답에 없음, [TBD](§4)
    val isGgukPick: Boolean,                 // "꾹 Pick" 정렬 근거([FR-005]) — 서버 응답에 없음, [TBD](§4)
    val distanceMeters: Double?,             // "거리순" 정렬 근거([FR-005]). 현재 위치 미확보 시 null. 서버 Pin.place.lat/lng와 클라이언트 위치로 계산(서버 필드 아님)
)
```

- **`id`와 `placeId`가 둘 다 있는 이유**: 두 식별자가 서로 다른 질문에 답한다. `id`(핀)는 「이 방에 담긴 이 한 장」을 가리켜 공유·삭제가 쓰고, `placeId`(장소)는 「같은 장소」를 가리켜 **어느 방에 이미 담겨 있는지**를 묻는 데 쓴다(`RoomRepository.getRooms(placeId)` → `hasPlace`). 핀 id로는 그 질문을 할 수 없다 — 방마다 값이 다르기 때문이다. [SYS-003] 방 선택 시트의 「체크된 채 비활성」([spec.md](./spec.md) EC-004)이 이 값을 요구한다.
- **범위**: 장소 카드/리스트 렌더링에 필요한 필드로 한정한다. 코멘트 본문·이미지 갤러리 등 [SCR-006] 장소 상세 자체의 구성은 이 spec의 비목표([spec.md §3.2](./spec.md))라 담지 않는다.
- **서버 응답과의 관계**: 서버는 `Place`를 단독으로 내려주지 않고 `Pin { id, roomId, place: { id, provider, providerPlaceId, name, address, city, district, lat, lng, category, phone, mapUrl, createdAt, updatedAt }, images, createdBy, createdAt }`이 감싼다(`GET /api/v1/pins`·`GET /api/v1/pins/{pinId}`, [research.md D14](./research.md)). 이 domain `Place`는 **Pin과 그 안의 place를 합쳐 만든 표현 모델**이다 — `id`는 `Pin.id`를 쓴다(`Pin.place.id`가 아니다), 나머지 필드는 위 주석대로 매핑한다. Mapper(`PlaceMapper.toDomain()`)가 이 변환을 전담한다.
- **검증 규칙**: `distanceMeters`는 사용자 위치 권한이 없거나 미확보 상태면 `null`이고, 이 경우 "거리순" 정렬에서 해당 장소는 목록 하단으로 밀린다(구현 세부, 정렬 알고리즘 자체는 `/mino-task`가 정한다).
- **관계**: `Place` N — 1 `Room`(`roomId`는 이 모델에 없다 — `PlaceRepository.observePlaces(roomId)`가 이미 방 단위로 스코프하므로 각 `Place`가 자신의 `roomId`를 알 필요가 없다, [contracts/place-repository.md](./contracts/place-repository.md) 참조).

### `RoomMember` (신규, [research.md D16](./research.md))

```kotlin
data class RoomMember(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val isOwner: Boolean,
    val joinedAt: Instant,
)
```

- **범위**: `GET /api/v1/rooms/{roomId}/members` 응답 그대로 매핑([research.md D15·D16](./research.md)). [FR-011] 초대 시트의 참여자 전체 목록과 [FR-013] 방장 위임 대상 선택 모달이 함께 소비한다 — 두 화면이 같은 서버 응답을 쓰므로 타입을 하나로 합친다(헌법 원칙 I).
- **`RoomMemberSummary`(room-list, 재사용 아님)와의 차이**: `RoomMemberSummary`는 방 카드에 그리는 "최대 4개 아바타 + overflow count" 축약 표현이고, `RoomMember`는 이름·역할·가입일을 포함한 전체 목록 항목이다. 서로 다른 API 응답(카드 목록 vs 멤버 목록)에서 오므로 하나를 다른 하나로 파생시키지 않는다.

### 재사용 (신규 정의 없음)

- `MapMarkerSortOption`(`:core:domain`) — [FR-005] 정렬 드롭다운. room-list가 정의([room-list/data-model.md §1](../room-list/data-model.md)). room-detail은 `research.md D4`에 따라 그대로 재사용한다.
- `PlaceCategoryFilter`(`:core:domain`) — [FR-006] 카테고리 칩. room-list가 정의([room-list/data-model.md §1](../room-list/data-model.md)). 위 `Place.category`도 이 타입을 그대로 쓴다.
- `BottomSheetLevel`(`feature/room/main/model/BottomSheetLevel.kt`) — 시트 3단계. room-list가 정의(`:core:domain`이 아니라 `:feature:room` 모듈 내부). `research.md D5`에 따라 그대로 재사용한다.

---

## 2. `:feature:room/detail/` UI 상태

이 절은 domain이 아니라 화면 상태다. `RoomDetailUiState`·`Intent`·`SideEffect`·분기 규칙의 공식 계약은 [contracts/room-detail-main-contract.md](./contracts/room-detail-main-contract.md)가 소유한다 — 아래는 그 계약이 참조하는 보조 enum만 남긴다(중복 정의 금지).

### `PlaceViewType` (enum, 신규)

```kotlin
enum class PlaceViewType { LIST, CARD }
```

- [FR-007] 리스트형/카드형 뷰 토글의 상태. domain이 아니라 순수 UI 상태다([research.md D6](./research.md) — `core/domain/README.md` §3 기준 비즈니스 개념이 아님).

---

## 3. Repository 계약 참조

인터페이스 시그니처는 [contracts/place-repository.md](./contracts/place-repository.md) 참조 — 중복 기술하지 않는다. 방 자체 데이터(`Room`)와 멤버·나가기·위임·초대([research.md D15·D16](./research.md)로 `RoomRepository` 확장)는 room-list의 [data-model.md](../room-list/data-model.md)를 SSOT로 삼되, 이 plan이 추가하는 메서드는 [contracts/place-repository.md](./contracts/place-repository.md) 하단 "`RoomRepository` 확장" 절이 정의한다.

## 4. `[TBD]` 항목

plan 2.0.0의 서버 API 대조([research.md D14~D16](./research.md))로 대부분 해소됐다. 남는 것은 서버가 실제로 아직 제공하지 않는 한 가지뿐이다.

- **`Place.commentCount`·`Place.isGgukPick`의 서버 원천** — "코멘트순"·"꾹 Pick" 정렬([FR-005])의 근거 필드가 `GET /api/v1/pins` 응답에 없다(댓글 수·꾹 Pick 판정 로직 모두 서버 미노출). 구현 시 임시 목데이터/플레이스홀더로 채운다([contracts/place-repository.md](./contracts/place-repository.md) "DTO 갭 대응" 참고).

`RoomFormLauncher` 편집 모드의 extra 키·result 스키마(서버 `PATCH /rooms/{roomId}` 자체는 이미 있음)는 클라이언트 크로스 feature 계약 문제라 이 목록에 포함하지 않는다 — [research.md NEEDS CLARIFICATION 해소 현황](./research.md) 참고.
