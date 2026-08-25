# 데이터 모델: 방 상세 (Room Detail)

**대상 spec**: [spec.md](./spec.md) 2.1.3 · **대상 plan**: [plan.md](./plan.md) · 근거: [research.md](./research.md) D4·D5·D6·D7·D8

이 문서는 현재 설계 상태만 담는다(개정 시 대체). 실제 필드 목록은 구현 단계에서 API 계약이 확정되면 좁혀질 수 있다.

---

## 1. `:core:domain` 도메인 모델

### `Place` (신규)

```kotlin
data class Place(
    val id: String,
    val name: String,
    val address: String,
    val category: PlaceCategoryFilter,      // room-list가 정의(재사용) — CAFE/RESTAURANT만 실값, ALL은 필터 전용
    val thumbnailUrl: String?,
    val savedAt: Instant,                   // "최신순" 정렬 근거([FR-005])
    val commentCount: Int,                  // "코멘트순" 정렬 근거([FR-005])
    val isGgukPick: Boolean,                // "꾹 Pick" 정렬 근거([FR-005]) — PRD 「꾹 Pick」 정의를 그대로 따름
    val distanceMeters: Double?,             // "거리순" 정렬 근거([FR-005]). 현재 위치 미확보 시 null
)
```

- **범위**: 장소 카드/리스트 렌더링에 필요한 필드로 한정한다. 코멘트 본문·이미지 갤러리 등 [SCR-006] 장소 상세 자체의 구성은 이 spec의 비목표([spec.md §3.2](./spec.md))라 담지 않는다.
- **검증 규칙**: `distanceMeters`는 사용자 위치 권한이 없거나 미확보 상태면 `null`이고, 이 경우 "거리순" 정렬에서 해당 장소는 목록 하단으로 밀린다(구현 세부, 정렬 알고리즘 자체는 `/mino-task`가 정한다).
- **관계**: `Place` N — 1 `Room`(`roomId`는 이 모델에 없다 — `PlaceRepository.observePlaces(roomId)`가 이미 방 단위로 스코프하므로 각 `Place`가 자신의 `roomId`를 알 필요가 없다, [contracts/place-repository.md](./contracts/place-repository.md) 참조).

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

인터페이스 시그니처는 [contracts/place-repository.md](./contracts/place-repository.md) 참조 — 중복 기술하지 않는다. 방 자체 데이터(`Room`·`RoomRepository`, 방 멤버 카드 미리보기용 `RoomMemberSummary`)는 room-list의 [data-model.md](../room-list/data-model.md)를 SSOT로 그대로 재사용한다.

## 4. `[TBD]` 항목

아래는 spec이 요구사항으로는 확정했지만, 그 요구사항을 만족시킬 실제 데이터 원천이 이 저장소에 아직 정의돼 있지 않아 이 plan이 채우지 못하는 지점이다([research.md D10·D11·D12](./research.md) 참조).

- **참여자 전체 목록 타입**([FR-011] 초대 시트 참여자 목록) — room-list의 `RoomMemberSummary`는 카드 미리보기(최대 4개 아바타 + overflow count)용이라 이름·역할을 포함한 전체 목록에는 부족하다. `RoomMember`(가칭) 같은 신규 타입이 필요할 수 있으나, 이 spec 범위(§3.2 "초대 링크 생성·공유 로직 자체는 [SYS-006]이 정의")를 벗어나 정의하지 않는다.
- **초대 링크 발급 결과 타입**([FR-011]) — 클립보드 복사·OS 공유 시트에 넘길 실제 링크 문자열의 출처(Repository 메서드 시그니처)가 미정.
- **나가기·권한 위임 요청 타입**([FR-013]) — 위임 대상 멤버 선택 후 서버에 보낼 요청 스키마가 미정.
