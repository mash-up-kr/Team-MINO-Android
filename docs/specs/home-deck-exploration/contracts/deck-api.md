# 계약: 홈 덱 데이터 출처

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**계획서**: [plan.md](../plan.md)

---

## 1. 대조 근거

| 항목 | 값 |
|---|---|
| 출처 | `https://api.gguk.org/api-docs-json` |
| 조회 시점 | **2026-08-26T21:33:25+09:00** |
| 문서 | Team MINO API 1.0.0 |
| 오퍼레이션 | 21개 |

조회는 [`.claude/skills/mino-plan/scripts/openapi_digest.py`](../../../../.claude/skills/mino-plan/scripts/openapi_digest.py)로 수행했다. 이 계약은 **위 시점의 문서**에 근거한다 — 문서가 바뀌면 다시 대조해야 한다.

---

## 2. 대응 API 있음

### 2.1 `GET /api/v1/rooms` — 내가 속한 방 목록

FR-013(장소 0개 방 건너뛰기) · FR-017·018(홈 방 시트) · FR-021(방 뱃지·캐릭터) · FR-022(시작 방)이 쓴다.

응답 `data[]`에서 홈이 쓰는 필드:

```
id          string(uuid)
type        string  enum: personal | shared     ← FR-022의 개인방 판별
name        string
color       string  example: "black"
pinCount    integer                              ← FR-013 판정
```

- **`type == "personal"`인 방이 개인방(`내 장소`)** 이다. FR-022의 "앱 최초 실행 시 개인방" 판별에 이 값을 쓴다.
- **`pinCount == 0`인 방을 순회에서 제외**한다(FR-013, TS-019). 응답에 `hasPlace`도 있으나 `?showHasPlaceId=` 지정 시에만 포함되는 선택 필드라, 항상 내려오는 `pinCount`를 판정에 쓴다.

### 2.2 `GET /api/v1/pins` — 핀 목록

카드 본문(FR-008 제외)의 원천이다.

```
data[].id                  string(uuid)          ← PlaceCard.pinId
data[].place.name          string                ← 장소명
data[].place.address       string                ← 주소
data[].images              string[]              ← 대표 이미지 2칸 그리드
data[].createdBy           object|null           ← 등록자 아바타
  .userId / .nickname / .avatar.id
```

### 2.3 `POST /api/v1/pins/{pinId}/duplicate` — 다른 방에 핀 복제

FR-005의 `다른 방 저장`이 쓴다.

```
requestBody: { roomIds: string(uuid)[] }   required, minItems: 1
409: 대상 방 중 하나라도 같은 장소가 있으면 전체 거절
```

- spec §3.2가 **복제 처리 자체를 비목표**로 두었다. 홈은 「홈 방 시트」를 열고 선택한 `roomId`를 전달하는 데까지만 다룬다.
- 409(중복)는 도메인 예외로 매핑해 스낵바로 알린다 — [`conventions/error_handling.md`](../../../conventions/error_handling.md).

### 2.4 `POST /api/v1/pins/{pinId}/accesses` — 핀 접근 기록

문서 설명: *"개인별 카드 큐레이션의 재생성 제외 조건이자 클릭수 집계의 원천. append-only 로그."*

spec §3.2의 **"홈은 카드를 넘긴 사실을 알릴 뿐"** 에 대응하는 통지 경로다. 카드를 **넘긴 시점**에 호출한다.

> ⚠️ **협의 필요** — 이 엔드포인트가 "카드를 넘김"과 "장소 상세 진입"을 구분하지 않는다. spec FR-007은 **상세를 열어본 것은 확인으로 보지 않는다**(TS-013)고 명시했다. 서버가 이 로그를 큐레이션 제외 조건으로 쓴다면, 상세 진입 시에도 이 API가 호출되는 다른 화면([SCR-006])과의 구분이 서버 쪽에 필요하다. §4 참고.

---

## 3. 서버 미구현 — mock으로 진행

