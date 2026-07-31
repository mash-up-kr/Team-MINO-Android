# 네비게이션 셸을 `MinoScaffold`로 승격하고 feature 셸(`XShell`)과 화면 그래프(`XNavHost`)를 분리한다

- **상태**: Accepted
- **작성일**: 2026-07-31
- **작성자**: Jaesung Lee

## 컨텍스트

[Scaffold·insets는 셸이 소유한다](2026-07-29-scaffold-ownership-navhost.md)로 "`Scaffold`는 그래프당 하나, `XNavHost`가 소유한다"까지는 정해졌지만, 셸의 **모양**은 각 feature가 알아서 M3 `Scaffold`를 여는 형태였다. `:feature:main`에 탭 셸을 붙이면서 이 셸을 상위 공통 컴포저블로 올릴지가 논점이 됐다.

조사에서 두 가지가 드러났다.

- `error_handling.md` §7 리뷰 규약 2번("모든 Activity는 `setContent` 루트에서 `CollectUncaughtError`를 선언한다")을 지키는 곳이 **0곳**이었다. `CollectUncaughtError`·`CollectDomainError`는 정의만 있고 호출부가 없었고, `SnackbarHost`도 코드베이스에 없었다. 규약이 리뷰 항목으로만 존재하고 구조가 뒷받침하지 않았다.
- 같은 문서 §8에 "`CollectUncaughtError` 선언을 공용 루트 컴포저블(가칭 `MinoAppContent`)로 묶는 구조적 보장 — 추후 논의"가 이연 항목으로 남아 있었다. 셸을 공통화하면 이 항목이 자연히 해소된다.

초기 제안은 셸이 `NavHost` 슬롯과 `content` 슬롯을 **각각** 갖고, 화면 전환이 있는 feature는 전자를, 단일 화면 feature는 후자를 쓰는 형태였다.

셸에 표준을 넣기로 하자 이름 문제가 따라왔다. 기존 `XNavHost`는 `navController` 소유·화면 조회 로깅·chrome·그래프 등록 네 가지를 하는데 이름은 그중 하나만 가리켰고, 그마저도 `MinoScaffold`가 최상위가 되면서 더 이상 바깥 껍데기가 아니게 됐다. 안에 `MinoNavHost`를 품고 있어 "NavHost가 NavHost를 담는" 모양이기도 했다. 문서가 이미 전부 "셸(`XNavHost`)"로 괄호를 쳐 온 것이 이름이 역할을 못 담고 있다는 신호였다.

## 결정

네비게이션 셸을 `:core:common:ui`의 `MinoScaffold`로 승격하고, feature 쪽 컴포저블을 **셸과 그래프 둘로 나눈다**.

- **`XShell`** — `MinoScaffold`로 chrome·insets를 열고, `navController`를 만들어 화면 조회 로깅(`TrackScreenViews`)까지 담당한다. Activity가 호출하는 진입점이다.
- **`XNavHost`** — `MinoNavHost` + `screen<T>` 등록만 한다. `navController`는 셸에서 인자로 받는다.
- **슬롯은 `content` 하나다.** 화면 전환이 있으면 그 안에서 `XNavHost`를, 단일 화면이면 화면 컴포저블을 직접 그린다.
- **미처리 예외 안내를 셸이 소유한다.** `CollectUncaughtError` + `SnackbarHost`를 셸이 갖고 있어 feature는 배선하지 않는다. 대신 **Activity당 `MinoScaffold`는 하나**다.
- **스낵바 호스트는 `LocalSnackbarHostState`(CompositionLocal)로 내려준다.** 도메인 에러는 ViewModel 인스턴스별 채널이라 셸이 대신 수집할 수 없으므로, 수집은 Route가 하고 표시할 호스트만 셸에서 받는다. Route에서만 읽고 `XScreen`으로는 내려보내지 않는다.
- **인셋 패딩은 셸이 적용하지 않고 `PaddingValues`로 넘긴다.**
- 배경·인셋 기본값은 `MinoScaffoldDefaults`가 공급한다.
- 화면이 slot API나 full-bleed 때문에 예외적으로 `Scaffold`를 열어야 할 때는 **M3 `Scaffold`를 직접** 쓴다. `MinoScaffold`를 중첩하면 미처리 예외 수집이 중복된다.

