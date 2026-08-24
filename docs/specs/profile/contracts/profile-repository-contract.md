# 계약: 프로필 도메인 (`:core:domain` ↔ `:core:data`)

프로필 데이터를 읽고 쓰는 계약. 모델의 필드·규칙과 저장 키는 [`data-model.md`](../data-model.md)가 소유한다. 이 문서는 도메인 표면과 실패 계약만 정한다.

> **범위**: 이번 설계에 원격 API는 없다([research.md D22](../research.md#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)). 아래 표면이 넓어지는 시점과 그때 바뀌는 지점은 [research.md D24](../research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 미리 고정했다.

## Repository (`core:domain/repository/ProfileRepository.kt`)

```kotlin
interface ProfileRepository {
    fun observeProfile(): Flow<Profile?>
    suspend fun saveProfile(profile: Profile)
}
```

| 멤버 | 성공 | 실패 |
|---|---|---|
| `observeProfile()` | 저장된 값이 없으면 `null`, 있으면 값. 저장이 일어날 때마다 새 값을 흘린다 | 도메인 실패를 정의하지 않는다 |
| `saveProfile(profile)` | 닉네임·아바타 id를 함께 덮어쓴다. 반환값 없음 | `MinoDomainException` |

- **멤버가 둘뿐인 이유**는 [research.md D23](../research.md#d23-repository-표면--observeprofile--saveprofile-두-멤버)에 있다. 등록/수정 분기(`registerProfile`·`updateProfile`)와 `refreshProfile()`은 이번 범위에서 부를 곳이 없어 열지 않는다.
- `saveProfile`이 값을 돌려주지 않는 이유도 같은 항목에 있다 — 저장된 값의 원천은 `observeProfile()` 하나다.
- 구현체는 `:core:data`의 `internal class ProfileRepositoryImpl`이고 바인딩은 그 모듈의 `repository/di/`가 소유한다([DI 규칙](../../../conventions/dependency-injection.md)).
- 예외를 잡아 `Result`로 바꾸지 않는다. 소비는 ViewModel의 `runCatchingDomain`이 한다([에러 처리 규약](../../../conventions/error_handling.md) §3).
- 로컬 단독 구간에서 `saveProfile`의 실패는 디스크 이상 같은 예외 상황뿐이다. 통로를 지금 배선하는 이유는 [research.md D25](../research.md#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다)에 있다.
- **다만 이번 범위에는 `MinoDomainException`을 만드는 지점이 없다.** DataStore는 이 변경으로 처음 소비되는 원천인데 매핑 지점을 두지 않기로 했으므로([research.md D30](../research.md#d30-로컬-저장-실패용-도메인-예외-리프를-추가하지-않는다)), 실제 저장 실패는 도메인 예외가 아니라 CEH까지 간다. 위 표의 실패 계약은 **원격 연동에서 매핑 지점이 생길 때 실제로 성립**하며, 그 사실을 인터페이스 KDoc이 함께 든다.

## UseCase (`core:domain/usecase/`)

```kotlin
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(rawNickname: String): Boolean
}

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val validateNickname: ValidateNicknameUseCase,
) {
    suspend operator fun invoke(rawNickname: String, avatarId: Int)
}
```

| UseCase | 책임 |
|---|---|
| `ValidateNicknameUseCase` | 앞뒤 공백을 제거한 값이 한글 음절·영문 알파벳만으로 2자 이상인지 판정한다(FR-002). 화면의 실시간 판정(UX-002)과 저장 경로가 같은 것을 쓴다 |
| `SaveProfileUseCase` | ① 판정 통과 확인 → ② 앞뒤 공백 제거 → ③ `Profile(trimmed, avatarId)`로 `saveProfile` 호출 |

- 판정 실패는 정상 흐름에서 도달할 수 없는 경로다 — 도메인 예외로 감싸지 않고 프로그래머 오류로 전파한다(에러 처리 규약 §1의 "버그" 갈래).
- 아바타 미선택 저장(EC-002)에서 기본 아바타의 `avatarId`를 채우는 것은 화면의 책임이다. UseCase는 유효한 `avatarId`가 온다고 전제한다.
- `SaveProfileUseCase`는 `DeviceRepository`를 알지 않는다. 기기 식별자를 요구하던 것은 등록 요청이었고, 그 요청이 이번 범위에 없다([research.md D14](../research.md#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다)).

## 건드리지 않는 기존 표면

| 대상 | 판정 |
|---|---|
| `DeviceRepository.ensureDeviceId()` | **그대로 `Unit` 반환.** plan 1.1.0이 `String`으로 넓히려던 변경을 철회한다 |
| `EnsureDeviceIdUseCase` | 그대로 |
| `network/di/NetworkModule` · `HttpClientConfig` | 그대로. 이번 범위는 네트워크를 쓰지 않는다 |
| `storage/DataStoreModule` | 그대로 쓴다. 새 DataStore 인스턴스를 만들지 않는다 |

## 저장 계층 (`core:data`)

```kotlin
internal interface ProfileLocalDataSource {
    fun observeProfile(): Flow<Profile?>
    suspend fun saveProfile(profile: Profile)
}
```

> **미해결 — 규약과 충돌한다.** 아래 형태(로컬 DataSource가 도메인 모델을 반환하고 `Preferences → Profile` 변환을 직접 한다)는 [`core:data` README](../../../../core/data/README.md) §5("반환 타입: DTO만 반환, 도메인 모델 반환 금지" · "책임: 변환 없음")·§2("변환은 `RepositoryImpl` 안에서 끝난다")와 정면으로 어긋난다. 로컬 DataSource도 §5가 "작성 규칙: 원격과 동일"로 못박아 같은 규칙이 걸린다.
>
> **어느 쪽으로도 규약을 다 지킬 수 없다.** Preferences에는 자연적 DTO가 없고(원시 형태가 `Preferences` 자체), README가 "키 상수는 DataSource 구현체 안에 둔다"고 정해 변환을 `RepositoryImpl`로 옮기면 키가 밖으로 새어 같은 README를 다시 위반한다. `ProfileEntry`를 끼워도 DataSource의 "변환 없음"은 못 지키고 문면만 만족한다.
>
> 해소는 둘 중 하나이며 **이 문서가 임의로 정하지 않는다**: (a) README에 "DTO 없는 로컬 DataSource" 갈래를 보완한다, (b) 이 계약을 바꿔 `ProfileEntry`를 도입한다.

- DataStore 키와 미저장 판정은 [`data-model.md` §3](../data-model.md)이 소유한다.
- 원격이 없어 DTO가 없으므로 이번 범위에는 **매퍼가 없다.** `ProfileLocalDataSourceImpl`이 `Preferences`에서 `Profile`을 직접 조립한다 — 중간 형태(`ProfileEntry`)를 두면 필드가 같은 타입을 한 겹 더 만드는 것뿐이다. 원격이 붙어 DTO가 생기면 그때 `mapper/`가 필요해진다.
- `observeProfile()`은 `dataStore.data`를 `map`으로 변환해 흘린다. 두 키 중 하나라도 없으면 `null`이다.

## 테스트 계약

| 대상 | 방식 |
|---|---|
| `ValidateNicknameUseCase` | JVM 단위 테스트 — `민`·`abc1`·`  민호  `·공백만·한글 30자·낱자(`ㄱㄱ`) (TS-012·TS-013·TS-017, EC-008·EC-009) |
| `SaveProfileUseCase` | Fake Repository로 ① trim된 값이 저장되는지 ② 무효 입력이 차단되는지 ③ Repository가 던진 예외가 그대로 전파되는지 |
| `ProfileLocalDataSourceImpl` | 저장 → `observeProfile()` 왕복, 키가 하나만 있을 때 `null`, 두 키가 한 번에 쓰이는지 |
| `ProfileRepositoryImpl` | DataSource를 Fake로 두고 위임이 그대로인지 |
| `ProfileViewModel` | Fake Repository — 프리필, 실시간 판정, `지우기`, 저장 중 두 번째 인텐트 무시(UX-003·EC-004), 저장 실패 시 입력값 보존(FR-012) |
