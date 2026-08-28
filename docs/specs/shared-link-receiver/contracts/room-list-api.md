# 계약: 방 목록 조회 API

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](../spec.md) · **계획**: [plan.md](../plan.md)

방 선택 시트가 카드 목록을 그리기 위해 쓰는 계약(FR-005·FR-006). **현행 API가 필요한 필드를 전부 갖는다** — plan 2.x가 서버 확장으로 요청했던 썸네일 항목이 `thumbnailList`로 붙었다([research.md R-022](../research.md)). 이 feature는 이 엔드포인트를 실서버로 붙인다 — mock을 쓰지 않는 근거는 [research.md R-015](../research.md).

---

## 1. 현행 계약

2026-08-27 `https://api.gguk.org/api-docs-json`(`Team MINO API` 1.0.0) 기준. plan 1.0.0에서 확인한 스키마와 같다.

```
GET /api/v1/rooms
Authorization: Bearer <익명 세션 ID 토큰>

200:
{
  "data": [
    {
      "id": "<uuid>",
      "type": "personal" | "shared",
      "name": "맛집 탐방",
      "description": string | null,
      "color": "red",                      // 13색 팔레트 키(snake_case), 개인방 기본 gray
      "ownerId": "<uuid>",
      "createdAt": "<date-time>",
      "pinCount": 12,
      "memberCount": 3,
      "thumbnailList": ["<url>", ...],     // §2
      "hasPlace": boolean | null,          // ?showHasPlaceId= 지정 시에만 — 쓰지 않는다(§3)
      "users": [...] | null                // ?showUsers=true 지정 시에만 — 쓰지 않는다(§3)
    }
  ]
}
401: UNAUTHORIZED | TOKEN_EXPIRED | USER_NOT_REGISTERED
```

- 나간 방은 응답에서 제외된다.
- `?showHasPlaceId=` · `?showUsers=true` 쿼리는 **쓰지 않는다** — 아래 §3 참고. 붙이지 않으면 `hasPlace`·`users`가 응답에 포함되지 않는다.
- `color`는 **13색 enum**(`red`·`red_orange`·`orange`·`lime`·`green`·`cyan`·`violet`·`pink`·`blue`·`brown`·`light_blue`·`purple`·`gray`)이며 `RoomMapper`의 기존 대응표와 일치한다. 실제 색 매핑은 클라이언트가 갖는다.

### 1.1 필드 대응

| 응답 필드 | 도메인 (`RoomSummary`) | 요구사항 |
|---|---|---|
| `id` | `id` | FR-010 |
| `name` | `name` | FR-006 |
| `description` | `description` (`null` → `""`) | FR-006 |
| `type` | `type` (`personal`→`PERSONAL`, 그 외→`GROUP`) | FR-005 |
| `color` | `color` | FR-006 (썸네일 폴백) |
| `pinCount` | `placeCount` | FR-006 |
| `thumbnailList` | `thumbnailImageUrls` (**URL만 남기고 색상 키는 버린다** — §2) | FR-006 |
| `ownerId` · `createdAt` · `memberCount` · `hasPlace` · `users` | **매핑하지 않는다** | 시트가 쓰지 않는다 |

---

## 2. 썸네일 — `thumbnailList`는 두 가지를 담는다

FR-006이 요구하는 방 썸네일의 이미지가 이 필드로 온다. plan 2.x가 `thumbnailImageUrls`라는 이름으로 서버 확장을 요청했던 항목이며, **실제 이름과 의미가 다르다.**

```
thumbnailList: string[]
  "최근 핀 최대 4개의 장소 대표 이미지 URL(최신순).
   저장된 핀이 없으면 방장 아바타 색상 키 1개."
```

| 방의 상태 | 서버가 주는 것 | 클라이언트 처리 |
|---|---|---|
| 저장된 핀이 있다 | 대표 이미지 URL 최대 4장(최신순) | 그대로 콜라주에 쓴다 |
| 저장된 핀이 없다 | **색상 키 1개**(`red`·`gray` 같은 문자열) | **버린다.** 빈 목록이 되어 폴백이 그려진다 |

