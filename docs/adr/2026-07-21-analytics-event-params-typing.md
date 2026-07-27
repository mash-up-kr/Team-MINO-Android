# AnalyticsTracker.logEvent 파라미터는 Map<String, Any>를 유지하고 타입 세이프 빌더는 커스텀 이벤트 도입 시점까지 보류한다

- **상태**: Accepted
- **작성일**: 2026-07-21
- **작성자**: full_avocado

## 컨텍스트

`core:analytics`의 커스텀 이벤트 API는 현재 느슨하게 타입을 받는다.

```kotlin
fun logEvent(name: String, params: Map<String, Any> = emptyMap())
```

`FirebaseAnalyticsTracker.toBundle()`은 지원 타입(String/Int/Long/Double/Float/Boolean)만 Bundle에 담고, 그 외 타입은 `else` 분기에서 `toString()`으로 떨어뜨린다. PR #74 리뷰에서 "`Any`로 받으면 Firebase가 지원하지 않는 타입(커스텀 객체 등)이 들어와도 컴파일에서 막히지 않고 조용히 `toString()`으로 새어, 잘못된 사용을 늦게 발견한다"는 Should Fix 지적이 나왔다. 지원 타입만 컴파일 타임에 강제하는 빌더/sealed 타입 제안이 함께 올라왔다.

## 결정

- **현행 유지**: `logEvent`의 파라미터는 당분간 `Map<String, Any>`로 둔다.
- **타입 세이프 빌더는 보류**: 지원 타입을 컴파일 타임에 강제하는 빌더 DSL 도입은 **실제 커스텀 이벤트를 처음 도입하는 시점**까지 미룬다.
- **도입 시 방향 확정**: Firebase KTX의 `logEvent("name") { param(...) }`처럼 지원 타입별 오버로드(`param(String, String)`·`param(String, Long)`·`param(String, Double)`·`param(String, Boolean)` 등)를 가진 빌더 DSL로 전환한다.

## 근거

- **아직 호출부가 없다.** PR #74는 화면 자동 로깅(`TrackScreenViews` → `logScreenView`)만 포함하고 `logEvent`는 호출부가 하나도 없다. 사용 예가 없는 상태에서 API 표면을 미리 고정하면, 실제 이벤트가 요구하는 파라미터 형태와 어긋난 채 굳을 위험이 있다.
- **첫 실사용 시점이 설계 근거가 된다.** 실제 이벤트를 붙이는 순간 어떤 파라미터 타입·네이밍·검증이 필요한지가 드러난다. 그때 빌더를 설계하면 근거 있는 API가 나온다.
- **지적의 위험은 인지하고 있다.** `toString()` 폴백으로 잘못된 타입이 조용히 통과하는 문제는 실재하며, 도입 방향(타입별 오버로드 빌더)도 이미 합의됐다. 보류는 "안 한다"가 아니라 "실사용과 함께 반영한다"이다.

## 결과

- `AnalyticsTracker.logEvent`는 현재 시그니처를 유지한다. 이 API를 쓰는 새 코드가 생기기 전까지는 변경하지 않는다.
- **전환 트리거**: 첫 커스텀 이벤트를 도입할 때 아래를 함께 반영한다.
  1. `AnalyticsTracker`에 타입별 오버로드를 가진 빌더 DSL(`logEvent(name) { param(...) }`)을 추가하고 `Map<String, Any>` 오버로드는 제거하거나 내부용으로 좁힌다.
  2. `FirebaseAnalyticsTracker`의 `toBundle()` `else`(`toString()`) 폴백을 걷어내 미지원 타입이 컴파일 타임에 막히도록 한다.
  3. `core/analytics/README.md`의 이벤트 파라미터 규칙을 갱신한다.
- 관련 리뷰 스레드: PR #74 `AnalyticsTracker.kt` 타입 안전성 코멘트.

## 고려한 대안

- **지금 타입 세이프 빌더로 반영** — 리뷰 지적을 즉시 해소하고 잘못된 타입을 컴파일 타임에 막는다. 그러나 `logEvent` 호출부가 아직 없어 실사용 근거 없이 API를 고정하게 되므로, 첫 이벤트 도입과 묶기로 하고 보류했다.
- **sealed 타입(`AnalyticsValue`)으로 값 래핑** — 컴파일 타임 제약은 되지만 호출부가 `mapOf("k" to AnalyticsValue.Str("v"))`처럼 장황해진다. 빌더 DSL이 같은 안전성을 더 간결하게 제공하므로 빌더 쪽을 채택 방향으로 둔다.
- **현행 `Map<String, Any>` 영구 유지** — Firebase Bundle의 느슨한 타입을 그대로 반영하지만, `toString()` 폴백으로 오류가 늦게 발견되는 문제가 남아 배제했다.
