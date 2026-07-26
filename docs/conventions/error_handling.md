# 에러 처리 규약

이 문서는 레이어를 가로지르는 **정책**(분류 기준·책임 배분·리뷰 규약)만 다룬다. 각 API의 시그니처·사용법·확장 규칙은 담당 모듈 README를 단일 출처로 한다 (§2 표 참조).

## 1. 골격 — 2단 분류

분류 기준은 **"data 레이어가 예상할 수 있는 실패인가"** 다. "API 에러인가"가 아니다.

```
[예상 가능한 실패]  Ktor 예외 ─(validator 전역 매핑)→ MinoDomainException ─throw→ Repository
                    → runCatchingDomain이 잡음 ┬─ 주 데이터 로드 실패 → State(리프 보관) → 화면이 문구 매핑
                                               └─ 액션 일회성 실패 → emitDomainError → Route가 수집·매핑 (스낵바)
[버그]              아무도 잡지 않음 → launchSafely의 CEH → 리포터 + UncaughtErrorHandler(전역) → Activity 루트 수집 (스낵바)
```

- 예상 가능한 실패(네트워크 단절·타임아웃·HTTP 에러)는 `MinoDomainException`으로 매핑해 ViewModel이 소비한다.
- 프로그래머 버그(NPE 등)는 잡지 않고 전파한다. 최상위 코루틴의 `CoroutineExceptionHandler`(CEH)가 안전망으로 처리하며, **정상 유저 시나리오에서 CEH에 도달하면 안 된다.**

## 2. 레이어별 책임

| 모듈 | 책임 | API 상세 |
|---|---|---|
| `core:error-handling` | 도메인 예외 계층(`MinoDomainException`)·소비 헬퍼(`runCatchingDomain`·`onDomainFailure`)·에러 채널(`DomainErrorEmitter`·`UncaughtErrorHandler`·`UncaughtErrorReporter`) 정의 | [`core/error-handling/README.md`](../../core/error-handling/README.md) |
| `core:data` | Ktor 예외 → 도메인 예외 **전역 매핑** (`NetworkModule`의 `HttpResponseValidator`) | [`core/data/README.md`](../../core/data/README.md) |
| `core:common:android` | CEH 결합 `launchSafely` | [`core/common/android/README.md`](../../core/common/android/README.md) |
| `core:common:ui` | `CollectDomainError` · `CollectUncaughtError` | [`core/common/ui/README.md`](../../core/common/ui/README.md) |
| `feature:*` | 소비 — State/`DomainErrorEmitter` 분기(§5), Activity 루트 수집(§6) | — |

Ktor 예외 타입 등 구현 디테일은 data 레이어 밖으로 새어 나가지 않는다. UI 문구 매핑은 presentation 레이어(Route)의 책임이다 — 도메인 예외는 사용자 노출 문구를 담지 않는다. (리프별 문구 정책은 §8 미정)

## 3. 매핑 정책 — data 레이어

매핑은 데이터소스별 catch가 아니라 `HttpClient` 설정의 `HttpResponseValidator`(`core:data`의 `convertDomainException`)에서 **전역 수행**한다. 새 API 추가 시 매핑 누락이 구조적으로 불가능하다. `expectSuccess = true`를 유지해 HTTP 에러도 예외로 받아 실패 모델을 throw로 통일한다.

매핑은 **화이트리스트 열거**다. 열거 밖 예외는 그대로 rethrow되어 CEH로 간다:

| 예외 | 처리 |
|---|---|
| `CancellationException` | **절대 매핑 금지** — 최상단에서 그대로 rethrow (코루틴 취소 보존) |
| `ResponseException` (non-2xx) | `MinoDomainException.Http(code)` |
| `IOException` 계열 | `MinoDomainException.Network` |
| 그 외 전부 (직렬화 예외 포함) | rethrow → CEH (버그 취급) |

- 직렬화 실패는 서버 계약 위반(버그)으로 본다 — 도메인 예외로 매핑하지 않는다.
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

