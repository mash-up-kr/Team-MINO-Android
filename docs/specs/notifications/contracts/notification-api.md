# 계약: 알림함이 쓰는 서버 API

**대상 스펙**: [spec.md](../spec.md) 7.0.0 · **계획**: [plan.md](../plan.md)

**근거 문서**: `https://api.gguk.org/api-docs-json` (Team MINO API 1.0.0, 오퍼레이션 29개) · **조회 시점**: 2026-09-04T16:54:22+09:00 *(재조회 — 2026-09-01 조회분과 달라진 곳은 `payload`다)*

아래 스키마 제약은 위 문서에서 인용한 것이다. 값이 다르면 문서가 옳고 이 문서가 낡은 것이다.

---

## 1. `GET /api/v1/notifications` — 알림 목록 조회

FR-001·FR-018의 원천. 문서의 설명은 `알림함 스펙 3.0.0 기준. 읽음 상태 없음. offset 기반 페이지네이션, 기본 20건.`이다.

### 요청

| 파라미터 | 위치 | 타입 | 제약 | 이 화면이 보내는 값 |
|---|---|---|---|---|
| `page` | query | `string` | `pattern ^\d+$`, `minimum 0`, 기본 `"0"` | 0부터 1씩 |
| `pageSize` | query | `string` | `pattern ^\d+$`, `minimum 1`, `maximum 100`, 기본 `"20"` | **보내지 않는다** |

`pageSize`를 보내지 않는 이유는 spec §4다 — "한 묶음의 크기 20건은 서버가 정한 기본값을 따른다. 화면이 이 숫자를 정하지 않으므로, 서버 기본값이 바뀌면 화면에 한 번에 이어 붙는 건수도 함께 바뀐다." 클라이언트가 `20`을 박아 보내면 그 문장이 거짓이 된다.

**두 파라미터의 타입이 `integer`가 아니라 `string`이다.** 쿼리 문자열이라 그렇고, 요청을 만들 때 숫자를 문자열로 넘긴다.

인증은 `bearer`가 걸려 있다 — 기존 `AuthPlugin` 경로를 그대로 탄다.

### 응답 200

```
data: [
  id            string(uuid)
  type          enum[PIN_DUPLICATED, SAVE_FAILED, NEARBY_PLACE,
                     TOP_COMMENTED_PLACE, ROOM_MEMBER_JOINED, ROOM_JOINED_SELF]
  typeLabel     string     예: "이미 저장해둔 곳이에요"
  targetName    string     예: "패스트리 순간"
  thumbnailUrl  string?    nullable
  payload       object?    oneOf —
                           { placeId: uuid, pinId: uuid }  장소 대상 3종 (둘 다 required)
                           { roomId: uuid }                ROOM_* 2종
                           null                            SAVE_FAILED
  createdAt     string(date-time)
]
pagination: { pageSize: int, page: int, hasNext: bool }
```

`type`의 문서 설명이 spec FR-005와 그대로 맞물린다 — "장소 대상 3종은 장소 상세, `ROOM_*`는 방 상세, `SAVE_FAILED`는 앱 내 저장 오류 안내 화면으로 이동한다."

`payload`의 문서 설명은 **"이동 대상 식별자. 장소 대상은 `placeId`와 `pinId`, 방 대상은 `roomId`이며 저장 오류는 `null`이다. 장소 상세는 `pinId`로 연다."**

**이 한 줄이 FR-022의 도착지 판정을 서버로 옮겼다.** 2026-09-01 조회에는 `pinId`가 없어 `placeId`에서 방을 역산하는 설계가 섰으나(구 research D6·D7), 지금은 서버가 핀을 지목해 준다. `placeId`도 함께 오지만 **도착지 판정에는 쓰지 않는다** — push-notification의 payload가 같은 구성이고 그쪽도 `placeId`를 버린다([push-payload-contract](../../push-notification/contracts/push-payload-contract.md)).

### 응답 401

`errorCode`가 `UNAUTHORIZED` / `TOKEN_EXPIRED` / `USER_NOT_REGISTERED`. 알림함이 따로 다루지 않고 공통 에러 경로로 흘린다.

### spec 대응

