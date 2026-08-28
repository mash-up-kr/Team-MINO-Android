# 작업 목록: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**기준 plan 버전**: 3.0.1

**최초 작성일**: 2026-08-27

**최종 수정일**: 2026-08-27

**사전 조건**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/)

**테스트**: 포함한다. spec §1이 테스트 시나리오 TS-001~016을 정의하고 [quickstart.md](./quickstart.md) §2가 JVM 단위 테스트를 검증 수단으로 지정한다.

**구성 방식**: spec의 유저 플로우 2개를 사용자 스토리로 삼는다 — US1(브랜드 노출 및 자동 전환), US2(세션 확보 지연 및 실패 안내).

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
- [X] T006 `core/domain/src/main/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCase.kt` 생성 (T004·T005에 의존) — 세션은 확보하지 않는다([research.md R-012](./research.md))
- [X] T007 [P] `core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt`에 `GET /api/v1/users/me` 호출 추가 — Bearer는 기존 플러그인이 자동 첨부하므로 이 서비스가 토큰을 다루지 않는다
- [X] T008 `core/data/src/main/java/team/mino/core/data/repository/ProfileRegistrationRepositoryImpl.kt` 구현 (T005·T007에 의존) — `200`→`true`, `401`+`USER_NOT_REGISTERED`→`false`, `401`+`UNAUTHORIZED`·`TOKEN_EXPIRED`→`MinoDomainException.Auth`, 연결 실패→`Network`
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

- [X] T014 [P] [US1] `core/domain/src/test/kotlin/team/mino/core/domain/usecase/ResolveSplashEntryUseCaseTest.kt` 작성 — `isRegistered()`가 `false`→`Onboarding`, `true`→`Main`, 예외는 그대로 전파되고 `Onboarding`으로 오판하지 않음 (TS-002·TS-003·EC-004)
- [X] T015 [P] [US1] `core/data/src/test/java/team/mino/core/data/repository/ProfileRegistrationRepositoryImplTest.kt` 작성 — `401`+`USER_NOT_REGISTERED`는 `false`, `401`+`UNAUTHORIZED`·`TOKEN_EXPIRED`는 `Auth` 예외 (SC-002)

### 사용자 스토리 1 구현

- [X] T016 [US1] `feature/splash/src/main/java/team/mino/feature/splash/main/SplashScreen.kt`에 브랜드 레이어 구현 (캐릭터 5종·워드마크·태그라인·구름 배경) — 상태와 무관하게 항상 노출한다 (FR-001·UX-004)
- [X] T017 [US1] `feature/splash/src/main/java/team/mino/feature/splash/main/SplashViewModel.kt`에 정상 경로 구현 — 최소 노출 3초와 `EnsureAnonymousSessionUseCase`를 독립 작업으로 띄우고 **둘 다 끝났을 때** `ResolveSplashEntryUseCase`를 호출해 `NavigateTo` 발행 (FR-002·UX-002, [research.md R-004](./research.md))
- [X] T018 [US1] `feature/splash/src/main/java/team/mino/feature/splash/main/SplashRoute.kt`에 전환 배선 — `Main`은 `MainLauncher`, `Onboarding`은 `ProfileLauncher`에 `PROFILE_ENTRY_POINT_ONBOARDING`을 실어 열고 둘 다 `withFinish = true` (FR-003·FR-004)
- [X] T019 [US1] 스플래시가 터치·제스처를 소비하지 않고 전환에 영향을 주지 않게 한다 (FR-005·TS-004)

**체크포인트**: 정상 네트워크에서 스플래시가 끝까지 동작한다. 지연·실패 경로는 아직 없다.

---

## Phase 4: 사용자 스토리 2 - 세션 확보 지연 및 실패 안내 *(최초 실행 전용)*

**목표**: 세션 확보가 늦거나 실패해도 사용자가 앱이 멈췄다고 오해하지 않게 하고, 원인이 해소되면 조작 없이 이어간다.

**독립 테스트**: 네트워크를 끊고 앱 데이터를 지운 뒤 콜드 스타트해 3초에 스피너·13초에 토스트가 뜨고 어느 화면으로도 넘어가지 않는지, 네트워크를 되살리면 재실행 없이 전환되는지 확인한다 ([quickstart.md](./quickstart.md) §3-C~F).

### 사용자 스토리 2 테스트 ⚠️

- [X] T020 [P] [US2] `feature/splash/src/test/java/team/mino/feature/splash/main/SplashViewModelTest.kt` 작성 — 3초 경과 시 스피너 노출, 13초 경과 시 스피너 해제와 일시적 오류, `Network`/`Auth`가 서로 다른 토스트로 갈리는지, 토스트 반복이 10초 간격을 지키는지 (TS-007~TS-010·TS-015)

### 사용자 스토리 2 구현

