# 에러 처리 규약

이 문서는 레이어를 가로지르는 **정책**(분류 기준·책임 배분·리뷰 규약)만 다룬다. 각 API의 시그니처·사용법·확장 규칙은 담당 모듈 README를 단일 출처로 한다 (§2 표 참조).

## 1. 골격 — 2단 분류

분류 기준은 **"data 레이어가 예상할 수 있는 실패인가"** 다. "API 에러인가"가 아니다.

```
[예상 가능한 실패]  원천 예외 ─(원천별 단일 지점 매핑 §3)→ MinoDomainException ─throw→ Repository
                    → runCatchingDomain이 잡음 ┬─ 주 데이터 로드 실패 → State(리프 보관) → 화면이 문구 매핑
                                               └─ 액션 일회성 실패 → emitDomainError → Route가 수집·매핑 (스낵바)
[버그]              아무도 잡지 않음 → launchSafely의 CEH → 리포터 + UncaughtErrorHandler(전역) → 셸(MinoScaffold)이 수집 (스낵바)
```

- 예상 가능한 실패(네트워크 단절·타임아웃·HTTP 에러)는 `MinoDomainException`으로 매핑해 ViewModel이 소비한다.
- 프로그래머 버그(NPE 등)는 잡지 않고 전파한다. 최상위 코루틴의 `CoroutineExceptionHandler`(CEH)가 안전망으로 처리하며, **정상 유저 시나리오에서 CEH에 도달하면 안 된다.**

## 2. 레이어별 책임

| 모듈 | 책임 | API 상세 |
|---|---|---|
| `core:error-handling` | 도메인 예외 계층(`MinoDomainException`)·소비 헬퍼(`runCatchingDomain`·`onDomainFailure`)·에러 채널(`DomainErrorEmitter`·`UncaughtErrorHandler`) 정의 | [`core/error-handling/README.md`](../../core/error-handling/README.md) |
| `core:data` | 원천 예외 → 도메인 예외 매핑. 지점은 **원천마다 하나**다 — HTTP는 `HttpResponseValidator`, 인증 제공자 SDK는 `Task` → suspend 변환 지점 (§3) | [`core/data/README.md`](../../core/data/README.md) |
| `core:common:android` | CEH 결합 `launchSafely` | [`core/common/android/README.md`](../../core/common/android/README.md) |
| `core:common:ui` | `CollectDomainError` · `CollectUncaughtError` · 미처리 예외 수집과 스낵바 호스트를 소유하는 셸 `MinoScaffold` | [`core/common/ui/README.md`](../../core/common/ui/README.md) |
| `feature:*` | 소비 — State/`DomainErrorEmitter` 분기(§5), 셸에서 `MinoScaffold` 사용(§6) | — |

Ktor·인증 제공자 SDK의 예외 타입 등 구현 디테일은 data 레이어 밖으로 새어 나가지 않는다. UI 문구 매핑은 presentation 레이어(Route)의 책임이다 — 도메인 예외는 사용자 노출 문구를 담지 않는다. (리프별 문구 정책은 §8 미정)

## 3. 매핑 정책 — data 레이어

매핑은 데이터소스별 catch가 아니라 **원천마다 하나씩 둔 지점**에서 수행한다. 그 지점에 요구되는 성질은 둘이다 — 해당 원천의 **모든 호출이 통과하는 한 곳**일 것, 그리고 **화이트리스트 열거**일 것. 이 성질 덕분에 새 API·새 호출을 더해도 매핑 누락이 구조적으로 불가능하다.

