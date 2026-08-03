# Category 항목은 `MinoChip`으로 흡수하지 않고 전용으로 그리되, 치수 토큰만 공유한다

- **상태**: Accepted
- **작성일**: 2026-08-03
- **작성자**: Jaesung Lee

## 컨텍스트

`MinoCategory`는 가로 스크롤 탭이고 항목이 칩 모양이다. 구현 초기부터 파일 내부에 `private CategoryChip`을 두고 `MinoChip`과 무관하게 패딩·셰이프·타이포를 하드코딩해 왔다.

이슈 [#120](https://github.com/mash-up-kr/Team-MINO-Android/issues/120) 갭 조사에서 이 중복이 문제로 잡혔다. Category에 크기(4종)·변형(2종)을 도입하려면 항목 칩의 치수 체계가 필요한데, 그 값을 Category가 또 한 벌 갖는 것이 맞는지 먼저 정해야 했다(갭 문서의 "구현 전 결정이 필요한 항목" 3번).

Figma 실측 결과는 다음과 같다.

- **치수는 완전히 같다.** Category 리소스 칩과 독립 Chip의 크기가 네 단계 모두 일치한다(48×24 · 57×32 · 65×36 · 67×40). 폭까지 같다는 것은 패딩과 폰트가 모두 같다는 뜻이다. 내부 구조(콘텐츠 간격, 글자 좌우 Wrapper 여백)도 동일하다.
- **이름만 한 단계 밀려 있다.** Figma가 Category 리소스 칩을 `XSmall/Small/Normal/Large`로, 독립 Chip을 `XSmall/Small/Medium/Large`로 따로 부른다. Category 자신의 `Size` 축은 또 `Small/Medium/Large/XLarge`다.
- **색 체계는 다르다.** Category `Alternative`는 Chip `Outlined`와 값까지 완전히 같지만, Category `Normal`의 **비선택** 항목은 `Background/Normal/Normal`(불투명 배경) + `Line/Normal/Neutral` 테두리인 반면, Chip `Solid`의 비선택은 `Fill/Alternative`(5% 틴트)에 테두리가 없다.
- Figma도 `Category/Resource/Chip/*`을 `Chip/Chip`과 **별도 컴포넌트셋**으로 두고 있다.

## 결정

`MinoCategory`의 항목은 `MinoChip`을 호출하지 않고 전용 항목 컴포저블로 그린다. 대신 **치수 토큰은 공유한다** — `CategorySize` → `ChipSize` 매핑을 `CategoryTokens`에 두고, 패딩·셰이프·폰트·글자 여백을 `component.chip.token`의 `internal` 확장에서 그대로 가져온다. Category가 자체적으로 갖는 값은 색과, Category 레벨의 배치 값(항목 간격·좌우 여백·상하 여백·페이드 폭·트레일링 슬롯 크기)뿐이다.

## 근거

- `MinoChip`을 재사용하려면 Chip의 색 해석 로직을 열어야 한다. `MinoChipDefaults.containerColor`/`borderColor`는 `Solid`의 테두리를 `null`로, `Outlined`의 비선택 배경을 `Color.Transparent`로 **하드코딩**한다. Category `Normal`의 비선택(배경 + 테두리 동시)은 두 variant 어느 쪽으로도 표현할 수 없다. `colors` 파라미터를 넘겨도 막힌다.
- 그렇다고 Chip에 상태별 배경·테두리 슬롯을 모두 뚫으면, Figma Chip 컴포넌트셋에 없는 조합이 공개 API에 생긴다. 디자인에 없는 조합을 열지 않는다는 기존 방침과 어긋난다.
- 반대로 치수까지 중복 정의하면 Figma가 칩 크기를 바꿀 때 두 곳을 따로 고쳐야 하고, 실제로 조사 전까지 Category가 셰이프 6dp·패딩 12/9라는 서로 다른 크기의 조합을 들고 있었다. 치수 공유는 이 유형의 드리프트를 구조적으로 막는다.
- 치수만 공유하고 색은 분리하는 경계가 Figma의 컴포넌트 구성과도 일치한다(같은 기하, 별도 컴포넌트셋).

## 결과

- `CategoryTokens`에 `CategorySize.chipSize` 매핑이 생기고, 이름이 한 단계 밀린다는 사실이 이 매핑 한 곳에만 기록된다. 다른 곳에서 Category 크기와 Chip 크기를 직접 대응시키지 않는다.
- Chip의 치수 토큰(`contentPadding`·`shape`·`font`·`textHorizontalPadding`)은 `component.chip.token`의 `internal` API로 남고, 모듈 내 다른 컴포넌트가 칩 기하를 필요로 할 때 같은 방식으로 재사용한다. 모듈 밖으로는 노출하지 않는다.
- Chip 쪽 치수를 고칠 때 Category도 함께 바뀐다. Figma에서 두 컴포넌트의 치수가 갈라지는 날이 오면 이 ADR을 다시 판단해야 한다.
- 색은 두 컴포넌트가 독립적으로 관리한다. Category `Alternative`가 현재 Chip `Outlined`와 값이 같지만, 우연한 일치로 보고 합치지 않는다.

## 고려한 대안

- **`MinoChip`을 그대로 항목으로 쓴다** — 가장 중복이 적지만, 위 근거대로 Category `Normal` 비선택 색을 표현할 수 없다. Chip의 색 해석을 상태별 슬롯으로 일반화해야 하고, 그 과정에서 Figma에 없는 조합이 공개 API에 열린다.
- **Chip에 Category용 variant를 추가한다** — 예: `ChipVariant.SolidOutlined`. Figma Chip 컴포넌트셋에 없는 변형을 코드가 발명하게 된다. 디자인 시스템의 SSOT가 Figma라는 전제를 깬다.
- **지금처럼 완전히 분리해 둔다(치수도 각자)** — 변경 폭이 가장 작지만, 조사에서 드러난 드리프트(Category가 Chip과 무관한 패딩·셰이프 조합을 들고 있던 상태)를 그대로 남긴다. 크기 4종을 새로 도입하는 시점에 값 4벌이 통째로 복제되므로 가장 나쁜 시점이다.
