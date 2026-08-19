# 데이터 모델: 마이페이지 & 환경설정

Phase 1 산출물. 현재 설계 상태만 담는다(개정 시 이 파일은 통째로 대체된다).

## 1. 도메인 모델 (`core:domain/model/`)

### `Profile`
```kotlin
data class Profile(
    val nickname: String,
    val avatarId: Int,   // 백엔드 Avatar 스키마 { id: integer } 그대로 — design-system 12종 아바타 카탈로그의 키 (research.md D8·D9)
)
```
- 앱 전체 단일 인스턴스. 검증(2자 이상 등)은 도메인이 아니라 `feature:mypage`의 `ProfileUiState` 계산 로직이 담당 — spec FR-004는 UX 규칙이지 도메인 불변식이 아니다.

### `PermissionType`
```kotlin
enum class PermissionType { NOTIFICATION, LOCATION }
```
- `PermissionRepository`의 "요청 이력" 조회·기록에 쓰는 식별자(research.md D3).

## 2. Repository 인터페이스 (`core:domain/repository/`)

### `ProfileRepository`
```kotlin
interface ProfileRepository {
    suspend fun getProfile(): Profile
    suspend fun saveProfile(profile: Profile): Profile   // PATCH 응답이 갱신된 프로필을 돌려주므로 재조회 없이 그대로 반환
}
```
- 원격 API(`GET`/`PATCH /api/v1/users/me`) 기반이다(research.md D9) — 로컬 캐시 없음, 호출마다 서버 왕복.
- `getProfile()`이 `Profile?`이 아니라 non-null인 이유: spec의 진입 전제("프로필이 생성된 사용자")와 swagger 계약(인증된 사용자는 항상 프로필을 가짐) 둘 다 "프로필 없음" 상태를 허용하지 않는다.
- 실패(네트워크·인증 등)는 예외로 전파된다 — 매핑 정책은 `docs/conventions/error_handling.md` §3을 따른다(신규 규칙 아님).

### `PushNotificationRepository`
```kotlin
interface PushNotificationRepository {
    suspend fun syncPushToken()   // 로컬에서 FCM 토큰을 얻어 PUT /users/me/push-token 로 등록/갱신
}
```
- 알림 권한이 처음 허용되는 시점(FR-007 성공 콜백)에 호출한다(research.md D10). 인자·반환값이 없는 이유: 토큰 획득(FCM SDK)과 서버 등록을 이 Repository 안에서 한 동작으로 묶어, ViewModel은 "언제 부를지"만 안다.

### `AppSettingsRepository`
```kotlin
interface AppSettingsRepository {
    fun observeNotificationDeliveryEnabled(): Flow<Boolean>
    suspend fun setNotificationDeliveryEnabled(enabled: Boolean)
}
```
- `notificationDeliveryEnabled`는 로컬 전용 표시 억제 플래그다 — 서버 발송을 막지 못한다(research.md D10). 기본값은 `false`. `true`로 바뀌는 유일한 자연 경로는 알림 권한이 처음 허용되는 시점(FR-007 성공 콜백, spec 가정: "OS 알림 권한이 어느 진입점에서든 최초로 허용되면 기본값 ON")이며, 이때 `PushNotificationRepository.syncPushToken()`도 함께 호출된다.

### `PermissionRepository`
```kotlin
interface PermissionRepository {
    fun isNotificationPermissionGranted(): Boolean
    fun isLocationPermissionGranted(): Boolean
    suspend fun hasRequestedPermissionBefore(type: PermissionType): Boolean
    suspend fun markPermissionRequested(type: PermissionType)
}
```
- 앞의 두 함수는 `ContextCompat.checkSelfPermission`을 그대로 조회하는 얇은 래퍼다(OS가 SSOT, 캐시하지 않음 — spec FR-009).
- 뒤의 두 함수만 로컬(DataStore) 상태를 갖는다 — "요청한 적 있는가"는 OS가 노출하지 않는 값이라 이 저장소가 유일한 출처다(research.md D3).

