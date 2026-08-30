# 계약: 핀 상세 · 접근 기록 · 복제 · 방 목록 (Place API)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [../plan.md](../plan.md)

**참조 API 문서**: <https://api.gguk.org/api-docs-json> (Team MINO API 1.0.0) — 조회 시점 **2026-08-28T22:54:07+09:00**

이 문서의 스키마 인용은 위 시점의 문서 원문이다. 요약하지 않고 제약(`enum`·`nullable`·`required`)을 그대로 옮긴다.

---

## 1. `GET /api/v1/pins/{pinId}` — 핀 상세 조회

> 설명: "장소 정보(places 컬럼 전체) + 출처 링크 + 저장한 멤버 프로필"

**요청**: 경로 파라미터 `pinId: string`

**응답 200 `data`**

| 필드 | 타입 | 매핑 |
|---|---|---|
| `id` | `string(uuid)` | `PlaceDetail.pinId` |
| `roomId` | `string(uuid)` | `PlaceDetail.roomId` — 「지금 보고 있는 방」 |
| `place.id` | `string(uuid)` | `PlaceDetail.placeId` |
| `place.name` | `string` | `PlaceDetail.name` |
| `place.address` | `string` | `PlaceDetail.address` |
| `place.lat` / `place.lng` | `number` | `PlaceDetail.location: GeoPoint` |
| `place.mapUrl` | `string, nullable` | `PlaceDetail.mapUrl` (FR-016) |
| `place.category` | `string, nullable` | **매핑하지 않는다** — FR-005가 카테고리 미노출을 명시 |
| `place.provider` | `enum(kakao, google)` | 매핑하지 않는다 |
| `place.city` · `district` · `phone` · `providerPlaceId` · `createdAt` · `updatedAt` | | 매핑하지 않는다 |
| `images` | `string[]` | `PlaceDetail.imageUrls` (FR-007). 설명에 "places.images — pins 이동 전 임시 매핑"이라 적혀 있다 |
| `createdBy` | `object, nullable` | `PlaceDetail.registrant` (FR-003). `null`이면 기본 아바타(EC-004) |
| `createdBy.userId` / `nickname` | `string` | |
| `createdBy.avatar.color` | `string, nullable` | `RoomColor`로 매핑 — §4 협의 항목 참고 |
| `sourceUrl` | `string, nullable` | `PlaceDetail.sourceUrl` (FR-017). `null`이면 [원문보기] 비활성(EC-017) |
| `createdAt` | `string(date-time)` | 매핑하지 않는다 — 저장 경과일 미노출 |

**어긋남**: 응답에 **`labelGroup`이 없다.** FR-005가 요구하는 장소분류 라벨을 이 엔드포인트에서 얻을 수 없다 → §4 협의 항목, [research.md D12](../research.md).

**어긋남**: 응답에 **방의 대표 색상이 없다.** `roomId`만 오고 그 방의 `color`가 없어 마커 색상(FR-002)을 그릴 수 없다. `GET /api/v1/rooms`로 받은 방 목록에서 `roomId`로 찾아 쓴다 — 방 목록은 [다른방에 공유] 시트를 위해 어차피 조회한다(§3).

**오류**: `401`(`UNAUTHORIZED` / `TOKEN_EXPIRED` / `USER_NOT_REGISTERED`). `MinoIdentityProofPlugin`이 헤더를 싣고 `convertDomainException`이 `MinoDomainException`으로 바꾼다 — 기존 `PinApiService`와 같다.

## 2. `POST /api/v1/pins/{pinId}/accesses` — 접근 기록 (FR-026)

> 설명: "홈 카드 덱의 묵힘 계산과 클릭수 집계의 원천. append-only 로그."

**요청**: 경로 파라미터 `pinId`. 본문 없음.

**응답 200**: `data: { ok: boolean }` — 반환값을 쓰지 않는다.

「경과일 초기화 확인」의 서버 대응이 정확히 이것이다. append-only라 중복 호출이 문제되지 않으므로 EC-023(짧은 간격이라도 횟수를 줄이지 않는다)을 그대로 지킬 수 있다. 실패는 삼킨다([research.md D7](../research.md)).

## 3. `GET /api/v1/rooms` — 방 목록 + 저장 여부 (FR-018 · FR-022)

