# 계약: 공유 링크 저장 API

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](../spec.md) · **계획**: [plan.md](../plan.md)

`[저장하기]` 이후 워커가 **한 번** 호출하는 계약(FR-010·FR-014·FR-015). 2026-08-28 `https://api.gguk.org/api-docs-json`(`Team MINO API` 1.0.0) 기준이며, **서버에 이미 배포돼 있다.**

> [!NOTE]
> plan 2.x가 명세했던 `POST /api/v1/rooms/{roomId}/pins`(방 하나당 요청 하나)는 **경로가 사라졌다.** 지금은 `roomIds` 배열을 받는 아래 계약이 대신하며, 그 결과 방 단위 분해가 클라이언트에서 서버로 되돌아갔다 — 경위는 [research.md R-021](../research.md).

---

## 1. 엔드포인트

```
POST /api/v1/rooms/pins
Authorization: Bearer <익명 세션 ID 토큰>

body:
{
  "url": "https://www.instagram.com/p/XXXX/",
  "roomIds": ["<uuid>", "<uuid>"]
}
```

**요약**: "인스타그램 링크에서 장소를 추출해 **여러 방에** 핀을 추가한다."

### 1.1 요청

| 위치 | 이름 | 타입 | 필수 | 규칙 |
|---|---|---|---|---|
| body | `url` | `string` (`format: uri`) | O | 공유받은 원문 URL. **클라이언트는 도메인을 검사하지 않는다** — 지원 여부 판정은 서버가 한다([research.md R-002](../research.md)) |
| body | `roomIds` | `string[]` (`format: uuid`, `minItems: 1`) | O | 사용자가 고른 방 전부. **한 요청이 방 전부를 담는다** |

- 인증은 `MinoIdentityProofPlugin`이 Mino 호스트로 나가는 모든 요청에 자동으로 싣는다. 이 엔드포인트에만 별도 처리를 두지 않는다.
- **요청은 방 개수와 무관하게 1건이다.** 방마다 쪼개 보내지 않는다 — 근거는 [research.md R-021](../research.md).
- `roomIds`가 비어 있는 요청은 만들어지지 않는다. 선택 0개면 `[저장하기]`가 비활성이다(FR-009).

### 1.2 응답

| 코드 | 의미 | 워커 처리 |
|---|---|---|
| `202` | 장소 추출 작업 등록 완료. 추출·저장은 비동기로 진행된다 | 성공으로 확정하고 워커를 종료한다 |
| `400` | `errorCode` + `message` | **재시도하지 않는다.** 실패로 확정 |
| `401` | `UNAUTHORIZED` · `TOKEN_EXPIRED` · `USER_NOT_REGISTERED` | **재시도하지 않는다.** 실패로 확정 |
| `403` | 멤버가 아닌 방이 섞여 있다 | **재시도하지 않는다.** 실패로 확정 |
| `502` · 그 밖의 `5xx` | 서버 오류 · 작업 등록 실패 | 지수 백오프로 재시도 |

재시도 판정의 근거는 [research.md R-005](../research.md)가, 도메인 예외가 아닌 실패의 처리는 [research.md R-016](../research.md)이 소유한다.

> **`202`의 본문을 읽지 않는다.** 스키마가 정의돼 있지 않고, 접수 사실 외에 클라이언트가 쓸 값이 없다. `expectSuccess = true`가 `202`를 성공으로 판정하므로 `ApiService` 함수의 반환은 `Unit`이다.

> **`errorCode` 값에 분기하지 않는다.** swagger가 `400`·`403`·`502` 세 응답에 모두 `DUPLICATE_PIN_IN_ROOM`을 example로 달아 두어 어느 코드가 어느 상황에 오는지 확정할 수 없다. 클라이언트 판정은 **HTTP 상태 코드만** 본다. 사용자에게 사유를 보이는 것은 이 화면의 책임이 아니므로(§3) 값에 분기할 이유도 없다.

> **재시도가 중복 저장을 만들지 않는다.** `202`는 접수만 확정하고 중복 판정은 서버가 저장 시점에 한다(FR-015). 재시도로 같은 `url`·`roomIds`가 다시 가도 서버가 중복으로 흡수하며, 사용자에게는 알림함의 중복 알림으로 닿는다.

