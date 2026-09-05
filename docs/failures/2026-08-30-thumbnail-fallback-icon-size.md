# 장소 카드의 사진 없음 폴백 아이콘이 썸네일 박스 전체 크기로 커져 깨져 보였다

- **상태**: Resolved
- **발생일자**: 2026-08-30
- **작성자**: Chea-yunzi
- **관련 ADR**: 없음 — 문서화되지 않은 결정(design-system-builder 서브에이전트가 `feature:room`의 기존 코드를 그대로 이관하며 함께 옮겨온 누락)
- **관련 커밋/PR**: `feature:room`의 `PlaceListItem`/`PlaceGridItem`을 `:core:design-system`의 `MinoCardLocationList`/`MinoCardLocationCollage`로 이관한 작업(오늘 세션) — 이관 시 원본에도 이미 있던 누락을 그대로 옮겨왔다. 이번 수정은 같은 브랜치(`feature/154-room-list/room-detail-task`)의 커밋 예정 변경.

## 무엇을 시도했는가

`MinoAsyncImage`(`core/design-system/.../util/image/MinoAsyncImage.kt`)는 `imageUrl`이 없거나 로드에 실패하면 `fallback` Painter를 `Icon`으로 그린다. 이 `Icon`은 기본 `modifier`(호출부가 준 크기 제약, 예: 리스트형 94dp 정사각 박스)를 그대로 받고, 별도로 `fallbackModifier`를 넘기면 그 크기로 아이콘만 작게 오버라이드할 수 있게 설계돼 있다. `MinoCardLocationList`·`MinoCardLocationCollage`(오늘 새로 만든 두 컴포넌트, 원본은 `feature:room`의 `PlaceListItem`·`PlaceGridItem`) 둘 다 `fallbackModifier`를 넘기지 않고 `MinoAsyncImage`를 호출했다.

## 무엇이 잘못됐는가

`fallbackModifier`를 안 넘기면 폴백 `Icon`이 컨테이너 크기 제약(리스트형 94x94dp, 콜라주형 카드 폭 절반 × 4:5 비율)을 그대로 받아 그 **박스 전체를 채우는 크기로 아이콘이 커진다.** 카메라 모양 벡터 아이콘이 원래 24dp 안팎으로 그려질 걸 94dp+로 확대되니 픽셀이 뭉개지고 형태가 깨져 보였다. 실기기 확인 결과 사용자가 이를 "썸네일 영역 자체가 없다"고 오인할 정도로 어색했다 — 실제로는 영역이 있었지만 그 안의 아이콘이 형태를 알아볼 수 없게 커져 있었을 뿐이다.

같은 문제를 `feature:home`의 `PlaceCardImageSlot`(`feature/home/.../component/PlaceCardItem.kt`)은 이미 `fallbackModifier = Modifier.wrapContentSize().size(ImageFallbackGlyphSize)`(24dp)로 피해 뒀었다 — 이관 작업이 그 선례를 확인하지 않고 진행됐다.

## 어떻게 발견했는가

실기기(무선 디버깅)에서 방장 계정으로 사진이 등록되지 않은 실제 서버 데이터(Swagger로 만든 테스트 장소 9개)를 카드형(콜라주) 뷰로 확인하던 중 발견. 사용자가 처음엔 "이미지 카드 2개 안 보여"로 보고했으나, 확인 질문으로 좁혀가며 "카메라 아이콘 크기가 너무 커서"가 실제 원인임이 드러났다.

## 무엇으로 대체했는가

- `CardLocationTokens`(공유 토큰)에 `ThumbnailPlaceholderIconSize = 24.dp`를 추가했다(`feature:home`의 `ImageFallbackGlyphSize`와 같은 값).
- `MinoCardLocationList`·`MinoCardLocationCollage` 둘 다 `MinoAsyncImage` 호출에 `fallbackModifier = Modifier.wrapContentSize().size(CardLocationTokens.ThumbnailPlaceholderIconSize)`를 추가해, 사진이 없을 때도 박스 안에 작게 중앙 정렬된 아이콘만 그리도록 고쳤다.
- 같은 세션에서 콜라주형(`MinoCardLocationCollage`)이 사진이 하나도 없으면 사진 줄 자체를 그리지 않던 것도 함께 고쳐, 리스트형과 마찬가지로 사진 유무와 무관하게 항상 2슬롯 박스를 보여주도록 바꿨다(별도 결함은 아니고 같은 확인 과정에서 함께 정리).

변경 파일: [`MinoCardLocationList.kt`](../../core/design-system/src/main/java/team/mino/core/designsystem/component/cardlocation/MinoCardLocationList.kt), [`MinoCardLocationCollage.kt`](../../core/design-system/src/main/java/team/mino/core/designsystem/component/cardlocation/MinoCardLocationCollage.kt), [`CardLocationTokens.kt`](../../core/design-system/src/main/java/team/mino/core/designsystem/component/cardlocation/token/CardLocationTokens.kt).
