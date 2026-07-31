# feature 간 전환 계약은 `:core:navigation`에 두고, 탭 feature는 등록 함수로 셸 그래프에 편입한다

- **상태**: Accepted
- **작성일**: 2026-08-01
- **작성자**: Jaesung Lee

## 컨텍스트

feature를 단일 모듈로 두기로 한([ADR](2026-07-30-single-feature-module.md)) 뒤, 화면 전환 구조에서 두 가지가 미정으로 남아 이슈 #104에서 결정했다.

**첫째, feature 간 전환 계약을 어디에 둘 것인가.** 다른 feature의 Activity를 열려면 호출자가 무언가를 알아야 한다. 그 대상이 상대 feature 모듈이면 A가 B를 열고 B가 A를 여는 흔한 상황에서 Gradle 모듈 순환이 되어 빌드가 거부된다. 계약을 두는 자리가 곧 feature 간 결합의 모양을 정한다.

**둘째, 탭 feature가 셸 그래프에 어떻게 편입되는가.** 탭 화면은 Activity로 진입하지 않고 셸(`:feature:main`)의 그래프에 중첩된다. 등록 함수의 시그니처, Route를 셸과 탭 모듈 중 누가 소유하는지, 화면 묶음을 중첩 그래프로 감쌀지가 모두 열려 있었다.

## 결정

**전환 계약을 `:core:navigation/activity/launcher/`로 모으고, 구현은 각 feature 모듈에 둔다.**

- `XLauncher` 인터페이스와 Intent extra 키(`ExtraTag.kt`)가 이 패키지에 모인다. 키 이름은 `EXTRA_<대상 feature>_<이름>` — 최상위 상수라 스코프가 없으므로 접두어로 소유를 드러낸다.
- `XLauncherImpl`과 Hilt 바인딩은 대상 feature 모듈에 그대로 둔다. 호출자는 계약만 주입받으므로 feature 모듈끼리 컴파일 타임에 서로를 모른다.
- `ActivityLauncher.launch`의 첫 인자를 `Context`에서 `Activity`로 좁히고 `withFinish: Boolean`을 더한다.

**탭 feature는 `XNavigation.kt` 하나를 공개 표면으로 삼아 셸 그래프에 편입한다.**

- 그 파일이 진입 Route(`XGraph`)와 등록 함수(`NavGraphBuilder.xGraph(...)`)를 노출하고, 나머지는 `internal`로 둔다. 셸(`:feature:main`)이 등록 함수를 호출해 그래프를 조립한다.
- 모듈 안에서 끝나는 전환은 그 모듈이 `navController`로 처리하고, feature 밖으로 나가는 전환만 콜백으로 받는다.
- 화면이 하나뿐이어도 중첩 그래프로 감싼다. 이를 위해 `screen<T>`와 짝이 되는 `graph<T : Route>` 래퍼를 `:core:navigation`에 둔다.

## 근거

- **순환 참조를 규칙이 아니라 구조로 막는다.** 계약이 한 모듈에 모여 있으면 feature 간 의존 자체가 생기지 않으므로, "순환 참조 금지"를 문서로 선언하고 지켜지길 기대할 필요가 없다. 모듈 경계를 검사하는 장치가 리포에 없는 상태에서, 이 배치는 적어도 순환에 대해서는 컴파일 타임 보장을 준다.
- **계약과 구현의 분리를 유지하면서 모듈은 늘리지 않는다.** 호출자는 인터페이스만 알고 Hilt가 구현을 주입하는 형태가 그대로 성립하고, feature마다 모듈을 더 만들지 않아도 된다.
- **`Activity`로 좁히는 편이 안전하다.** 비-Activity `Context`로 `startActivity`를 호출하면 `FLAG_ACTIVITY_NEW_TASK` 없이 런타임에 실패한다. 기존 호출부는 전부 Activity에서 시작하고 있었으므로 좁혀도 잃는 것이 없고, `withFinish`가 요구하는 호출자 종료도 타입으로 성립한다.
- **화면이 하나여도 그래프로 감싸면 셸이 안정적이다.** 탭 진입 Route가 그래프 Route로 고정되므로 탭 안에 화면이 늘어도 셸의 탭 목록과 선택 판별(`hierarchy` 탐색)이 바뀌지 않는다. 평면 등록은 하위 화면으로 들어가는 순간 탭 선택 표시가 풀린다.
- **`graph<T>`를 감싸는 이유는 `screen<T>`와 같다.** androidx의 `navigation<T>`를 그대로 쓰면 그래프 진입점만 `Route` 계약에서 벗어난다.

## 결과

- `:core:navigation`이 feature 이름을 알게 된다. 컴파일 의존은 없지만 새 진입형 feature마다 이 모듈에 계약 파일이 하나씩 늘고, 그만큼 공용 모듈에 변경이 몰린다.
- [`core/navigation/README.md`](../../core/navigation/README.md)의 배치 규칙("특정 feature 고유의 것은 이 모듈이 아니라 그 feature에 둔다")을 다시 쓴다. 계약은 이 모듈, 구현은 feature로 갈린다.
- 셸이 탭 feature 모듈을 직접 의존한다. feature 간 의존을 금지하는 규칙의 유일한 예외다.
- `:app`은 진입형 feature만 등록한다. 탭 feature는 셸을 통해 런타임 클래스패스에 들어오므로 직접 의존이 필요 없다.
- `:feature:home`을 첫 탭 feature로 전환해 등록 경로를 컴파일로 검증했다. 진입형 검증은 `:feature:sample`이 담당한다.
- 모듈 의존 방향을 강제하는 검증 장치는 여전히 없다. 이 결정이 순환 참조에 한해 구조적 보장을 주지만, 레이어 경계 전반은 문서 규칙에 머문다.

## 고려한 대안

- **계약을 각 feature 모듈에 두고 호출자가 상대 모듈을 직접 의존** — 옮길 것이 없어 가장 단순하지만, 서로를 여는 두 feature가 생기는 순간 Gradle 모듈 순환으로 빌드가 막힌다. 폐기.
- **`XLauncher` 인터페이스를 없애고 구현 클래스만 Hilt로 주입** — 파일이 하나 줄지만 호출자가 대상 feature 모듈을 의존해야 하는 건 같아서 순환 문제가 그대로 남는다. 폐기.
- **Launcher 계층을 걷어내고 대상 Activity가 Intent 팩토리를 직접 노출** — 인자가 타입 세이프해지고 `EXTRA_*` 상수가 사라지는 대신, 호출자가 대상 Activity 클래스를 알아야 해 역시 모듈 간 직접 의존이 된다. 폐기.
- **`:app`이 탭 그래프를 조립** — 모듈 경계 규칙을 건드리지 않는 대신 탭 셸의 UI 조립 책임이 `:app`으로 새어나가 `:feature:main`의 존재 의미가 옅어진다. 폐기.
- **`:core:navigation`에 등록 계약을 두고 각 탭 모듈이 Hilt multibinding으로 제공** — 셸이 탭 화면을 컴파일 타임에 모르게 되지만, 아이콘·라벨 같은 표현 정보를 네비게이션 계약에 넣어야 하고 탭 순서·등록 누락 검증이 런타임으로 밀린다. 리포에 선례가 없어 도입 비용도 크다. 폐기.
- **탭 화면을 셸 그래프에 평면 등록** — 중첩 그래프 없이 `screen<T>`만 나열하면 등록은 단순해지지만, 탭 하위 화면으로 들어가는 순간 상위 탭이 선택 상태를 잃는다. 폐기.
