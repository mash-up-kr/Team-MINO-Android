# Scaffold·insets는 화면이 아니라 네비게이션 셸(`XNavHost`)이 소유한다

- **상태**: Accepted
- **작성일**: 2026-07-29
- **작성자**: Jaesung Lee

## 컨텍스트

`:feature:main`에 BottomNavigation 기반 탭 전환을 도입하면서, `bottomBar`를 어디에 둘지가 문제가 됐다. 탭 바는 그래프 안의 여러 화면에 **공통으로** 걸려야 하는데, 기존 컨벤션(`docs/architecture/feature-module.md` 4장)은 "`Scaffold`·insets를 화면별로 소유해 topBar/bottomBar를 독립 제어한다"였다. 규칙대로면 탭 바를 둘 자리가 없다.

선택지는 탭 셸만 예외로 두거나, 규칙 자체를 바꾸는 것이었다. 판단 근거로 현재 사용 현황을 조사한 결과, `Scaffold` 사용처 4곳(`HomeScreen`·`SampleScreen`·`SampleDetailScreen`·`SampleMapScreen`)이 **모두 slot을 하나도 쓰지 않고** `Scaffold(modifier) { innerPadding -> … }` 형태로 인셋 패딩만 얻는 용도였다. 기존 규칙이 내세운 "화면별 독립 제어"는 한 번도 행사된 적이 없었다.

## 결정

`Scaffold`는 **그래프당 하나**, 셸(`XNavHost`)이 소유한다. 화면(`XScreen`)은 `Scaffold`를 열지 않고 셸이 계산한 영역 안을 그린다.

- 여러 화면에 걸치는 chrome(`bottomBar`, Snackbar host)은 셸의 slot에 둔다. 탭 전환 화면도 `bottomBar`를 채운 `XNavHost`일 뿐, 별도 구조나 예외가 아니다.
- 화면 고유 chrome(topBar 등)은 그 화면이 자기 컨테이너 최상단에 직접 배치한다.
- 예외로 화면이 `Scaffold`를 열 수 있는 경우는 두 가지다 — `TopAppBar` 스크롤 연동처럼 slot API가 반드시 필요할 때, 인셋을 무시하고 full-bleed로 그려야 할 때. 이때 안쪽 `Scaffold`는 `contentWindowInsets = WindowInsets(0)`으로 둬 인셋 이중 적용을 막는다.

## 근거

- 기존 규칙이 내세운 근거(화면별 topBar/bottomBar 독립 제어)가 실사용으로 뒷받침되지 않았다. 유지하면 쓰지 않는 유연성의 비용만 남는다.
- 탭 셸을 예외로 처리하면 규칙과 실제 구조가 어긋난 채로 굳는다. 규칙을 바꾸면 탭 셸이 규칙의 일반형이 되어 예외 자체가 사라진다.
- `Scaffold` 중첩은 안쪽 인셋을 매번 0으로 눌러야 안전한데, 이를 상시 규칙으로 삼으면 지키지 못했을 때 하단 여백이 두 번 적용되는 형태로 조용히 깨진다. 소유자를 하나로 못박으면 이 실수 자체가 성립하지 않는다.
- `XScreen`이 인셋·chrome 지식을 갖지 않게 되어, state와 콜백만으로 렌더되는 순수 컴포저블이라는 기존 Route↔Screen 분리 원칙과 오히려 더 잘 맞는다.

## 결과

- `docs/architecture/feature-module.md` 4장이 이 규칙의 단일 출처가 되고, `feature-navigation.md`의 `XNavHost` 스켈레톤도 `Scaffold`를 포함한 형태로 바뀐다.
- 기존 화면 4곳에서 `Scaffold`를 걷어내고 각 `XNavHost`(`HomeNavHost`·`SampleNavHost`)로 옮기는 마이그레이션이 필요하다.
- Snackbar host를 도입할 때 위치를 다시 논의할 필요가 없다 — 셸이 소유한다.
- 새 feature를 만들 때 화면 스켈레톤에서 `Scaffold`가 빠지므로, 화면은 컨테이너(`Column`·`Box`)로 시작한다.

## 고려한 대안

- **탭 셸만 예외로 명시** — 문서에 "`:feature:main`은 셸이 Scaffold를 갖는다"를 덧붙이는 방식. 변경 범위가 가장 작지만, 근거 없이 특정 모듈에만 붙는 예외라 이후 다른 feature가 탭·공통 chrome을 도입할 때마다 같은 논의가 반복된다. 기각.
- **`Scaffold` 중첩 허용 (셸=bottomBar, 화면=topBar)** — chrome의 적용 범위에 따라 소유자를 나누는 절충안. 기존 코드를 건드리지 않아도 되지만, 중첩이 상시화되고 안쪽 `contentWindowInsets = WindowInsets(0)`을 매 화면에서 지켜야 한다. 현재 어떤 화면도 slot을 쓰지 않는 상황에서 그 복잡도를 미리 떠안을 이유가 없어 기각하고, slot이 실제로 필요한 화면에 한정된 예외로만 남겼다.
