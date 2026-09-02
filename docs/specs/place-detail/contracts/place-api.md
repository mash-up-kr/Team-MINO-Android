# 계약: 장소(핀) API 대조

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](../plan.md)

**출처**: <https://api.gguk.org/api-docs-json> (Team MINO API 1.0.0, 오퍼레이션 27개)

**조회 시점**: **2026-09-01T21:46:23+09:00**

> plan 1.1.0의 대조 시점은 2026-08-28T22:54:07+09:00이었고 오퍼레이션이 25개였다. **이번 조회에서 두 개가 늘고 `GET /api/v1/rooms`의 응답이 넓어졌다.**

---

## 0. 지난 대조에서 달라진 것

| 항목 | plan 1.1.0 | plan 2.0.0 (이번 조회) |
|---|---|---|
| `GET /api/v1/rooms?showHasPlaceId=` | `hasPlace`만 | **`hasPlace` + `matchedPinId`** → FR-024 구현 가능 |
| `labelGroup` (핀 상세) | 없음 → 서버 협의 항목 | **협의 철회** — spec 4.0.0이 요구사항을 제거 |
| `createdBy.nickname` | 있으나 미사용 | **FR-005의 공급원**이 되었다 |

---

## 1. `GET /api/v1/pins/{pinId}` — 핀 상세 (FR-003·FR-005·FR-007·FR-016·FR-017·FR-027)

**판정: 대응 API 있음.**

```
description: "장소 정보(places 컬럼 전체) + 출처 링크 + 저장한 멤버 프로필"
parameters: pinId (path, required, string)
```

응답 `data`:

```json
{
  "id":     { "type": "string", "format": "uuid" },
  "roomId": { "type": "string", "format": "uuid" },
  "place": {
    "type": "object",
    "description": "places 컬럼 전체 (images 제외 — 핀 응답으로 이동)",
    "properties": {
      "id":              { "type": "string", "format": "uuid" },
      "provider":        { "type": "string", "enum": ["kakao", "google"] },
      "providerPlaceId": { "type": "string" },
      "name":            { "type": "string" },
      "address":         { "type": "string" },
      "city":            { "type": "string", "nullable": true },
      "district":        { "type": "string", "nullable": true },
      "lat":             { "type": "number" },
      "lng":             { "type": "number" },
      "category":        { "type": "string", "nullable": true },
      "phone":           { "type": "string", "nullable": true },
      "mapUrl":          { "type": "string", "nullable": true },
      "createdAt":       { "type": "string", "format": "date-time" },
      "updatedAt":       { "type": "string", "format": "date-time" }
    }
  },
  "images":    { "type": "array", "items": { "type": "string" },
                 "description": "게시물 이미지 (places.images — pins 이동 전 임시 매핑)" },
  "createdBy": {
    "type": "object", "nullable": true,
    "description": "핀을 저장한 멤버 프로필 (\"누가 추가한 곳\" 표시용)",
    "properties": {
      "userId":   { "type": "string", "format": "uuid" },
      "nickname": { "type": "string" },
      "avatar":   { "type": "object", "nullable": true,
                    "properties": { "color": { "type": "string", "example": "red" } } }
    }
  },
  "createdAt": { "type": "string", "format": "date-time" },
  "sourceUrl": { "type": "string", "nullable": true,
                 "description": "출처 링크 (sources.original_url, 단일)" }
}
```

### 1.1 매핑

| 요구사항 | 응답 필드 | 비고 |
|---|---|---|
| FR-003 장소명·주소 | `place.name`·`place.address` | |
| **FR-005 등록자 닉네임** | `createdBy.nickname` | **spec 4.0.0 신규 요구. 대응 있음** |
| FR-003 등록자 아바타 | `createdBy.avatar.color` | `null`이면 기본 아바타(EC-004) |
| FR-002 좌표 | `place.lat`·`place.lng` | |
| FR-007 대표 이미지 | `images` | 비면 캐러셀 영역이 사라진다(EC-009) |
| FR-016 외부 지도 | `place.mapUrl` | `geo:` 실패 시의 웹 대체 후보 |
| FR-017 원문 | `sourceUrl` | `null`이면 [원문보기] 비활성(EC-017) |
| FR-027 지금 보고 있는 방 | `roomId` | **탭 간 진입이 방을 해석하는 근거** |
| FR-018·FR-024 질의 키 | `place.id` | `?showHasPlaceId=`에 싣는다 |

### 1.2 담지 않는 필드

`provider`·`providerPlaceId`·`city`·`district`·`category`·`phone`·`createdAt`·`updatedAt`. 카테고리와 저장 경과일을 장소 상세 어디에도 노출하지 않기로 한 spec §3.2의 규정이다.

