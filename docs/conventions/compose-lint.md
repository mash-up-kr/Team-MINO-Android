# Compose Lint 컨벤션

Jetpack Compose 코드 품질을 [Slack Compose Lint](https://slackhq.github.io/compose-lints/)와 AGP/AndroidX 내장 Compose 룰로 자동 검사한다.

## 적용 범위

- **SSOT**: 룰 목록·severity는 루트의 [`/lint.xml`](../../lint.xml)을 단일 출처로 한다. 본 문서에서 룰 항목을 다시 풀어쓰지 않는다.
- **적용 모듈**: Compose 컨벤션 플러그인(`team.mino.android.compose`)이 적용된 모듈에 자동 전파된다. 모듈 build script에 별도 설정 불필요.
- **연결 위치**: `build-logic/.../AndroidCompose.kt`의 `configureAndroidCompose(...)`에서 `lint.lintConfig = rootProject.file("lint.xml")` 설정.
- **의존성 주입**: 같은 함수의 `addComposeDependencies()`에서 `lintChecks(libs.slack-compose-lints)` 추가. 카탈로그 alias는 `slack-compose-lints`.

## severity 정책

- 기본은 **error** — 빌드 실패로 즉시 가시화.
- **warning**은 점진 개선이 필요하거나 false positive 가능성이 있는 룰에만 한정 (예: `ComposeM2Api`, `OpaqueUnitKey`, `FrequentlyChangedStateReadInComposition` 등 — 자세한 분류는 lint.xml 참조).

## ComposeCompositionLocalUsage 운영

- 룰 자체는 `error`로 활성화돼 있고, `allowed-composition-locals`에 예외를 **개별 등재**해야 통과한다.
- 등재 기준은 **화면의 도메인 입력이 아니라 셸·테마 환경**일 것 — 트리 어디서 읽든 같은 값이고, 파라미터로 관통시키면 중간 컴포저블 전부에 노이즈가 되는 값. 디자인 토큰(`LocalColorScheme`·`LocalTypography`·`LocalShadows`)과 셸이 소유한 `LocalSnackbarHostState`가 여기 해당한다.
- 그 외(화면 상태·ViewModel·콜백)는 등재하지 않는다 — 파라미터로 넘긴다.

```xml
<issue id="ComposeCompositionLocalUsage" severity="error">
    <option name="allowed-composition-locals"
        value="LocalSpacing, LocalTypography, ..." />
</issue>
```

## 검증

- 전체: `./gradlew lint` 또는 `./gradlew lintDebug`
- 단일 모듈: `./gradlew :모듈:lintDebug`
- 리포트: 각 모듈 `build/reports/lint-results-debug.html`

## 위반 발견 시 처리 순서

1. **코드 수정으로 해소** — 가장 바람직. 룰 의도에 맞춰 리팩토링.
2. **lint.xml에서 severity 조정** — 팀 합의 후 `error` → `warning`으로 완화하거나 룰을 비활성화(`severity="ignore"`). 변경 사유는 PR 본문에 남긴다.
3. **baseline 동결 (최후 수단)** — 위반이 누적되어 단기 해소가 어려울 때만. 모듈 단위 `lint-baseline.xml`을 생성하고 점진 제거 계획을 함께 둔다.

## 룰 추가·조정

새 룰 도입이나 severity 변경은 **lint.xml만 수정**하면 된다. 컨벤션 플러그인이 모든 Compose 모듈로 전파한다.

## 룰 설명

각 룰의 한 줄 의미. severity는 [`/lint.xml`](../../lint.xml) 참조.

### Slack Compose Lint
- `ComposeViewModelForwarding` — ViewModel을 하위 Composable로 전달 금지
- `ComposeViewModelInjection` — Composable 본문에서 `hiltViewModel()` 직접 호출 금지
- `ComposeRememberMissing` — `mutableStateOf(...)` 등 stateful 객체는 `remember`로 감쌀 것
- `ComposeUnstableCollections` — 파라미터에 `List/Set/Map` 대신 `ImmutableList` 권장
- `ComposeMutableParameters` — Composable 파라미터에 `var`/`MutableList` 등 mutable 타입 금지
- `ComposeUnstableReceiver` — 안정성 미보장 수신자에서의 Composable 호출 경고
- `ComposeContentEmitterReturningValues` — UI emit Composable은 값을 반환하지 말 것 (Unit만)
- `ComposeMultipleContentEmitters` — 한 Composable이 여러 UI emit 금지 — 부모에 Layout 위임
- `SlotReused` — slot 람다(`content`)를 본문에서 두 번 호출 금지
- `ComposePreviewNaming` — `@Preview` 함수명은 단수, multi-preview는 복수형
- `ComposePreviewPublic` — `@Preview` 함수는 `private`/`internal`
- `ComposeNamingUppercase` — `Unit` 반환 Composable은 `PascalCase`
- `ComposeNamingLowercase` — 값 반환 Composable은 `camelCase`
- `ComposeParameterOrder` — 파라미터 순서: 필수 → `modifier` → 기본값 있는 옵션 → trailing lambda
- `ComposeCompositionLocalUsage` — 임의 `CompositionLocal` 사용 금지, allowlist 기반
- `ComposeModifierMissing` — public Composable은 `Modifier` 파라미터 노출 (`visibility-threshold=all`)
- `ComposeModifierWithoutDefault` — `Modifier` 파라미터 기본값 `= Modifier` 필수
- `ComposeModifierReused` — 같은 `Modifier` 인스턴스를 형제 Composable 둘에 주입 금지
- `ComposeComposableModifier` — Modifier를 만드는 함수에 `@Composable` 붙이지 말 것
- `ComposeModifierComposed` — `Modifier.composed { }` 금지 — deprecated, `Modifier.Node` 사용
- `ComposeM2Api` — Material2 API 사용 경고 (Material3 환경)

### Compose Naming
- `ComposableLambdaParameterNaming` — trailing 람다는 `content`, 그 외 슬롯은 의미명

### Compose Modifier
- `ModifierFactoryReturnType` — Modifier 팩토리 반환 타입은 `Modifier`
- `ModifierFactoryExtensionFunction` — Modifier 팩토리는 `Modifier`의 확장 함수로 정의

### Color
- `InvalidColorHexValue` — `#RGB`/`#ARGB`/`#RRGGBB`/`#AARRGGBB` 외 hex 형식 거부
- `MissingColorAlphaChannel` — hex 색상에 alpha 채널 누락 경고

### Compose State and Composition
- `AutoboxingStateCreation` — `mutableStateOf(0)` 대신 `mutableIntStateOf(0)` (autoboxing 방지)
- `AutoboxingStateValueProperty` — `IntState.value` 대신 `.intValue`
- `MutableCollectionMutableState` — `mutableStateOf(mutableListOf())` 금지
- `FrequentlyChangedStateReadInComposition` — composition에서 자주 바뀌는 state 읽기 경고
- `FrequentlyChangingValue` — composition에서 자주 바뀌는 값(scroll/animation 진행값) 읽기 경고

### Compose Architecture
- `OpaqueUnitKey` — `LaunchedEffect(Unit)` 같은 의미 불명 `Unit` 키 경고
- `ReturnFromAwaitPointerEventScope` — `awaitPointerEventScope { }`에서 외부 `return` 시 unconsumed event 위험
- `MultipleAwaitPointerEventScopes` — 한 Composable에 여러 `awaitPointerEventScope` 호출 — 한 곳에 모아라
- `UseOfNonLambdaOffsetOverload` — `Modifier.offset(x, y)` 대신 람다 오버로드 권장
- `LocalContextResourcesRead` — `LocalContext.current.resources` 직접 읽기 금지 — configuration 미반영
- `ConfigurationScreenWidthHeight` — `configuration.screenWidthDp/HeightDp` 대신 `BoxWithConstraints`/`WindowSizeClass`

## 참고

- Slack Compose Lint 룰 인덱스: https://slackhq.github.io/compose-lints/rules/
- AndroidX Compose Lint(JetBrains/Google 제공): Android Studio 인스펙션 문서 참조