## 근거

- **슬롯을 나눌 이유가 없다.** 셸 입장에서 두 슬롯이 하는 일은 "`innerPadding`을 받아 그 안을 그린다"로 동일하다. 나누면 (1) 배타성이 타입으로 강제되지 않아 둘 다 넘기거나 둘 다 안 넘겨도 컴파일되고, (2) `navController`를 셸이 만들지 호출부가 만들지가 흐려진다. 셸이 만들면 단일 화면에서 낭비고 `TrackScreenViews`까지 떠안으며, 안 만들면 셸이 `NavHost`를 알 이유가 없다.
- **얇은 래퍼는 가치가 없다.** M3 `Scaffold`를 감싸기만 하면 파라미터 pass-through만 늘어난다. 셸이 값을 가지려면 프로젝트 표준을 안에 넣어야 하고, 지금 가장 값이 큰 것이 규약만 있고 지켜지지 않던 미처리 예외 수집이었다.
- **`snackbarHost`를 파라미터로 뚫지 않는 것이 요점이다.** 셸이 호스트를 소유해야 수집·표시가 한 몸으로 보장된다. 대가로 "Activity당 하나"라는 제약이 생기지만, 이는 `RESUMED` Activity가 최대 1개라는 기존 수집 전제와 같은 층위의 제약이다.
- **CompositionLocal을 고른 이유** — 스낵바 호스트는 화면의 도메인 입력이 아니라 테마·인셋과 같은 성격의 셸 환경이다. `screen<T> { }` 등록부를 전부 관통시키면 화면마다 파라미터 노이즈가 생긴다. 기본값을 `error(...)`로 둬 셸 밖 사용은 즉시 실패한다.
- **인셋 패딩을 자동 적용하지 않는 이유** — 셸이 `Modifier.padding(innerPadding)`을 안쪽에 적용해버리면 리스트가 `contentPadding`으로 하단 바 뒤까지 스크롤되는 표준 동작을 만들 수 없다. `Modifier.padding` 누락을 구조로 막는 이득보다 이 손실이 크다.
- **셸과 그래프를 나눈 이유** — 이름을 하나 고르는 대신 컴포저블을 쪼개면 두 이름이 모두 정확해진다. Now in Android도 `NiaApp`(chrome)과 `NiaNavHost`(그래프)를 같은 방식으로 나눈다. 부수적으로 `navController` 소유자가 셸로 못박혀, chrome이 현재 목적지를 읽는 일과 화면 로깅이 그래프 등록부에 섞이지 않는다.
- 배치를 `:core:common:ui`로 정한 것은 `CollectUncaughtError`를 물리는 순간 에러 인프라 의존이 생기기 때문이다. `:core:design-system`에 두면 "색·타이포·컴포넌트" 경계가 깨진다. `:core:common:ui`는 이미 `:core:error-handling`·`:core:design-system`을 의존하고 모든 feature `impl`이 이 모듈을 물고 있어 빌드 스크립트 변경이 없다.

## 결과

