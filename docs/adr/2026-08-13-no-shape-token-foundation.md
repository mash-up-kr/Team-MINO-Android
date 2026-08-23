# 모서리(셰이프)는 토큰 foundation을 두지 않고 컴포넌트 토큰이 실측값을 직접 든다

- **상태**: Accepted
- **작성일**: 2026-08-13
- **작성자**: Jaesung Lee

> [!NOTE]
> 아래 결정 중 **"값의 출처는 KDoc에 Figma 노드·변수명으로 적는다"는 더 이상 유효하지 않다.** [디자인 토큰은 값이 일치할 때만 강제한다](2026-08-13-design-token-when-value-matches.md)가 값을 복사한 주석을 금지로 뒤집었고(그 주석은 Figma의 사본이라 조용히 낡는다), 현행 규칙은 [`conventions/figma-design-fidelity.md`](../conventions/figma-design-fidelity.md) §2 금지·§5가 소유한다 — 코드에는 값의 출처도 노드 ID도 적지 않으며, 예외는 §2.2 마커 주석(**변수명만**, 값은 적지 않음) 하나뿐이다. foundation을 두지 않고 컴포넌트 토큰이 실측값을 직접 든다는 이 ADR의 본 결정은 그대로다.

## 컨텍스트

`:core:design-system`은 색·타이포·셰이프·그림자 네 foundation을 같은 3계층(`Atomic → Semantic → AccessKey + Holder + CompositionLocal`)으로 세웠다. 셰이프는 `66a9c46 feat: 디자인 시스템 셰이프 토큰 구성`에서 `Small=8 / Medium=12 / Large=16`(dp) 3단 스케일로 만들어졌고, 다른 세 foundation과 달리 **Figma에서 추출한 값이 아니라 placeholder**였다. `README`도 그 사실을 "셰이프(8/12/16dp)는 아직 placeholder"라고 명시하고 있었다.

이후 Figma 컴포넌트를 실제로 옮기면서 실측 모서리가 스케일에 들어맞지 않는 일이 반복됐다. 현재 코드베이스에 존재하는 서로 다른 모서리 값은 **13종**이다 — 1, 4, 5, 6, 8, 10, 12, 14, 16, 18.29, 20, 24, 1000(dp). 스케일이 담던 8/12/16은 이 중 3종에 불과했고, 나머지는 전부 컴포넌트가 값을 직접 들면서 "셰이프 스케일에 없는 값이라 직접 쓴다"는 취지의 변명 주석을 함께 달았다(Button·Snackbar·Tooltip·ContentBadge·RoomCard·Invitation 등).

색·타이포·그림자는 Figma 디자인 시스템(MU_Wanted)이 **이름 붙은 시맨틱 토큰 세트**로 정의해 배포하지만, 모서리는 그런 세트가 없다. 컴포넌트 노드마다 개별 `Radius` 값이 붙어 있을 뿐이다. 즉 셰이프만 "감쌀 원본 토큰"이 애초에 없는 상태에서 홀더 구조부터 세워둔 셈이었다.

## 결정

셰이프 토큰 foundation을 **전부 삭제한다**. `foundation/shape/` 아래 `AtomicShapeToken`·`ShapeTokens`·`ShapeAccessKeyToken`·`Shapes` 홀더·`LocalShapes`·`ShapesPreview`가 모두 제거 대상이며, `MinoAndroidTheme.shapes` 접근자와 루트 `lint.xml`의 `ComposeCompositionLocalUsage` allowlist 항목도 함께 사라진다.

모서리 값은 **각 컴포넌트의 `internal object <Name>Tokens`가 `RoundedCornerShape` 리터럴로 직접 보유**하고, 공개는 기존 M3 패턴대로 `Mino<Name>Defaults`가 담당한다. 값의 출처는 KDoc에 Figma 노드·변수명으로 적는다.

이는 [디자인시스템 컴포넌트 API는 Material3 컴포넌트 패턴을 따른다](2026-07-25-design-system-component-m3-pattern.md)의 "`<Name>Tokens`는 슬롯 → 토큰 키 매핑**만** 담는다" 조항을 모서리·치수에 한해 좁히는 것이다. 색·타이포·그림자에 대해서는 해당 ADR이 그대로 유효하다.

## 근거

