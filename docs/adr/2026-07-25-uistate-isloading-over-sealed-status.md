# UiState는 sealed 화면 상태 대신 isLoading 분리형으로 설계한다

- **상태**: Proposed
- **작성일**: 2026-07-25
- **작성자**: Jaesung Lee

> 에러 처리 규약(이슈 #72) 논의에서 파생된 **제안 단계** 문서다. 전 feature의 UiState 설계 표준을 바꾸는 결정이라 별도 논의 후 확정한다. 확정 시 상태를 `Accepted`로 바꾸고, 아래 "결과"의 미확정 항목을 채운다.

## 컨텍스트

에러 처리 규약(이슈 #72)은 프로그래머 버그를 잡지 않고 전파해 최상위 `CoroutineExceptionHandler`(CEH)가 안전망으로 처리하는 구조를 채택했고, CEH 도달 시 화면 상태 복구는 하지 않기로 확정했다(규약 §3.3 A안 — 버그를 런타임에서 정상 흐름처럼 위장하지 않는다).

이때 남는 문제가 로딩 고착이다. 기존 화면들은 UiState를 `sealed` 단일 상태(예: `SampleStatus.Loading / Success / Error`) 하나로 모델링하는데, 이 형태에서는 `updateState { Loading }` 이후 버그로 코루틴이 죽으면 이후 상태 갱신이 실행되지 않아 화면이 무한 로딩에 고착된다. 그렇다고 로딩을 해제하려 해도 `Loading`이 화면 상태 그 자체라 **되돌릴 목적지가 없다** — 이전 상태를 보관하지 않고, "알 수 없는 오류" 상태를 신설하면 도메인 예외 계층에서 의도적으로 제거한 `Unknown` 리프가 State 쪽으로 부활한다.

`Result` 체인(`.also` 등)에서의 해제는 성립하지 않는다는 점도 확인됐다 — 버그 예외는 `runCatchingDomain`이 잡지 않아 `Result`가 반환되기 전에 throw로 전파되므로, 체인 뒷부분은 실행 자체가 되지 않는다.

## 결정

(제안) feature의 UiState는 화면 전체를 `sealed` 상태 하나로 표현하지 않고, **`isLoading: Boolean` + 콘텐츠·에러 필드 분리형**을 표준으로 한다.

```kotlin
// 이렇게 하지 않는다                          // 이렇게 한다
data class UiState(                            data class UiState(
    val status: Status,  // sealed                 val isLoading: Boolean = false,
)                                                  val user: GithubUser? = null,
                                                   val error: MinoDomainException? = null,
                                               )
```

`launchSafely`(CEH 결합 코루틴 헬퍼)가 코루틴 종료 시(`finally`) 로딩 해제를 자동 수행하는 것과 결합한다.

## 근거

- 로딩이 독립 필드가 되면 "해제"(`isLoading = false`)가 화면 상태와 무관하게 **항상 유효한 연산**이 된다. sealed 단일 상태의 "되돌릴 목적지 부재" 문제가 소멸한다.
- 해제를 `launchSafely`에 구우면 성공·도메인 실패·버그·취소 어느 경로로 끝나든 자동으로 동작한다. 호출부마다 `finally`를 심는 방식과 달리 리뷰 의존이 없다 — CEH 상태 복구 없이(§3.3 A안 유지) 로딩 고착을 **예방**으로 구조적으로 해결한다.
- 버그를 담을 가짜 에러 상태가 필요 없다. 버그 시 화면은 기존 콘텐츠(또는 빈 상태)로 남고, 사용자 안내는 전역 스낵바(`UncaughtErrorHandler` → Activity 루트 `CollectUncaughtError`)가 담당한다.
- 로딩과 콘텐츠가 동시에 존재하는 상태(pull-to-refresh 중 기존 목록 유지 등)를 자연스럽게 표현한다. sealed 단일 상태는 이런 경계 사례에서 콘텐츠를 버리는 표현밖에 없다.

## 결과

확정 시 예상되는 영향과, 논의에서 함께 확정해야 할 항목:

- 기존 sealed 단일 상태 화면(`SampleStatus` 등)의 분리형 전환 범위·시점 — 미확정.
- `launchSafely`의 자동 해제 메커니즘(헬퍼가 각 feature UiState의 `isLoading`에 접근하는 방법) — 미확정.
- sealed 상태가 주던 "불가능한 상태 표현 차단"(예: 로딩과 에러 동시 존재)의 상실을 수용할지, 별도 규약으로 보완할지 — 미확정.
- 주 데이터 로드 실패는 State의 에러 필드에 사용자 문구가 아닌 `MinoDomainException` 리프를 담고 화면이 렌더링 시점에 문구로 매핑한다(규약 §3.5와 정합).

## 고려한 대안

- **sealed 단일 상태 유지 + 고착 수용**: 예방 수단 없이 §3.3 A안만 적용하는 형태. release에 버그가 새어 나간 기간 동안 사용자가 무한 로딩 화면을 보게 되는 리스크를 그대로 안는다.
- **sealed 단일 상태 유지 + 호출부 `finally`/복구 콜백**: 호출부마다 심어야 해 리뷰 의존이 재발하고, 되돌릴 목적지 부재 문제가 그대로 남는다.
- **`Result` 체인에서의 해제 (`.also` 등)**: 버그 예외는 `Result`에 담기지 않고 즉시 전파되므로 체인이 실행되지 않아 성립 불가.