## 3. 원격 API (`core:data/network`, research.md D9·D11)

| 항목 | 값 |
|---|---|
| 엔드포인트 | `GET /api/v1/users/me`, `PATCH /api/v1/users/me`, `PUT /api/v1/users/me/push-token` |
| 소스 | [`Team-MINO-Node` swagger.yaml](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml) — `User`·`Avatar`·`Nickname` 스키마 |
| DTO | `network/dto/response/UserResponse.kt` (`id: String`(uuid) · `nickname: String` · `avatar: AvatarDto(id: Int)` · `createdAt: String`) |
| Mapper | `repository/mapper/UserMapper.kt` — `UserResponse.toDomain(): Profile`. `id`·`createdAt`은 도메인에 없음(서버 전용 필드, `core:domain/README.md` §5) |
| 인증·baseUrl | **이 feature 범위 밖.** `HttpClient`가 Bearer 토큰을 이미 부착하고 실서버(`https://api.gguk.org`)를 가리킨다고 가정한다(research.md D11 — 선행 의존성) |

`PUT /users/me/push-token` 요청 바디는 `{ token: String, platform: "ios"|"android" }`. Android는 `platform = "android"` 고정.

## 4. 로컬 저장 (`core:data/storage`, 공유 `mino_preferences` DataStore)

| 키 | 타입 | 대응 Repository |
|---|---|---|
| `notification_delivery_enabled` | `Boolean` | `AppSettingsRepository` |
| `notification_permission_requested` | `Boolean` | `PermissionRepository` |
| `location_permission_requested` | `Boolean` | `PermissionRepository` |

- 프로필은 원격 전용이라(§2·§3) 더 이상 로컬 키가 없다.
- 새 `DataStore<Preferences>` 인스턴스를 만들지 않는다 — `storage/DataStoreModule`이 제공하는 단일 인스턴스를 공유한다(`core:data/README.md` §5).

## 5. UI 상태 계약

세부 필드는 [`contracts/mypage-main-contract.md`](contracts/mypage-main-contract.md)·[`contracts/profile-setup-contract.md`](contracts/profile-setup-contract.md) 참조. 여기서는 두 화면이 공유하는 상태 갱신 규칙만 적는다.

- **재조회 지점**: `MyPageMain` 화면은 진입·복귀(`ON_RESUME` 또는 NavBackStackEntry 재활성화) 시점마다 `ProfileRepository.getProfile()`(원격 호출)·`AppSettingsRepository.observe*()`·`PermissionRepository.is*Granted()`를 다시 읽는다. 이 하나의 재조회 지점이 spec FR-003(프로필 즉시 반영)과 FR-009(권한 상태 동기화)를 함께 만족시킨다 — 프로필 화면에서 저장 후 뒤로 돌아오는 것도 "복귀"이므로 별도의 전역 상태 공유가 필요 없다. `saveProfile()`이 갱신된 `Profile`을 바로 반환하므로, 프로필 화면 자체는 저장 직후 재조회가 필요 없다.
- **로컬 캐시 금지 대상**: `isNotificationSwitchOn`·`isLocationSwitchOn`은 위 재조회 결과로만 계산하고 `UiState`에 낙관적으로 미리 반영하지 않는다(UX-003).

## 6. `core:design-system` 신규 컴포넌트 (research.md D7)

| 컴포넌트 | 위치 | 용도 |
|---|---|---|
| `MinoDialog` | `component/dialog/` | 권한 재요청 불가 안내(EC-003·EC-007) |
| `MinoSwitch` | `component/switch/` | 알림·위치 토글 |

두 컴포넌트 모두 M3 패턴(Defaults·Colors·`token/`)을 따른다(README §6.1). Figma 노드 대조는 구현 단계에서 `figma-design-fidelity.md` 절차로 수행한다 — 이 문서는 존재 여부·역할만 못박는다.