- **감쌀 원본이 없다.** 토큰 3계층의 값어치는 "원시값 하나를 바꾸면 그것을 참조한 모든 시맨틱이 따라 바뀐다"에서 나온다. 셰이프의 원시값은 Figma가 정의해 준 것이 아니라 우리가 임의로 고른 8/12/16이었으므로, 바꿔야 할 일도 없고 바꿔도 의미가 없었다. 색·타이포·그림자와 겉모습만 같은 껍데기였다.
- **준수율이 낮은 규칙은 규칙이 아니다.** 13종 중 3종만 커버하는 스케일은 컴포넌트를 옮길 때마다 예외를 만들었고, 예외마다 "왜 토큰을 안 썼는지" 해명 주석이 붙었다. 규칙보다 예외가 많으면 읽는 사람이 어느 쪽을 따라야 할지 판단할 수 없다.
- **모서리는 시맨틱하지 않다.** 색의 `LabelNormal`, 타이포의 `Body1NormalRegular`는 용도를 말하지만, 모서리는 `Small/Medium/Large`라는 상대적 크기 외에 붙일 이름이 없다. 이름이 값보다 정보량이 적으면 간접층은 비용만 남는다.
- **placeholder를 유지하는 비용이 계속 늘고 있었다.** 언제 디자이너가 셰이프 토큰을 정의해 줄지 알 수 없는 상태에서, 그 사이에 추가되는 모든 컴포넌트가 예외 주석을 하나씩 더 쌓았다.

## 결과

- `MinoAndroidAppTheme`이 provide 하는 `CompositionLocal`은 색·타이포·그림자 셋으로 줄고, `lint.xml`의 allowlist도 `LocalColorScheme, LocalTypography, LocalShadows, LocalSnackbarHostState`가 된다.
- 컴포넌트 밖(feature 코드·프리뷰)에서 모서리가 필요하면 `Mino<Name>Defaults`가 노출한 값을 쓰거나, 그 컴포넌트 소유가 아닌 일회성 모서리는 `RoundedCornerShape(n.dp)`를 그 자리에서 만든다. 카탈로그 프리뷰처럼 디자인 근거 없는 값은 그 사실을 주석으로 밝힌다.
- **모서리 값의 전역 일괄 변경 수단이 없어진다.** 이전에는 `ShapeAccessKeyToken`의 망라적 `when`이 슬롯 누락을 컴파일 에러로 잡았고 `AtomicShapeToken` 한 파일만 고치면 됐지만, 이제는 grep이 유일한 수단이다. 같은 12dp가 여러 컴포넌트에 독립 선언되어 있으므로, "전 컴포넌트 모서리를 일괄 조정"하는 요구가 실제로 생기면 이 결정을 다시 검토한다.
- `foundation/shape/` 카탈로그 프리뷰(`ShapesPreview`)는 대체 없이 사라진다. 모서리를 한눈에 보려면 각 컴포넌트의 프리뷰를 본다.
- **다른 치수 foundation(spacing 등)을 새로 만들 때 이 기록을 먼저 읽는다.** 연속적인 치수는 열거형 스케일에 가두기 어렵다는 것이 여기서 얻은 교훈이며, Figma가 이름 붙은 토큰 세트로 배포하지 않는 축이라면 foundation을 세우지 않는 쪽이 기본값이다.

## 고려한 대안

이번 결정은 대안을 나란히 놓고 비교한 끝에 고른 것이 아니라, placeholder 스케일이 실측값을 담지 못한다는 사실이 누적되어 폐기가 자명해진 경우다. 다만 나중에 다시 제안되기 쉬운 두 방향과 그것이 답이 아닌 이유를 남긴다.

- **스케일을 실측값 전부로 넓히기** — 13종을 다 담으려면 `Shape1 / Shape4 / Shape5 / …` 식이 되어 이름이 값을 그대로 되뇌는 dp 나열이 된다. 시맨틱이 없는 열거는 `RoundedCornerShape(n.dp)`보다 나은 점이 없고, 18.29 같은 값까지 스케일에 올리면 이름의 의미는 완전히 사라진다.
- **디자이너가 셰이프 시맨틱 토큰을 정의할 때까지 placeholder 유지** — 그때가 오면 그때 foundation을 다시 세우면 된다. 원본 토큰이 실제로 배포된 뒤 그것을 감싸는 것이 3계층의 원래 순서이고, 없는 원본을 미리 흉내 내는 것이 이번에 실패한 방식이다.
