# 계약: 프로필 도메인 (`:core:domain` ↔ `:core:data`)

프로필을 읽고 쓰는 계약. 모델의 필드·규칙과 저장 형태는 [`data-model.md`](../data-model.md)가, 서버 엔드포인트는 [`profile-api-contract.md`](profile-api-contract.md)가 소유한다. 이 문서는 도메인 표면과 실패 계약만 정한다.

> **범위**: 원천은 서버이고 로컬 DataStore는 캐시다([research.md D36](../research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)). plan 3.0.0까지의 로컬 단독 구간은 끝났다.

## Repository (`core:domain/repository/ProfileRepository.kt`)

```kotlin
interface ProfileRepository {
    fun observeProfile(): Flow<Profile?>
    suspend fun refreshProfile()
    suspend fun saveProfile(profile: Profile)
}
```

| 멤버 | 성공 | 실패 |
|---|---|---|
| `observeProfile()` | 캐시된 값이 없으면 `null`, 있으면 값. 캐시가 바뀔 때마다 새 값을 흘린다 | 도메인 실패를 정의하지 않는다 |
| `refreshProfile()` | 서버에서 프로필을 받아 캐시에 쓴다. **미등록이면 캐시를 비우고 정상 종료한다** | `MinoDomainException` (네트워크·미등록 아닌 HTTP 실패) |
| `saveProfile(profile)` | 서버에 반영한 뒤 캐시를 갱신한다. 반환값 없음 | `MinoDomainException` |

