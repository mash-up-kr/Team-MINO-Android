# 계약: 홈 덱 데이터 출처

**대상 스펙 경로**: `docs/specs/home-deck-exploration`

**계획서**: [plan.md](../plan.md)

---

## 1. 대조 근거

| 항목 | 값 |
|---|---|
| 출처 | `https://api.gguk.org/api-docs-json` |
| 최초 조회 | 2026-08-27T21:12:20+09:00 — 오퍼레이션 24개 |
| 재조회 | **2026-08-29 — 오퍼레이션 25개.** 늘어난 하나가 `GET /api/v1/rooms/{roomId}/cards`다 |
| 문서 | Team MINO API 1.0.0 |

최초 조회는 [`.claude/skills/mino-plan/scripts/openapi_digest.py`](../../../../.claude/skills/mino-plan/scripts/openapi_digest.py)로 수행했다. 그 시점의 이 계약은 배포 문서와 미배포 서버 PR [Node#94](https://github.com/mash-up-kr/Team-MINO-Node/pull/94)를 구분해 적었으나, **재조회 시점에 그 PR이 배포되어 구분이 사라졌다.** 아래는 전부 배포된 계약이다.

---

## 2. 홈 덱의 주 계약 — `GET /api/v1/rooms/{roomId}/cards`

> **배포됨(2026-08-29 재조회).** 응답 코드는 `200`·`400`·`401`·`403`이다. 다만 문서와 실제 응답이 두 곳에서 어긋나므로 DTO가 방어한다.
>
> - 카드 객체에 `required` 배열이 없다 → `CardResponse`가 모든 필드에 기본값을 둔다.
> - **`createdBy.avatar`를 `{ id: integer }`로 적어 두었으나 실제 응답에 `id`가 없다.** 같은 서버의 `/users/me`·`/pins`·`/pins/{pinId}`는 모두 아바타를 `{ color }`로 내려주며, 실제 `/cards` 응답도 그쪽이다. **문서가 아니라 실제 응답을 따른다** — `CardCreatedByResponse`가 프로필과 같은 `AvatarResponse`를 쓴다.

FR-004 · 009~012 · 015가 이 하나에 걸려 있다. 홈 카드 덱의 전부다.

### 2.1 요청

```
GET /api/v1/rooms/{roomId}/cards
  sort  ?  enum: ggukPick | latest | nearby   default: ggukPick
  lat   ?  number(double)   — sort=nearby일 때 필수
  lng   ?  number(double)   — sort=nearby일 때 필수
```

- **`sort`가 후보 집합을 좁히고 순위까지 정한다.** 홈은 받은 순서를 그대로 쓴다(spec §3.2).
- **`sort=nearby`에 좌표가 없으면 400**이다. 홈은 그 경우 요청을 보내지 않고 빈 덱으로 다루며, 그러면 EC-009가 별도 분기 없이 성립한다(R-013).
- 방 멤버십을 검증하므로 **403**이 올 수 있다.

### 2.2 응답

```
200: { data: Card[] }   maxItems: 10
```

`Card` 한 장이 담는 것 — 배포된 `GET /api/v1/pins`의 `data[]`와 **같은 모양에 `labelGroup` 하나가 더 붙는다.**

```
id          string(uuid)                 ← PlaceCard.pinId
roomId      string(uuid)
place       { name, address, lat, lng, category, ... }
images      string[]                     ← 대표 이미지 2칸 그리드
createdBy   { userId, nickname, avatar } | null   ← 등록자 아바타 (avatar는 { color }, 위 경고 참조)
createdAt   string(date-time)
labelGroup  enum: worthVisiting | manySaves | manyComments | manyViews
```

**모양이 같아서 매퍼를 재사용한다.** 실서버 전환 때 새로 쓸 매퍼가 없다.

### 2.3 홈이 기대는 성질

| 성질 | 계약 | 쓰는 곳 |
|---|---|---|
| 최대 10장 | `maxItems: 10` — 서버가 잘라 준다 | FR-004, TS-004 |
| 후보가 적으면 짧은 덱 | *"방의 후보가 적으면 그만큼 짧다"* | TS-005 |
| 응답 순서 = 노출 순위 | *"후보 정렬 순서를 그대로 유지한다 — 라벨별로 묶지 않는다"* | spec §3.2 |
| 페이지네이션 없음 | *"페이지네이션과 재생성 계약은 두지 않는다"* | spec §4 가정(덱을 다시 채우지 않음) |
| 매 호출 새로 계산 | 같음 | FR-011의 "전환 시점마다 다시 판정" |

**FR-004의 "최대 10장" 절단을 클라이언트가 하지 않는다.** 서버가 이미 잘라 주므로 Repository는 받은 것을 그대로 담는다.

### 2.4 라벨 배정은 서버 소관

서버가 `worthVisiting 4 / manySaves 2 / manyComments 2 / manyViews 2` 정원으로 배정하고, 자격 미달분은 `worthVisiting`이 흡수한다. **홈은 판정에 관여하지 않고 `labelGroup` 값을 표시만 한다**(FR-008, spec §3.2).

> 정원이 있다고 해서 덱 구성이 흔들리지는 않는다. 라벨은 **뽑힌 10장에 붙이는 표시**이고 응답 순서는 후보 순위를 그대로 유지한다.

---

## 3. 배포된 계약 — 지금 쓸 수 있는 것

### 3.1 `GET /api/v1/rooms` — 내가 속한 방 목록

FR-013(장소 0개 방 건너뛰기) · FR-017·018(홈 방 시트) · FR-021(방 뱃지·캐릭터) · FR-022(시작 방)이 쓴다.

```
data[].id         string(uuid)
data[].type       enum: personal | shared    ← FR-022의 개인방 판별
data[].name       string
data[].color      string   example: "black"
data[].pinCount   integer                     ← FR-013 판정
```

- **`type == "personal"`이 개인방(`내 장소`)** 이다. FR-022의 최초 실행 시작 방이자, 마지막 방을 모를 때의 폴백이다.
- **`pinCount == 0`인 방을 순회에서 제외**한다(FR-013, TS-019).
- **자동 방 전환에 별도 API가 필요 없다.** 클라이언트가 이 목록과 `pinCount`로 다음 방을 골라 그 방의 `cards`를 부른다.

### 3.2 `POST /api/v1/pins/{pinId}/accesses` — 「경과일 초기화 확인」

```
POST /api/v1/pins/{pinId}/accesses
200: { data: { ok: true } }
```

문서 설명: *"개인별 카드 큐레이션의 재생성 제외 조건이자 클릭수 집계의 원천. append-only 로그."*

**spec 3.0.0의 ①에 정확히 대응한다.** 카드를 눌러 [SCR-006] 상세로 이동할 때 호출한다(FR-007·023).

- **출처 구분자가 필요 없다.** ①은 앱 전역에서 동일하게 일어나므로 홈에서 부르든 다른 화면에서 부르든 같다(spec §3.2, PRD 9.0.0 §5).
- **넘김(②)에는 호출하지 않는다.** 서버는 스와이프를 기록하지 않는다.
- 결과를 기다리지 않고 화면을 전환한다(R-012).

### 3.3 `POST /api/v1/pins/{pinId}/duplicate` — 다른 방 저장

```
requestBody: { roomIds: string(uuid)[] }   required, minItems: 1
409: 대상 방 중 하나라도 같은 장소가 있으면 전체 거절
```

FR-005가 쓴다. spec §3.2가 복제 처리 자체를 비목표로 두었으므로 홈은 시트를 열고 선택한 `roomId`를 전달하는 데까지만 다룬다. 409는 도메인 예외로 매핑해 스낵바로 알린다 — [`conventions/error_handling.md`](../../../conventions/error_handling.md).

---

## 4. mock 계약과 전환 지점

> **전환 완료(2026-08-29).** 아래 세 지점을 모두 닫고 mock(`DeckMockRemoteDataSourceImpl`·`DeckMockStore`)을 삭제했다. 예고한 대로 `DeckRemoteDataSource`·`DeckMapper`·`HomeDeckRepositoryImpl`·화면은 바뀌지 않았다. 이 절은 무엇을 걷어냈는지 남기기 위해 그대로 둔다.

`/cards`가 배포될 때까지 `DeckRemoteDataSource` 뒤에 mock을 둔다(R-001·R-002). `group-room-form`의 [`room-api-mock.md`](../../group-room-form/contracts/room-api-mock.md) 선례를 따른다.

```
interface DeckRemoteDataSource {
    /** GET /api/v1/rooms/{roomId}/cards 를 그대로 흉내 낸다. */
    suspend fun getCards(
        roomId: String,
        sort: DeckSort,
        lat: Double? = null,
        lng: Double? = null,
    ): List<CardResponse>
}
```

**mock은 §2의 계약을 지어내지 않고 그대로 따른다.** 1.0.0 때와 달리 실제 계약이 확정돼 있으므로, 응답 형태·`labelGroup` enum 값·10장 절단·짧은 덱·순서 유지를 전부 실제와 맞춘다. 그래야 전환 때 매퍼와 호출부가 바뀌지 않는다.

**반드시 재현해야 하는 경우** — 이것들이 없으면 검증되지 않는 요구사항이 생긴다.

| 재현할 것 | 검증되는 것 |
|---|---|
| 후보 0건인 정렬 | TS-017, TS-023, EC-013 |
| 10장 미만인 덱 | TS-005 |
| 정렬 간 후보 겹침 | spec §4 가정(중복 허용) |
| `labelGroup` 4종이 섞인 덱 | TS-014 |
| 지표가 전부 0인 방(전부 `worthVisiting`) | FR-008의 라벨이 항상 존재함 |

**이 다섯은 mock과 함께 사라지지 않고 테스트 픽스처로 옮겼다** — `core/data/src/test/.../network/DeckApiFixtures.kt`. 실서버는 짧은 덱도 0건 정렬도 요청한다고 내려주지 않으므로, 옮기지 않았다면 위 요구사항을 검사할 자리가 없어진다.

### 전환 지점

교체는 **세 곳**이었고, 그 밖은 손대지 않았다.

1. ~~`core/data/.../datasource/DeckRemoteDataSourceImpl.kt`~~ → 신설 완료. 도메인 `DeckSort` → 서버 문자열 대응을 이 파일이 소유한다(`DeckApiService`는 문자열만 받는다)
2. ~~`core/data/.../datasource/di/DeckDataSourceModule.kt`~~ → `@Binds` 인자를 실구현으로 교체 완료
3. ~~`core/data/.../network/dto/response/CardResponse.kt`~~ → 대조 완료. 필드는 일치했고, `required` 부재에 대비해 기본값만 추가했다

---

## 5. 서버팀과 맞춰야 할 것

1.0.0의 협의 항목 6건 중 5건이 서버 PR Node#94로 해소됐다. 남은 것은 아래뿐이다.

| # | 지점 | 상태 |
|---|---|---|
| 1 | ~~`/cards` 배포 일정~~ | **해소(2026-08-29).** 배포 확인 후 mock을 걷었다 |
| 2 | `users.last_viewed_room_id` | **만들지 않아도 된다** — 마지막 방은 앱이 기기에 저장한다(FR-022, R-004) |

**해소된 것**: 정렬 3종 API 부재 → Node#94가 만듦 / 라벨 필드 부재 → `labelGroup` / `GET /pins` 파라미터 미문서화 → Node#94가 문서화 / `accesses` 출처 구분 → ①이 앱 전역이라 구분자 불필요(PRD 9.0.0) / `꾹 Pick` 정의 불일치 → 순위 산출이 서버 소유라 PRD·spec이 규정하지 않음(spec §3.2).
