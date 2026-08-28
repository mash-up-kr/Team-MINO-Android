# 계약: 서버 API

**대상 스펙 경로**: `docs/specs/group-room-form` · **부속 문서**: [plan.md](../plan.md)

배포된 [Team MINO API](https://api.gguk.org/api-docs-json) `1.0.0` 중 이 feature가 쓰는 엔드포인트의 계약이다.

**조회 시점**: **2026-08-28T11:39:53+09:00.** 이 문서의 스키마 제약은 그 시점의 문서를 원문 그대로 인용한 것이다.

> [!NOTE]
> 이 재조회에서 세 오퍼레이션의 스키마에 **변동이 없음**을 확인했다 — 색 `enum` 13색은 그대로이고, 남은 어긋남도 `description.maxLength` 하나 그대로다(§2 · [research.md](../research.md) R-033). 그것이 고쳐지면 아래 인용을 다시 대조한다.

> 레이어 구성·작성 규칙(`ApiService`·`DataSource`·`RepositoryImpl`·Mapper)은 [`core/data/README.md`](../../../../core/data/README.md)가 소유한다.

---

## 1. 쓰는 엔드포인트

셋 다 `security: [{ bearer: [] }]`다 — `Authorization` 헤더는 `minoIdentityProofPlugin`이 전역으로 싣는다([`core/data/README.md`](../../../../core/data/README.md) §4). 이 feature가 헤더를 손으로 붙이는 코드는 없다.

| 메서드 · 경로 | 쓰는 곳 | 성공 | 실패 |
|---|---|---|---|
| `POST /api/v1/rooms` | `createRoom` | `201` → `{ data: Room }` | `401` |
| `PATCH /api/v1/rooms/{roomId}` | `updateRoom` | `200` → `{ data: Room }` | `401` · `403` |
| `GET /api/v1/rooms/{roomId}` | `getRoom` | `200` → `{ data: RoomDetail }` | `401` · `403` · `404` |

`POST /api/v1/rooms`의 설명은 **"생성자가 방장(owner)이 된다"**다 — FR-010의 방장 지정은 클라이언트가 하는 일이 아니라 서버가 보장한다.

### 요청 본문 스키마 (원문)

`POST`와 `PATCH`가 **같은 스키마**를 쓰고, `required`만 다르다.

```json
{
  "type": "object",
  "properties": {
    "name":        { "type": "string", "minLength": 1, "maxLength": 15 },
    "description": { "anyOf": [{ "type": "string", "maxLength": 20 }, { "type": "null" }] },
    "color":       { "type": "string",
                     "enum": ["red","red_orange","orange","lime","green","cyan",
                              "violet","pink","blue","brown","light_blue","purple","gray"] }
  },
  "required": ["name", "color"]     // PATCH는 "required": []
}
```

### 응답 본문 스키마 (원문)

```json
{
  "data": {
    "id":          { "type": "string", "format": "uuid" },
    "type":        { "type": "string", "enum": ["personal", "shared"] },
    "name":        { "type": "string", "example": "맛집 탐방" },
    "description": { "type": "string", "nullable": true },
    "color":       { "type": "string", "enum": [ …요청과 같은 13색… ], "example": "red",
                     "description": "팔레트 색상 키(13색, snake_case). 실제 색 매핑은 클라이언트 담당, 개인방 기본은 gray." },
    "ownerId":     { "type": "string", "format": "uuid" },
    "createdAt":   { "type": "string", "format": "date-time" }
  }
}
```

`GET /api/v1/rooms/{roomId}`만 `pinCount`·`memberCount` 두 필드를 더 내려준다. 이 feature는 셋 다(`type`·`createdAt`·집계 수) 읽지 않고 DTO에도 두지 않는다 — `ignoreUnknownKeys = true`가 흡수한다([data-model.md](../data-model.md) §5).

---

## 2. 계약이 spec과 어긋나는 지점

**서버가 `enum`을 배포해 색 계약이 확정됐다**(2026-08-28T00:55 조회 · [research.md](../research.md) R-030). 네 건 중 **둘이 해소, 하나가 부분 해소, 하나가 미해소**다. 같은 날 11:39 재조회에서도 이 표는 그대로다(R-033).

| # | 어긋남 | 상태 | 이 구현 |
|---|---|---|---|
| 1 | `description.maxLength: 20` vs FR-005 · PRD의 **30자** | **미해소.** 협의(30으로 늘린다)가 아직 반영되지 않았다. 2026-08-28T11:39 재조회에서도 `20`이다(R-033) | 30자 유지 — spec을 따른다. **21~30자는 서버가 거절한다** |
| 2 | `color.maxLength: 7`이 `red_orange`·`light_blue`를 자른다 | **해소.** 상한이 제거되고 `enum`으로 대체됐다 | 무변경 |
| 3 | 색 표현의 자기모순 | **부분 해소.** 방 응답은 `enum` + `example: "red"`로 정리됐으나 `InvitationPreview.room.color`는 여전히 hex 예시다 | 무변경 — **이 feature는 초대 미리보기를 쓰지 않는다** |
| 4 | `color`에 `enum`이 없다 | **해소.** 13색이 명시됐다 | 무변경 |

> [!IMPORTANT]
> **회색의 서버 어휘는 `"gray"`다.** plan 2.1.0이 구두 협의를 근거로 `"grey"`(영국식)로 확정했으나 **배포된 `enum`이 그것을 뒤집었다.** [R-018](../research.md#r-018-mock의-색상-식별자-표기-plan-110)의 원래 표기가 13색 전부에서 맞았으므로 **`RoomMapper`는 고칠 것이 없다** — 경위와 교훈은 [research.md](../research.md) R-030.

### 색 어휘 (서버 `enum` · 확정)

서버 응답이 이 필드에 붙인 설명이 계약의 성격을 그대로 말한다 — **"팔레트 색상 키(13색, snake_case). 실제 색 매핑은 클라이언트 담당, 개인방 기본은 gray."** 색값을 클라이언트가 소유한다는 [R-003](../research.md#r-003-swagger-계약이-spec과-어긋나는-세-지점-plan-100)의 판단을 서버가 문서에 명시한 것이다.

| 색 | 식별자 | `RoomColor` |
|---|---|---|
| red | `"red"` | `RED` |
| red orange | `"red_orange"` | `RED_ORANGE` |
| orange | `"orange"` | `ORANGE` |
| lime | `"lime"` | `LIME` |
| green | `"green"` | `GREEN` |
| cyan | `"cyan"` | `CYAN` |
| violet | `"violet"` | `VIOLET` |
| pink | `"pink"` | `PINK` |
| blue | `"blue"` | `BLUE` |
| brown | `"brown"` | `BROWN` |
| light blue | `"light_blue"` | `LIGHT_BLUE` |
| purple | `"purple"` | `PURPLE` |
| 미선택 기본값 | **`"gray"`** | `GRAY` |

- **공백은 밑줄이 된다** — `red orange` → `"red_orange"`, `light blue` → `"light_blue"`.
- **13색 전부가 도메인 상수 이름의 소문자 스네이크와 일치한다.** 그래도 [`RoomMapper`](../../../../core/data/src/main/java/team/mino/core/data/repository/mapper/RoomMapper.kt)는 **표를 손으로 유지하고 이름에서 파생하지 않는다** — 도메인 이름이 바뀌었을 때 서버 계약이 조용히 따라 바뀌면 안 되기 때문이다(R-018).
- **`enum`의 순서를 칩 그리드에 반영하지 않는다.** 배포된 순서가 `RoomColor`의 선언 순서와 우연히 같지만, 그 선언 순서는 Figma가 소유한다. 두 순서를 서로 맞추면 한쪽이 바뀔 때 조용히 어긋난다.

**서버팀에 남은 사항**: 1번(`description.maxLength`)뿐이다. 3번은 초대 미리보기 쪽 예시 정리라 이 feature의 진행을 막지 않는다.

---

## 3. 응답 봉투

성공 응답은 모두 `{ "data": ... }`로 감싸여 있고, 실패 응답은 `{ "errorCode", "message" }`다.

**봉투를 벗기는 곳은 `RoomApiService` 하나다.** `DataSource`·`RepositoryImpl`·Mapper는 봉투를 모른다.

이 결정의 소유자는 이 문서가 아니라 **응답 봉투 ADR**(`docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md`)이다. `shared-link-receiver`가 먼저 같은 문제를 만나 정했고, 그 ADR이 "`group-room-form`이 mock을 걷어내고 실서버로 전환할 때 같은 타입을 쓴다"를 명시했다. 이 feature는 **따르기만 한다** — 타입을 새로 만들지 않는다([research.md](../research.md) R-025).

```
// network/dto/response/MinoResponse.kt  ← ADR이 소유. 이 feature가 만들지 않는다
@Serializable
internal data class MinoResponse<T>(val data: T)
```

**이 타입과 ADR은 이제 `develop`에 있다.** 기다리던 선행 조건이 닫혔고, 기존 `RoomApiService.listRooms()`가 이미 이 봉투를 벗기고 있다 — 이 feature가 더할 세 함수는 같은 형태를 잇기만 한다([research.md](../research.md) R-031).

- `RoomApiService`의 반환 타입에 `MinoResponse`가 드러나면 안 된다 — `body<MinoResponse<RoomResponse>>().data`로 그 줄에서 끝난다.
- `errorCode`·`message`는 DTO로 만들지 않는다 — `expectSuccess = true`가 비2xx를 예외로 바꾸고 `convertDomainException`이 코드만 남기므로 본문을 읽는 코드가 없다(§6).

---

## 4. 레이어 구성

> [!IMPORTANT]
> **이 계획은 방 데이터 레이어를 백지에서 세우지 않는다.** `RoomApiService`와 방 목록용 DataSource 한 벌이 `shared-link-receiver`의 손으로 이미 `develop`에 들어와 있다. 이 절의 `[확장]`·`[삭제]` 표기는 그 상태를 기준으로 한 것이다 — [research.md](../research.md) R-031·R-032.

```
:core:data/
├── network/
│   ├── dto/response/MinoResponse.kt    # [기존] 봉투 ADR 소유 — 이 feature가 만들지 않는다
│   └── service/RoomApiService.kt       # [확장] listRooms()가 이미 있다. 세 함수를 더한다
├── datasource/
│   ├── RoomRemoteDataSource.kt         # [확장] listRooms() 흡수
│   ├── RoomRemoteDataSourceImpl.kt     # [신규] RoomApiService 위임 (네 함수)
│   ├── RoomListRemoteDataSource.kt     # [삭제] 합병 — R-032
│   ├── RoomListRemoteDataSourceImpl.kt # [삭제] 합병 — R-032
│   └── di/
│       ├── RoomDataSourceModule.kt     # [수정] @Binds 대상을 실구현으로
│       └── RoomListDataSourceModule.kt # [삭제] 바인딩이 하나로 줄었다 — R-032
└── repository/
    ├── RoomRepositoryImpl.kt           # [수정] 생성자 인자 2개 → 1개
    └── mapper/
        ├── RoomMapper.kt               # [무변경] 색 식별자 표가 배포된 enum과 일치 (R-030)
        └── RoomSummaryMapper.kt        # [무변경] shared-link-receiver 소유
```

### `RoomApiService` — 확장

**이미 있는 파일이다.** `listRooms()`를 지우지 않는다 — 그 함수는 방 선택 시트의 유일한 데이터 경로다.

```
internal class RoomApiService @Inject constructor(private val client: HttpClient) {
    suspend fun listRooms(): List<RoomSummaryResponse>          // 기존 — 건드리지 않는다
    suspend fun getRoom(roomId: String): RoomResponse           // 추가
    suspend fun createRoom(request: RoomRequest): RoomResponse  // 추가
    suspend fun updateRoom(roomId: String, request: RoomRequest): RoomResponse  // 추가
}
```

더할 세 함수는 기존 함수가 이미 정해 둔 형태를 그대로 잇는다.

| 항목 | 계약 |
|---|---|
| 경로 | `defaultRequest.url` 기준 **상대** 경로 — `api/v1/rooms` · `api/v1/rooms/$roomId`. 앞에 `/`를 붙이지 않는다 |
| 반환 | `body<MinoResponse<RoomResponse>>().data` — 봉투는 이 줄에서 끝난다 |
| 예외 | 잡지 않는다. `convertDomainException`이 전역 매핑한다 |
| 가시성 | `internal` |

### `RoomRemoteDataSource` — 확장

**인터페이스가 바뀐다.** [R-024](../research.md#r-024-실서버가-붙었다--mock-데이터-레이어를-걷어낸다-plan-200)는 "한 글자도 바뀌지 않는다"고 적었으나, 그 뒤 같은 리소스에 DataSource가 하나 더 생겼고 이 계획이 둘을 합친다([R-032](../research.md#r-032-방-리소스의-두-datasource를-하나로-합친다-plan-300)).

```
internal interface RoomRemoteDataSource {
    suspend fun listRooms(): List<RoomSummaryResponse>   // RoomListRemoteDataSource에서 옮겨 온다
    suspend fun getRoom(roomId: String): RoomResponse
    suspend fun createRoom(request: RoomRequest): RoomResponse
    suspend fun updateRoom(roomId: String, request: RoomRequest): RoomResponse
}
```

- **시그니처를 바꾸지 않고 옮기기만 한다.** `listRooms()`의 반환 타입·실패 계약의 소유자는 그대로 `docs/specs/shared-link-receiver/contracts/room-list-api.md` §6이다. 이 문서는 그 함수가 **어느 인터페이스에 놓이는지**만 정한다.
- 구현체는 `RoomRemoteDataSourceImpl` 하나이며 네 함수 모두 `RoomApiService`에 위임만 한다([`core/data/README.md`](../../../../core/data/README.md) §5).

### `RoomRepositoryImpl` — 수정

과도기가 끝난다. KDoc이 스스로 적어 둔 *"출처가 함수마다 갈리는 과도기"*가 이번에 지워진다.

```
internal class RoomRepositoryImpl @Inject constructor(
    private val remoteDataSource: RoomRemoteDataSource,   // listRemoteDataSource가 사라진다
) : RoomRepository
```

네 함수의 본문과 Mapper 호출은 그대로다 — `getRooms()`가 무는 대상만 `listRemoteDataSource` → `remoteDataSource`로 바뀐다.

### 사라지는 것

| 파일 | 사유 |
|---|---|
| `datasource/RoomMockRemoteDataSourceImpl.kt` | 실구현으로 대체 — [R-024](../research.md#r-024-실서버가-붙었다--mock-데이터-레이어를-걷어낸다-plan-200) |
| `datasource/mock/RoomMockStore.kt` | 인메모리 저장소가 필요 없다. `mock/` 디렉터리째 사라진다 — R-024 |
| `datasource/RoomListRemoteDataSource.kt` | 합병 — [R-032](../research.md#r-032-방-리소스의-두-datasource를-하나로-합친다-plan-300) |
| `datasource/RoomListRemoteDataSourceImpl.kt` | 합병 — R-032 |
| `datasource/di/RoomListDataSourceModule.kt` | 바인딩이 하나로 줄었다 — R-032 |

> [!WARNING]
> 아래 다섯은 **삭제 대상이 아니다.** `RoomSummaryResponse` · `RoomSummaryMapper` · `RoomSummary`(도메인) · `RoomType`(도메인) · `RoomRepository.getRooms()`. 합병이 옮기는 것은 함수 선언 한 자리뿐이고, 방 목록의 DTO·변환·도메인 계약은 `shared-link-receiver`가 소유한 채로 남는다.

### 회귀 위험 — 다른 feature가 이 경로를 쓰고 있다

`getRooms()`는 방 선택 시트(`:feature:sharereceiver`)가 실제로 무는 경로다. 합병이 그 동작을 바꾸면 안 된다.

| 지켜야 할 것 | 확인 방법 |
|---|---|
| `listRooms()`의 경로·봉투 해제·기본값 흡수 | 이미 있는 `RoomApiServiceTest`의 기존 케이스가 그대로 통과 |
| `getRooms()`가 정렬하지 않고 받은 순서를 그대로 돌려준다 | `docs/specs/shared-link-receiver/contracts/room-list-api.md` §5 — 시트 쪽 UseCase 테스트가 덮는다 |
| 실패를 빈 목록으로 수렴시키지 않는다 | 같은 문서 — 수렴은 화면의 몫이다 |

---

## 5. 요청 본문 직렬화

`RoomRequest`는 생성·편집이 같은 타입을 쓰고 **세 값을 항상 함께 보낸다.** PATCH의 `required: []`를 부분 전송으로 쓰지 않는 이유는 [data-model.md](../data-model.md) §5가 소유한다.

> [!IMPORTANT]
> `description`에 **기본값을 두지 않는다.** `NetworkModule`의 `Json`은 `encodeDefaults`가 기본값 `false`라, `description: String? = null`로 선언하면 설명이 없을 때 그 필드가 **본문에서 통째로 빠진다.** PATCH에서 빠진 필드는 "건드리지 않았다"는 뜻이므로, **편집에서 설명을 지운 사용자의 변경이 조용히 사라진다.** 기본값을 없애면 `"description": null`이 항상 실린다([research.md](../research.md) R-027).

`name`·`color`는 nullable이 아니고 기본값도 없어 이 함정에 걸리지 않는다.

---

## 6. 실패

`convertDomainException`이 모든 비2xx를 `MinoDomainException.Http(code)`로, I/O 실패를 `Network`로 바꾼다. **이 feature가 새로 더할 매핑은 없다.**

| 상태 | 서버 `errorCode` | 도메인 | 도달 경로 |
|---|---|---|---|
| `401` | `UNAUTHORIZED` · `TOKEN_EXPIRED` · `USER_NOT_REGISTERED` | `Http(401)` | 세션·유저 등록 선행 조건 미충족(§7) |
| `403` | 방장이 아님 | `Http(403)` | 방장이 아닌 사용자가 편집을 시도. **mock에는 도달 경로가 없어 계약에서 뺐던 분기가 이제 실재한다** |
| `404` | — | `Http(404)` | 없는 `roomId`로 편집 진입 |

**`errorCode`로 분기하지 않는다.** spec의 실패 요구(UX-003·EC-009·EC-014)는 문구를 구분하지 않고 "입력값을 유지한 채 스낵바"뿐이며, 코드별 문구 매퍼는 [`error_handling.md`](../../../conventions/error_handling.md)가 이미 금지한 것이다. 실패의 성격 분류(State vs `DomainErrorEmitter`)는 [contracts/room-repository.md](./room-repository.md) §4가 소유한다.

---

## 7. 선행 조건 — 세션과 유저 등록

세 엔드포인트 모두 `401`에 `USER_NOT_REGISTERED`를 두고 있다. **신원 증명이 실려도 서버에 유저가 등록되어 있지 않으면 거절된다.**

| 선행 | 현재 상태 | 소유 |
|---|---|---|
| 익명 세션 확보 | `EnsureAnonymousSessionUseCase`가 `:core:domain`에 있으나 **호출하는 코드가 저장소에 없다** | `docs/specs/anonymous-auth-session` |
| 유저 등록 (`POST /api/v1/users`) | **구현이 없다.** `ProfileRepositoryImpl`은 로컬 저장뿐이다 | `docs/specs/profile` |

> [!WARNING]
> 세션이 없으면 `minoIdentityProofPlugin`이 `checkNotNull`로 **`IllegalStateException`을 던진다** — 도메인 예외가 아니므로 `runCatchingDomain`이 잡지 않고 CEH 안전망까지 올라간다. 그 판정은 플러그인의 호출자 계약 C-1 위반이라는 뜻이며 이 feature가 고칠 것이 아니다.

이 feature는 두 선행을 **구현하지 않는다.** 배선이 붙기 전까지 실기기 검증이 막히며, 그 사실과 우회는 [quickstart.md](../quickstart.md) §1이 소유한다.
