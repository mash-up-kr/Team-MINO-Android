# 리서치: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**계획서**: [plan.md](./plan.md)

> 이 문서는 **누적 문서**다. 기존 항목을 지우지 않고, 결정이 뒤집히면 취소선과 `재검토됨(plan X.Y.Z)`을 남긴 뒤 새 항목을 덧붙인다.

---

## R-001. 스플래시의 모듈 형태 — 진입형 feature `:feature:splash`

**결정 (plan 1.0.0)**: 새 진입형 feature 모듈 `:feature:splash`를 만들고, `SplashActivity`가 앱의 LAUNCHER가 된다. 현재 LAUNCHER인 `:feature:main`의 `MainActivity`에서 `intent-filter`를 제거한다.

**근거**:
- 스플래시는 Activity로 독립 진입하고 탭 셸의 그래프에 편입되지 않는다 → [feature-module.md §1](../../architecture/feature-module.md)의 진입형 정의에 정확히 해당한다.
- 목적지가 온보딩(진입형)과 메인 탭(`:feature:main`)으로 갈리므로, 전환은 `:core:navigation`의 `ActivityLauncher` 계약을 통한다.
- `:app`에 두면 `:app`이 화면 구현을 갖게 되어 원칙 II(레이어 경계)에 어긋난다. `:app`은 그래프를 조립할 뿐이다.

**Alternatives considered**:
- *`:app`에 `SplashActivity`를 둔다* — 모듈 하나를 아끼지만 `:app`이 UI·ViewModel을 갖게 된다. 헌법 원칙 II 위반이라 기각.
- *`:feature:main`에 스플래시 화면을 넣고 MainActivity가 분기* — 메인 탭 셸의 생애주기에 스플래시가 묶이고, 세션 미확보 상태에서 탭 셸이 이미 생성된다. FR-010(세션 확보 전 어느 화면으로도 진입 금지)을 구조적으로 보장할 수 없어 기각.

---

## ~~R-002. 「비회원 익명 세션」 확보 계약 — 스웨거에 없다~~ *재검토됨(plan 2.0.0)*

> 스웨거만 보고 "계약이 없다"고 판정했으나, 이 계약은 **다른 스펙이 이미 확정해 소유하고 있었다.** → R-010

**결정 (plan 1.0.0)**: 세션 확보를 `:core:domain`의 `SessionRepository.ensureSession()` **인터페이스로만 고정**하고, 실제 발급 수단은 이 plan이 정하지 않는다. `:feature:splash`는 이 인터페이스의 성공·실패만 소비한다.

**근거**:
- 지목된 스웨거(`Team-MINO-Node@KKardy/GM-111-outline-prd`)에는 **세션·토큰 발급 엔드포인트가 존재하지 않는다.** `securitySchemes.bearerAuth`에 `"잠정. 인증 방식은 별도 인증 설계 문서에서 확정한다"`라고 명시돼 있다.
- PRD 5.0.0은 세션 근거를 "인증 제공자가 발급한 익명 계정 식별자"로 규정하므로, 발급 주체는 이 백엔드 API가 아니라 외부 인증 제공자다. 저장소에 `feature/176-firebase-anonymous-auth` 브랜치가 존재해 Firebase Anonymous Auth가 유력하나 **아직 머지되지 않았다.**
- 인터페이스로 잘라 두면 발급 수단이 확정될 때 `:core:data` 구현만 갈리고 `:feature:splash`는 손대지 않는다.

**[TBD-P1]**: 익명 세션 발급 수단과 그 계약(Firebase Anonymous Auth인지, 서버 교환 단계가 있는지, 토큰 저장 위치). `feature/176-firebase-anonymous-auth` 또는 백엔드 인증 설계 문서가 확정돼야 `:core:data`의 `SessionRepositoryImpl`을 구현할 수 있다.

**Alternatives considered**:
- *`POST /api/v1/users`를 세션 발급으로 쓴다* — 이 엔드포인트는 `deviceId` + `nickname`을 **필수**로 받고 개인방까지 생성한다. 닉네임은 온보딩에서 사용자가 입력하는 값이라 스플래시 시점에 존재하지 않는다. 또한 `deviceId` 전제는 PRD 5.0.0이 명시적으로 폐기한 기기 식별자 방식이다. 스플래시의 계약이 아니라 온보딩([SCR-002]) 소관이므로 기각. → R-003 참조

---

## R-003. 프로필 존재 여부 판정 — `GET /api/v1/users/me`

> **결정(서버를 단일 출처로 삼는다)은 plan 3.0.0에서도 유지된다.** 다만 아래 본문의 반환 형태 `UserProfile?`는 R-014에서 `Boolean`으로 좁혀졌다. 로컬 판정을 쓰지 않는 이유는 R-015가 이어받는다.

**결정 (plan 1.0.0)**: `UserRepository.getMyProfile(): UserProfile?`로 감싸고, `null`이면 프로필 없음(최초 실행)으로 판정한다. HTTP 응답과 `null`의 대응은 `:core:data`가 흡수한다.

**근거**:
- 스웨거의 `GET /api/v1/users/me`가 유일한 프로필 조회 경로다. `200 → User`, `401 → Error`만 정의돼 있다.
- 도메인은 HTTP 코드를 몰라야 한다(원칙 II). `UserProfile?`로 노출하면 feature는 "있음/없음"만 본다.

