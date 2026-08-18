# 계약: 프로필 원격 API (`:core:data`)

이 기능이 소비하는 서버 계약의 **스냅숏**과 그것을 코드로 옮기는 형태. 원본은 꾹 API 초안이며 브랜치 문서라 이동·변경될 수 있다.

> 원본: `https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml` (`openapi 3.0.3`, `info.version 0.1.0-draft`, 확인일 2026-08-18)

## 1. 공통 규약

| 항목 | 내용 |
|---|---|
| 성공 응답 | `{ "data": ... }`로 감싼다. 본문 없는 성공은 `{ "data": { "ok": true } }` |
| 에러 응답 | `{ "errorCode": string, "message": string }` |
| 인증 | Bearer 토큰(잠정). 인증 예외는 `POST /api/v1/users` 등 일부뿐 |
| baseUrl | prod `https://api.gguk.org`, local `http://localhost:3000` |

- 봉투(`data`) 해제는 DTO 레벨에서 한다 — `@Serializable data class ApiResponse<T>(val data: T)`. 도메인·화면은 봉투를 모른다.
- `errorCode` 본문은 이번 설계에서 읽지 않는다. 비2xx는 `HttpClient` validator가 `MinoDomainException.Http(code)`로 전역 매핑한다([에러 처리 규약](../../../conventions/error_handling.md) §3). 리프를 코드별로 쪼개는 것은 문구 정책이 정해질 때 별도로 다룬다.
- 인증 헤더는 이번 범위에서 배선하지 않는다(research.md D20).

## 2. 소비하는 엔드포인트

### `POST /api/v1/users` — 유저 등록 (+ 개인방 자동 생성)

| 항목 | 내용 |
|---|---|
| 인증 | 불필요 |
| 요청 | `{ deviceId: string, nickname: string, avatar: { id: int } }` (`deviceId`·`nickname` 필수) |
| 성공 | `201` → `{ data: User }` |
| 실패 | `409` 중복 등록 |
| 쓰는 곳 | 저장 시 캐시에 프로필이 없을 때(research.md D14) |

- 개인방(`내 장소`)은 이 요청으로 서버가 함께 만들며 응답에 포함되지 않는다 — FR-008의 트리거가 이 호출이다(research.md D17).
- `deviceId`는 `DeviceRepository`가 확보한 값이다([repository 계약](profile-repository-contract.md)).

### `PATCH /api/v1/users/me` — 프로필 수정

| 항목 | 내용 |
|---|---|
| 인증 | 필요(Bearer) |
| 요청 | `{ nickname?: string, avatar?: { id: int } }` |
| 성공 | `200` → `{ data: User }` |
| 실패 | `401` |
| 쓰는 곳 | 저장 시 캐시에 프로필이 있을 때 |

- 두 값을 모두 보낸다. 화면이 "무엇이 바뀌었는지"를 따로 추적하지 않으며, 값을 바꾸지 않은 저장(EC-006)도 같은 요청을 보낸다.

### `GET /api/v1/users/me` — 내 프로필 조회

| 항목 | 내용 |
|---|---|
| 인증 | 필요(Bearer) |
| 성공 | `200` → `{ data: User }` |
| 실패 | `401` |
| 쓰는 곳 | **이번 화면은 호출하지 않는다.** `ProfileRepository.refreshProfile()`로 계약만 열어 두고, 호출 시점(스플래시·앱 시작 동기화)은 그 화면의 스펙이 정한다 |

### 스키마

```
User     { id: uuid, nickname: Nickname, avatar: Avatar, createdAt: date-time }
Nickname string, 2~15자, "공백 포함 한글/영문, 특수문자 불가"
Avatar   { id: integer }   // 12종과 id 값의 대응표는 API 문서에 없음 (research.md D18)
```

- `User.id`·`createdAt`은 이번 범위에서 소비하지 않는다. DTO에는 두되 도메인 모델로 넘기지 않는다 — spec에 이 값을 쓰는 요구사항이 없다.
- **`Nickname` 규칙이 spec과 어긋난다**(상한·공백). 클라이언트는 spec을 따르고 서버 거절은 저장 실패로 받는다(research.md D19).

## 3. 코드 배치 (`core:data`)

| 파일 | 역할 |
|---|---|
| `network/dto/request/RegisterUserRequest.kt` · `UpdateProfileRequest.kt` | 요청 DTO (`@Serializable`) |
| `network/dto/response/UserResponse.kt` · `AvatarResponse.kt` | 응답 DTO |
| `network/dto/response/ApiResponse.kt` | `{ data }` 봉투 (공용) |
| `network/service/UserApiService.kt` | Ktor 직접 호출 (`internal`, 예외를 잡지 않는다) |
| `datasource/ProfileRemoteDataSource.kt`(+`Impl`) · `di/` | 원격 DataSource (DTO 반환) |
| `datasource/ProfileLocalDataSource.kt`(+`Impl`) · `di/` | 캐시 DataSource (공유 DataStore) |
| `repository/ProfileRepositoryImpl.kt` · `mapper/ProfileMapper.kt` · `di/` | 원격 호출 + 캐시 갱신 + 도메인 변환 |

- 절차·명명·가시성 규칙은 [core:data README §4~§8](../../../../core/data/README.md)을 그대로 따른다. 이 문서가 다시 쓰지 않는다.
- `ApiResponse`는 유저 엔드포인트 밖에서도 쓰이는 공용 봉투다. 다른 기능이 먼저 만들었다면 그것을 쓴다.

## 4. 목(mock) 계약

`:core:data`의 flavor 소스셋으로 엔진을 가른다(research.md D15).

| 소스셋 | 엔진 | 의존 |
|---|---|---|
| `src/qa/` | Ktor `MockEngine` | `qaImplementation(libs.ktor.client.mock)` |
| `src/prod/` | `OkHttp` | 기존 그대로 |

`NetworkModule`은 `HttpClientEngine`을 주입받아 클라이언트를 조립하는 형태로 바꾼다. `expectSuccess`·`convertDomainException`·`ContentNegotiation`·`Logging` 설정은 한 곳에 그대로 둔다.

### 목 핸들러가 답하는 것

| 요청 | 응답 |
|---|---|
| `POST /api/v1/users` | 등록된 유저가 없으면 `201` + `{data: User}`(요청 값 그대로 + 생성 id·시각). 이미 있으면 `409` |
| `GET /api/v1/users/me` | 등록된 유저가 있으면 `200`, 없으면 `401` |
| `PATCH /api/v1/users/me` | 등록된 유저가 있으면 병합해 `200`, 없으면 `401` |
| 그 밖의 모든 경로 | `501` — 아직 목이 없는 API임을 드러낸다 |

| 스위치 | 기본값 | 용도 |
|---|---|---|
| 응답 지연 | 300ms 내외 | `isSaving` 동작(UX-003·EC-004)을 눈으로 확인 |
| 강제 실패 | 꺼짐 | 켜면 저장 요청에 `500`을 돌려준다 — FR-012·TS-006·EC-003·EC-007 재현 |

- 등록 상태는 **프로세스 메모리**에만 산다. 앱을 다시 켜면 목 서버는 비어 있고, 화면의 프리필은 로컬 캐시가 담당한다.
- 목은 요청의 인증 헤더를 검사하지 않는다.
- 이 계약은 실서버·인증이 확정되면 통째로 사라진다. 그때 qa 소스셋의 엔진을 `OkHttp`로 바꾸고 `Flavor.apiBaseUrl`을 실제 값으로 채운다.