- `DomainErrorEmitter`는 **ViewModel 인스턴스별 채널**이다. 전역 버스로 두면 백그라운드 ViewModel의 실패가 다른 화면에 오배달된다.
- 기존 MVI SideEffect 채널과의 역할 구분: feature 고유 이벤트(네비게이션·성공 토스트 등)는 SideEffect, 도메인 에러의 일회성 안내는 `DomainErrorEmitter`.
- 문구 매핑은 Route 측에서 한다. 리프별 사용자 문구 정책이 미정이라 공통 기본 매퍼는 두지 않는다 — 정책 확정 시 도입을 검토한다 (§8).
- 경계 사례(pull-to-refresh 실패·페이징 추가 로드 실패 등)의 분류는 첫 적용 화면 구현 시 결정한다.

## 6. CEH 안전망 정책

- CEH 동작: `UncaughtErrorReporter` 훅(fatal 경로만 정의) 호출 후 사용자 안내. 리포팅 도구는 Crashlytics로 확정하되 도입은 별도 이슈로 이연 — 도입 전까지 기본 구현은 로그 출력(`Timber.e`, 빌드 타입 무관)이며, 도입 시 release 빌드만 Crashlytics 리포트로 교체한다.
- 사용자 안내는 빌드 타입과 무관하게 전역 이벤트 버스 `UncaughtErrorHandler`로 전달한다. **미처리 예외(버그) 전용 통로**이며 도메인 예외는 여기로 오지 않는다.
- UI 수집은 **Activity 루트**에서 한다: 각 Activity가 `setContent` 바로 아래(NavHost 밖)에서 `CollectUncaughtError`를 선언하고, 스낵바 호스트도 같은 레벨에 둔다. 수집 기준은 `RESUMED` — resumed Activity는 최대 1개이므로 이중 수신이 없고, 수집 공백 중 이벤트는 Channel 버퍼가 보존한다.
- **CEH 도달 시 화면 상태는 복구하지 않는다.** 버그 대응은 리포팅 → 수정이 정공법이며, 런타임에서 버그를 정상 흐름처럼 위장하지 않는다. 죽는 것은 해당 코루틴 하나이며 UI·네비게이션은 살아 있다.

## 7. 리뷰 규약

커스텀 lint 규칙으로의 기계 검사 전환은 후속 이슈로 둔다.

1. ViewModel에서 `viewModelScope.launch` 직접 호출 금지 — 항상 `launchSafely`.
2. 모든 Activity는 `setContent` 루트에서 `CollectUncaughtError`를 선언한다.
3. `Result`에 표준 `onFailure` 사용 금지 — 항상 `onDomainFailure`.

## 8. 미해결·이연 항목

- 경계 사례(pull-to-refresh·페이징 추가 로드 실패)의 State/이벤트 분류 — 첫 적용 화면 구현 시 결정
- 도메인 에러 리프별 사용자 문구 정책(공통 기본 매퍼 도입 여부 포함) — 미정, 확정 시 `core:common:ui`에 매퍼 도입 검토
- UiState를 `isLoading` 분리형으로 표준화(로딩 고착의 구조적 예방) — [UiState isLoading 분리형 ADR](../adr/2026-07-25-uistate-isloading-over-sealed-status.md) `Proposed`, 논의 후 확정
- `CollectUncaughtError` 선언을 공용 루트 컴포저블(가칭 `MinoAppContent`)로 묶는 구조적 보장 — 추후 논의
- Crashlytics 도입 — 별도 이슈
- 리뷰 규약의 커스텀 lint 전환 — 후속 이슈
- feature 고유 비즈니스 에러 리프 확장·`else` 허용 재검토 — API 스펙 확정 시

---

결정 배경·기각한 대안은 [에러 처리 2단 구조 ADR](../adr/2026-07-25-error-handling-two-tier-convention.md)에 기록되어 있다.
