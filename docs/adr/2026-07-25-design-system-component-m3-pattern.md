# 디자인시스템 컴포넌트 API는 Material3 컴포넌트 패턴(Defaults·Colors·컴포넌트 토큰)을 따른다

- **상태**: Accepted
- **작성일**: 2026-07-25
- **작성자**: Jaesung Lee

## 컨텍스트

`core:design-system`의 `component` 패키지에 첫 컴포넌트(Menu, 이슈 #79)를 추가하게 되면서, 컴포넌트 공개 API를 어떤 구조로 설계할지 기준이 필요해졌다. 첫 컴포넌트의 구조가 이후 모든 컴포넌트(버튼·칩 등)의 기준 패턴이 되므로, 컴포넌트마다 임의로 시그니처를 정하기 전에 관례를 확정해야 하는 시점이었다.

foundation 층에는 이미 3계층 토큰 시스템(Atomic → Semantic → AccessKey/Holder)이 있지만, 그 위에서 컴포넌트가 토큰을 소비하고 커스터마이징을 노출하는 방식에 대한 관례는 없었다.

## 결정

컴포넌트 공개 API는 **Material3 컴포넌트 구조**를 따른다. 레퍼런스:

- [Button.kt](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Button.kt) — 상태(enabled) 있는 컴포넌트의 Defaults·Colors·토큰 참조 구조
- [Badge.kt](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Badge.kt) — 상태 없는 컴포넌트의 `Defaults` 단일 값 패턴(`BadgeDefaults.containerColor`)
- [IconButtonDefaults.kt](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/IconButtonDefaults.kt) — Defaults를 별도 파일로 분리한 사례, `ColorScheme` 캐시 필드(`default*ColorsCached`) 다수 운용
- [tokens/](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/) — 컴포넌트별 `internal` 토큰 object를 전용 패키지로 모은 구조

구조 규칙:

- **Defaults object** — 컴포저블의 커스터마이징 파라미터(셰이프·색·패딩 등)는 디폴트를 전부 `Mino<Name>Defaults`에서 공급한다. 파일이 커지면 M3 `IconButtonDefaults.kt`처럼 Defaults를 별도 파일로 분리한다.
- **Colors 클래스** — 상태(enabled 등)를 가지는 컴포넌트의 색은 `@Immutable`한 `Mino<Name>Colors`로 묶는다. `Color.Unspecified`를 "원본 유지"로 해석하는 `copy`, `@Stable internal` 상태 해석 함수(`containerColor(enabled)` 등), equals/hashCode를 갖춘다. 기본 인스턴스는 M3처럼 `ColorScheme`의 `internal var default<Name>ColorsCached` 필드에 캐시해 테마당 1회만 생성한다. 상태 없는 컴포넌트는 M3 `BadgeDefaults`처럼 Colors 클래스 없이 Defaults의 단일 값 프로퍼티로 둔다.
- **컴포넌트 토큰 층** — 색·타이포·수치의 출처는 `internal object <Name>Tokens`(M3 `tokens/` 패키지의 `FilledButtonTokens`·`BadgeTokens` 대응)로 분리한다. 컴포넌트 슬롯 → 디자인 토큰 키(`*AccessKeyToken`) 매핑만 담고, 실제 값 해석은 기존 `*AccessKeyToken.value`가 담당한다(M3의 `<Key>.value`·`fromToken` 대응).

M3와 **의도적으로 다르게** 가는 지점 두 가지:

1. **클릭 처리** — M3는 내부적으로 `Surface(onClick)`을 쓰지만, 우리는 DS 클릭 규칙에 따라 `rippleSingleClickable` 등 클릭 Modifier 유틸로 처리한다. API 모양은 M3를 따르되 내부 구현은 DS 규칙이 우선한다.
2. **토큰 해석** — M3의 `ColorSchemeKeyTokens`/`fromToken` 자리에는 동일 개념의 기존 구현인 `*AccessKeyToken`/`.value`를 그대로 쓴다. 토큰 시스템을 이중으로 만들지 않는다.

또한 아직 동작하지 않는 기능의 파라미터는 미리 만들지 않고, 이후 기능 추가 시 **디폴트 파라미터로 소스 호환 확장**한다.

## 근거

- **학습 비용 최소**: M3는 안드로이드 개발자에게 가장 익숙한 Compose 컴포넌트 관례다. `XDefaults`·`XColors` 구조는 별도 설명 없이도 사용법(부분 오버라이드, 기본값 탐색)이 예측된다.
- **기존 토큰 시스템과 자연 결합**: 우리 `*AccessKeyToken`(키 enum + `value` 해석)은 M3의 `ColorSchemeKeyTokens` + `fromToken`과 동일한 개념이라, M3 패턴을 얹을 때 새 메커니즘이 필요 없다.
- **커스터마이징 관례의 일관성**: 색 오버라이드는 `colors = ...Defaults.itemColors(x = ...)` 한 가지 방식으로 통일되고, `Color.Unspecified` 기반 `copy` 덕에 부분 오버라이드가 안전하다.
- **재구성 성능**: `@Immutable` Colors + `ColorScheme` 캐시는 M3가 검증한 재구성 스킵·할당 최소화 구조다.

## 결과

- Menu(이슈 #79)부터 적용하며, `component` 패키지의 모든 컴포넌트가 이 구조를 따른다.
- 컴포넌트 파일 구성: `component/<name>/` 아래 컴포저블 파일, `Mino<Name>Defaults.kt`(Defaults + Colors), `token/<Name>Tokens.kt`, 프리뷰 파일.
- foundation의 `ColorScheme`에 컴포넌트별 캐시 필드가 추가된다. foundation이 component 타입을 참조하게 되지만, M3도 같은 모듈 안에서 동일하게 하는 구조라 허용한다.
- 구현 절차 요약은 `core/design-system/README.md` 5.1 컴포넌트 구현 패턴에 있다.

## 고려한 대안

- **토큰 직접 소비 방식(초기 제안)**: 컴포넌트가 내부에서 `*AccessKeyToken.value`를 바로 읽고, variant는 개별 enum 파라미터로 노출하는 단순 구조. 구현은 가장 짧지만 색 오버라이드 경로가 없어 프리뷰·특수 화면 대응이 막히고, 컴포넌트마다 파라미터 설계가 제각각이 될 여지가 있어 M3 관례 채택으로 대체했다.
