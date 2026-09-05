# 계약: 코멘트 API 대조

**대상 스펙 경로**: `docs/specs/place-detail`

**계획서**: [plan.md](../plan.md)

**출처**: <https://api.gguk.org/api-docs-json> (Team MINO API 1.0.0)

**조회 시점**: **2026-09-01T21:46:23+09:00**

---

## 1. `GET /api/v1/pins/{pinId}/comments` — 목록 (FR-010·FR-028)

**판정: 대응 API 있음.**

```
description: "최신 코멘트가 첫 페이지(page=0)이고, page가 커질수록 더 예전 코멘트다.
              한 페이지 안에서는 오래된 코멘트가 위, 최신이 아래로 온다(대화창 순서).
              hasNext=true면 더 예전 코멘트가 남아있다는 뜻이다."

parameters:
  pinId     (path,  required, string)
  page      (query, optional, string, pattern "^\d+$", minimum 0, default "0")
  pageSize  (query, optional, string, pattern "^\d+$", minimum 1, maximum 100, default "20")
```

응답 `data[]` (required: `id`, `content`, `createdAt`, `author`, `canDelete`):

```json
{
  "id":        { "type": "string", "format": "uuid" },
  "content":   { "type": "string", "minLength": 1, "maxLength": 200 },
  "createdAt": { "type": "string", "format": "date-time" },
  "author": {
    "type": "object",
    "required": ["id", "nickname", "avatar"],
    "properties": {
      "id":       { "type": "string", "format": "uuid" },
      "nickname": { "type": "string", "minLength": 2, "maxLength": 15, "example": "지은" },
      "avatar":   { "type": "object", "nullable": true, "required": ["color"],
                    "properties": { "color": { "type": "string",
                      "enum": ["red","red_orange","orange","lime","green","cyan","violet",
                               "pink","blue","brown","light_blue","purple","gray"] } } }
    }
  },
  "canDelete": { "type": "boolean" }
}
```

### 1.1 매핑

| 요구사항 | 응답 필드 | 비고 |
|---|---|---|
| FR-010 본문·작성자 | `content`·`author.nickname`·`author.avatar.color` | |
| **FR-028 작성 시각** | **`createdAt`** | **spec 4.0.0 신규 요구. 대응 있음** |
| FR-015 [⋮] 노출 | `canDelete` | 클라이언트가 작성자를 다시 따지지 않는다([research.md D6](../research.md)) |
| FR-010 나열 순서 | 페이지 안 순서 그대로 | 클라이언트가 재정렬하지 않는다 |

### 1.2 역방향 페이징

`page` 0이 최신이다. 화면은 오래된 것이 위로 오게 나열하므로 **페이지 방향과 화면 방향이 반대다.**

```
page 0  ← 최초 조회. 그대로 그린다 (안에서는 오래된 것이 위)
page 1  ← 위로 스크롤해 더 받은 것. 목록 "앞"에 붙인다
page 2  ← 그 앞에 또 붙인다
```

`pageSize`를 지정하지 않고 서버 기본값 20을 쓴다. `hasNext`는 도메인에서 `PlaceCommentPage.hasOlder`로 바꿔 든다 — "더 받을 **이전** 페이지가 있는지"라는 화면의 물음에 맞춘 이름이다([research.md D11](../research.md)).

### 1.3 `createdAt`을 표기로 옮기는 것은 이 계약이 하지 않는다

서버는 ISO-8601 시각만 준다. 구간 판정(`방금`/`N시간 전`/`N일 전`/`NNNN년 NN월 NN일`)은 feature의 UI 매핑이 한다 — [place-detail-main-contract.md §6](./place-detail-main-contract.md), [research.md D22](../research.md).

**어느 시계로 재는지는 spec §3.2가 위임하지 않은 채 남긴 지점이다.** 이 계약은 서버가 준 절대 시각을 그대로 도메인에 올리는 데까지만 정의한다.

---

## 2. `POST /api/v1/pins/{pinId}/comments` — 작성 (FR-013·FR-014)

**판정: 대응 API 있음.**

```
requestBody (required):
  { "content": { "type": "string", "description": "앞뒤 공백 제거 후 1~200자" } }
```

응답 `201`의 `data`는 §1의 항목과 **같은 스키마다** — `id`·`content`·`createdAt`·`author`·`canDelete`.

- **만들어진 코멘트를 그대로 목록 끝에 붙인다.** 목록을 다시 조회하지 않는다(FR-014).
- 돌려받은 `createdAt`이 곧 `방금`으로 표기된다(TS-054).
- **앞뒤 공백 제거는 서버가 한다.** 클라이언트가 다듬지 않는다. 200자 상한은 입력 컴포저블이 201자째를 받지 않는 것으로 이미 막힌다(FR-012, EC-011).

오류 응답: `400 VALIDATION_ERROR`, `401 UNIDENTIFIED_USER`, `403 NOT_ROOM_MEMBER`, `404 PIN_NOT_FOUND`. 모두 공통 매핑을 따르며 화면이 문구를 만들지 않는다([research.md D14](../research.md), `docs/conventions/error_handling.md`).

---

## 3. `DELETE /api/v1/pins/{pinId}/comments/{commentId}` — 삭제 (FR-015)

**판정: 대응 API 있음.**

```
parameters:
  pinId     (path, required, string)
  commentId (path, required, string)
```

- 반환값을 도메인에 올리지 않는다. 되돌리기 수단이 없어 삭제된 항목을 돌려받을 이유가 없다(EC-013).
- **권한을 클라이언트가 판정하지 않는다.** 호출 자체가 `canDelete == true`인 코멘트에서만 일어난다.

---

## 4. 대응 API가 없는 요구사항

**없다.** 이 화면의 코멘트 관련 요구사항(FR-010·FR-012~015·FR-021·**FR-028**)이 모두 위 세 오퍼레이션으로 덮인다.

spec 4.0.0이 새로 요구한 FR-028도 서버가 이미 `createdAt`을 주고 있어 **서버 변경 없이 닫힌다** — plan 1.1.0이 "코멘트에 시각을 표기하지 않는다"는 가정 아래 도메인에서 뺐던 필드를 되살리는 것뿐이다.
