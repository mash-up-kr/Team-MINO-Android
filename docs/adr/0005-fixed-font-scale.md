# 0005. 시스템 폰트 배율을 무시하고 텍스트 크기를 고정한다 (1sp = 1dp)

- **상태**: Accepted
- **작성일**: 2026-07-24
- **작성자**: Jaesung Lee

## 컨텍스트

디자인 시스템 타이포그래피 토큰(`AtomicTypographyToken`)의 폰트 크기·행간은 `.sp`로 정의되어 있어, 사용자가 시스템 설정에서 폰트 크기를 키우면 앱 텍스트도 함께 확대된다. 배율 확대 시 Figma 디자인 기준으로 잡은 레이아웃이 깨질 수 있어, 시스템 폰트 배율과 무관하게 텍스트 크기를 디자인 값 그대로 고정하기로 했다.

한편 Compose의 `TextStyle.fontSize`는 `TextUnit` 타입이라 dp를 직접 넣을 수 없으므로, "dp로 고정"은 sp 값이 배율의 영향을 받지 않게 만드는 방식으로 구현해야 한다.

## 결정

테마 진입점 `MinoAndroidAppTheme`에서 `LocalDensity`를 `fontScale = 1f`로 오버라이드해 제공한다. `density`(화면 밀도)는 기존 값을 유지한다. 시스템 입력을 정책으로 해석하는 계층이 `MinoAndroidAppTheme`(다크 모드 해석과 동일 층위)이므로, 토큰을 주입하기만 하는 private `MinoAndroidTheme`가 아니라 진입점에 둔다.

```kotlin
CompositionLocalProvider(
    LocalDensity provides Density(LocalDensity.current.density, fontScale = 1f),
    ...
)
```

토큰과 사용처 코드는 계속 `.sp`로 표기한다. sp→px 환산이 `값 × density × fontScale`이므로, fontScale이 1로 고정되면 테마 하위에서는 항상 1sp = 1dp로 렌더링된다.

## 근거

- **변경 범위 최소**: 테마 한 곳만 수정하면 디자인 시스템 토큰은 물론 feature 코드의 직접 `.sp` 사용까지 일괄 적용되어 누락이 구조적으로 불가능하다.
- **레이아웃 무영향**: dp→px 환산은 `density`만 사용하므로 컴포넌트 크기·패딩·터치 영역 등 dp 기반 레이아웃은 영향을 받지 않는다. Android 14의 비선형 폰트 스케일링도 sp 환산 경로 안에서 함께 무력화된다.
- **접근성 트레이드오프 인지**: 저시력 사용자의 폰트 확대가 앱에 반영되지 않는 단점을 인지하고 내린 의도적 결정이다.

## 결과

- 테마 컴포지션 하위의 모든 텍스트는 시스템 폰트 크기 설정을 무시한다. 폰트 크기는 그대로 `.sp`로 표기한다(dp 표기는 컴파일 불가).
- Compose 밖의 표면(알림, 앱 위젯, XML 뷰 등)과 `LocalConfiguration.current.fontScale` 직접 참조는 이 오버라이드의 영향 밖이므로, 그런 표면이 생기면 별도 처리가 필요하다.
- 검증: `TypographyPreview`의 `@Preview(fontScale = 2f)` 프리뷰가 기본 프리뷰와 동일하게 렌더링되는지로 확인한다.
- 정책 요약은 `core/design-system/README.md` 4.5 토큰 규칙에 있다.

## 고려한 대안

- **토큰 레벨 dp→sp 변환 (`11.dp.toSp()`)**: 환산에 `Density`가 필요해 정적 `val`인 토큰 오브젝트 전체를 `@Composable` 함수나 fontScale 파라미터를 받는 팩토리로 재구성해야 하고, `staticCompositionLocalOf { minoTypography() }` 기본값 구조도 깨진다. 변경 범위가 큰 데 비해 얻는 결과는 동일해 배제했다.