- `error_handling.md` §6의 수집 위치가 "Activity 루트"에서 "셸(`MinoScaffold`)"로 바뀌고, §7 리뷰 규약 2번은 "모든 셸은 `MinoScaffold`를 연다"로 완화된다(구조가 강제하므로 확인 대상이 줄어든다). §8의 `MinoAppContent` 이연 항목은 제거된다.
- 2026-07-29 ADR이 남긴 마이그레이션(화면 4곳에서 `Scaffold` 제거)을 이 작업과 함께 완료했다. 셸이 `MinoScaffold`를 여는 상태에서 화면이 `Scaffold`를 유지하면 인셋이 두 번 적용되므로 분리할 수 없었다.
- `feature-module.md` 4장·`feature-navigation.md` 2장 스켈레톤이 `MinoScaffold` 기준으로 바뀐다.
- 새 feature는 화면 수와 무관하게 `MinoScaffold` 하나를 여는 `XShell` + `XNavHost` 두 컴포저블로 시작한다. 화면이 하나여도 인자 복원(`toRoute`)·화면 조회 로깅이 `NavHost`에 딸려 있어 `XNavHost` 유지가 기본이고, VM·인자·로깅이 모두 없는 정적 화면만 셸이 화면을 직접 그린다.
- **`XNavHost` → `XShell` 이름 변경이 규약 전반에 걸린다.** `feature-module.md`(패키지 구조·객체 역할·진입점 스켈레톤·체크리스트)·`feature-navigation.md`·`modularization.md`·`core:navigation`/`core:analytics`/`core:common:ui` README가 함께 갱신됐다. 2026-07-29 ADR은 불변 기록이라 `XNavHost` 표기를 그대로 두고, 인용하는 쪽에서 "당시 셸 이름"임을 밝힌다.
- `TrackScreenViews` 호출 지점이 "NavHost 진입점"에서 "`navController`를 소유한 셸"로 바뀐다 — 위치는 실질적으로 같지만 근거가 `navController` 소유로 명확해진다.
- 파일이 feature당 하나 늘고 `navController`를 인자로 관통시켜야 한다. 화면이 1~4개인 현재 규모에서는 순비용이며, 셸에 chrome이 붙는 feature(탭 셸)에서 이득이 드러난다.
- Snackbar를 실제로 띄우는 첫 화면이 나올 때 문구 매핑 정책(§8 이연)이 함께 정해져야 한다. 지금은 `DomainErrorEmitter` 사용처가 없어 Route 측 수집 코드는 문서 스켈레톤으로만 남긴다.

## 고려한 대안

- **`NavHost` 슬롯과 `content` 슬롯 분리(초기 제안)** — 기획 성격에 따라 슬롯을 골라 쓰는 형태. 의도는 명확하지만 두 슬롯이 셸 관점에서 같은 일을 하고, 배타성을 타입으로 못 막으며 `navController` 소유가 흐려진다. `content` 하나로 두 용례가 모두 표현되므로 기각.
- **스낵바 호스트를 명시적 파라미터로 전달** — 의존이 시그니처에 드러나고 프리뷰가 명확하다는 장점이 있으나, `MinoNavHost`의 `screen<T>` 등록부를 전부 관통해야 해 화면마다 파라미터가 하나씩 늘어난다. 셸 환경 성격의 값이라 CompositionLocal이 더 맞다고 보고 기각.
- **`:core:design-system`에 배치** — `Scaffold` 래핑이라 M3 컴포넌트 패턴([2026-07-25 ADR](2026-07-25-design-system-component-m3-pattern.md))의 자리처럼 보이지만, 미처리 예외 수집을 넣으려면 DS가 에러 인프라를 의존해야 한다. DS를 토큰·표현 전용으로 유지하기 위해 기각.
- **컴포저블을 나누지 않고 `XNavHost`를 `XShell`로 이름만 변경** — 파일이 늘지 않고 `navController`를 관통시킬 필요도 없다. 다만 한 컴포저블이 chrome·`navController`·로깅·그래프 네 가지를 계속 갖고, `screen<T>` 등록이 늘어나면 chrome 코드가 그래프 목록에 파묻힌다. 이름 하나로는 네 역할을 정확히 못 담는다고 보고 기각.
- **셸을 만들지 않고 인셋만 `Modifier` 확장/CompositionLocal로 표준화** — pass-through 논쟁이 사라지지만 `bottomBar` 같은 chrome을 둘 자리가 없어 탭 셸이 다시 예외가 된다. 2026-07-29 ADR이 없앤 예외를 되살리는 셈이라 기각.
- **지금은 만들지 않고 두 번째 chrome 사용처가 생길 때 추출** — 2026-07-29 ADR이 쓴 "실사용으로 뒷받침되지 않는 유연성은 비용" 잣대를 그대로 적용하면 타당한 선택지였다. 셸의 값이 중복 제거가 아니라 **지켜지지 않던 에러 규약의 구조적 강제**에 있다고 판단해 지금 도입한다 — 그 표준을 넣지 않을 거였다면 이 대안이 맞았다.