**대조 결과 (plan 3.0.0에서 TBD-P2 해소)**: 배포된 OpenAPI(`https://api.gguk.org/api-docs-json`, 2026-08-27 조회)가 `401`의 `errorCode`를 enum으로 정의했다 — `UNAUTHORIZED` · `TOKEN_EXPIRED` · **`USER_NOT_REGISTERED`**. 마지막 값이 "세션은 유효하나 등록되지 않은 유저"를 나타낸다.

따라서 판정은 HTTP 코드가 아니라 **errorCode**로 한다.

| 응답 | 판정 |
|---|---|
| `200` | 프로필 있음 → `SplashEntry.Main` |
| `401` + `USER_NOT_REGISTERED` | 프로필 없음 → `SplashEntry.Onboarding` |
| `401` + `UNAUTHORIZED`·`TOKEN_EXPIRED` | 세션 문제 → 실패로 던진다. 최초 실행으로 오판하지 않는다 |

세 번째 줄이 이 결정의 핵심이다. `401`을 통째로 "프로필 없음"으로 읽으면 세션이 깨진 기존 사용자가 온보딩으로 떨어진다(SC-002 위반).

**Alternatives considered**:
- *로컬 저장 여부로 판정* — 네트워크 없이 즉시 판정 가능하지만, 재설치 시 서버에 프로필이 있어도 없다고 오판한다. PRD 5.0.0이 "앱을 지웠다 다시 설치하면 이전 세션과 그 데이터로 돌아갈 수 없다"고 못박았으므로 재설치는 곧 새 세션이라 오판이 아니게 되지만, 세션 복원 경로(FR-011)와 판정 경로가 서로 다른 저장소를 보게 되어 어긋날 여지가 있다. 서버를 단일 출처로 두는 편이 안전해 보류.

---

## R-004. 3초 최소 노출과 13초 타임아웃의 구현 형태

**결정 (plan 1.0.0)**: ViewModel이 **경과 시간이 아니라 상태로** 다룬다. 최소 노출 3초와 세션 확보를 각각 독립 작업으로 띄우고 **둘 다 끝났을 때** 전환한다. 스피너·토스트는 그 위에 얹는 타이머다.

- 최소 노출: `delay(3.seconds)` 코루틴 하나
- 세션 확보: `EnsureAnonymousSessionUseCase()` 코루틴 *(plan 2.0.0에서 계약 이름 정정 → R-010)*
- 스피너: 3초 경과 시점에 세션이 미완료면 `isSpinnerVisible = true`
- 일시적 오류: 앱 실행 기준 13초 시점에 미완료면 스피너를 감추고 토스트

**근거**: FR-001·FR-002·FR-006·FR-007의 시간 기준이 모두 **앱 실행 시점**을 원점으로 하므로, 원점 하나를 잡고 파생 시점을 재는 편이 상태 전이가 단순하다. `awaitAll` 형태로 두면 UX-002(최소 3초 보장)가 코드 구조로 보장된다.

**Alternatives considered**:
- *타임아웃을 `withTimeout`으로 감싼다* — 13초 초과 시 세션 확보 코루틴이 취소되어 버린다. FR-010은 실패 후에도 **계속 재시도**해야 하므로 취소되면 안 된다. 기각.

---

## ~~R-005. 실패 원인 분류 — `MinoDomainException`을 그대로 쓴다~~ *재검토됨(plan 2.0.0)*

> 두 분기를 `Network`/`Http`로 잡았으나 실제 확정된 리프는 `Network`/`Auth`다. → R-011

**결정 (plan 1.0.0)**: `MinoDomainException.Network` → 네트워크 연결 에러 토스트(FR-008), 그 밖의 모든 실패(`Http` 포함) → 일시적 오류 토스트(FR-009).

**근거**: `:core:error-handling`의 `MinoDomainException`이 이미 `Network(cause)`와 `Http(code, cause)` 두 리프를 갖고 있어([MinoDomainException.kt](../../../core/error-handling/src/main/kotlin/team/mino/core/errorhandling/MinoDomainException.kt)) spec의 2분기와 그대로 맞는다. 스플래시 전용 예외 리프를 새로 만들 이유가 없다.

**Alternatives considered**:
- *`MinoDomainException.SessionUnavailable` 리프 신설* — 리프가 늘면 모든 소비처의 `when`이 넓어진다. 기존 두 리프로 spec의 분기가 남김없이 표현되므로 기각.

---

## R-006. 오류 토스트의 표현 수단 — `MinoSnackbar`

**결정 (plan 1.0.0)**: `:core:design-system`의 [`MinoSnackbar`](../../../core/design-system/src/main/java/team/mino/core/designsystem/component/snackbar/MinoSnackbar.kt)를 쓰고, 스플래시가 화면 하단에서 **40dp** 띄운 위치에 배치한다(UX-003).

**근거**: 디자인 시스템에 이미 존재하는 컴포넌트다. 스플래시 전용 토스트를 만들면 헌법 §기술 표준과 제약(디자인 시스템 단일 접근점)에 어긋난다.