| spec | 대응 |
|---|---|
| FR-001 최신순 | 서버가 정렬해 준다. 클라이언트는 받은 순서를 그대로 쓴다 |
| FR-003 경과 시간 | `createdAt`에서 클라이언트가 계산([research.md D12](../research.md)) |
| FR-022 도착지 방 | `payload.pinId` 그대로. 추가 조회가 없다([research.md D6·D7](../research.md)) |
| FR-004 유형 문구 | `typeLabel` 그대로([research.md D4](../research.md)) |
| FR-012 썸네일 | `thumbnailUrl` 그대로. 저장 오류만 유형으로 갈라 고정 아이콘을 쓴다([research.md D5](../research.md)) |
| FR-014 위치 권한 | 서버가 걸러 내려준다. 클라이언트에 필터가 없다 |
| FR-018 페이징 | `page` + `hasNext` |
| FR-019 대표 알림 제외 | `type` enum에 대표 알림 값이 없어 구조적으로 실릴 수 없다 |
| FR-021 저장 시도 단위 묶음 | 항목 하나가 곧 한 행이므로 서버가 묶어 준다면 그대로 성립한다. 스키마로는 확인되지 않으나 §3대로 협의 대상에서 내렸다 |

---

## 2. 이 화면이 쓰지 않는 API

| API | 5.0.0까지 쓰려던 이유 | 지금 안 쓰는 이유 |
|---|---|---|
| `GET /api/v1/rooms` (인자 없이) | 공동방 참가 ①②의 방 썸네일을 `thumbnailList`·`color`로 합성 | spec 6.0.0 FR-012가 「서버가 준 `thumbnailUrl` 한 장」으로 바뀌었다([research.md D5](../research.md)) |
| `GET /api/v1/rooms?showHasPlaceId=` | `placeId`에서 도착지 방을 역산 | spec 5.0.0 FR-022가 `payload.pinId`를 쓴다([research.md D6·D7](../research.md)) |
| `GET /api/v1/pins/{pinId}` | 후보 방 중 「가장 최근에 저장한 방」 판정 | 같음 |

**알림 탭이 부르는 서버 API는 §1 하나뿐이다.** 목록을 그리는 데도, 알림을 눌러 도착지를 정하는 데도 다른 요청이 없다.

세 API 자체는 살아 있고 place-detail·room-list가 쓴다 — 이 문서가 그것을 안 쓴다는 것이지 없어진 것이 아니다.

---

## 3. 서버팀 협의 항목

**현재 없다.** 4.1.1까지 네 건이 있었고 모두 닫혔다.

| 협의 항목 | 어떻게 닫혔나 |
|---|---|
| 도착지 `pinId` 부재 | 서버가 `payload.pinId`를 실었다(2026-09-04 재조회) → spec 5.0.0 FR-022 |
| `matchedPinSavedAt` 부재 | 방을 역산할 일이 사라져 함께 소멸 |
| `thumbnailUrl`로 방 썸네일을 실을 수 없음 | spec 6.0.0 FR-012가 서버가 주는 값을 그대로 쓰는 것으로 바뀌었다(§5 Q13) |
| FR-021 묶음 여부를 확인할 수 없음 | **사용자 판단으로 협의 대상에서 내렸다**(spec 6.0.0 §5 Q13). spec §4 가정을 신뢰하고 진행하며, 실제 데이터 확인만 [quickstart.md](../quickstart.md) §4.3에 남는다 |

`thumbnailUrl`은 재조회에서도 단일 nullable 문자열이고 오퍼레이션 설명도 여전히 `알림함 스펙 3.0.0 기준`이지만, **spec이 그 형태에 맞춰졌으므로 더는 어긋남이 아니다.**

## 4. 대응 API가 없는 요구사항

현재 없다. FR-005의 공동방 참가 ①② → [SCR-005] 방 상세 이동은 1.0.0에서 "화면이 없다"로 이 표에 올라 있었으나, 방 상세 화면과 `RoomDetailRequestHolder`가 저장소에 있어 해소되었다([research.md D10](../research.md)). 이 전환은 `payload.roomId`만 쓰고 추가 API를 부르지 않는다.

요구사항은 모두 **§1 하나**로 덮인다. `index`에서 `일치하는 오퍼레이션 없음`으로 나온 항목은 없다.
