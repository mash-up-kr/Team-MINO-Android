# 계약: Repository 인터페이스 (`core:domain/repository/`)

타입 정의는 [`data-model.md`](../data-model.md) §2가 단일 출처다. 이 문서는 구현체 배치와 시그니처별 책임 경계만 못박는다.

## `ProfileRepository`

- **구현**: `core:data/repository/ProfileRepositoryImpl` — `datasource/ProfileLocalDataSource`(DataStore) 하나만 호출. Mapper 불필요(DTO 없음, `core:data`가 곧 로컬 저장 포맷 ↔ 도메인 모델 변환을 겸함).
- **DI**: `core:data/repository/di/ProfileRepositoryModule.kt` — `@Binds @Singleton`.

## `AppSettingsRepository`

- **구현**: `core:data/repository/AppSettingsRepositoryImpl` — `datasource/AppSettingsLocalDataSource`(DataStore) 호출. `observeAppTheme()`/`observeNotificationDeliveryEnabled()`는 DataStore의 `Flow<Preferences>`를 `map`으로 변환해 그대로 반환(자체 캐시 없음).
- **DI**: `core:data/repository/di/AppSettingsRepositoryModule.kt`.

## `PermissionRepository`

- **구현**: `core:data/device/PermissionRepositoryImpl` — 기존 `device/` 패키지(§`DeviceInfoProvider`와 같은 급의 "기기 원천 접근자")에 위치.
  - `isNotificationPermissionGranted()`/`isLocationPermissionGranted()`: `ContextCompat.checkSelfPermission(context, ...)`를 `@ApplicationContext Context`로 호출. **Activity 불필요** — Application 스코프에서 안전.
  - `hasRequestedPermissionBefore()`/`markPermissionRequested()`: 내부적으로 `datasource/AppSettingsLocalDataSource`(또는 별도 `PermissionLocalDataSource` — 수명주기가 앱 설정과 다르므로 분리, `core:data/README.md` §5 "DataSource 분리" 규칙)를 호출.
- **DI**: `core:data/device/di/PermissionRepositoryModule.kt`.

## 소비자

| Repository | 소비 화면 | 소비 방식 |
|---|---|---|
| `ProfileRepository` | `MyPageMain`(조회), `MyPageProfile`(조회+저장) | ViewModel 직접 호출(UseCase 없음) |
| `AppSettingsRepository` | `MyPageMain`(조회+저장), `:feature:main`의 `MainActivity`(조회 전용, research.md D4) | ViewModel/Activity 직접 호출 |
| `PermissionRepository` | `MyPageMain` | ViewModel 직접 호출 |

세 Repository 모두 "단일 API·단순 조회·비즈니스 규칙 없음·재사용 없음"(`core:domain/README.md` §4) 조건을 만족해 UseCase를 두지 않는다. 유일하게 여러 값을 조합하는 지점은 `isNotificationSwitchOn` 계산(권한 AND 발송 설정)인데, 이는 "정렬·필터링 같은 비즈니스 규칙"이 아니라 두 Repository 결과를 화면이 조립하는 표현 로직이라 ViewModel(`MyPageViewModel`)에 남긴다.