**대조 결과 (plan 2.1.0에서 TBD-P3 해소)**: Figma 012-3(`3798-166743`)·012-4(`3798-166766`) 모두 토스트가 `Snackbar/Snackbar` **컴포넌트 인스턴스**다 — 디자인 시스템 컴포넌트가 맞고 `MinoSnackbar`가 대응한다.

실측(375×812 프레임 기준, 두 노드 동일):

| 항목 | 값 |
|---|---|
| 위치·크기 | `x=20, y=724, w=335, h=48` |
| 화면 하단 여백 | `812 − (724+48)` = **40** → UX-003의 근거를 실측으로 확인 |
| 좌우 여백 | 각 **20** |

- 배치는 `Modifier.fillMaxWidth().padding(horizontal = 20.dp)` + 하단 40dp로 잡는다. `MinoSnackbar`의 `MaxWidth = 420.dp`는 상한이라 375 화면에서 걸리지 않는다.
- **~~남은 확인 1건~~ 닫힘(2026-08-28 대조)**: 높이 48과 토큰 조합(`VerticalPadding 11×2 + MinContentHeight 32 = 54`)의 어긋남은 **인스턴스 리사이즈**였다. 디자인 시스템 원본 `Snackbar/Snackbar`(`MU_Wanted Design System` `16215-19587`)의 `Variant=Normal`은 **335×54**이고 그 안의 `Container`가 `x=16, y=11, 303×32` — 좌우 16·상하 11이 컴포넌트 자체 값이다. 스플래시 시안의 인스턴스(`3798-166765`)만 48로 줄여 놓은 것이라 **`SnackbarTokens.VerticalPadding = 11.dp`가 맞고 고칠 것이 없다.** 시안 인스턴스를 원본 높이로 되돌릴지는 디자이너 소관이다.

---

## R-007. 브랜드 이미지 에셋의 자리