| 원천 | 매핑 지점 | 비고 |
|---|---|---|
| HTTP (Ktor) | `HttpClient` 설정의 `HttpResponseValidator` (`core:data`의 `convertDomainException`) | `expectSuccess = true`를 유지해 HTTP 에러도 예외로 받아 실패 모델을 throw로 통일한다 |
| 인증 제공자 SDK | `Task` → suspend 변환 지점 (`core:data`의 `awaitDomain`) | 인증 제공자 호출은 반드시 `Task`를 반환하고 데이터 레이어 계약은 전부 `suspend`이므로, 이 변환을 우회하는 호출 경로가 없다 |
| FCM Messaging SDK | `Task` → suspend 변환 지점 (`core:data`의 `push/extension/Task.kt` `awaitDomain`) | 인증 제공자용과 형태는 같지만 지점은 별개다 — 화이트리스트가 원천마다 다르므로 재사용하지 않는다. 배경은 [push-notification research D10](../specs/push-notification/research.md#d10-firebase-messaging-sdk-예외의-도메인-매핑-지점을-새로-연다) |

`HttpClient`를 거치지 않는 원천(다른 SDK·시스템 API)이 추가되면 같은 형태를 따른다 — 그 원천의 모든 호출이 통과하는 변환·접근 지점을 하나 만들고 매핑을 거기에만 둔다. 지점이 여럿이라고 데이터소스별 catch가 열리는 것은 아니다. 결정 배경은 [원천별 매핑 지점 ADR](../adr/2026-08-22-domain-exception-mapping-per-source.md).

열거 밖 예외는 지점과 무관하게 그대로 rethrow되어 CEH로 간다:

| 원천 | 예외 | 처리 |
|---|---|---|
| 공통 | `CancellationException` | **절대 매핑 금지** — 원본 그대로 rethrow (코루틴 취소 보존). 넓은 타입(`RuntimeException` 등) 분기를 추가해 이 예외가 매핑되게 하지 말 것 |
| HTTP | `ResponseException` (non-2xx) | `MinoDomainException.Http(code)` |
| HTTP | `IOException` 계열 | `MinoDomainException.Network` |
| 인증 제공자 | 연결 실패 | `MinoDomainException.Network` |
| 인증 제공자 | 세션·신원 증명 발급 실패 (호출 한도 초과·인증 구성 오류 등) | `MinoDomainException.Auth` |
| FCM Messaging SDK | 연결 실패 | `MinoDomainException.Network` |
| FCM Messaging SDK | 토큰 조회 실패 (서비스 불가·전달자 서비스 없음 등) | `MinoDomainException.Network` |
| 공통 | 그 외 전부 (직렬화 예외 포함) | rethrow → CEH (버그 취급) |

- 직렬화 실패는 서버 계약 위반(버그)으로 본다 — 도메인 예외로 매핑하지 않는다.
- 인증 제공자 갈래에 속하는 SDK 예외 클래스의 목록은 매핑 지점의 화이트리스트가 소유한다. 위 표는 갈래를 정하고, 어느 클래스가 어느 갈래인지는 그 열거가 정한다. 열거를 넓히는 상위 타입 분기를 추가하지 않는다.
- 엔드포인트별 특수 정책(예: 특정 API의 404를 빈 결과로 취급)이 필요한 지점만 지역 catch를 병용한다.
- Repository 인터페이스는 도메인 모델을 직접 반환한다 (실패 = throw). `Result` 반환형으로 바꾸지 않는다.
- DB 예외는 DB 도입 시점에 매핑 대상 추가를 검토한다.

## 4. ViewModel 소비 정책

- 코루틴 시작과 `Result` 실패 소비는 리뷰 규약(§7)을 따른다 — `launchSafely`·`onDomainFailure`만 사용.
- 예외 → `Result` 변환 지점은 `runCatchingDomain` **한 곳**이다. `MinoDomainException`만 잡고 버그·`CancellationException`은 통과시켜 CEH로 보낸다. 같은 이유로 `kotlin.runCatching`은 사용하지 않는다 — `Throwable` 전부를 잡아 버그가 CEH에 도달하지 못하고, 코루틴 취소까지 깨뜨린다.
- CEH는 `launch`에만 동작한다. `async` / `withContext`에는 적용되지 않는다.

## 5. UI 소비 정책 — State vs `DomainErrorEmitter`

| 실패 성격 | 통로 | UI |
|---|---|---|
| 화면의 주 데이터 로드 실패 | **State** — 문구가 아닌 `MinoDomainException` 리프를 담는다 | 에러 화면 + 재시도, 렌더링 시점에 문구 매핑 |
| 사용자 액션의 일회성 실패 | **`DomainErrorEmitter`** — 리프를 그대로 방출 | Route가 수집해 스낵바 |

- `DomainErrorEmitter`는 **ViewModel 인스턴스별 채널**이다. 전역 버스로 두면 백그라운드 ViewModel의 실패가 다른 화면에 오배달된다. 셸은 어느 ViewModel이 방출하는지 알 수 없으므로 이 수집은 셸이 아니라 **Route**가 한다 — 스낵바 호스트만 셸에서 `LocalSnackbarHostState`로 받아 쓴다(§6).
- 기존 MVI SideEffect 채널과의 역할 구분: feature 고유 이벤트(네비게이션·성공 토스트 등)는 SideEffect, 도메인 에러의 일회성 안내는 `DomainErrorEmitter`.
- 문구 매핑은 Route 측에서 한다. 리프별 사용자 문구 정책이 미정이라 공통 기본 매퍼는 두지 않는다 — 정책 확정 시 도입을 검토한다 (§8).
- 경계 사례(pull-to-refresh 실패·페이징 추가 로드 실패 등)의 분류는 첫 적용 화면 구현 시 결정한다.

## 6. CEH 안전망 정책

- CEH 동작: `Timber.e` 리포팅 후 사용자 안내. 빌드 타입별 출력은 앱 모듈의 Timber Tree 배선이 결정한다 — debug는 Logcat, release는 Crashlytics non-fatal 수집. 별도 리포터 훅은 두지 않는다(Tree가 교체 지점이므로 중복 간접층).
- 사용자 안내는 빌드 타입과 무관하게 전역 이벤트 버스 `UncaughtErrorHandler`로 전달한다. **미처리 예외(버그) 전용 통로**이며 도메인 예외는 여기로 오지 않는다.
- UI 수집은 **셸이 소유**한다: `MinoScaffold`가 `CollectUncaughtError`와 `SnackbarHost`를 함께 갖고 있어 feature는 별도 배선 없이 규약을 만족한다. 셸은 NavHost 바깥·`setContent` 바로 아래에 위치한다. 수집 기준은 `RESUMED` — resumed Activity는 최대 1개이므로 이중 수신이 없고, 수집 공백 중 이벤트는 Channel 버퍼가 보존한다.
- 이 "최대 1개" 전제 때문에 **`MinoScaffold`는 Activity당 하나만 연다**. 한 Activity에서 두 번 열면 같은 예외로 스낵바가 두 번 뜬다.
- 셸이 만든 스낵바 호스트는 `LocalSnackbarHostState`로 하위에 제공된다. Route가 도메인 에러(§5)를 표시할 때 이 호스트를 쓰고, stateless한 `XScreen`으로는 내려보내지 않는다.
- **CEH 도달 시 화면 상태는 복구하지 않는다.** 버그 대응은 리포팅 → 수정이 정공법이며, 런타임에서 버그를 정상 흐름처럼 위장하지 않는다. 죽는 것은 해당 코루틴 하나이며 UI·네비게이션은 살아 있다.

## 7. 리뷰 규약

커스텀 lint 규칙으로의 기계 검사 전환은 후속 이슈로 둔다.

1. ViewModel에서 `viewModelScope.launch` 직접 호출 금지 — 항상 `launchSafely`.
2. 모든 셸(`XShell`)은 `MinoScaffold`를 연다 — 미처리 예외 수집이 여기 딸려 오므로 화면·Activity에서 따로 선언하지 않는다.
3. `Result`에 표준 `onFailure` 사용 금지 — 항상 `onDomainFailure`.
4. 모든 `runCatchingDomain` 결과는 실패 분기를 반드시 소비한다(§5에 따라 State 반영 또는 `emitDomainError`). `Result`는 실패를 던지지 않으므로, 생략하면 도메인 예외가 조용히 유실된다.

## 8. 미해결·이연 항목

- 경계 사례(pull-to-refresh·페이징 추가 로드 실패)의 State/이벤트 분류 — 첫 적용 화면 구현 시 결정
- 도메인 에러 리프별 사용자 문구 정책(공통 기본 매퍼 도입 여부 포함) — 미정, 확정 시 `core:common:ui`에 매퍼 도입 검토
- UiState를 `isLoading` 분리형으로 표준화(로딩 고착의 구조적 예방) — [UiState isLoading 분리형 ADR](../adr/2026-07-25-uistate-isloading-over-sealed-status.md) `Proposed`, 논의 후 확정
- Crashlytics 도입 — 별도 이슈
- 리뷰 규약의 커스텀 lint 전환 — 후속 이슈
- feature 고유 비즈니스 에러 리프 확장·`else` 허용 재검토 — API 스펙 확정 시

---

결정 배경·기각한 대안은 [에러 처리 2단 구조 ADR](../adr/2026-07-25-error-handling-two-tier-convention.md)에 기록되어 있다.