- 세 멤버인 이유는 [research.md D39](../research.md#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버)에 있다. 저장을 `registerProfile`·`updateProfile`로 가르지 않는 이유는 [D38](../research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)에 있다.
- `refreshProfile()`·`saveProfile()` 모두 값을 돌려주지 않는다 — 결과를 읽는 원천은 `observeProfile()` 하나다.
- **미등록은 실패가 아니다.** 온보딩 진입에서는 미등록이 정상 상태이므로, `refreshProfile()`이 예외를 던지면 프로필을 처음 만드는 사용자가 화면에 들어서자마자 오류 스낵바를 본다.
- 구현체는 `:core:data`의 `internal class ProfileRepositoryImpl`이고 바인딩은 그 모듈의 `repository/di/`가 소유한다([DI 규칙](../../../conventions/dependency-injection.md)).
- 예외를 잡아 `Result`로 바꾸지 않는다. 소비는 ViewModel의 `runCatchingDomain`이 한다([에러 처리 규약](../../../conventions/error_handling.md) §3).

### 저장의 불변식

| # | 규칙 | 근거 |
|---|---|---|
| 1 | **원격 성공 → 캐시 갱신** 순서다. 원격이 실패하면 캐시를 건드리지 않는다 | FR-012·SC-006 — 저장이 실패했는데 캐시가 바뀌면 화면을 다시 열었을 때 저장되지 않은 값이 프리필된다 |
| 2 | 캐시에 프로필이 없으면 등록(`POST`), 있으면 수정(`PATCH`) | [D38](../research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다) |
| 3 | `409 USER_ALREADY_REGISTERED`는 **저장 실패**다. 자동으로 `PATCH`로 갈아타지 않는다 | 같은 항목 — 복구는 다음 진입의 `refreshProfile()`이 맡는다 |
| 4 | 닉네임과 아바타는 항상 함께 나간다. 부분 전송을 하지 않는다 | [API 계약 §1](profile-api-contract.md) |

## UseCase (`core:domain/usecase/`)

```kotlin
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(rawNickname: String): Boolean
}

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val validateNickname: ValidateNicknameUseCase,
) {
    suspend operator fun invoke(rawNickname: String, avatar: ProfileAvatar)
}
```

| UseCase | 책임 |
|---|---|
| `ValidateNicknameUseCase` | 앞뒤 공백을 제거한 값이 한글 음절·영문 알파벳만으로 2자 이상인지 판정한다(FR-002). 화면의 실시간 판정(UX-002)과 저장 경로가 같은 것을 쓴다. **길이 상한은 판정하지 않는다** — 15자는 오류가 아니라 입력 차단이며 `ProfileViewModel`이 소유한다(FR-014, [D51](../research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)) |
| `SaveProfileUseCase` | ① 판정 통과 확인 → ② 앞뒤 공백 제거 → ③ `Profile(trimmed, avatar)`로 `saveProfile` 호출 |

- `avatar` 파라미터의 타입이 `Int`에서 `ProfileAvatar`로 바뀐다([D37](../research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)).
- **클라이언트 검증은 spec을 따르고, 이제 서버 스키마와 완전히 같다** — 길이 `2~15자`·문자 `한글 음절·영문`·공백 불가가 양쪽에서 일치한다([API 계약 §2](profile-api-contract.md)의 소멸 항목). spec 3.0.0이 상한을 채택하고 서버가 `pattern`에서 공백을 뺀 결과다.
- **그래도 상한을 이 UseCase에 심지 않는다.** spec FR-002가 "길이 상한 15자는 이 판정에 넣지 않는다"로 명시하고 FR-014가 그것을 입력 차단으로 돌린다. 심으면 오류 상태가 생기고, 디자인에 없는 "15자 초과" 문구를 만들어야 한다([D51](../research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)). 같은 저장소의 `ValidateRoomNameUseCase`가 방 이름 15자를 판정하지 않는 것과 같은 형태다.
- 판정 실패는 정상 흐름에서 도달할 수 없는 경로다 — 도메인 예외로 감싸지 않고 프로그래머 오류로 전파한다(에러 처리 규약 §1의 "버그" 갈래).
- 아바타 미선택 저장(EC-002)에서 기본 아바타를 채우는 것은 화면의 책임이다. UseCase는 유효한 값이 온다고 전제한다. **채워 넣는 값 자체는 도메인이 소유한다**(`ProfileAvatar.Default`) — 화면은 그것을 지목만 한다([D53](../research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)).
- `refreshProfile()`을 감싸는 UseCase는 두지 않는다. 판정도 조합도 없는 위임 한 줄이므로 ViewModel이 Repository를 직접 부른다([`core/domain/README.md`](../../../../core/domain/README.md)).

## 건드리지 않는 기존 표면

| 대상 | 판정 |
|---|---|
| `network/di/NetworkModule` · `HttpClientConfig` | **그대로.** 인증 플러그인·봉투 아닌 전역 설정·예외 매핑이 이미 필요한 전부다([D41](../research.md#d41-목-엔진을-만들지-않는다)) |
| `MinoIdentityProofPlugin` | 그대로. Bearer 첨부는 이미 전역이며 이 feature가 더할 것이 없다 |
| `MinoDomainException` | 리프를 추가하지 않는다. `Http`·`Network`로 충분하다([D30](../research.md#d30-로컬-저장-실패용-도메인-예외-리프를-추가하지-않는다) 보정) |
| `storage/DataStoreModule` | 그대로 쓴다. 새 DataStore 인스턴스를 만들지 않는다 |
| `RoomMockRemoteDataSourceImpl` 등 방의 mock | 그대로. [group-room-form](../../group-room-form/contracts/room-api-mock.md)의 소관이다 |

## 저장 계층 (`core:data`)

**원격 DataSource를 새로 만들지 않는다.** `user` 태그 엔드포인트는 develop이 이미 만든 `UserRemoteDataSource`가 소유하므로 그것을 넓힌다([D49](../research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다) · [API 계약 §3](profile-api-contract.md)).

```kotlin
internal interface UserRemoteDataSource {
    /** 기존 — splash-screen의 진입 판정. 본문을 읽지 않는다. */
    suspend fun isRegistered(): Boolean

    /** 신규 — 미등록이면 `null`. */
    suspend fun getMe(): ProfileResponse?

    suspend fun register(request: ProfileRequest): ProfileResponse

    suspend fun updateMe(request: ProfileRequest): ProfileResponse
}

internal interface ProfileLocalDataSource {
    fun observeProfile(): Flow<ProfileEntry?>
    suspend fun saveProfile(entry: ProfileEntry)
    suspend fun clearProfile()
}
```

- **두 DataSource 모두 DTO만 반환한다.** 로컬이 `ProfileEntry`를 돌려주게 되면서 plan 3.0.0에 남아 있던 [`core:data` README](../../../../core/data/README.md) §5·§2 위반이 닫힌다([D42](../research.md#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다)).
- 키 상수(`profile_nickname`·`profile_avatar`)는 `ProfileLocalDataSourceImpl` 안에 남는다(README §5). DataSource는 `Preferences` ↔ `ProfileEntry`까지만 알고 도메인 타입을 모른다.
- `clearProfile()`은 `refreshProfile()`이 미등록을 확인했을 때만 부른다. 두 키를 같은 `edit {}`에서 지운다.
- 변환은 `ProfileRepositoryImpl` 안에서 끝난다 — `ProfileMapper`가 `ProfileResponse`·`ProfileEntry` ↔ `Profile`을 맡고, 아바타 문자열 표를 소유한다([API 계약 §3](profile-api-contract.md)).
- **`isRegistered()`는 이 feature가 쓰지 않는다.** 같은 인터페이스에 있지만 소비자는 splash-screen(`ProfileRegistrationRepository`)이다. 이 feature가 그 함수의 계약을 바꾸지 않는다 — 아래 §건드리지 않는 기존 표면과 같은 성격이다.
- `UserRemoteDataSourceImpl`은 `UserApiService`로 위임만 한다. develop이 그 안에 두었던 `401` 본문 파싱은 `UserApiService`로 옮겨간다([D49](../research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)).

## 테스트 계약

| 대상 | 방식 |
|---|---|
| `ValidateNicknameUseCase` | JVM 단위 테스트 — `민`·`abc1`·`  민호  `·공백만·낱자(`ㄱㄱ`)·중간 공백 (TS-012·TS-013, EC-008·EC-009). **한글 30자 케이스는 유지하되 의미가 바뀐다** — "상한 없이 통과"가 아니라 **"상한은 이 판정의 몫이 아니다"** 를 고정하는 케이스다. 상한 자체(TS-017·TS-020)는 `ProfileViewModelTest`가 검증한다 |
| `SaveProfileUseCase` | Fake Repository로 ① trim된 값이 저장되는지 ② 무효 입력이 차단되는지 ③ Repository가 던진 예외가 그대로 전파되는지 |
| `UserApiService` | `MockEngine` — 봉투(`{data}`) 해제, `401 USER_NOT_REGISTERED` → `getMe()` `null` / `hasProfile()` `false`, 다른 401·409의 전파, **`hasProfile()`이 성공 본문 스키마에 의존하지 않는지**(develop의 `{"data":{"id":1}}` 픽스처가 지키던 사실) |
| `UserRemoteDataSourceImpl` | 네 함수가 서비스로 위임만 하는지. 기존 `isRegistered()` 테스트 중 `401` 판정 케이스는 `UserApiService` 쪽으로 옮겨간다 |
| `ProfileMapper` | 아바타 문자열 왕복 **13종**(기본 아바타 ↔ `gray` 포함), 모르는 문자열·`null` 아바타 → 기본 아바타 |
| `ProfileLocalDataSourceImpl` | 저장 → `observeProfile()` 왕복, 키가 하나만 있을 때 `null`, `clearProfile()` 후 `null` |
| `ProfileRepositoryImpl` | Fake DataSource — 등록/수정 분기, **원격 실패 시 캐시 불변**, 미등록 시 캐시 비움 |
| `ProfileViewModel` | Fake Repository — 프리필, 실시간 판정, `지우기`, 저장 중 두 번째 인텐트 무시(UX-003·EC-004), 저장 실패 시 입력값 보존(FR-012), **아바타 미선택 저장이 `ProfileAvatar.Default`를 넘기는지**(TS-023), **기본 아바타로 저장된 프로필을 프리필하면 `selectedAvatar`가 `null`이 되는지**([D53](../research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)) |

범위와 근거는 [research.md D43](../research.md#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)에 있다.
