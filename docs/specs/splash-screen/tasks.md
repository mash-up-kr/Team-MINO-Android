# 작업 목록: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**기준 plan 버전**: 4.1.0

**최초 작성일**: 2026-08-27

**최종 수정일**: 2026-08-29

**사전 조건**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/)

**테스트**: 포함한다. spec §1이 테스트 시나리오 TS-001~016을 정의하고 [quickstart.md](./quickstart.md) §2가 JVM 단위 테스트를 검증 수단으로 지정한다.

**구성 방식**: spec의 유저 플로우 2개를 사용자 스토리로 삼는다 — US1(브랜드 노출 및 자동 전환), US2(세션 확보 지연 및 실패 안내).

> **plan 4.1.0 반영(2026-08-29)**: spec 5.0.0이 오프라인 재실행의 약속을 걷어냈다. `T027`의 검증 대상이 좁아지고 **`T035`가 새로 붙었다** — 오프라인 재실행이 판정에서 막히는 것을 **확인**하는 작업이며, 기대값이 "진입 실패"인 유일한 시나리오다([research.md R-020](./research.md)). 코드 변경은 없다.

> **plan 4.0.0 반영(2026-08-29)**: 진입 판정이 근거 하나에서 둘로 늘었다. 이 변경이 만드는 작업 셋(T032·T033·T034)은 **다른 작업이 실행한다** — [이관된 작업](#이관된-작업-onboarding-flow가-실행) 섹션을 본다. 기존 작업의 ID와 체크 상태는 그대로다. 3.0.1 → 4.0.0 사이의 3.0.2 개정(DataStore 계층 정정)은 이미 T008에 반영돼 있었다.

## 형식: `[ID] [P?] [Story] 설명`

- **[ID]**: `T` + 세 자리 번호. 한 번 부여한 ID는 바꾸지 않고, 지운 번호는 재사용하지 않는다.
- **[P]**: 병렬 실행 가능 (서로 다른 파일, 의존성 없음)
- **[Story]**: 이 작업이 속한 사용자 스토리

## 경로 규칙

모바일 다중 Gradle 모듈. 신규 모듈은 `feature/splash/`, 공유 계층은 `core/`. 전체 트리는 [plan.md §프로젝트 구조](./plan.md)가 소유한다.

---

## Phase 1: 셋업 (공통 인프라)

**목적**: `:feature:splash` 모듈을 만들고 앱 그래프에 편입한다.

- [X] T001 `feature/splash/build.gradle.kts` 생성과 `settings.gradle.kts`에 `include(":feature:splash")` 추가 — 기존 진입형 feature(`feature/profile`)의 빌드 스크립트를 형태의 기준으로 삼는다
- [X] T002 `app/build.gradle.kts`에 `:feature:splash` 의존 추가
- [X] T003 [P] 브랜드 에셋을 `feature/splash/src/main/res/drawable-{mdpi,xhdpi,xxhdpi}/`에 WebP로 배치 (캐릭터 5종·`gguk` 워드마크·구름 배경) — 포맷·밀도 규칙은 [component-asset-placement.md §1.1](../../conventions/component-asset-placement.md), export 절차는 [figma-design-fidelity.md §1.3](../../conventions/figma-design-fidelity.md)

---

## Phase 2: 기반 작업 (공통 인프라)

**목적**: 두 스토리가 공통으로 쓰는 도메인 계약·데이터 구현·화면 골격.

- [X] T004 [P] `core/domain/src/main/kotlin/team/mino/core/domain/model/SplashEntry.kt`에 `SplashEntry` 봉인 타입 생성 (`Onboarding`·`Main`) — US1의 T017, US2의 T024가 쓴다. 정의는 [data-model.md §1.2](./data-model.md)
- [X] T005 [P] `core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRegistrationRepository.kt`에 `isRegistered(): Boolean` 인터페이스 생성 — T006·T008이 쓴다. 계약은 [contracts/profile-registration.md](./contracts/profile-registration.md)
- [X] T006 `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCase.kt` 생성 (T004·T005에 의존) — 세션은 확보하지 않는다([research.md R-012](./research.md)). **파일 생성은 끝났고, plan 4.0.0이 요구하는 두 근거 조합은 T032다**
- [X] T007 [P] `core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt`에 `GET /api/v1/users/me` 호출 추가 — Bearer는 기존 플러그인이 자동 첨부하므로 이 서비스가 토큰을 다루지 않는다
- [X] T008 `core/data/src/main/java/team/mino/core/data/datasource/`에 `UserRemoteDataSource` 인터페이스·구현과 `di/UserDataSourceModule.kt` 바인딩 생성, `repository/ProfileRegistrationRepositoryImpl.kt`는 DataSource 위임만 (T005·T007에 의존) — `200`→`true`, `401`+`USER_NOT_REGISTERED`→`false`, 그 밖의 `401`(`UNAUTHORIZED`·`TOKEN_EXPIRED`·모르는 코드)→`MinoDomainException.Http(401)` 그대로 전파, 연결 실패→`Network`. `errorCode` 분기는 DataSource가 갖는다 — `RepositoryImpl`은 `ApiService`를 알지도 예외를 잡지도 않는다([core/data README §3·§4·§6](../../../core/data/README.md))
- [X] T009 `core/data/src/main/java/team/mino/core/data/repository/di/`에 `ProfileRegistrationRepository` 바인딩 추가 (T008에 의존) — 배치 규칙은 [dependency-injection.md](../../conventions/dependency-injection.md)
- [X] T010 `feature/splash/src/main/java/team/mino/feature/splash/SplashActivity.kt`와 `SplashShell.kt` 골격 생성, `feature/splash/src/main/AndroidManifest.xml`에 MAIN·LAUNCHER intent-filter 선언 — 공개 표면은 `SplashActivity` 하나뿐이며 나머지는 `internal`([contracts/splash-ui.md §1](./contracts/splash-ui.md))
- [X] T011 `feature/main/src/main/AndroidManifest.xml`에서 `MainActivity`의 MAIN·LAUNCHER intent-filter 제거 (T010과 같은 커밋으로 묶는다) — 둘 다 LAUNCHER면 런처 아이콘이 두 개 뜬다
- [X] T012 `feature/splash/src/main/java/team/mino/feature/splash/main/SplashUiState.kt`에 `SplashUiState`·`SplashIntent`·`SplashSideEffect`·`SplashToast` 정의 — 전이표는 [data-model.md §2](./data-model.md)
- [X] T013 [P] `feature/splash/src/main/res/values/strings.xml`에 토스트 문구 2종 추가 (`네트워크 연결을 확인해주세요`, `일시적인 오류가 발생했어요`) — 문구는 PRD `[SCR-001]` Flow C·D가 확정한 값이다

**체크포인트**: T006이 끝나면 US1의 테스트(T014)를 시작할 수 있고, T012가 끝나면 US1·US2의 화면 작업을 시작할 수 있다.

---

## Phase 3: 사용자 스토리 1 - 브랜드 노출 및 자동 전환

**목표**: 앱을 실행하면 브랜드 화면이 3초 노출되고, 세션 확보와 프로필 판정이 끝나면 사용자 상태에 맞는 화면으로 자동 이동한다.

**독립 테스트**: 정상 네트워크에서 앱을 콜드 스타트해 3초(±0.5초) 뒤 프로필 유무에 맞는 화면으로 전환되는지, 그동안 스피너가 한 번도 보이지 않는지 확인한다 ([quickstart.md](./quickstart.md) §3-A·B).

### 사용자 스토리 1 테스트 ⚠️

> 구현 전에 작성하고 실패를 확인한다.

- [X] T014 [P] [US1] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCaseTest.kt` 작성 — `isRegistered()`가 `false`→`Onboarding`, `true`→`Main`, 예외는 그대로 전파되고 `Onboarding`으로 오판하지 않음 (TS-002·TS-003·EC-004). **plan 4.0.0이 요구하는 3갈래 확장은 T033이다**
- [X] T015 [P] [US1] `core/data/src/test/java/team/mino/core/data/datasource/UserRemoteDataSourceImplTest.kt` 작성 — `401`+`USER_NOT_REGISTERED`는 `false`, `401`+`UNAUTHORIZED`·`TOKEN_EXPIRED`·모르는 `errorCode`는 `Http(401)` 그대로 (SC-002). 검증 대상이 `errorCode` 분기를 가진 DataSource이므로 테스트도 그쪽에 둔다(T008)

### 사용자 스토리 1 구현

- [X] T016 [US1] `feature/splash/src/main/java/team/mino/feature/splash/main/SplashScreen.kt`에 브랜드 레이어 구현 (캐릭터 5종·워드마크·태그라인·구름 배경) — 상태와 무관하게 항상 노출한다 (FR-001·UX-004)
- [X] T017 [US1] `feature/splash/src/main/java/team/mino/feature/splash/main/SplashViewModel.kt`에 정상 경로 구현 — 최소 노출 3초와 `EnsureAnonymousSessionUseCase`를 독립 작업으로 띄우고 **둘 다 끝났을 때** `ResolveSplashEntryUseCase`를 호출해 `NavigateTo` 발행 (FR-002·UX-002, [research.md R-004](./research.md))
- [X] T018 [US1] `feature/splash/src/main/java/team/mino/feature/splash/SplashActivity.kt`에 전환 배선 — `Main`은 `MainLauncher`, `Onboarding`은 `ProfileLauncher`에 `PROFILE_ENTRY_POINT_ONBOARDING`을 실어 열고 둘 다 `withFinish = true` (FR-003·FR-004). **`OnboardingLauncher`로의 교체는 T034다**
- [X] T019 [US1] 스플래시가 터치·제스처를 소비하지 않고 전환에 영향을 주지 않게 한다 (FR-005·TS-004)

**체크포인트**: 정상 네트워크에서 스플래시가 끝까지 동작한다. 지연·실패 경로는 아직 없다.

> **plan 4.0.0이 이 스토리에 더한 작업 셋(T032·T033·T034)은 여기 있지 않다.** 실행자가 `docs/specs/onboarding-flow`라서 아래 [이관된 작업](#이관된-작업-onboarding-flow가-실행) 섹션이 갖는다. 그것들이 들어오기 전까지 **프로필을 저장한 뒤 온보딩을 중단한 사용자는 메인 탭으로 떨어진다**(spec SC-002 미충족).

---

## Phase 4: 사용자 스토리 2 - 세션 확보 지연 및 실패 안내 *(최초 실행 전용)*

**목표**: 세션 확보가 늦거나 실패해도 사용자가 앱이 멈췄다고 오해하지 않게 하고, 원인이 해소되면 조작 없이 이어간다.

**독립 테스트**: 네트워크를 끊고 앱 데이터를 지운 뒤 콜드 스타트해 3초에 스피너·13초에 토스트가 뜨고 어느 화면으로도 넘어가지 않는지, 네트워크를 되살리면 재실행 없이 전환되는지 확인한다 ([quickstart.md](./quickstart.md) §3-C~F).

### 사용자 스토리 2 테스트 ⚠️

- [X] T020 [P] [US2] `feature/splash/src/test/java/team/mino/feature/splash/main/SplashViewModelTest.kt` 작성 — 3초 경과 시 스피너 노출, 13초 경과 시 스피너 해제와 일시적 오류, `Network`와 그 밖(`Auth`·`Http`)이 서로 다른 토스트로 갈리는지, 토스트 반복이 10초 간격을 지키는지 (TS-007~TS-010·TS-015)

### 사용자 스토리 2 구현

- [X] T021 [US2] `SplashViewModel`에 스피너 전이 추가 — 최소 노출 3초가 지났는데 세션이 미확보면 `isSpinnerVisible = true`, 3초 안에 끝나면 한 번도 노출하지 않는다 (FR-006·UX-005)
- [X] T022 [US2] `SplashViewModel`에 13초 전이 추가 — 스피너 노출 후 10초가 더 지나면 스피너를 감추고 `ShowToast(TemporaryError)` 발행. **`withTimeout`을 쓰지 않는다**(`TimeoutCancellationException`이 CEH로 샌다, [research.md R-004·R-013](./research.md)) (FR-007)
- [X] T023 [US2] 실패 분기 추가 — `MinoDomainException.Network`→네트워크 문구, 그 밖의 리프(`Auth`·`Http`)→일시적 오류 문구 (FR-008·FR-009·UX-007). 리프→문구 매핑은 ViewModel이 아니라 `SplashRoute`가 갖는다([error_handling.md §5·§8](../../conventions/error_handling.md))
- [X] T024 [US2] `SplashViewModel`에 자동 재시도 루프 추가 — 실패·시간 초과 뒤에도 세션 확보를 반복 호출하고 성공하면 즉시 전환. 횟수 상한을 두지 않으며 **도메인 예외 수신에만 종속시키지 않는다**(호출자 계약 C-5) (FR-010·EC-005)
- [X] T025 [US2] `SplashViewModel`에 토스트 반복 억제 추가 — 직전 표출로부터 10초가 지나지 않으면 발행하지 않는다. 마지막 표출 시각은 UI 상태가 아니라 ViewModel 내부 값이다 (UX-006)
- [X] T026 [US2] `SplashScreen`에 스피너(Material3 `CircularProgressIndicator`, 28dp)와 `MinoSnackbar` 배치 — 브랜드 레이어 **위에** 얹고 토스트는 좌우 20dp·화면 하단 40dp 띄운 위치 (UX-003·UX-004, 실측 근거는 [research.md R-006](./research.md))
- [ ] T027 [US2] **온라인** 재실행 경로가 지연·실패 경로를 타지 않는지 확인 — 온보딩을 완료한 설치에서 저장된 세션이 네트워크 없이 복원되고 스피너·토스트 없이 메인 탭으로 전환된다 (FR-011·**TS-016-1**). 절차는 [quickstart.md §3-G](./quickstart.md)
- [ ] T035 [US2] **오프라인** 재실행이 앱 진입 판정에서 막히는지 확인 — 세션은 복원되지만 판정이 끝나지 않아 3초 스피너·13초 토스트를 거쳐 스플래시에 머물고, 네트워크 복구 시 재실행 없이 전환된다 (FR-010·FR-011·TS-016·EC-002·EC-006). 절차는 [quickstart.md §3-G-1](./quickstart.md). **기대값이 「진입 실패」인 유일한 작업이다** — 메인 탭이 열리면 판정을 건너뛰었거나 실패를 `Main`으로 뭉갠 회귀다([research.md R-020](./research.md))

**체크포인트**: 오프라인에서도 화면이 멈춘 것처럼 보이지 않고, 복구 시 스스로 이어간다. **최초 실행과 재실행이 같은 경로를 탄다** — 재실행은 세션 복원까지만 빠르고 판정은 똑같이 네트워크를 기다린다(T027·T035).

---

## Phase 5: 마무리 및 공통 관심사

- [X] T028 [P] Figma 대조 — 기본(`2314-134659`)·로딩(`3798-166720`)·네트워크 에러(`3798-166743`)·일시적 오류(`3798-166766`) 4종. 절차는 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)
- [X] T029 `./gradlew :app:assembleQaDebug` 빌드 확인 — 헌법 §품질 게이트가 정한 최소선
- [ ] T030 [quickstart.md](./quickstart.md) 검증 실행 — §1 진입점, §2 JVM 테스트, §3-A~G 수동 시나리오, §5 C-5 회귀. **§2의 판정 세 갈래와 §3-H는 T032~T034가 들어온 뒤에 돌아간다**
- [X] T031 `./gradlew ktlintCheck` 통과 확인

---

## 이관된 작업 (onboarding-flow가 실행)

plan 4.0.0이 정의했지만 **이 작업 목록에서는 실행하지 않는** 항목이다. 실행자는 `docs/specs/onboarding-flow`의 구현 작업이다.

**왜 이관하는가**: 세 작업 모두 `OnboardingProgressRepository`(온보딩 소유)를 필요로 한다. 그 계약이 `:core:domain`에 들어오기 전에는 컴파일되지 않으므로, 여기서 실행 가능한 항목으로 두면 헌법 §품질 게이트의 빌드 조건(`./gradlew :app:assembleQaDebug` 성공)을 통과할 수 없는 작업이 된다. 근거는 [research.md R-019](./research.md)·[plan.md D-1](./plan.md).

**설계의 소유자는 이 스펙이다.** 세 항목이 무엇이어야 하는지는 [contracts/splash-entry-decision.md](./contracts/splash-entry-decision.md)가 정의한다. 아래는 그 계약을 작업 단위로 옮긴 것이며, 온보딩 쪽 `tasks.md`가 같은 내용을 실행 항목으로 갖는다.

- [ ] T032 [US1] `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCase.kt`에 `OnboardingProgressRepository`를 함께 주입해 두 근거를 조합 (T006 위에 얹는다) — 세 갈래는 [contracts/splash-entry-decision.md §2](./contracts/splash-entry-decision.md), **`isRegistered()` 선행 호출 제약은 같은 문서 §3**. 순서를 바꾸면 컴파일도 테스트도 통과하면서 프로필 저장이 깨진다 (FR-002·FR-003·FR-004). **같은 커밋에서 `core/domain/src/main/kotlin/team/mino/core/domain/model/SplashEntry.kt`의 KDoc을 갱신한다** — 두 리프 주석이 아직 "프로필이 없다/있다"로 3.0.2 의미를 들고 있다([research.md R-018](./research.md))
- [ ] T033 [US1] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCaseTest.kt`를 **네 조합 전수**로 확장 (T014·T032 위에 얹는다) — `등록+미완료 → Onboarding`(TS-003-1)과 **`미등록+완료 → Onboarding`(EC-002-2)** 을 더한다. 두 Fake의 호출 기록으로 순서를 검증하고, 로컬 조회 실패가 `Main`으로 오판되지 않는 것도 함께 (TS-003-2·SC-002)
- [ ] T034 [US1] `feature/splash/src/main/java/team/mino/feature/splash/SplashActivity.kt`의 온보딩 전환을 `ProfileLauncher` 직접 호출에서 `OnboardingLauncher`로 교체 (T018 위에 얹는다) — 고정할 성질 셋은 [contracts/splash-ui.md §5](./contracts/splash-ui.md), 교체 근거는 [onboarding-flow contracts/onboarding-launcher.md §8](../onboarding-flow/contracts/onboarding-launcher.md) (FR-003)

**체크 상태의 주인**: 온보딩 작업이 머지된 뒤 사람이 확인해 닫는다. 이 목록이 스스로 체크하지 않는다.

**회귀 대상**: T032가 들어가면 `ResolveSplashEntryUseCaseTest`뿐 아니라 `:feature:splash`의 테스트도 함께 돌린다 — `./gradlew :core:domain:test :feature:splash:test`.

---

## 의존성 및 실행 순서

### 단계 간 의존성

- **Phase 1**: 의존성 없음 — 즉시 시작
- **Phase 2**: T001 완료에 의존(모듈이 있어야 파일을 놓는다). 단 T004~T009는 `core/`라 T001 없이도 가능
- **Phase 3·4**: 각 작업이 실제로 쓰는 기반 산출물에만 의존한다. Phase 2 전체를 기다리지 않는다
- **Phase 5**: 목표한 스토리 완료에 의존

### 작업 단위 의존

```
T001 ──> T002, T003, T010, T012, T013
T004 ─┐
T005 ─┴> T006 ──> T014, T017
T005 ─┐
T007 ─┴> T008 ──> T009, T015
T010 ──> T011 (같은 커밋)
T012 ──> T016, T017, T021~T026
T017 ──> T018, T019, T021, T022, T023, T024
T021 ──> T022
T023 ──> T025
T026 ──> T027, T035   # 두 재실행 시나리오는 화면이 완성된 뒤 눈으로 확인한다

# 이관된 작업 (onboarding-flow 실행)
onboarding-flow의 OnboardingProgressRepository ─┐
T006 ───────────────────────────────────────────┴> T032 ──> T033
onboarding-flow의 OnboardingLauncher ──┐
T018 ──────────────────────────────────┴> T034
```

### 사용자 스토리 간 의존성

- **US1**: 기반 산출물이 준비되면 시작 가능. 다른 스토리에 의존하지 않는다. **단 T032~T034는 `docs/specs/onboarding-flow`의 구현에 의존한다** — 이 스펙 밖의 일정 의존이며 그 셋이 없어도 US1의 나머지는 완결된다
- **US2**: **US1의 T017에 의존한다.** 지연·실패 전이가 정상 경로의 상태 기계 위에 얹히기 때문이다 — 두 스토리가 같은 `SplashViewModel`을 공유하므로 완전히 독립적이지 않다. 그래도 US1까지만 구현해도 정상 네트워크에서는 동작하는 증분이 된다

### 병렬 처리 기회

- T003은 T001과 무관하게 에셋 export만 먼저 할 수 있다
- T004·T005·T007은 서로 다른 파일이라 동시 진행 가능
- T014·T015는 서로 다른 모듈의 테스트라 동시 진행 가능
- T028은 화면 구현이 끝나는 대로 다른 마무리 작업과 병행 가능
- T027·T035는 기기 네트워크 상태만 다른 같은 절차라 연달아 수행한다 — 병렬은 아니지만 한 번에 묶어 확인하는 편이 빠르다

---

## 병렬 실행 예시: 기반 작업

```bash
# 도메인 계약과 네트워크 서비스를 함께:
Task: "core/domain/.../model/SplashEntry.kt에 SplashEntry 봉인 타입 생성"
Task: "core/domain/.../repository/ProfileRegistrationRepository.kt에 isRegistered() 인터페이스 생성"
Task: "core/data/.../network/service/UserApiService.kt에 GET /api/v1/users/me 추가"
```

## 병렬 실행 예시: 사용자 스토리 1 테스트

```bash
Task: "ResolveSplashEntryUseCaseTest.kt 작성 — 분기와 예외 전파"
Task: "ProfileRegistrationRepositoryImplTest.kt 작성 — errorCode 분기"
```

---

## 구현 전략

### MVP 우선 (US1만)

1. Phase 1 셋업 → 2. US1이 쓰는 기반(T004~T012) → 3. Phase 3 US1
4. **중단하고 검증**: 정상 네트워크에서 [quickstart.md](./quickstart.md) §3-A·B
5. 이 시점에 스플래시가 런처로 동작하고 분기가 맞는다. 지연·실패 안내만 없다

### 점진적 전달

1. US1 완료 → 정상 경로 데모
2. US2 추가 → 오프라인·지연 시나리오 데모
3. Phase 5로 디자인 대조와 빌드·검증을 닫는다

### 팀 병렬 전략

- 개발자 A: `core/` 계약·구현·테스트 (T004~T009, T014, T015)
- 개발자 B: `feature/splash` 모듈·화면 (T001, T003, T010~T013, T016)
- 두 갈래가 만나는 지점이 T017이다. 그 뒤 US2(T021~T027·T035)는 한 사람이 이어가는 편이 낫다 — 전부 같은 `SplashViewModel` 파일을 건드린다

---

## 커버리지 확인

spec의 요구사항이 모두 작업에 대응한다.

| 요구사항 | 대응 작업 |
|---|---|
| FR-001 | T003, T016 |
| FR-002 | T006, T017, **T032** |
| FR-003 | T018, **T032**, **T034** |
| FR-004 | T018, **T032** |
| FR-005 | T019 |
| FR-006 | T021 |
| FR-007 | T022 |
| FR-008 · FR-009 | T023 |
| FR-010 | T024, **T035** |
| FR-011 | T027 (온라인·TS-016-1), **T035** (오프라인·TS-016) |
| UX-001 | T019, T024 |
| UX-002 | T017 |
| UX-003 | T026 |
| UX-004 | T016, T026 |
| UX-005 | T021 |
| UX-006 | T025 |
| UX-007 | T023, T013 |
| SC-001~006 | T030 (quickstart 검증). **SC-002는 T032·T033이 들어와야 충족된다** |

굵게 표시한 셋은 [이관된 작업](#이관된-작업-onboarding-flow가-실행)이다.

**설계 미확정 없음.** plan 4.1.0의 미확정이 0건이고, 모든 계약이 배포 OpenAPI 또는 형제 스펙의 계약에 근거한다.

**일정 의존 1건.** `OnboardingProgressRepository`가 아직 없어 T032~T034를 착수할 수 없다(plan D-1). 설계가 갈리는 지점이 아니라 순서 제약이다.

**spec 5.0.0이 승인한 제약 1건.** 오프라인 재실행 사용자는 네트워크가 돌아올 때까지 앱을 쓸 수 없다. 결함이 아니라 명세가 명시적으로 정한 동작이며 `T035`가 그것을 확인한다. 되돌리려 할 때의 대체안 둘과 각각의 대가는 [research.md R-020](./research.md)이 든다.

**현재 코드가 spec 5.0.0을 충족하지 못하는 지점 1건.** `ResolveSplashEntryUseCase`가 프로필 등록 여부만 보고 있어 **프로필 저장 후 온보딩을 중단한 사용자가 메인 탭으로 떨어진다**(FR-003·TS-003-1·SC-002 미충족). T032가 닫는다.

**참고 (이 스펙 범위 밖)**: Figma 스낵바 인스턴스 높이 48과 `MinoSnackbar` 토큰 조합(54)이 어긋나 보인다. T028에서 드러나면 `:core:design-system` 쪽 이슈로 넘긴다 — 스플래시는 컴포넌트를 그대로 쓴다([research.md R-006](./research.md)).

---

## 참고 사항

- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다. T010·T011은 런처가 둘이 되는 중간 상태를 남기지 않도록 **한 커밋**으로 묶는다
- 이 저장소에는 PR을 검증하는 CI가 없다(헌법 §검증 장치의 한계). T029·T031은 로컬에서 직접 돌린다
- T032~T034는 **온보딩 작업의 커밋에 들어간다.** 이 스펙의 브랜치에서 따로 커밋하지 않는다 — 나누면 어느 쪽 브랜치도 빌드되지 않는다
