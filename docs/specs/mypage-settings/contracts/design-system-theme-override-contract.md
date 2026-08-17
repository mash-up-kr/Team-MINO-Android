# 계약: `MinoAndroidAppTheme` 다크모드 오버라이드

research.md D4의 구현 계약. `core:design-system`과 `:feature:main` 경계를 가른다.

## `core:design-system` 쪽 변경

```kotlin
// core/design-system/.../theme/MinoAndroidTheme.kt
@Composable
fun MinoAndroidAppTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
)
```

- 내부 `provideColorScheme()` 호출부만 `darkTheme ?: isSystemInDarkTheme()`로 바뀐다. 그 외 로직(폰트 배율 고정 등)은 변경 없음.
- `core:design-system`은 `AppTheme`(도메인 enum)도 `AppSettingsRepository`도 알지 못한다 — `Boolean?`만 받는다.
- 기존 호출부(`:feature:sample` 등)는 `darkTheme` 인자를 생략하면 되므로 소스 호환이 깨지지 않는다.

## `:feature:main` 쪽 변경 — `MainActivity`

```kotlin
// feature/main/.../MainActivity.kt (개념 스케치 — 함수 본문은 구현 단계)
@Inject lateinit var appSettingsRepository: AppSettingsRepository   // core:domain

override fun onCreate(savedInstanceState: Bundle?) {
    ...
    setContent {
        val appTheme by appSettingsRepository.observeAppTheme()
            .collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM_DEFAULT)

        MinoAndroidAppTheme(
            darkTheme = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM_DEFAULT -> null
            },
        ) {
            MainShell(...)
        }
    }
}
```

- `MainActivity`는 이미 모든 feature 공통 컨벤션(`AndroidFeatureConventionPlugin`)으로 `:core:domain`을 의존하므로 `build.gradle.kts` 변경이 필요 없다.
- `AppSettingsRepository`의 실제 구현(`core:data`)은 Hilt가 런타임에 바인딩 — `MainActivity`는 인터페이스만 안다(헌법 원칙 II).

## 영향받지 않는 것

- `:feature:mypage`의 `MyPageMain`은 이 계약과 무관하게 자기 화면의 `appTheme` 표시값만 `AppSettingsRepository.observeAppTheme()`로 따로 구독한다(같은 Repository, 다른 소비자 — data-model.md §2).
- 다른 feature(`home`·`sample`)는 아무것도 바꾸지 않는다. `MinoAndroidAppTheme` 호출부가 `:feature:main`에만 있기 때문이다.