**결정 (plan 1.0.0)**: 캐릭터 5종·`gguk` 워드마크·구름 배경을 `:feature:splash`의 `src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 **WebP**로 둔다.

**근거**: [component-asset-placement.md §1.1](../../conventions/component-asset-placement.md)의 기본값 — 한 feature의 화면에서만 쓰는 이미지 에셋은 그 feature가 갖는다. 포맷·밀도 규칙도 같은 문서가 소유한다. 다른 화면에도 쓸 것 같다는 예상은 근거가 되지 않는다.

**Alternatives considered**:
- *`:core:common:ui`에 둔다* — 사용처가 스플래시 하나뿐이라 같은 문서의 승격 기준을 충족하지 않는다. 기각.
- *`:core:design-system`에 둔다* — 이 모듈은 이미지 에셋을 받지 않는다(같은 문서 §1). 기각.

---

## R-008. 목적지 전환 계약

**결정 (plan 1.0.0)**: 재실행 경로는 기존 [`MainLauncher`](../../../core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/MainLauncher.kt)를 그대로 쓰고 `withFinish = true`로 연다. 최초 실행 경로는 온보딩 진입 계약이 필요하다.

**근거**: `:core:navigation`에 `MainLauncher` 계약과 `:feature:main`의 `MainLauncherImpl`이 이미 있다. `withFinish`로 스플래시를 종료해 뒤로가기로 스플래시에 돌아오지 못하게 한다(FR-005·UX-001의 취지).

**해소 (plan 3.0.0)**: `:feature:profile`이 `develop`에 머지되어 `ProfileLauncher` 계약과 `ProfileActivity`가 존재한다. 새 계약을 만들 필요가 없다.

```kotlin
// core:navigation — profile 스펙 소유
interface ProfileLauncher : ActivityLauncher
const val PROFILE_ENTRY_POINT_ONBOARDING = "onboarding"
const val PROFILE_ENTRY_POINT_EDIT = "edit"
```

스플래시는 `ProfileLauncher.launch(activity, withFinish = true) { putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_ONBOARDING) }` 로 연다. 진입점 값이 계약 자리에 상수로 놓여 있어 호출자와 화면이 같은 문자열을 본다.

---

## ~~R-009. UseCase를 둘 것인가~~ *재검토됨(plan 2.0.0)*

> 결론(UseCase를 둔다)은 유지되나, 세션 확보가 이미 `EnsureAnonymousSessionUseCase`로 존재하므로 조합 형태가 달라진다. → R-012

**결정 (plan 1.0.0)**: `:core:domain`에 `ResolveSplashEntryUseCase` 하나를 둔다.

**근거**: [core/domain README §2](../../../core/domain/README.md)는 "비즈니스 규칙 있을 때 UseCase, 단순 조회는 Repository 직접"으로 가른다. 스플래시는 **세션 확보가 성공해야만 프로필을 조회하고, 그 결과로 목적지가 갈린다**는 규칙을 갖고 두 Repository를 순서대로 엮는다. 단순 조회가 아니다. Kotlin JVM 모듈에 두면 Android 없이 JVM 단위 테스트로 FR-002·003·004·011의 분기를 검증할 수 있다.

**Alternatives considered**:
- *ViewModel이 두 Repository를 직접 호출* — 모듈 하나를 아끼지만 분기 규칙이 Android 의존 ViewModel에 갇혀 JVM 테스트가 불가능해진다. 스플래시의 분기는 이 기능의 핵심 요구사항이라 테스트 가능성을 우선해 기각.

---

## R-010. 익명 세션 확보 — `anonymous-auth-session` 스펙의 계약을 소비한다

**결정 (plan 2.0.0)**: 스플래시는 세션 계약을 **만들지 않는다.** `feature/176-firebase-anonymous-auth`의 [`anonymous-auth-session`](https://github.com/mash-up-kr/Team-MINO-Android/issues/176) 스펙이 확정한 `EnsureAnonymousSessionUseCase`를 주입받아 호출하기만 한다.

```kotlin
// core:domain — anonymous-auth-session 스펙 소유
interface AnonymousAuthRepository { suspend fun ensureSession(): AnonymousSession }
class EnsureAnonymousSessionUseCase { suspend operator fun invoke(): AnonymousSession }
data class AnonymousSession(val userId: String)
```

**근거**:
- R-002는 지목된 스웨거만 근거로 "계약이 없다"고 판정했다. 실제로는 이슈 #176과 `anonymous-auth-session` 스펙 1.2.0 · plan 1.0.0이 계약·데이터 모델·예외 매핑까지 확정해 두었다. **스웨거에 없는 이유는 발급 주체가 백엔드가 아니라 Firebase이기 때문**이지, 미확정이어서가 아니다.
- 헌법 원칙 I(SSOT) — 같은 계약을 스플래시가 다시 정의하면 두 번째 출처가 생긴다.
- 이슈 #176이 **호출 위치를 스플래시로 이미 지목**했다: "앱 시작 시 첫 API 호출 전, 스플래시. (…) 현재 레포에 스플래시가 없다 — `[SCR-001]` 작업 때 배선하고, 그때까지는 `MainActivity` 진입 시점이 임시 자리다." 이 plan이 그 이관을 수행한다.

**발급 메커니즘 요약** (본문은 `anonymous-auth-session` 스펙이 소유한다):
- Firebase 익명 인증으로 익명 계정을 만들고 Firebase가 서명한 ID 토큰(JWT)을 얻는다. 서버는 Admin SDK `verifyIdToken()`으로 uid를 꺼낸다.
- 토큰은 **앱이 저장하지 않는다.** Firebase SDK가 앱 프라이빗 저장소에 영속화하며, 별도 캐싱은 진실 원천을 둘로 가르므로 금지다.
- 서버 요청에는 Ktor `createClientPlugin`의 `onRequest`가 `Authorization: Bearer`를 자동 첨부하며, **요청 host가 `BuildConfig.API_BASE_URL`의 host와 같을 때만** 붙인다. 스플래시는 이 배선에 어떤 코드도 쓰지 않는다.
- 재실행은 로컬에 유지된 세션을 네트워크 왕복 없이 복원한다 — FR-011의 실제 근거다.

**Alternatives considered**: R-002의 기각 이력을 참조한다. `POST /api/v1/users`를 세션 발급으로 쓰지 않는 판단은 그대로 유효하다.

---

## ~~R-011. 실패 원인 분류 — `Network` / `Auth`~~ *재검토됨(plan 3.0.2)*

> 세션 확보(Firebase 원천)만 보고 두 리프로 잡았으나, 같은 화면이 소비하는 프로필 조회는 HTTP 원천이라 `Http(401)`로 온다. → R-016

**결정 (plan 2.0.0)**: `MinoDomainException.Network` → 네트워크 연결 에러 토스트(FR-008), **`MinoDomainException.Auth`** → 일시적 오류 토스트(FR-009·FR-007).

**근거**:
- `Auth` 리프는 `anonymous-auth-session` 스펙의 [`domain-exception-auth-leaf.md`](https://github.com/mash-up-kr/Team-MINO-Android/issues/176)가 신설해 소유한다. R-005는 그 리프의 존재를 몰라 `Http`로 잡았다.
- 그쪽 예외 매핑 계약이 인증 제공자 예외를 **연결 실패 → `Network` / 발급 실패 → `Auth`** 두 갈래로만 화이트리스트 매핑한다. 스플래시의 2분기와 정확히 1:1이다.
- 그 밖의 `Throwable`은 매핑하지 않고 CEH로 간다 → R-013 참조.

**Alternatives considered**: R-005의 기각 이력을 참조한다. 스플래시 전용 리프를 만들지 않는 판단은 그대로 유효하다.

---

## R-012. 두 UseCase의 조합 — `ResolveSplashEntryUseCase`는 세션을 확보하지 않는다

**결정 (plan 2.0.0)**: `ResolveSplashEntryUseCase`는 **프로필 판정과 목적지 결정만** 맡는다. 세션 확보는 ViewModel이 `EnsureAnonymousSessionUseCase`를 먼저 호출해 끝낸다.

```
SplashViewModel
 ├ 1) EnsureAnonymousSessionUseCase()        ← anonymous-auth-session 소유
 └ 2) ResolveSplashEntryUseCase() : SplashEntry  ← 이 스펙 소유
