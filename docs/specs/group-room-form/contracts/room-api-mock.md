# 계약: 서버 API와 mock 구현

**대상 스펙 경로**: `docs/specs/group-room-form` · **부속 문서**: [plan.md](../plan.md)

[swagger 초안](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)(`0.1.0-draft`) 중 이 feature가 쓰는 엔드포인트와, 서버가 없는 동안 그것을 대신하는 mock의 계약이다.

> 레이어 구성·작성 규칙(`ApiService`·`DataSource`·`RepositoryImpl`·Mapper)은 [`core/data/README.md`](../../../../core/data/README.md)가 소유한다.

---

## 1. 쓰는 엔드포인트

| 메서드 · 경로 | 쓰는 곳 | 요청 | 응답 |
|---|---|---|---|
| `POST /api/v1/rooms` | `createRoom` | `CreateRoomRequest` | `201` → `{ data: Room }` |
| `PATCH /api/v1/rooms/{roomId}` | `updateRoom` | `UpdateRoomRequest` | `200` → `{ data: Room }` · `403` 방장 아님 |
| `GET /api/v1/rooms/{roomId}` | `getRoom` | — | `200` → `{ data: RoomDetail }` · `403` · `404` |

응답은 공통 인터셉터가 `{ "data": ... }`로 감싼다. 에러는 `{ "errorCode", "message" }`다. **mock도 같은 봉투를 흉내내지 않는다** — mock은 HTTP 레이어를 건너뛰고 DTO를 직접 돌려주므로 봉투를 벗기는 코드가 없다. 서버 연결 시 봉투 처리는 `ApiService`가 담당한다.

---

## 2. 계약이 spec과 어긋나는 지점

| # | swagger | 이 구현 | 근거 |
|---|---|---|---|
| 1 | `description.maxLength: 20` | **30자** | FR-005 · [research.md](../research.md) R-003 |
| 2 | `color`: 팔레트 **5색**의 hex(`"#FF6B6B"`) | **12색 + 회색의 식별자 문자열** | FR-006 · R-003 |
| 3 | `Room.color`는 hex, `InvitationPreview.color`는 `{id}` 객체 | 식별자 문자열로 통일 | 같은 문서 안의 자기모순 |

**색상 식별자 표기**

| `RoomColor` | 식별자 |
|---|---|
| `RED` · `RED_ORANGE` · `ORANGE` · `LIME` · `GREEN` · `CYAN` · `VIOLET` · `PINK` · `BLUE` · `BROWN` · `LIGHT_BLUE` · `PURPLE` · `GRAY` | `"red"` · `"red_orange"` · `"orange"` · `"lime"` · `"green"` · `"cyan"` · `"violet"` · `"pink"` · `"blue"` · `"brown"` · `"light_blue"` · `"purple"` · `"gray"` |

이 표기는 [research.md](../research.md) R-018에서 확정됐다. **서버가 다른 표현으로 확정하면 고칠 곳은 `RoomMapper` 한 파일이다.** 도메인·UI·mock 저장소 어디에도 이 문자열이 새어 나가지 않는다.

**서버팀에 제기할 사항**: 위 세 지점. 특히 2번은 hex를 계약으로 두면 앱이 팔레트 hex 사본을 data 레이어에 갖게 되어 디자인 시스템과 갈라진다(R-003).

---

## 3. mock 구현 계약

```
:core:data/datasource/
├── RoomRemoteDataSource.kt            # internal interface — DTO 반환
├── RoomMockRemoteDataSourceImpl.kt    # internal — 유일한 구현
├── mock/RoomMockStore.kt              # internal @Singleton — 인메모리 저장소
└── di/RoomDataSourceModule.kt         # @Binds @Singleton
```

### `RoomRemoteDataSource`

```
internal interface RoomRemoteDataSource {
    suspend fun getRoom(roomId: String): RoomResponse
    suspend fun createRoom(request: CreateRoomRequest): RoomResponse
    suspend fun updateRoom(roomId: String, request: UpdateRoomRequest): RoomResponse
}
```

### `RoomMockStore`

| 항목 | 계약 | 근거 |
|---|---|---|
| 보관 | `MutableMap<String, RoomResponse>`. 프로세스 수명 동안만 유지된다 | — |
| 동시성 | `Mutex`로 맵 접근을 감싼다 — **자료구조 보호가 이유다.** 중복 생성 차단은 `Mutex`가 아니라 `isSubmitting`이 한다(직렬화는 두 요청을 순서대로 처리할 뿐 둘 다 방을 만든다) | [research.md](../research.md) R-012 |
| `id` 생성 | 사람이 눈으로 읽을 수 있는 짧은 문자열. `UUID`를 쓰지 않는다 | — |
| 현재 사용자 | 고정 상수 `ownerId`. 만들어진 방의 `ownerId`가 그 값이 된다 | FR-010 |
| 시드 | 편집 경로를 바로 눌러 볼 수 있도록 공동방 1개(이름 `야호`, 설명 `야호호`, 색 `red`)와 개인방 1개를 미리 넣는다 | TS-018 |
| 지연 | 각 함수에 **관측 가능한 지연**을 둔다. 로딩과 중복 제출 차단이 눈으로 확인돼야 한다. 구체 값은 구현이 정한다 | UX-001·SC-005 |
| 실패 주입 | **두지 않는다.** 실패 경로는 Fake Repository를 쓰는 ViewModel 테스트가 소유한다 | [research.md](../research.md) R-002 |

### 실패 표현

mock이 던지는 예외도 도메인 예외여야 한다 — `HttpClient`의 전역 매핑을 타지 않기 때문이다.

| 상황 | 던지는 것 |
|---|---|
| `roomId`가 없다 | `MinoDomainException.Http(404, cause)` |

`cause`는 비-nullable이므로 실패 상황을 나타내는 표준 예외(`IOException` 등)를 넘긴다. 전용 표식 예외를 새로 만들지 않는다.

**403(방장 아님)은 두지 않는다.** 현재 사용자가 고정 상수이고 실패 주입도 없어 **도달 경로가 없다**. 검증되지 않는 분기를 계약에 남기지 않는다 — 권한 실패는 서버 연결 시 `convertDomainException`이 전역으로 매핑한다.

---

## 4. 실서버 전환 지점

바뀌는 곳은 셋이다.

1. `network/service/RoomApiService.kt` 신규 — Ktor 직접 호출, `{ data }` 봉투 해제.
2. `datasource/RoomRemoteDataSourceImpl.kt` 신규 — `RoomApiService`를 주입받는 구현.
3. `di/RoomDataSourceModule.kt`의 `@Binds` 대상을 mock에서 실구현으로 바꾼다.

**바뀌지 않는 곳**: DTO · Mapper · `RoomRepositoryImpl` · `:core:domain` 전체 · `:feature:roomform` 전체. 예외 매핑도 그대로다 — `convertDomainException`이 이미 전역으로 걸려 있어 새 API에 매핑을 붙일 일이 없다([`core/data/README.md`](../../../../core/data/README.md) §4).

`NetworkModule`의 임시 `baseUrl`(GitHub)을 `BuildConfig.API_BASE_URL`로 교체하는 것은 이 feature의 몫이 아니다 — 같은 README가 실서버 배포 전 과제로 이미 적어 두었다.