- **색상 키를 도메인으로 올리지 않는다.** URL이 아닌 문자열이 `MinoRoomThumbnail`에 닿으면 이미지로 로드하려 한다. 판정과 폐기는 `RoomSummaryMapper`가 하며, 판정 기준은 URL 스킴(`http://`·`https://`)이다 — 근거는 [research.md R-022](../research.md).
- 색상 키를 버려도 정보가 사라지지 않는다. 같은 내용이 `color` 필드에 있고, 폴백은 그 값으로 그려진다([research.md R-019](../research.md)).
- 4장을 넘겨 내려줘도 `RoomSummaryMapper`가 앞 4장만 쓴다.

## 3. 쓰지 않는 쿼리 파라미터와 그 이유

| 파라미터 | 쓰지 않는 이유 |
|---|---|
| `?showHasPlaceId=` | 특정 장소가 이미 저장된 방인지를 내려주지만, **이 시트는 링크 분석 전이라 대상 장소가 무엇인지조차 모른다**(FR-017). FR-016이 중복 방도 구분 없이 선택 가능하게 두기로 확정했으므로 판정 자체가 불필요하다 |
| `?showUsers=true` | 멤버 목록을 내려주지만 FR-006이 "멤버 아바타는 넣지 않는다"로 명시 제외한다 |

두 파라미터를 붙이지 않는 것은 응답 크기와 서버 조인을 줄여 SC-001(1초 이내 표출)에도 유리하다.

---

## 4. 정렬

**서버에 정렬을 요구하지 않는다.** 개인방 최상단 고정(FR-005)은 `GetRoomPickerRoomsUseCase`가 `type`으로 판정한다. 공동방 사이의 순서는 서버가 준 순서를 그대로 쓴다 — spec §4 가정이 "PRD와 디자인 어디에도 정렬 기준이 없으므로 앱이 방 리스트에서 쓰는 기본 순서를 그대로 쓴다"로 확정했다.

---

## 5. 실패 처리

| 상황 | 클라이언트 처리 |
|---|---|
| `401` (세션 없음·만료·미등록) | 빈 목록 시트로 수렴 (FR-013) |
| 네트워크 오류·오프라인 | 빈 목록 시트로 수렴 (FR-013) |
| `5xx` | 빈 목록 시트로 수렴 (FR-013) |

세 경우를 사용자에게 구분해 보이지 않는다. 근거는 [research.md R-006](../research.md)과 spec §4 가정("복원할 세션이 없어 방 목록을 조회할 수조차 없는 경우와 세션은 있으나 방이 0개인 경우를 사용자에게 구분해 보이지 않는다")이다.

> **요청 전제.** `MinoIdentityProofPlugin`은 신원 증명이 없으면 `checkNotNull`로 즉시 실패한다(도메인 예외가 아니라 프로그래머 버그로 전파). 따라서 **세션 확인(`currentUserId()`)이 이 요청보다 먼저 끝나야 한다**([research.md R-012](../research.md)).

---

## 6. 클라이언트 측 인터페이스

계층별 작성 규칙은 [`core/data/README.md`](../../../../core/data/README.md) §4·§5가 소유한다. 이 계약이 확정하는 것은 시그니처뿐이다.

응답은 `{ "data": [...] }`로 감싸여 온다. 봉투를 벗기는 자리는 `ApiService`이며, 그 위 계층은 봉투를 모른다([research.md R-018](../research.md)).

```
// :core:data/network/dto/response/RoomSummaryResponse.kt (@Serializable)
// :core:data/network/service/RoomApiService.kt (internal)
suspend fun listRooms(): List<RoomSummaryResponse>   // MinoResponse<List<RoomSummaryResponse>>.data

// :core:data/datasource/RoomListRemoteDataSource.kt (internal)
suspend fun listRooms(): List<RoomSummaryResponse>
```

**기존 `RoomRemoteDataSource`에 함수를 더하지 않는다.** 그쪽은 `group-room-form`이 확정한 mock 바인딩(`RoomMockRemoteDataSourceImpl`)을 물고 있어 `getRoom`·`createRoom`·`updateRoom`이 함께 실서버로 넘어가 버린다. 이 목록 조회만을 위한 DataSource를 따로 신설하는 근거는 [research.md R-015](../research.md).

`RoomRepositoryImpl`은 과도기 동안 두 DataSource를 함께 주입받는다 — `getRooms()`는 실서버, 나머지 셋은 mock이다.
