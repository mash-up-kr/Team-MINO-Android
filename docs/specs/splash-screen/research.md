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

**결정 (plan 1.0.0)**: `UserRepository.getMyProfile(): UserProfile?`로 감싸고, `null`이면 프로필 없음(최초 실행)으로 판정한다. HTTP 응답과 `null`의 대응은 `:core:data`가 흡수한다.

**근거**:
- 스웨거의 `GET /api/v1/users/me`가 유일한 프로필 조회 경로다. `200 → User`, `401 → Error`만 정의돼 있다.
- 도메인은 HTTP 코드를 몰라야 한다(원칙 II). `UserProfile?`로 노출하면 feature는 "있음/없음"만 본다.

**[TBD-P2]**: **프로필 미생성 사용자를 나타내는 응답이 스웨거에 정의돼 있지 않다.** `401`은 인증 실패이지 프로필 부재가 아니며, `404`는 이 엔드포인트에 없다. 세션은 확보됐지만 프로필이 없는 최초 실행 사용자에게 서버가 무엇을 돌려주는지 확정이 필요하다. 확정 전에는 FR-003(최초 실행 → 프로필 설정)의 판정 근거가 없다.

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

**[TBD-P3]**: Figma 012-3·012-4의 토스트가 `MinoSnackbar` 컴포넌트셋과 같은 것인지 디자인 대조가 필요하다. 절차는 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)를 따른다.

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

**[TBD-P4]**: **온보딩/프로필 설정 feature 모듈이 아직 없다.** `feature/194-onboarding-flow`·`feature/159-profile-setup` 브랜치가 존재하나 `develop`에 머지되지 않았다. 따라서 `OnboardingLauncher` 계약의 대상 Activity가 없어 FR-003의 전환을 배선할 수 없다. 온보딩 feature가 들어온 뒤 계약을 추가한다.

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

## R-011. 실패 원인 분류 — `Network` / `Auth`

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

---

## R-013. CEH로 새는 실패에도 재시도가 끊기지 않게 한다

**결정 (plan 2.0.0)**: 재시도 루프를 **도메인 예외 수신에만 종속시키지 않는다.** `MinoDomainException`으로 매핑되지 않은 실패가 발생해도 화면이 안내·재시도 없이 멈춘 채 남지 않아야 한다.

**근거**: `anonymous-auth-session`의 호출자 계약 **C-5**가 요구하는 조건이다. 예외 매핑은 화이트리스트라 열거 밖의 실패는 CEH로 가는데, 그때 재시도 루프가 `runCatchingDomain`의 실패 콜백에만 걸려 있으면 루프가 조용히 끝나고 스플래시가 영구히 멈춘다. FR-010(세션 확보까지 자동 재시도)과 SC-003(멈춘 것으로 오해할 여지 없음)이 함께 깨진다.

**함께 지킬 것**: 13초 임계(FR-007)를 `withTimeout`으로 걸면 `TimeoutCancellationException`이 `CancellationException`이라 도메인 예외 경로를 타지 않고 CEH로 샌다. R-004가 이미 `withTimeout`을 기각했으므로 충돌하지 않는다.
