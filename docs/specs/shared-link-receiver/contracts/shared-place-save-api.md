# 계약: 공유 링크 저장 API (서버 확장 요청)

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](../spec.md) · **계획**: [plan.md](../plan.md)

> [!IMPORTANT]
> **이 계약은 아직 서버에 존재하지 않는다.** 현행 `POST /api/v1/place/places`는 아래 §2가 요구하는 `roomIds`와 인증을 받지 않는다. 이 문서는 **서버 협의의 근거**이며, 클라이언트는 그때까지 mock 구현으로 개발한다([research.md R-001](../research.md)).

---

## 1. 현행 계약과 그 한계

2026-08-26 `https://api.gguk.org/api-docs-json` 기준.

```
POST /api/v1/place/places
security: (없음)
body:     { "url": string }        // pattern: instagram\.com
202:      { "data": { "ok": true } }
400:      VALIDATION_ERROR | INVALID_INSTAGRAM_URL
502:      ENQUEUE_FAILED
```

| 한계 | 막히는 요구사항 |
|---|---|
| `roomIds`를 받지 않는다 | FR-007(복수 선택) · FR-010(선택된 모든 방에 저장) |
| 인증이 걸려 있지 않다 | 서버가 저장 주체를 특정할 수 없다. 멤버십 검증도 불가 |
| `pinId`를 돌려주지 않는다 | `POST /api/v1/pins/{pinId}/duplicate`를 이어 붙이는 2단계 우회가 성립하지 않는다 |

---

## 2. 클라이언트가 필요로 하는 계약

```
POST /api/v1/place/places
Authorization: Bearer <익명 세션 ID 토큰>

body:
{
  "url": "https://www.instagram.com/p/XXXX/",
  "roomIds": ["<uuid>", "<uuid>"]     // minItems: 1
}
```

### 2.1 요청

| 필드 | 타입 | 필수 | 규칙 |
|---|---|---|---|
| `url` | `string` | O | 공유받은 원문 URL. **클라이언트는 도메인을 검사하지 않는다** — 지원 여부 판정은 서버가 한다([research.md R-002](../research.md)) |
| `roomIds` | `string[]` (uuid) | O | 사용자가 고른 방들. 최소 1개. 순서는 의미를 갖지 않는다 |

- 인증은 기존 `Authorization: Bearer` 방식을 그대로 쓴다. 앱은 `MinoIdentityProofPlugin`이 Mino 호스트로 나가는 모든 요청에 자동으로 싣는다 — 이 엔드포인트에만 별도 처리를 두지 않는다.

### 2.2 응답

| 코드 | 의미 | 클라이언트 처리 |
|---|---|---|
| `202` | 접수 완료. 추출·저장은 비동기로 진행된다 | 성공으로 확정하고 워커를 종료한다 |
| `400` | `VALIDATION_ERROR` · `INVALID_INSTAGRAM_URL` — 지원하지 않는 URL | **재시도하지 않는다.** 실패로 확정 |
| `401` | `UNAUTHORIZED` · `TOKEN_EXPIRED` · `USER_NOT_REGISTERED` | **재시도하지 않는다.** 실패로 확정 |
| `403` | 멤버가 아닌 방이 `roomIds`에 섞여 있다 | **재시도하지 않는다.** 실패로 확정 |
| `5xx` | 서버 오류 · `ENQUEUE_FAILED` | 지수 백오프로 재시도 |

재시도 판정의 근거는 [research.md R-005](../research.md)가 소유한다.

### 2.3 서버에 요청하는 동작 규칙

이 규칙들은 spec의 요구사항에서 그대로 따라온다. 클라이언트가 구현할 수 없는 부분이다.

| 규칙 | 근거 |
|---|---|
| **방마다 저장이 독립적으로 성립한다.** 한 방의 실패가 다른 방의 저장을 되돌리지 않는다 | spec §4 가정, TS-019 |
| 이미 같은 장소가 있는 방에는 중복 저장하지 않고 `이미 저장해둔 곳이에요` 중복 알림을 남긴다 | FR-015 |
| 링크 분석에 실패하면 저장하지 않고 `장소를 저장하지 못했어요.` 오류 알림을 남긴다 | FR-014, EC-007, EC-008 |
| 부분 실패 시 실패한 방에 대해서만 알림을 남긴다 | TS-019 |

> **`POST /api/v1/pins/{pinId}/duplicate`와 다르다.** 그쪽은 "대상 방 중 하나라도 같은 장소가 있으면 409로 전체 거절"인데, 이 경로는 **전체 거절하지 않는다.** FR-016이 시트에서 중복 방을 가려내지 않기로 확정했으므로, 중복이 섞인 요청이 정상 경로다.

---

## 3. 알림

FR-014·FR-015의 알림은 저장이 비동기로 확정된 뒤 발생하므로 **서버가 만든다.** 현재 API에 알림 관련 엔드포인트가 없으나, 알림함 화면은 spec §3.2가 [SCR-007]로 넘긴 비목표다. 이 feature의 클라이언트 책임은 저장 요청 전달까지다.

---

## 4. 클라이언트 측 인터페이스

서버 계약이 확정돼도 아래 인터페이스는 바뀌지 않는다. 바뀌는 것은 Hilt 바인딩 대상뿐이다.

```
// :core:data/datasource/SharedPlaceRemoteDataSource.kt (internal)
suspend fun requestSave(body: SharedPlaceSaveRequestBody)
```

| 구현 | 시점 |
|---|---|
| `SharedPlaceMockRemoteDataSourceImpl` | 서버 확장 전 — 지연 후 성공 반환, 오류 코드 주입 가능 |
| `SharedPlaceRemoteDataSourceImpl` | 서버 확장 후 — Ktor `HttpClient` 호출 |
