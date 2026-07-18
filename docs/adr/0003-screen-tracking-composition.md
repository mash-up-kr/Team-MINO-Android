# 0003. 화면 트래킹은 호출부 조립 방식을 유지하고 TrackNavHost 래퍼는 강제화 시점까지 보류한다

- **상태**: Accepted
- **작성일**: 2026-07-18
- **작성자**: full_avocado

## 컨텍스트

`core:analytics`가 화면 자동 로깅을 제공한다(이슈 #73 / PR #74). 현재 feature의 NavHost 진입점에서는 두 컴포저블을 나란히 호출해 조립한다.

```kotlin
val navController = rememberNavController()
TrackScreenViews(navController)          // core:analytics — 화면 조회 자동 로깅
MinoNavHost(navController, startDestination, modifier) { ... }  // core:navigation — 표준 NavHost
```

이 둘을 하나로 묶은 `TrackNavHost` 래퍼를 제공하면 호출부가 한 줄로 줄고, `TrackScreenViews` 호출을 깜빡해 특정 feature의 화면 로깅이 누락되는 footgun을 없앨 수 있다는 제안이 나왔다. 지금 결정해야 할 것은 "래퍼를 지금 도입할지, 그리고 도입한다면 어디에 둘지"다.

## 결정

- **현행 유지**: 화면 트래킹은 feature NavHost 진입점에서 `TrackScreenViews` + `MinoNavHost`를 **호출부에서 명시적으로 조립**한다. 각 교차 관심사는 독립된 drop-in 컴포저블로 둔다.
- **TrackNavHost 래퍼는 보류**: 화면 트래킹에 **강제성이 필요해지는 시점**까지 도입하지 않는다.
- **도입 시 위치는 `core:analytics`로 확정**: `TrackNavHost`는 `MinoNavHost`(+`Route`, `core:navigation`)와 `TrackScreenViews`(`core:analytics`) 둘 다에 의존하므로 `core:analytics`에 두고, `core:analytics`가 `core:navigation`을 `implementation`으로 의존한다. `core:navigation`에는 두지 않는다.

## 근거

- **아직 강제성이 필요 없다.** NavHost에 얹히는 교차 관심사가 analytics 하나뿐이라, 호출부에서 명시적으로 조립하는 편이 각 모듈의 책임을 선명하게 유지한다. 트래킹이 필수가 아니어서 `MinoNavHost`를 트래킹 없이 쓰는 유연성도 남겨둔다.
- **의존 방향이 결정을 제약한다.** `core:navigation`은 "전환 인프라만 제공"한다는 경계를 갖는다(README·`modularization.md`). 여기에 `TrackNavHost`를 두면 순수 전환 인프라가 analytics를 역의존하게 되어 레이어가 뒤집힌다. 반대로 `core:analytics`가 `core:navigation`을 의존하는 방향은 자연스럽고 사이클도 없다 — analytics는 이미 Compose + navigation-compose 모듈이고 `TrackScreenViews`라는 네비게이션-인지 컴포저블을 갖고 있어, `core:navigation` 의존 추가는 새로운 종류의 결합이 아니라 성격상 연장이다.
- **래퍼 조합 폭발을 경계한다.** 교차 관심사가 더 생기면(예: 화면별 권한 체크) `TrackNavHost` 위에 또 래핑하는 조합 폭발이 생긴다. 관심사가 하나뿐인 지금 래퍼의 실익은 제한적이고, 호출부 조립은 N개 관심사로 자연스럽게 확장된다.

## 결과

- feature NavHost는 당분간 `TrackScreenViews` + `MinoNavHost` 2줄 조립을 유지한다. 신규 feature도 동일 패턴을 따른다(트래킹은 컨벤션 플러그인이 `core:analytics`를 자동 의존시키므로 import만 하면 된다).
- **전환 트리거**: 화면 트래킹을 누락 없이 강제해야 하는 요구가 생기면 아래를 적용한다.
  1. `core/analytics/build.gradle.kts`에 `implementation(project(":core:navigation"))` 추가.
  2. `core/analytics/.../screen/TrackNavHost.kt` 추가 — `TrackScreenViews(navController)` 후 `MinoNavHost(navController, startDestination, modifier, builder)` 호출.
  3. feature NavHost들을 `TrackNavHost` 한 줄로 교체하고 `core/analytics/README.md` 갱신.
- 이 결정은 특정 관심사(analytics)에 한정한다. "모든 것을 NavHost에 래핑" 패턴으로 번지지 않게 선을 둔다.

## 고려한 대안

- **지금 TrackNavHost를 `core:analytics`에 도입** — 호출 한 줄로 줄고 footgun을 제거한다. 그러나 트래킹 강제성이 아직 불필요하고, 관심사가 하나뿐인 현재 래퍼의 실익이 크지 않아 보류했다. 강제화가 필요해질 때 즉시 적용할 수 있도록 위치·구현 형태만 확정해 두었다.
- **TrackNavHost를 `core:navigation`에 도입** — 호출부는 가장 간결해지지만 전환 인프라가 analytics를 역의존해 레이어가 뒤집힌다. 모듈 경계 위반으로 배제.
- **`MinoNavHost`에 트래킹을 내장(옵션 파라미터 등)** — `core:navigation`이 analytics를 직접 알게 되어 위와 같은 경계 위반. 배제.