- [X] T021 [US2] `SplashViewModel`에 스피너 전이 추가 — 최소 노출 3초가 지났는데 세션이 미확보면 `isSpinnerVisible = true`, 3초 안에 끝나면 한 번도 노출하지 않는다 (FR-006·UX-005)
- [X] T022 [US2] `SplashViewModel`에 13초 전이 추가 — 스피너 노출 후 10초가 더 지나면 스피너를 감추고 `ShowToast(TemporaryError)` 발행. **`withTimeout`을 쓰지 않는다**(`TimeoutCancellationException`이 CEH로 샌다, [research.md R-004·R-013](./research.md)) (FR-007)
- [X] T023 [US2] `SplashViewModel`에 실패 분기 추가 — `MinoDomainException.Network`→`NetworkError`, `Auth`→`TemporaryError` (FR-008·FR-009·UX-007)
- [X] T024 [US2] `SplashViewModel`에 자동 재시도 루프 추가 — 실패·시간 초과 뒤에도 세션 확보를 반복 호출하고 성공하면 즉시 전환. 횟수 상한을 두지 않으며 **도메인 예외 수신에만 종속시키지 않는다**(호출자 계약 C-5) (FR-010·EC-005)
- [X] T025 [US2] `SplashViewModel`에 토스트 반복 억제 추가 — 직전 표출로부터 10초가 지나지 않으면 발행하지 않는다. 마지막 표출 시각은 UI 상태가 아니라 ViewModel 내부 값이다 (UX-006)
- [X] T026 [US2] `SplashScreen`에 스피너(Material3 `CircularProgressIndicator`, 28dp)와 `MinoSnackbar` 배치 — 브랜드 레이어 **위에** 얹고 토스트는 좌우 20dp·화면 하단 40dp 띄운 위치 (UX-003·UX-004, 실측 근거는 [research.md R-006](./research.md))
- [ ] T027 [US2] 재실행 경로가 지연·실패 경로를 타지 않는지 확인 — 저장된 세션이 네트워크 없이 복원되어 스피너·토스트 없이 전환된다 (FR-011·TS-016·EC-002)

**체크포인트**: 오프라인 최초 실행에서도 화면이 멈춘 것처럼 보이지 않고, 복구 시 스스로 이어간다.

---

## Phase 5: 마무리 및 공통 관심사

- [X] T028 [P] Figma 대조 — 기본(`2314-134659`)·로딩(`3798-166720`)·네트워크 에러(`3798-166743`)·일시적 오류(`3798-166766`) 4종. 절차는 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)
- [X] T029 `./gradlew :app:assembleQaDebug` 빌드 확인 — 헌법 §품질 게이트가 정한 최소선
- [ ] T030 [quickstart.md](./quickstart.md) 검증 실행 — §1 진입점, §2 JVM 테스트, §3-A~G 수동 시나리오, §5 C-5 회귀
- [X] T031 `./gradlew ktlintCheck` 통과 확인

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
```

### 사용자 스토리 간 의존성

- **US1**: 기반 산출물이 준비되면 시작 가능. 다른 스토리에 의존하지 않는다
- **US2**: **US1의 T017에 의존한다.** 지연·실패 전이가 정상 경로의 상태 기계 위에 얹히기 때문이다 — 두 스토리가 같은 `SplashViewModel`을 공유하므로 완전히 독립적이지 않다. 그래도 US1까지만 구현해도 정상 네트워크에서는 동작하는 증분이 된다

### 병렬 처리 기회

- T003은 T001과 무관하게 에셋 export만 먼저 할 수 있다
- T004·T005·T007은 서로 다른 파일이라 동시 진행 가능
- T014·T015는 서로 다른 모듈의 테스트라 동시 진행 가능
- T028은 화면 구현이 끝나는 대로 다른 마무리 작업과 병행 가능

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
- 두 갈래가 만나는 지점이 T017이다. 그 뒤 US2(T021~T027)는 한 사람이 이어가는 편이 낫다 — 전부 같은 `SplashViewModel` 파일을 건드린다

---

## 커버리지 확인

spec의 요구사항이 모두 작업에 대응한다.

| 요구사항 | 대응 작업 |
|---|---|
| FR-001 | T003, T016 |
| FR-002 | T006, T017 |
| FR-003 | T018 |
| FR-004 | T018 |
| FR-005 | T019 |
| FR-006 | T021 |
| FR-007 | T022 |
| FR-008 · FR-009 | T023 |
| FR-010 | T024 |
| FR-011 | T027 |
| UX-001 | T019, T024 |
| UX-002 | T017 |
| UX-003 | T026 |
| UX-004 | T016, T026 |
| UX-005 | T021 |
| UX-006 | T025 |
| UX-007 | T023, T013 |
| SC-001~006 | T030 (quickstart 검증) |

**미결 사항 없음.** plan 3.0.0의 미확정이 0건이고, 모든 계약이 배포 OpenAPI에 근거한다.

**참고 (이 스펙 범위 밖)**: Figma 스낵바 인스턴스 높이 48과 `MinoSnackbar` 토큰 조합(54)이 어긋나 보인다. T028에서 드러나면 `:core:design-system` 쪽 이슈로 넘긴다 — 스플래시는 컴포넌트를 그대로 쓴다([research.md R-006](./research.md)).

---

## 참고 사항

- 커밋 단위는 [`commit-message.md`](../../conventions/commit-message.md)의 쪼개기 원칙을 따른다. T010·T011은 런처가 둘이 되는 중간 상태를 남기지 않도록 **한 커밋**으로 묶는다
- 이 저장소에는 PR을 검증하는 CI가 없다(헌법 §검증 장치의 한계). T029·T031은 로컬에서 직접 돌린다
