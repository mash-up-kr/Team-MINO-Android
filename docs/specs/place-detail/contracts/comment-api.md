# 계약: 코멘트 조회 · 작성 · 삭제 (Comment API)

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [../plan.md](../plan.md)

**참조 API 문서**: <https://api.gguk.org/api-docs-json> (Team MINO API 1.0.0) — 조회 시점 **2026-08-28T22:54:07+09:00**

코멘트는 (장소, 방) 단위로 귀속된다(FR-019). 서버가 핀을 그 단위로 다루므로 세 엔드포인트 모두 `pinId`를 경로에 받는다 — 별도의 방 인자가 필요 없다.

---

## 1. `GET /api/v1/pins/{pinId}/comments` — 목록 조회 (FR-010 · FR-011)

> 설명: "최신 페이지부터 가져오며, 각 페이지 안에서는 오래된 코멘트가 먼저 온다."

**요청**

| 파라미터 | 위치 | 필수 | 스키마 |
|---|---|---|---|
| `pinId` | path | 필수 | `string` |
| `page` | query | 선택 | `example: 0` (타입 미선언) |
| `pageSize` | query | 선택 | `maximum: 100` (타입 미선언) |

**응답 200**: `required: [data, pagination]`

`data[]` — `required: [id, content, createdAt, author, canDelete]`

| 필드 | 스키마 | 매핑 |
|---|---|---|
| `id` | `string(uuid)` | `PlaceComment.id` |
| `content` | `string`, `minLength: 1`, `maxLength: 200` | `PlaceComment.content` — FR-012의 200자 상한과 **정확히 일치** |
| `createdAt` | `string(date-time)` | 매핑하지 않는다 — [spec.md §4](../spec.md)가 작성 시각 미표기를 가정으로 닫았다 |
| `author.id` | `string(uuid)` | `PlaceCommentAuthor.userId` |
| `author.nickname` | `string`, `minLength: 2`, `maxLength: 15` | `PlaceCommentAuthor.nickname` |
| `author.avatar` | `object, nullable`, `required: [color]` | `PlaceCommentAuthor.avatarColor` |
| `author.avatar.color` | `enum(red, red_orange, orange, lime, green, cyan, violet, pink, blue, brown, light_blue, purple, gray)` | 기존 `RoomColor` enum과 **13개 값이 동일**해 그대로 재사용한다 |
| `canDelete` | `boolean` | `PlaceComment.canDelete` — [⋮] 노출의 유일한 근거([research.md D6](../research.md)) |

`pagination` — `required: [page, pageSize, hasNext]`

| 필드 | 스키마 | 매핑 |
|---|---|---|
| `page` | `integer` | `PlaceCommentPage.page` |
| `pageSize` | `integer` | 쓰지 않는다 |
| `hasNext` | `boolean` | `PlaceCommentPage.hasOlder` — **"다음 페이지"가 곧 "더 오래된 페이지"다**(§5) |

## 2. `POST /api/v1/pins/{pinId}/comments` — 작성 (FR-013 · FR-014)

**요청 본문**: `required: ["content"]`

```json
{ "content": { "type": "string", "description": "앞뒤 공백 제거 후 1~200자" } }
```

> 서버가 **앞뒤 공백을 제거한 뒤** 1~200자를 판정한다. 이는 클라이언트의 [등록] 활성 조건(FR-013 — "공백을 제외한 입력이 1자 이상")과 같은 규칙이라, 화면이 막는 것과 서버가 막는 것이 어긋나지 않는다. EC-012(공백만 입력)도 양쪽에서 함께 걸린다.

**응답 201 `data`**: §1의 `data[]` 항목과 같은 형태. 등록된 코멘트가 그대로 돌아오므로 **목록을 다시 조회하지 않고 이 값을 맨 아래에 덧붙인다**(FR-014·UX-007).

**오류 400**: `errorCode: VALIDATION_ERROR`. 화면이 먼저 막으므로 정상 흐름에서는 나오지 않는다.

## 3. `DELETE /api/v1/pins/{pinId}/comments/{commentId}` — 삭제 (FR-015)

**요청**: 경로 파라미터 `pinId`·`commentId`. 본문 없음.

**응답 200**: `data: { ok: boolean }` — `required: [ok]`.

확인 절차 없이 즉시 호출한다(FR-015). 되돌리기 수단이 없으므로 낙관적 제거 후 실패 시 되살리는 처리를 두지 않는다 — 실패는 공통 에러 경로로 흘리고 목록을 다시 조회한다. 구체적 형태는 [place-detail-main-contract.md](./place-detail-main-contract.md) §4가 정한다.

## 4. spec 대조 요약

| spec 요구사항 | 판정 | 근거 |
|---|---|---|
| FR-010 작성자 프로필·닉네임과 본문 | 대응 API 있음 | `author.nickname` · `author.avatar.color` |
| FR-010 오래된 것부터 나열 | **있으나 방향이 반대** | 페이지 단위가 최신부터 → §5 |
| FR-011 빈 상태 | 대응 API 있음 | `data: []` |
| FR-012 200자 상한 | 대응 API 있음 | `maxLength: 200` 일치 |
| FR-013 공백만 등록 불가 | 대응 API 있음 | "앞뒤 공백 제거 후 1~200자" |
| FR-014 등록 후 목록 반영 | 대응 API 있음 | 201이 생성된 코멘트를 반환 |
| FR-015 본인 코멘트만 삭제 | 대응 API 있음 | `canDelete` |
| FR-019 (장소, 방) 단위 귀속 | 대응 API 있음 | 경로가 `pinId` |
| FR-021 전문 노출 | 서버 무관 | 화면 레이아웃 규칙 |
| EC-016 작성자가 방을 나감 | **미확인** | 서버가 탈퇴 멤버의 코멘트를 어떻게 내려주는지 문서에 없다. `author`가 `required`라 값은 오겠으나 "작성 당시 프로필"인지 확인되지 않았다 → §5 |

## 5. 서버팀 협의 항목

| # | 항목 | 내용 |
|---|---|---|
| 1 | **페이징 방향** | 화면은 오래된 것이 위인 한 줄기 목록이고 입력창이 맨 아래다(FR-010·EC-015). 서버는 최신 페이지부터 준다. 이번 구현은 역방향 페이징으로 흡수하지만([research.md D11](../research.md)), 오름차순 정방향 옵션(`order=asc` 등)이 있으면 클라이언트가 단순해진다 |
| 2 | **`hasNext`의 의미** | 역방향 페이징에서 `hasNext = true`가 "더 오래된 페이지가 있다"로 읽히는지 확인이 필요하다. 이 계약은 그렇게 가정하고 `hasOlder`로 매핑한다 |
| 3 | **탈퇴 멤버의 코멘트** | EC-016이 "코멘트는 남기고 작성 당시의 프로필·닉네임으로 표시"를 요구한다. 서버가 방을 나간 사용자의 `author`를 어떻게 채우는지(작성 당시 스냅샷인지, 현재 프로필인지, 탈퇴 표시가 붙는지) 문서에 없다 |
| 4 | **쿼리 파라미터 타입 미선언** | `page`·`pageSize`의 `schema`에 `type`이 없고 `example`·`maximum`만 있다 |

3번은 이번 구현이 서버가 준 `author`를 그대로 그리는 것으로 진행한다 — 어느 쪽이든 화면 코드가 달라지지 않는다.