아래 둘은 **문서에 대응 오퍼레이션이 없다.** `sort`·`order`·`orderBy`·`curation`·`label`·`pick`·`nearby`·`latest`·`recommend` 키워드로 문서 전체를 훑어 **0건**임을 확인했다.

| 미구현 | 막히는 요구사항 |
|---|---|
| **정렬 3종별 후보 조회** | FR-004 · 009 · 010 · 011 · 012 · 015, TS-004·005·015~023, EC-009·012·013 |
| **장소분류 라벨 4종** | FR-008, TS-014 |

`GET /api/v1/pins`는 `parameters: []`라 정렬은 물론 **`roomId` 필터조차 문서화되어 있지 않다.** 설명문("roomId의 핀 목록")과 어긋난다 — §4 참고.

### 3.1 mock 계약

R-001·R-002에 따라 `DeckRemoteDataSource` 인터페이스 뒤에 mock을 둔다. `group-room-form`의 [`room-api-mock.md`](../../group-room-form/contracts/room-api-mock.md) 선례를 그대로 따른다.

```
interface DeckRemoteDataSource {
    /** roomId·sort에 해당하는 후보를 순위 순으로 돌려준다. 최대 10장 절단은 호출자(Repository)가 한다. */
    suspend fun getDeckCandidates(roomId: String, sort: DeckSort): List<PlaceCardResponse>
}
```

- 반환 순서가 곧 **순위**다. 홈은 재정렬하지 않는다(spec §3.2·§4 가정).
- mock은 `GET /api/v1/pins`의 실제 응답 형태를 그대로 흉내 내고, **라벨만 덧붙인다.** 실서버가 붙었을 때 매퍼가 바뀌지 않게 하기 위해서다.
- 정렬별 후보 집합은 **겹칠 수 있다** — spec §4 가정이 덱 간 중복을 허용했다.
- 후보 0건인 정렬을 반드시 재현한다. TS-017·TS-023과 EC-013이 그 경우를 검증한다.

### 3.2 실서버 전환 지점

교체는 아래 **세 곳**이다. 그 밖은 손대지 않는다.

1. `core/data/.../datasource/DeckRemoteDataSourceImpl.kt` — 실제 HTTP 구현 추가
2. `core/data/.../datasource/di/DeckDataSourceModule.kt` — `@Binds` 인자 타입을 mock → 실구현
3. `core/data/.../network/dto/response/PlaceCardResponse.kt` — 실제 응답 스키마에 맞춰 필드 정정

---

## 4. 서버팀 협의 항목

설계가 혼자 정할 수 없는 것들이다. **spec을 따를지 서버 제약을 따를지 결정이 필요하다.**

| # | 지점 | 내용 |
|---|---|---|
| 1 | **정렬 3종 API 부재** | 이 spec의 핵심 플로우(FR-011·012)가 통째로 이 API에 걸려 있다. 엔드포인트 형태와 일정 협의 필요 |
| 2 | **라벨 4종 필드 부재** | FR-008. `place.category`(카페/음식점)와는 다른 축이다. `GET /api/v1/pins` 응답에 추가할지 확인 필요 |
| 3 | **`GET /api/v1/pins`의 파라미터 미문서화** | 설명은 "roomId의 핀 목록 … page/pageSize"인데 `parameters: []`다. 실제로 받는 쿼리 파라미터를 문서에 반영해 달라 |
| 4 | **`GET /api/v1/rooms`의 파라미터 미문서화** | 설명은 `?showHasPlaceId=`·`?showUsers=true`인데 `parameters: []`다. 위와 같음 |
| 5 | **`accesses`가 넘김과 상세 진입을 구분하지 않음** | FR-007·TS-013이 "상세 진입은 확인이 아니다"를 요구한다. 서버 쪽 구분 필요 |
| 6 | **방 순회 순서** | spec §4 가정이 "전달받은 방 목록의 순서를 그대로 따른다"인데, `GET /api/v1/rooms`의 정렬 기준이 문서에 없다. 순서가 안정적인지 확인 필요 |