```

**근거**:
- R-009는 "세션 확보 성공해야만 프로필 조회"라는 순서 규칙 때문에 UseCase를 두기로 했다. 그 순서는 이제 **호출자 계약 C-1·C-2**(첫 서버 요청보다 먼저 호출, 정상 반환 전 전환 금지)가 강제하므로 스플래시가 다시 규칙으로 쓸 필요가 없다.
- 세션 확보를 `ResolveSplashEntryUseCase` 안으로 감추면 지연·실패의 원인이 "세션"인지 "프로필"인지 화면이 구분할 수 없어진다. FR-006(3초 스피너)·FR-007(13초 토스트)은 **세션 확보 진행 상태**에 걸린 요구사항이라 그 경계가 보여야 한다.
- 남는 규칙(프로필 `null` → `Onboarding`, 존재 → `Main`)만으로도 JVM 테스트 가치는 유지된다.

**Alternatives considered**:
- *`ResolveSplashEntryUseCase`가 두 UseCase를 순서대로 호출* — 화면이 세션 단계와 프로필 단계를 구분하지 못해 위 이유로 기각.
- *ViewModel이 `UserRepository`를 직접 호출하고 UseCase를 없앤다* — 분기 규칙이 Android 의존 ViewModel에 갇혀 JVM 테스트가 불가능해진다. R-009의 판단 그대로 기각.

> **plan 4.0.0 재확인**: 판정 근거가 둘로 늘어도 이 결정은 그대로다. 늘어난 근거(온보딩 완료 표시)는 **로컬 값이라 지연·실패를 만들지 않으므로**, 세션 단계와 판정 단계의 경계를 흐리지 않는다. 오히려 "이 UseCase는 판정만 한다"는 성질 덕분에 근거를 하나 더 넣는 자리가 이미 마련돼 있었다([R-017](#r-017-진입-판정에-온보딩-완료-표시를-더한다-plan-400)).

---

## R-013. CEH로 새는 실패에도 재시도가 끊기지 않게 한다

**결정 (plan 2.0.0)**: 재시도 루프를 **도메인 예외 수신에만 종속시키지 않는다.** `MinoDomainException`으로 매핑되지 않은 실패가 발생해도 화면이 안내·재시도 없이 멈춘 채 남지 않아야 한다.

**근거**: `anonymous-auth-session`의 호출자 계약 **C-5**가 요구하는 조건이다. 예외 매핑은 화이트리스트라 열거 밖의 실패는 CEH로 가는데, 그때 재시도 루프가 `runCatchingDomain`의 실패 콜백에만 걸려 있으면 루프가 조용히 끝나고 스플래시가 영구히 멈춘다. FR-010(세션 확보까지 자동 재시도)과 SC-003(멈춘 것으로 오해할 여지 없음)이 함께 깨진다.

**함께 지킬 것**: 13초 임계(FR-007)를 `withTimeout`으로 걸면 `TimeoutCancellationException`이 `CancellationException`이라 도메인 예외 경로를 타지 않고 CEH로 샌다. R-004가 이미 `withTimeout`을 기각했으므로 충돌하지 않는다.

---

## R-014. 프로필 존재 판정 계약을 `Boolean`으로 좁힌다

**결정 (plan 3.0.0)**: 스플래시 전용 계약을 `suspend fun isRegistered(): Boolean` 하나로 두고, `UserProfile` 도메인 모델은 **만들지 않는다.**

**근거**:
- plan 2.1.0은 `UserProfile(id, nickname, avatar)`를 신설하려 했고, 그 근거는 "같은 모델을 온보딩·마이페이지가 공유하므로"였다. 그 전제가 사라졌다 — `:core:domain`에 이미 [`Profile(nickname, avatarId)`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/Profile.kt)이 있고 profile 스펙이 소유한다. 같은 개념의 모델을 둘 두면 헌법 원칙 I(SSOT)에 어긋난다.
- 그렇다고 `Profile`을 반환 타입으로 재사용할 이유도 없다. **스플래시는 필드를 하나도 읽지 않는다**(plan 2.1.0부터 유지된 사실). 존재 여부만 쓰는 곳에 필드 3개짜리 모델을 실어 나르면 소비하지 않는 값에 계약이 묶인다.
- `Boolean`이면 서버 응답(`200` / `401 USER_NOT_REGISTERED`)과 도메인 값이 1:1로 맞고, 그 밖의 실패는 예외로 갈린다.

**Alternatives considered**:
- *`Profile?`을 반환* — profile 스펙 소유 모델을 스플래시 계약이 참조하게 되어 두 스펙이 결합한다. 게다가 서버 응답에 있는 `id`·`createdAt`이 `Profile`에 없어 매핑에서 정보를 버리는 모양이 된다. 기각.
- *`UserProfile` 신설* — plan 2.1.0의 안. `Profile`과 중복이라 기각. 이력은 위 R-003 참조.

---

## R-015. 로컬 `ProfileRepository`를 판정에 쓰지 않는다

**결정 (plan 3.0.0)**: 프로필 존재 판정에 [`ProfileRepository.observeProfile()`](../../../core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRepository.kt)를 쓰지 않는다. 서버 조회를 유지한다(R-003).

**근거**:
- 그 저장소는 profile 스펙이 **자기 화면의 표기·편집을 위해** 두는 로컬 캐시다. 문서에 "이번 범위는 로컬 저장소 단독", "도메인 실패를 정의하지 않는다 — 이 흐름은 오류로 끝나지 않는다"고 적혀 있다.
- 스플래시가 그걸 쓰면 spec이 무너진다. spec §4 가정은 "프로필 판정은 세션이 확보된 뒤에 가능하다"이고 EC-004는 "세션은 성공했지만 프로필 조회가 실패"를 다룬다 — 둘 다 **네트워크 판정**을 전제한다. 실패하지 않는 로컬 흐름에서는 두 조항이 성립하지 않는다.
- profile 스펙도 표기 목적의 매번 조회를 기각했을 뿐(그 스펙 research 120행), 스플래시의 **1회 존재 판정**까지 캐시로 대체하라고 정하지 않았다. `GET /api/v1/users/me`는 어느 스펙도 선점하지 않은 상태다.

**Alternatives considered**:
- *로컬 캐시로 판정하고 네트워크를 아예 안 탄다* — 3초 안에 끝나 빠르지만 위 두 조항이 죽고, 캐시가 비어 있는 재설치 사용자를 서버 상태와 무관하게 온보딩으로 보낸다. 기각.

> **plan 4.1.0 재확인**: 이 결정이 오프라인 재실행을 막는 직접 원인임이 드러났다. 그럼에도 **유지한다** — 대체안 검토와 그 결과는 [R-020](#r-020-오프라인-재실행이-진입-판정에서-막히는-것을-받아들인다-plan-410)이 갖는다.

---

## R-016. 실패 원인 분류 — `Network` / 그 밖(`Auth`·`Http`)

**결정 (plan 3.0.2)**: `MinoDomainException.Network` → 네트워크 연결 에러 토스트(FR-008), **그 밖의 리프(`Auth`·`Http`)** → 일시적 오류 토스트(FR-009·FR-007).

**근거**:
- R-011이 리프를 `Network`/`Auth` 둘로 못박은 것은 **세션 확보 한 갈래만** 보고 내린 판정이다. 스플래시는 같은 시도 안에서 프로필 등록 여부도 조회하고(R-003), 그쪽은 HTTP 원천이라 Ktor validator가 `Http(code)`로 매핑한다.
- `Auth` 리프는 **인증 제공자가 발급에 실패한 경우 전용**이고, 원천별 분류 기준은 [ADR: 도메인 예외 매핑 지점은 원천마다 하나씩 두고, 인증 실패용 `Auth` 리프를 추가한다](../../adr/2026-08-22-domain-exception-mapping-per-source.md)가 소유한다. 백엔드 HTTP 401을 `Auth`로 적은 것은 그 기준을 어긴 것이라 정정한다. 계약 표도 함께 고쳤다([profile-registration.md](./contracts/profile-registration.md)).
- 사용자 분기는 **연결 자체가 안 된 것**과 **그 밖**의 둘 그대로다(FR-008·FR-009). 세션 발급 실패든 서버 조회 실패든 사용자가 취할 행동이 같아 문구를 가르지 않는다. spec의 2분기는 리프 2개가 아니라 `Network`인지 아닌지로 성립한다.

**구현 대응**: `SplashRoute.messageResOf()`가 `Network` → `splash_error_network`, `Http`·`Auth` → `splash_error_temporary`로 매핑한다. 리프가 늘어도 `when`이 망라적이므로 누락이 컴파일에 걸린다.

**Alternatives considered**:
- *`Http(401)`을 `Auth`로 다시 매핑한다* — 매핑 지점이 validator 하나라는 성질이 깨지고, 위 ADR이 소유한 분류 기준(원천이 인증 제공자일 때만 `Auth`)을 어긴다. 기각.

---

## R-017. 진입 판정에 온보딩 완료 표시를 더한다 *(plan 4.0.0)*

**결정**: `ResolveSplashEntryUseCase`가 `OnboardingProgressRepository`를 함께 주입받아 **두 근거를 조합**한다. 프로필이 등록되어 있고 온보딩 완료 표시도 있을 때만 `SplashEntry.Main`이며, 둘 중 하나라도 없으면 `SplashEntry.Onboarding`이다.

```
ResolveSplashEntryUseCase
 ├ ProfileRegistrationRepository.isRegistered()   ← 이 스펙 소유 (서버 조회)
 └ OnboardingProgressRepository.getProgress()     ← onboarding-flow 소유 (로컬 조회)
