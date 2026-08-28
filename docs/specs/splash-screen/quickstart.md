# 검증 가이드: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**계획서**: [plan.md](./plan.md)

이 문서는 스플래시가 엔드투엔드로 동작함을 증명하는 **검증 절차**다. 구현 방법은 [contracts/](./contracts/)와 [data-model.md](./data-model.md)가 소유한다.

---

## 선행 조건

선행 의존은 모두 `develop`에 들어와 있다 — `EnsureAnonymousSessionUseCase`·`MinoDomainException.Auth`(anonymous-auth-session), `ProfileLauncher`·`ProfileActivity`(profile). 별도 대기 없이 착수할 수 있다.

서버 대조 기준: [Team MINO API 1.0.0](https://api.gguk.org/api-docs-json), 2026-08-27T20:19:22+09:00 조회.

## 빌드

```sh
./gradlew :app:assembleQaDebug
```

헌법 §품질 게이트가 정한 빌드 확인의 최소선이다. 로컬 `lintDebug`는 JBR JIT 이슈로 데몬이 죽을 수 있으며, 죽었다고 해서 검증이 수행된 것은 아니다.

## 1. 진입점 확인

```sh
./gradlew :app:assembleQaDebug
adb install -r app/build/outputs/apk/qa/debug/app-qa-debug.apk
adb shell cmd package resolve-activity -c android.intent.category.LAUNCHER com.mino.gguk.qa
```

**기대**: `SplashActivity`가 유일한 LAUNCHER로 나온다. `MainActivity`가 함께 나오면 §2 매니페스트 동반 변경이 누락된 것이다.

## 2. 분기 규칙 — JVM 단위 테스트

`ResolveSplashEntryUseCase`는 `:core:domain`(Kotlin JVM)에 있어 Android 없이 돌아간다. **세션 확보는 이 UseCase의 책임이 아니다**(→ [research.md R-012](./research.md)) — 세션 계약의 검증은 `anonymous-auth-session` 스펙의 quickstart가 소유한다.

```sh
./gradlew :core:domain:test
```

| 검증 | Given | 기대 | 대응 |
|---|---|---|---|
| 최초 실행 | `isRegistered()`가 `false` | `SplashEntry.Onboarding` | FR-003 / TS-002 |
| 재실행 | `isRegistered()`가 `true` | `SplashEntry.Main` | FR-004 / TS-003 |
| 네트워크 실패 분리 | `isRegistered()`가 `Network` 던짐 | 전파되고 `Onboarding`으로 오판하지 않는다 | EC-004 |
| 인증 실패 분리 | `isRegistered()`가 `Http(401)` 던짐 | 전파되고 `Onboarding`으로 오판하지 않는다 | FR-009 |

`:core:data` 쪽은 `errorCode` 분기를 따로 검증한다 — `401`+`USER_NOT_REGISTERED`는 `false`, `401`+`UNAUTHORIZED`·`TOKEN_EXPIRED`·모르는 `errorCode`는 `MinoDomainException.Http(401)` 그대로여야 한다(SC-002). `Auth`로 재매핑하지 않는다 — 그 리프는 인증 제공자 실패 전용이고, HTTP 원천의 매핑 지점은 Ktor validator 하나다([research.md R-016](./research.md)).

## 3. 화면 동작 — 수동 시나리오

각 시나리오는 **콜드 스타트**로 시작한다. 백그라운드 복귀는 스플래시를 노출하지 않는다(spec §4 가정).

```sh
adb shell am force-stop com.mino.gguk.qa
adb shell am start -n com.mino.gguk.qa/team.mino.feature.splash.SplashActivity
```

### A. 정상 경로 (TS-001·TS-002·TS-003·TS-006)

1. 앱을 실행한다
2. **기대**: 캐릭터 5종·워드마크·태그라인이 즉시 노출된다
3. **기대**: 로딩 스피너가 **한 번도** 보이지 않는다
4. **기대**: 3초(±0.5초) 뒤 프로필 유무에 맞는 화면으로 전환된다
5. 뒤로가기를 누른다 → **기대**: 스플래시로 돌아오지 않는다(`withFinish`)

### B. 터치 무시 (TS-005)

1. 스플래시 노출 중 화면을 연타·스와이프한다
2. **기대**: 전환 시점이 앞당겨지거나 늦춰지지 않는다

### C. 지연 → 스피너 → 일시적 오류 (TS-007·TS-008)

세션 확보를 인위적으로 지연시킨다(네트워크 쓰로틀 또는 가짜 구현).

1. 3초까지 → **기대**: 스피너 없음
2. 3초 경과 → **기대**: 브랜드 화면 위에 스피너가 나타나고 **전환하지 않는다**
3. 13초 경과(스피너 후 10초) → **기대**: 스피너가 사라지고 `일시적인 오류가 발생했어요` 토스트

> 13초는 **앱 실행 기준**이다. 스피너 노출부터 10초를 더 세는 값이며, 총 10초가 아니다(spec §5 TBD-1).

### D. 네트워크 에러 (TS-009·TS-011)

```sh
adb shell svc wifi disable && adb shell svc data disable
```

1. 앱 데이터를 지워 최초 실행 상태로 만든 뒤 실행한다
2. **기대**: `네트워크 연결을 확인해주세요` 토스트
3. **기대**: 토스트가 화면 하단에서 **40dp** 띄운 위치에 뜬다
4. **기대**: 어느 화면으로도 넘어가지 않고 스플래시에 머무른다 (TS-012)
5. **기대**: 재시도 버튼이 **없다** (TS-013)

### E. 자동 재시도 복구 (TS-014)

D 상태를 유지한 채로:

```sh
adb shell svc wifi enable && adb shell svc data enable
```

**기대**: 앱을 다시 실행하지 않아도 스스로 다음 화면으로 전환된다.

### F. 토스트 반복 간격 (TS-015)

D 상태를 60초간 유지한다.

**기대**: 토스트가 연달아 쌓이지 않고, 직전 표출로부터 최소 10초 간격으로만 다시 뜬다.

### G. 재실행은 지연 경로 미진입 (TS-016·EC-002)

1. A를 한 번 완료해 세션을 저장한다
2. 기기를 오프라인으로 만든다
3. 앱을 콜드 스타트한다
4. **기대**: 스피너·토스트 없이 3초 뒤 메인 탭으로 전환된다

> 이 시나리오가 실패하면 세션 복원이 네트워크를 타고 있다는 뜻이다(FR-011 위반).

## 4. 디자인 대조

[figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)의 절차로 아래 노드와 대조한다.

| 상태 | Figma 노드 |
|---|---|
| 기본 | [2314-134659](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-134659) |
| 로딩 중 | [3798-166720](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=3798-166720) |
| 네트워크 에러 | [3798-166743](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=3798-166743) |
| 일시적 오류 | [3798-166766](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=3798-166766) |

이미지 에셋은 WebP·밀도별 배치 여부를 함께 확인한다([component-asset-placement.md §1.1](../../conventions/component-asset-placement.md)).

## 5. 호출자 계약 회귀 (C-5)

`anonymous-auth-session`의 호출자 계약 C-5는 코드 리뷰로만 잡히는 조건이라 별도로 확인한다.

1. `EnsureAnonymousSessionUseCase`가 `MinoDomainException`으로 매핑되지 않는 예외(예: `IllegalStateException`)를 던지도록 Fake를 구성한다
2. 앱을 콜드 스타트한다
3. **기대**: 화면이 안내도 재시도도 없이 멈춘 채 남지 않는다 — 재시도 루프가 계속 돌고, 13초 경과 시 일시적 오류 토스트가 뜬다

> 이 시나리오가 실패하면 재시도 루프가 `runCatchingDomain`의 실패 콜백에만 걸려 있다는 뜻이다(→ [research.md R-013](./research.md)).