**`labelGroup`은 이 응답에 없고, 이제 필요하지도 않다.** plan 1.1.0이 세운 서버 협의 항목을 **철회한다** — spec 4.0.0 FR-005가 장소 상세에서 라벨 노출을 없앴다([research.md D21](../research.md)).

### 1.3 남는 어긋남 — 아바타 표현의 불일치

| 지점 | 서버 |
|---|---|
| `GET /pins/{pinId}` → `createdBy.avatar.color` | `{ "type": "string", "example": "red" }` — **enum 제약이 없다** |
| `GET /pins/{pinId}/comments` → `author.avatar.color` | 13색 `enum` 명시 |

같은 사용자 아바타 색인데 한쪽만 enum이 걸려 있다. **서버팀 협의 항목**이며, 그전까지 Mapper는 두 자리를 같은 13색 팔레트로 해석하고 모르는 값은 `null`로 떨어뜨린다(기본 아바타로 그려진다).

---

## 2. `POST /api/v1/pins/{pinId}/accesses` — 접근 기록 (FR-026)

**판정: 대응 API 있음.** 요약 "핀 접근 기록 (사용자별)".

- 열 때마다 그대로 호출한다. 디바운스·중복 제거를 하지 않는다(EC-023).
- 실패를 삼킨다(EC-022) — Repository 안에서 닫고 화면으로 올리지 않는다([place-repository.md §1](./place-repository.md)).

---

## 3. `POST /api/v1/pins/{pinId}/duplicate` — 다른 방에 공유 (FR-018)

**판정: 대응 API 있음.** 요약 `다른 방에 핀 복제 ("다른 방에 공유")`.

- 대상 방 목록을 싣는다. 서버가 `minItems: 1`이라 빈 목록으로 호출하지 않는다.
- 이미 저장된 방이 섞이면 `409`. 별도 분기를 두지 않고 `MinoDomainException`으로 전파한다([research.md D14](../research.md)).

---

## 4. `GET /api/v1/rooms?showHasPlaceId=` — 방 목록 (FR-018·FR-023·FR-024)

**판정: 대응 API 있음.** plan 1.1.0의 **구현 보류가 이 변경으로 해제된다.**

```
description: "나간 방은 제외. ?showHasPlaceId=로 장소 저장 여부 및 매칭 핀 ID, ?showUsers=true로 멤버 목록 포함."

parameters:
  showHasPlaceId  (query, optional, string/uuid)
      "장소 UUID. 지정하면 각 방에 hasPlace와 matchedPinId를 함께 반환한다."
  showUsers       (query, optional, enum["true","false"])
```

응답 `data[]`에서 이 화면이 쓰는 것:

```json
{
  "id":            { "type": "string", "format": "uuid" },
  "type":          { "type": "string", "enum": ["personal", "shared"] },
  "name":          { "type": "string" },
  "color":         { "type": "string", "enum": ["red","red_orange","orange","lime","green",
                     "cyan","violet","pink","blue","brown","light_blue","purple","gray"],
                     "description": "팔레트 색상 키(13색, snake_case). 개인방 기본은 gray." },
  "pinCount":      { "type": "integer" },
  "thumbnailList": { "type": "array", "items": { "type": "string" } },
  "hasPlace":      { "type": "boolean", "nullable": true,
                     "description": "?showHasPlaceId= 지정 시에만 포함" },
  "matchedPinId":  { "type": "string", "format": "uuid" }
}
```

### 4.1 매핑

| 요구사항 | 응답 필드 |
|---|---|
| FR-018 이미 저장된 방 표시 | `hasPlace` |
| FR-023 [저장된 방] 활성 판정 | `hasPlace == true`인 방이 2개 이상 |
| **FR-024 전환 대상 핀** | **`matchedPinId`** |
| FR-002 마커·방 색 | `color` |

### 4.2 주의

`matchedPinId`는 스키마상 `nullable`로 표시돼 있지 않지만 **`hasPlace == false`인 방에는 의미가 없다.** 도메인에서는 `String?`로 받고 `hasPlace == true`인 방에서만 읽는다([data-model.md §3](../data-model.md)) — 서버가 저장돼 있지 않은 방에 무엇을 싣든 화면이 그것을 전환 대상으로 삼지 않게 하는 방어다.

---

## 5. 대응 API가 없거나 협의가 필요한 지점

| 항목 | 상태 | 처리 |
|---|---|---|
| 아바타 색 enum 불일치(§1.3) | **서버팀 협의** | Mapper가 두 자리를 같은 팔레트로 해석. 모르는 값은 `null` |
| `labelGroup` 부재 | **철회** | spec 4.0.0이 요구사항을 제거. 더 이상 갭이 아니다 |
| 저장된 방 전환용 `pinId` | **해소** | 서버가 `matchedPinId` 신설 |

**이번 개정에서 서버 협의 항목이 둘 줄고 하나 남았다.**