```

| 프로필 등록 | 완료 표시 | 결과 |
|---|---|---|
| 없음 | 무관 | `Onboarding` |
| 있음 | `false` | `Onboarding` |
| 있음 | `true` | `Main` |

**근거**: spec 4.0.0 FR-002·FR-003·FR-004가 요구한다. 그 요구의 출처는 이 문서가 아니라 `docs/specs/onboarding-flow`(FR-021·FR-022)이며, 소유권 규칙은 [ADR 2026-08-29](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)가 갖는다. 프로필 저장은 온보딩 네 스텝 중 첫 스텝일 뿐이라 그것만으로 완료를 판정하면 중단한 사용자가 메인 탭으로 밀려난다.

**호출 순서가 고정이다** — `isRegistered()`가 먼저다. 그 판정이 미등록일 때 프로필 로컬 캐시를 비우는 부수 효과를 갖고, `:feature:profile`의 등록/수정 분기가 그 보장에 기댄다(`ProfileEntryPoint.needsRefresh`). **완료 표시를 먼저 읽고 단축 평가로 이 호출을 건너뛰면 컴파일도 이 UseCase의 테스트도 통과하면서 프로필 저장이 깨진다.** 제약의 근거와 구속력은 위 ADR이 소유한다.

**모듈 경계는 그대로다.** 늘어난 의존은 `:core:domain`의 인터페이스이고 `:feature:splash`가 `:feature:onboarding`을 의존하지 않는다. 헌법 원칙 II를 어기지 않는다.

**지연·실패 경로가 늘어나지 않는다.** 완료 표시는 이 설치의 로컬 값이라 네트워크가 필요 없다. FR-006~FR-010의 임계와 재시도 설계는 그대로다(R-004·R-013).

**Alternatives considered**:
- *`IsOnboardingCompletedUseCase`를 따로 두고 `SplashViewModel`이 조합* — 판정 규칙이 Android 의존 ViewModel로 새어 나가 JVM 테스트로 세 갈래를 덮을 수 없다. R-009·R-012가 이 UseCase를 둔 이유("분기 규칙을 JVM에서 테스트 가능하게")를 스스로 무너뜨린다. 기각.
- *스플래시를 그대로 두고 온보딩이 진입 직후 스스로 홈으로 튕겨 낸다* — 완료한 사용자가 온보딩 화면을 한 프레임 본다. 온보딩 spec SC-003 위반. 기각.
- *완료 표시를 서버가 갖고 `GET /users/me` 응답에 싣는다* — 서버 계약을 넓혀야 하고, 세션이 앱 설치에 묶여 있어 서버가 기억해도 돌려줄 대상이 없다. 기각(ADR이 같은 판단을 기록했다).

---

## R-018. `SplashEntry`의 리프를 그대로 두고 의미만 넓힌다 *(plan 4.0.0)*

**결정**: `SplashEntry.Onboarding`·`SplashEntry.Main` 두 리프를 유지한다. 리프를 늘리거나 이름을 바꾸지 않고, `Onboarding`의 의미를 "프로필이 없다"에서 **"온보딩을 끝내지 않았다"** 로 넓힌다.

**근거**: 스플래시가 **결정해야 하는 것은 여전히 둘**이다 — 온보딩으로 보낼지, 메인 탭으로 보낼지. 판정 근거가 둘로 늘어난 것은 그 결론에 이르는 과정이지 결론의 가짓수가 아니다.

**리프를 늘리지 않은 것이 특히 중요하다.** `Onboarding.FromStart` / `Onboarding.Resume` 식으로 가르면 **스플래시가 온보딩의 스텝 구조를 알게 되고**, 온보딩 spec §3.2가 이 문서에 넘기지 않은 재개 지점 판정(그 spec FR-023)이 여기로 새어 들어온다. 스플래시는 "온보딩으로 보낸다"까지만 알고 어느 스텝인지는 온보딩이 정한다.

**대가**: `SplashEntry.Onboarding`이라는 이름만 보면 판정 근거가 둘이라는 사실이 드러나지 않는다. KDoc이 그 의미를 든다.

**Alternatives considered**:
- *리프를 셋으로 가른다(`Onboarding.FromStart`·`Onboarding.Resume`·`Main`)* — 위 이유로 기각.
- *`SplashEntry` 대신 `OnboardingStep`을 그대로 돌려준다* — 스플래시가 온보딩의 도메인 enum을 화면 전환 값으로 쓰게 되어 결합이 더 세진다. 기각.

---

## R-019. 이 개정의 코드 변경은 온보딩 작업이 실행한다 *(plan 4.0.0)*

**결정**: `ResolveSplashEntryUseCase`와 그 테스트의 **설계는 이 계획이 소유**하고, **실제 코드 변경은 `docs/specs/onboarding-flow`의 구현 작업이 수행**한다. 이 계획의 `tasks.md`는 해당 작업을 `이관됨`으로 표시하고 실행 지시를 넣지 않는다.

**근거**: 새 의존인 `OnboardingProgressRepository`가 **온보딩 작업에서 처음 생긴다.** 두 변경을 다른 PR로 나누면 `ResolveSplashEntryUseCase`가 존재하지 않는 타입을 참조해 **스플래시 쪽 PR이 빌드되지 않는다.** 헌법의 빌드 게이트(`./gradlew :app:assembleQaDebug` 성공)를 통과할 수 없는 작업을 tasks에 실행 가능한 항목으로 두면 안 된다.

`docs/specs/onboarding-flow` plan 2.0.1이 이미 이 변경을 자기 범위 목록에 올려 두었고([contracts/onboarding-progress.md §4](../onboarding-flow/contracts/onboarding-progress.md)), 그 계획의 Constitution Check G14가 "요청 범위를 넘는 파일"로 명시적으로 판정했다.

**소유와 실행을 가른 이유**: 파일의 소유자는 그것을 만든 계획이어야 다음 개정에서 근거를 찾을 자리가 흔들리지 않는다. 실행자는 빌드 가능한 단위가 정한다. 둘은 다를 수 있고, 다를 때는 문서가 그것을 밝혀야 한다 — 밝히지 않으면 두 tasks.md가 같은 파일을 각자 고치거나 서로 미룬다.

**대가**: 이 계획의 tasks.md에 실행되지 않는 항목이 하나 생긴다. 온보딩 작업이 끝난 뒤 그 항목을 완료로 닫는 것은 사람의 확인이다.

**Alternatives considered**:
- *스플래시 tasks.md가 실행 가능한 작업으로 갖는다* — 온보딩이 Repository를 넣기 전에는 착수 불가라 "블록됨" 상태로 방치된다. 기각.
- *계약까지 온보딩 계획으로 넘긴다* — `ResolveSplashEntryUseCase`는 이 계획이 만든 표면이고 spec 4.0.0이 FR로 요구한다. 소유자가 파일과 어긋나 다음 개정에서 근거를 찾을 자리가 사라진다. 기각.

---

## R-020. 오프라인 재실행이 진입 판정에서 막히는 것을 받아들인다 *(plan 4.1.0)*

**결정**: 오프라인 재실행 사용자가 앱에 진입하지 못하는 현재 동작을 **설계로 확정한다.** 우회 경로를 만들지 않고, spec 5.0.0이 그 결과를 명세로 승인했다(그 문서 §5 TBD-5).

**드러난 사실**: `SplashViewModel`은 세션 확보와 진입 판정을 **한 `runCatchingDomain` 안에 묶어** 성공할 때까지 무한 재시도한다.

```
while (true) {
    attempt = runCatchingDomain { ensureAnonymousSession(); resolveSplashEntry() }
    attempt.getOrNull()?.let { return it }
    delay(RETRY_INTERVAL)
}
```

오프라인 재실행이면 `ensureAnonymousSession()`은 네트워크 없이 통과하지만 `resolveSplashEntry()`가 `GET /api/v1/users/me`에서 `Network`로 실패한다. 루프가 끝나지 않으므로 **스플래시에 영구 정박하고 10초마다 토스트만 뜬다.** plan 3.0.x·4.0.0은 이 사실을 문서 어디에도 적지 않았고, spec FR-011·EC-002·TS-016은 반대로 "오프라인 재실행도 정상 전환"을 적고 있었다.

**결정의 성격**: 이것은 설계 개선이 아니라 **문서와 코드 중 어느 쪽을 고칠지의 선택**이었다. 사용자가 코드를 고치지 않는 쪽을 택했다.

**대가 — 정직하게 적는다**:
- **오프라인 재실행은 흔한 경로다.** 지하철·비행기·데이터 소진 상태에서 앱을 여는 사용자가 이미 온보딩을 끝냈어도 저장된 장소를 볼 수 없다.
- PRD는 오프라인 이용 불가를 **최초 실행에만** 수용했다([SCR-001] Flow E). 이 결정은 그 범위를 넘어서므로 **PRD 개정 대상 2건 중 하나**가 됐다(spec §5 TBD-5).
- 사용자에게 보이는 것은 "네트워크 연결을 확인해주세요" 토스트가 10초마다 반복되는 스플래시다. 앱이 고장 났다는 인상을 줄 수 있으나, 안내 문구가 원인을 정확히 지목하므로 SC-003은 충족한다.

**Alternatives considered** — 둘 다 이번에 검토하고 기각했다. 나중에 이 대가를 되돌리려 할 때 같은 조사를 반복하지 않도록 남긴다.

- **안 ① — 완료 표시가 `true`면 서버 조회를 생략한다.** 온보딩 완료 표시는 프로필 저장 이후에만 기록되고 세션은 설치 수명 동안 만료되지 않으므로(PRD 「비회원 익명 세션」), `isCompleted == true`는 프로필 존재를 함의한다. 채택하면 **오프라인 재실행이 살아나고 재실행마다의 네트워크 왕복도 사라진다.**
  기각 사유: `프로필 없음 + 완료 표시 있음`(저장값 손상)을 진입 시점에 감지하지 못하게 되어 spec `EC-002-2`가 무효가 되고, [ADR 2026-08-29](../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md) §결정 3항의 호출 순서 제약에 예외를 세워야 한다. **spec·ADR 두 문서를 함께 고쳐야 하는 범위가 이번 선택보다 크다고 판단했다.**
- **안 ② — `isRegistered()`가 `Network`로 실패하고 완료 표시가 `true`면 `Main`으로 폴백한다.** 온라인에서는 `EC-002-2` 감지를 유지하면서 오프라인만 로컬 근거로 통과시킨다.
  기각 사유: spec `EC-004`의 "실패한 판정으로 임의 분기하지 않는다"를 정면으로 고쳐야 하고, 판정 규칙이 리프별 실패 처리와 얽혀 3갈래에서 5갈래로 늘어난다. 재실행마다의 네트워크 왕복도 그대로 남아 얻는 것이 절반이다.

**되돌리는 조건**: 오프라인 재실행이 실사용에서 문제로 보고되면 **안 ①이 첫 후보다.** 그때 필요한 것은 spec `EC-002-2` 폐기와 ADR §결정 3항의 적용 범위 축소 두 가지이며, 근거는 위에 다 적혀 있다.

**이 결정이 만드는 코드 변경**: **없다.** 이 계획이 문서를 코드에 맞췄다.