---

## 2. 방마다 독립적으로 성립한다 — 지키는 주체는 서버다

spec §4 가정("한 방의 실패가 다른 방의 저장을 되돌리지 않는다")과 TS-019(부분 실패)는 **서버가 방마다 갈라 처리하는 것으로 충족된다.** 요청이 하나이므로 클라이언트에는 방 단위 실행 경계가 없다.

| 규칙 | 어디가 지키는가 |
|---|---|
| 한 방의 실패가 다른 방의 저장을 되돌리지 않는다 | **서버.** 요청 하나를 받아 방마다 갈라 처리한다 — [research.md R-021](../research.md) |
| 이미 같은 장소가 있는 방에는 중복 저장하지 않고 `이미 저장해둔 곳이에요` 중복 알림을 남긴다 (FR-015) | **서버.** 중복 판정은 링크 추출이 끝나야 가능하고, 그 시점은 `202` 이후다 |
| 링크 분석에 실패하면 저장하지 않고 `장소를 저장하지 못했어요.` 오류 알림을 남긴다 (FR-014, EC-007, EC-008) | **서버.** 같은 이유로 `202` 이후에 확정된다 |

> **클라이언트가 부분 실패를 관측할 수 없다.** `202`는 요청 전체의 접수만 알린다. 어느 방이 성공하고 어느 방이 실패했는지는 알림함([SCR-007])이 전달하며, 그것은 spec §3.2가 비목표로 넘긴 범위다.

> **`POST /api/v1/pins/{pinId}/duplicate`와 다르다.** 그쪽은 **이미 존재하는 핀**을 복제하는 [SYS-003] 경로이고 "대상 방 중 하나라도 같은 장소가 있으면 `409`로 전체 거절"이다. 공유 링크는 아직 핀이 아니라 `pinId`가 없고, FR-016이 중복 방도 가려내지 않기로 확정했으므로 전체 거절은 이 경로의 정상 동작이 아니다.

---

## 3. 알림

FR-014·FR-015의 알림은 `202` 이후 저장이 비동기로 확정된 뒤 발생하므로 **서버가 만든다.** 현재 API에 알림 관련 엔드포인트가 없으나, 알림함 화면은 spec §3.2가 [SCR-007]로 넘긴 비목표다. 이 feature의 클라이언트 책임은 **요청 하나를 확실히 전달하는 데서** 끝난다.

---

## 4. 클라이언트 측 인터페이스

계층별 작성 규칙은 [`core/data/README.md`](../../../../core/data/README.md) §4·§5가 소유한다. 이 계약이 확정하는 것은 시그니처뿐이다.

```
// :core:data/network/dto/request/PinCreateRequest.kt (@Serializable)
url: String
roomIds: List<String>

// :core:data/network/service/PinApiService.kt (internal)
suspend fun createPin(request: PinCreateRequest)

// :core:data/datasource/PinRemoteDataSource.kt (internal)
suspend fun createPin(request: PinCreateRequest)
```

- **`roomId` 파라미터가 없다.** 대상 방은 본문에 실린다.
- mock 구현을 두지 않는다 — 근거는 [research.md R-013](../research.md).
- 워커가 이 `DataSource`를 같은 모듈 안에서 직접 호출한다. 도메인에 전송용 함수를 노출하지 않는다([research.md R-017](../research.md)).

### 4.1 워커 입력

`SharedPlaceSaveWorker` **하나가 요청 하나를 담당**하므로 입력도 요청 하나 몫이다.

| 키 | 타입 | 의미 |
|---|---|---|
| `url` | `String` | 공유받은 원문 URL |
| `roomIds` | `Array<String>` | 사용자가 고른 방 전부 |

- `androidx.work.Data`는 `String` 배열을 담을 수 있다(`workDataOf`).
- 두 값 중 하나라도 없거나 `roomIds`가 비어 있으면 워커를 예약한 쪽의 버그다. 도메인 예외로 감싸지 않는다([research.md R-016](../research.md)).
- **워커를 방 개수만큼 만들지 않는다**([research.md R-021](../research.md)).