> 설명: "나간 방은 제외. `?showHasPlaceId=`로 장소 저장 여부, `?showUsers=true`로 멤버 목록 포함."

**요청**: `?showHasPlaceId={placeId}` — `PlaceDetail.placeId`를 싣는다. `showUsers`는 쓰지 않는다(시트 카드에 멤버 아바타를 넣지 않는다).

> **문서 갭**: 이 오퍼레이션의 `parameters`가 `[]`로 비어 있다. 쿼리 파라미터가 `description` 문장에만 있고 스키마로 선언돼 있지 않다 → §4 협의 항목.

**응답 200 `data[]`** — 기존 `RoomSummaryResponse`에 필드 하나가 는다.

| 필드 | 타입 | 매핑 |
|---|---|---|
| `id` · `name` · `description` · `type` · `color` · `pinCount` · `thumbnailList` | | 기존 `RoomSummary` 매핑 그대로([room-list-api.md](../../shared-link-receiver/contracts/room-list-api.md)) |
| `hasPlace` | `boolean, nullable` | **`RoomSummary.hasPlace`** — "?showHasPlaceId= 지정 시에만 포함" |
| `users` | `array, nullable` | 쓰지 않는다 |

`hasPlace == true`인 방을 체크·비활성으로 그린다(FR-018). 전부 `true`면 FR-022의 상태가 자연히 만들어진다.

## 4. `POST /api/v1/pins/{pinId}/duplicate` — 다른 방에 공유 (FR-018)

> 설명: "원본 방·모든 대상 방 멤버십 검증. 대상 방 중 하나라도 같은 장소가 있으면 409로 전체 거절."

**요청 본문**

```json
{ "roomIds": { "type": "array", "items": { "type": "string", "format": "uuid" }, "minItems": 1 } }
```

`required: ["roomIds"]`. `minItems: 1`이라 방을 하나도 고르지 않은 요청은 스키마 위반이다 — 시트가 [공유하기]를 비활성으로 막는 규칙(FR-022)과 일치한다.

**응답 200**: `data: { ok: boolean }`

**409 처리**: 시트가 이미 저장된 방을 비활성으로 막으므로 정상 흐름에서는 나오지 않는다. 다른 기기에서 먼저 저장된 경합에서 발생할 수 있고, 공통 에러 경로로 흘린다([research.md D14](../research.md)).

## 5. 서버팀 협의 항목

이 절이 이번 대조에서 **spec을 따를지 서버 제약을 따를지 설계가 혼자 정할 수 없는** 지점이다.

| # | 항목 | spec 요구 | 서버 현황 | 요청 |
|---|---|---|---|---|
| 1 | **저장된 방의 `pinId`** | FR-024·FR-025 — 방 카드를 누르면 그 방 기준으로 갱신 | `?showHasPlaceId=` 응답이 `roomId`·`hasPlace`만 준다. 옮겨 갈 핀을 특정할 수 없다 | `hasPlace: true`인 방에 그 방의 `pinId`를 함께 실어 달라 |
| 2 | **핀 상세의 `labelGroup`** | FR-005 — 헤더에 장소분류 라벨 4종 중 부여된 값 표시 | `labelGroup`이 `GET /rooms/{roomId}/cards`에만 있다 | `GET /pins/{pinId}` 응답에 `labelGroup`을 추가해 달라 |
| 3 | **아바타 표현 불일치** | FR-003·FR-010 — 등록자·작성자 프로필 이미지 | 같은 아바타를 엔드포인트마다 다르게 준다: 핀 상세·코멘트는 `avatar: { color }`, 홈 카드와 `GET /users/me`는 `avatar: { id: integer }` | 어느 쪽이 정본인지 확정해 달라. 이 저장소의 `Profile.avatarId: Int`는 `{id}` 쪽을 따르고 있다 |
| 4 | **쿼리 파라미터 미선언** | — | `GET /rooms`의 `showHasPlaceId`·`showUsers`, `GET /pins`의 `roomId`·`page`·`pageSize`가 `description` 문장에만 있고 `parameters: []`다 | 스키마에 선언해 달라. 계약 근거가 문장뿐이라 타입·필수 여부를 확정할 수 없다 |

1·2번이 닫히기 전까지 FR-023~FR-025는 구현 보류, FR-005는 기본값 고정이다([research.md D10·D12](../research.md)).
