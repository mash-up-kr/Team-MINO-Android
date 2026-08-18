# 계약: 프로필 도메인 (`:core:domain` ↔ `:core:data`)

프로필 데이터를 읽고 쓰는 계약. 모델의 필드·규칙은 [`data-model.md`](../data-model.md)가, 서버 엔드포인트와 목 동작은 [`profile-api-contract.md`](profile-api-contract.md)가 소유한다. 이 문서는 도메인 표면과 실패 계약만 정한다.

## Repository (`core:domain/repository/ProfileRepository.kt`)

```kotlin
interface ProfileRepository {
    fun observeProfile(): Flow<Profile?>
    suspend fun refreshProfile(): Profile
    suspend fun registerProfile(deviceId: String, profile: Profile): Profile
    suspend fun updateProfile(profile: Profile): Profile
}
```

| 멤버 | 원격 | 성공 | 실패 |
|---|---|---|---|
| `observeProfile()` | 없음 — 로컬 캐시를 흘린다 | 캐시가 비었으면 `null`, 있으면 값. 캐시가 갱신될 때마다 새 값 | 도메인 실패를 정의하지 않는다 |
| `refreshProfile()` | `GET /users/me` | 서버 값으로 캐시를 갱신하고 그 값을 반환 | `MinoDomainException` |
| `registerProfile(...)` | `POST /users` | 응답으로 캐시를 갱신하고 그 값을 반환 | `MinoDomainException`(중복 등록은 `Http(409)`) |
| `updateProfile(...)` | `PATCH /users/me` | 응답으로 캐시를 갱신하고 그 값을 반환 | `MinoDomainException` |

- **캐시 갱신은 원격 성공 뒤에만 일어난다.** 실패하면 캐시는 이전 값 그대로다 — 사용자가 실패 후 재시도할 때 화면·저장 상태가 어긋나지 않는다(FR-012, SC-006).
- 구현체는 `:core:data`의 `internal class ProfileRepositoryImpl`이고 바인딩은 그 모듈의 `repository/di/`가 소유한다([DI 규칙](../../../conventions/dependency-injection.md)).
- 예외를 잡아 `Result`로 바꾸지 않는다. 소비는 ViewModel의 `runCatchingDomain`이 한다([에러 처리 규약](../../../conventions/error_handling.md) §3).
- `refreshProfile()`은 이번 화면이 호출하지 않는다. 호출 시점은 앱 시작 동기화를 정의하는 스펙이 정한다.

## 기기 식별자 (`core:domain/repository/DeviceRepository.kt`)

```kotlin
interface DeviceRepository {
    suspend fun ensureDeviceId(): String   // 변경 — 기존 반환 타입은 Unit
}
```

- 등록 요청이 `deviceId`를 요구하므로 확보한 값을 돌려주도록 넓힌다. 멱등 동작(이미 있으면 재사용)은 그대로다.
- 프로덕션 호출자가 아직 없어 영향 범위는 `DeviceRepositoryImplTest`와 `EnsureDeviceIdUseCase`의 반환 타입뿐이다(research.md D14).

## UseCase (`core:domain/usecase/`)

```kotlin
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(rawNickname: String): Boolean
}

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceRepository: DeviceRepository,
    private val validateNickname: ValidateNicknameUseCase,
) {
    suspend operator fun invoke(rawNickname: String, avatarId: Int): Profile
}
```

| UseCase | 책임 |
|---|---|
| `ValidateNicknameUseCase` | 앞뒤 공백을 제거한 값이 한글 음절·영문 알파벳만으로 2자 이상인지 판정한다(FR-002). 화면의 실시간 판정(UX-002)과 저장 경로가 같은 것을 쓴다 |
| `SaveProfileUseCase` | ① 판정 통과 확인 → ② 앞뒤 공백 제거 → ③ 캐시에 프로필이 있으면 `updateProfile`, 없으면 `ensureDeviceId()` 후 `registerProfile` → ④ 저장된 프로필 반환 |

- 등록/수정 갈래를 UseCase가 고르는 이유는 그것이 "서버에 내 유저가 있는가"라는 비즈니스 판단이기 때문이다(research.md D14). 화면은 어느 쪽이 호출됐는지 모른다.
- 판정 실패는 정상 흐름에서 도달할 수 없는 경로다 — 도메인 예외로 감싸지 않고 프로그래머 오류로 전파한다(에러 처리 규약 §1의 "버그" 갈래).
- 아바타 미선택 저장(EC-002)에서 기본 아바타의 `avatarId`를 채우는 것은 화면의 책임이다. UseCase는 유효한 `avatarId`가 온다고 전제한다.
- `SaveProfileUseCase`가 `deviceRepository`를 직접 아는 것은 등록 요청의 인자를 채우기 위해서다. 화면은 기기 식별자를 다루지 않는다.

## 저장 계층 (`core:data`)

```kotlin
internal interface ProfileRemoteDataSource {
    suspend fun getProfile(): UserResponse
    suspend fun registerUser(request: RegisterUserRequest): UserResponse
    suspend fun updateProfile(request: UpdateProfileRequest): UserResponse
}

internal interface ProfileLocalDataSource {
    fun observeProfile(): Flow<ProfileEntry?>
    suspend fun saveProfile(entry: ProfileEntry)
}
```

- 원격은 DTO만 반환하고, DTO → 도메인 변환은 `ProfileRepositoryImpl`에서 끝난다([core:data README §6](../../../../core/data/README.md)).
- `ProfileEntry`는 캐시에 담기는 형태(닉네임·아바타 id)이며 `internal`이다. 키·미저장 판정은 [`data-model.md` §3](../data-model.md)에 있다.
- 새 DataStore 인스턴스를 만들지 않고 `storage/DataStoreModule`의 단일 인스턴스를 공유한다.

## 테스트 계약

| 대상 | 방식 |
|---|---|
| `ValidateNicknameUseCase` | JVM 단위 테스트 — `민`·`abc1`·`  민호  `·공백만·한글 30자·낱자(`ㄱㄱ`) (TS-012·TS-013·TS-017, EC-008·EC-009) |
| `SaveProfileUseCase` | Fake Repository로 ① trim된 값이 저장되는지 ② 캐시 유무에 따라 등록/수정이 갈리는지 ③ 무효 입력이 차단되는지 |
| `ProfileRepositoryImpl` | `MockEngine` 기반 테스트 — 봉투 해제·DTO 매핑·비2xx의 `MinoDomainException` 변환·**실패 시 캐시가 그대로인지**(research.md D21). 기존 `DomainExceptionMappingTest` 방식을 따른다 |
| `DeviceRepositoryImpl` | 반환 타입 변경에 맞춰 기존 테스트 갱신 |
