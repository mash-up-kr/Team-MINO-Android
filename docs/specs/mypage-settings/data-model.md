# 데이터 모델: 마이페이지 & 환경설정

Phase 1 산출물. 현재 설계 상태만 담는다(개정 시 이 파일은 통째로 대체된다).

## 1. 도메인 모델 (`core:domain/model/`)

### `Profile`
```kotlin
data class Profile(
    val nickname: String,
    val avatarId: String,   // design-system 제공 아바타 12종 중 하나를 가리키는 키 (research.md D8)
)
```
- 앱 전체 단일 인스턴스. 검증(2자 이상 등)은 도메인이 아니라 `feature:mypage`의 `ProfileUiState` 계산 로직이 담당 — spec FR-004는 UX 규칙이지 도메인 불변식이 아니다.

### `AppTheme`
```kotlin
enum class AppTheme { LIGHT, DARK, SYSTEM_DEFAULT }
```
- 초기값(변경 이력 없음)은 `SYSTEM_DEFAULT` (spec FR-005).

### `PermissionType`
```kotlin
enum class PermissionType { NOTIFICATION, LOCATION }
```
- `PermissionRepository`의 "요청 이력" 조회·기록에 쓰는 식별자(research.md D3).

## 2. Repository 인터페이스 (`core:domain/repository/`)

### `ProfileRepository`
```kotlin
interface ProfileRepository {
    suspend fun getProfile(): Profile?
    suspend fun saveProfile(profile: Profile)
}
```
| 상태 전이 | 트리거 | 근거 |
|---|---|---|
| `null` → `Profile` | `saveProfile` 최초 호출 | 온보딩에서 프로필 최초 생성(§3.2 비목표 — 이 스펙은 이미 생성됨을 전제) |
| `Profile` → `Profile` | `saveProfile` 재호출 | spec FR-003, EC-006(지우기 후 재입력) |

### `AppSettingsRepository`
```kotlin
interface AppSettingsRepository {
    fun observeAppTheme(): Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
    fun observeNotificationDeliveryEnabled(): Flow<Boolean>
    suspend fun setNotificationDeliveryEnabled(enabled: Boolean)
}
```
- `observeAppTheme()`는 `:feature:main`의 `MainActivity`(research.md D4)와 `:feature:mypage`의 `MyPageViewModel` 양쪽이 구독한다 — 전자는 전역 적용, 후자는 현재 선택값 표시.
- `notificationDeliveryEnabled` 기본값은 `false`. `true`로 바뀌는 유일한 자연 경로는 알림 권한이 처음 허용되는 시점(FR-007 성공 콜백, spec 가정: "OS 알림 권한이 어느 진입점에서든 최초로 허용되면 기본값 ON")이다.

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

## 3. 로컬 저장 (`core:data/storage`, 공유 `mino_preferences` DataStore)

| 키 | 타입 | 대응 Repository |
|---|---|---|
| `profile_nickname` | `String` | `ProfileRepository` |
| `profile_avatar_id` | `String` | `ProfileRepository` |
| `app_theme` | `String` (enum name) | `AppSettingsRepository` |
| `notification_delivery_enabled` | `Boolean` | `AppSettingsRepository` |
| `notification_permission_requested` | `Boolean` | `PermissionRepository` |
| `location_permission_requested` | `Boolean` | `PermissionRepository` |

- 새 `DataStore<Preferences>` 인스턴스를 만들지 않는다 — `storage/DataStoreModule`이 제공하는 단일 인스턴스를 공유한다(`core:data/README.md` §5).
- 프로필과 앱 설정은 수명주기·변경 이유가 다르므로 `datasource/ProfileLocalDataSource`·`datasource/AppSettingsLocalDataSource`로 DataSource 자체를 분리한다(같은 README 표의 "DataSource 분리" 규칙).

## 4. UI 상태 계약

세부 필드는 [`contracts/mypage-main-contract.md`](contracts/mypage-main-contract.md)·[`contracts/profile-setup-contract.md`](contracts/profile-setup-contract.md) 참조. 여기서는 두 화면이 공유하는 상태 갱신 규칙만 적는다.

- **재조회 지점**: `MyPageMain` 화면은 진입·복귀(`ON_RESUME` 또는 NavBackStackEntry 재활성화) 시점마다 `ProfileRepository.getProfile()`·`AppSettingsRepository.observe*()`·`PermissionRepository.is*Granted()`를 다시 읽는다. 이 하나의 재조회 지점이 spec FR-003(프로필 즉시 반영)과 FR-009(권한 상태 동기화)를 함께 만족시킨다 — 프로필 화면에서 저장 후 뒤로 돌아오는 것도 "복귀"이므로 별도의 전역 상태 공유가 필요 없다.
- **로컬 캐시 금지 대상**: `isNotificationSwitchOn`·`isLocationSwitchOn`은 위 재조회 결과로만 계산하고 `UiState`에 낙관적으로 미리 반영하지 않는다(UX-003).

## 5. `core:design-system` 신규 컴포넌트 (research.md D6·D7)

| 컴포넌트 | 위치 | 용도 |
|---|---|---|
| `MinoBottomSheet` | `component/bottomsheet/` | 다크모드 3옵션 선택 |
| `MinoDialog` | `component/dialog/` | 권한 재요청 불가 안내(EC-003·EC-007) |
| `MinoSwitch` | `component/switch/` | 알림·위치 토글 |

세 컴포넌트 모두 M3 패턴(Defaults·Colors·`token/`)을 따른다(README §6.1). Figma 노드 대조는 구현 단계에서 `figma-design-fidelity.md` 절차로 수행한다 — 이 문서는 존재 여부·역할만 못박는다.

## 6. `core:design-system` API 변경

### `MinoAndroidAppTheme`
```kotlin
@Composable
fun MinoAndroidAppTheme(
    darkTheme: Boolean? = null,   // null = 시스템 추종(기존 동작 유지), 아니면 강제
    content: @Composable () -> Unit,
)
```
- 기본값이 기존 동작(`isSystemInDarkTheme()`)과 100% 동일해 기존 호출부(`:feature:sample` 등)는 수정 없이 컴파일된다.
- 읽기·주입 책임은 `:feature:main`의 `MainActivity`(research.md D4).
