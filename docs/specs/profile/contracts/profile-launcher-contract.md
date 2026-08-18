# 계약: 프로필 화면 전환 (`:core:navigation` ↔ `:feature:profile`)

다른 feature가 프로필 설정 화면을 여는 유일한 표면. 형식은 [feature-navigation.md](../../../architecture/feature-navigation.md) 1장을 그대로 따른다.

## 계약 (`:core:navigation`)

```kotlin
// activity/launcher/ProfileLauncher.kt
interface ProfileLauncher : ActivityLauncher

// activity/launcher/ExtraTag.kt — 기존 파일에 추가
const val EXTRA_PROFILE_ENTRY_POINT = "profile_entry_point"

const val PROFILE_ENTRY_POINT_ONBOARDING = "onboarding"
const val PROFILE_ENTRY_POINT_EDIT = "edit"
```

- 진입점 값은 두 상수 중 하나다. 호출자와 화면이 같은 문자열을 보게 상수로 공유한다.
- 값이 없거나 두 상수 중 어느 것도 아니면 화면은 `edit`(뒤로가기 가능)으로 해석한다([data-model.md §5](../data-model.md)).

## 구현 (`:feature:profile/di/`)

```kotlin
internal class ProfileLauncherImpl @Inject constructor() : BaseActivityLauncher(), ProfileLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<ProfileActivity>()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class ProfileNavigationModule {
    @Binds @ActivityRetainedScoped
    abstract fun bindProfileLauncher(impl: ProfileLauncherImpl): ProfileLauncher
}
```

## 호출 방법

**온보딩 진입** — 저장 결과를 받아 다음 스텝으로 넘어간다.

```kotlin
profileLauncher.launch(this, resultLauncher = resultLauncher) {
    putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_ONBOARDING)
}
```

**마이페이지 진입** — 결과가 필요 없다. 되돌아오면 구독 중인 프로필 Flow가 새 값을 흘린다.

```kotlin
profileLauncher.launch(this) {
    putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_EDIT)
}
```

## 결과

| 결과 | 의미 | 호출자가 할 일 |
|---|---|---|
| `RESULT_OK` | 프로필이 저장되었다 | 온보딩: 다음 스텝(공동방 생성)으로 진행 (FR-008) |
| `RESULT_CANCELED` | 저장 없이 화면이 닫혔다 | 마이페이지: 아무것도 하지 않는다 (FR-013) |

- 결과 Intent에 프로필 값을 싣지 않는다. 저장된 값은 `ProfileRepository.observeProfile()`이 원천이다([repository 계약](profile-repository-contract.md)).
- 온보딩 진입에서는 `RESULT_CANCELED`가 나올 수 없다 — 뒤로가기가 막혀 있고 저장 외에 화면을 닫는 경로가 없다(FR-010, EC-001).
- 이 계약을 호출하는 코드는 이번 범위에 없다. 온보딩·마이페이지 feature가 생길 때 각자 배선한다.

## 이번 범위에서 만드는 것

| 파일 | 모듈 |
|---|---|
| `activity/launcher/ProfileLauncher.kt` · `ExtraTag.kt`(추가) | `:core:navigation` |
| `ProfileActivity` · `di/ProfileLauncherImpl` · `di/ProfileNavigationModule` | `:feature:profile` |
| `implementation(project(":feature:profile"))` | `:app` |
