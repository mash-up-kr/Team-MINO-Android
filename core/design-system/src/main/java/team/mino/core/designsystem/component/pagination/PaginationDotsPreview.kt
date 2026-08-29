package team.mino.core.designsystem.component.pagination

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Pagination Dots 프리뷰.
 *
 * **만들지 않은 축.** 정의 노드는 `Variant`(Normal · White)와 `Size`(Medium · Small) 두 축을
 * 갖지만 이 컴포넌트는 둘 다 열지 않았다. `Variant`는 두 값이 색으로만 갈리는 축이라 코드는 그
 * 자리를 `colors` 파라미터로 대신 받는다 — 라이트/다크 전환이 아니라 어두운 배경 위에 얹는
 * 별도 구성이므로, 그 구성이 필요한 호출부가 [MinoPaginationDotsColors]를 넘긴다. `Size`는 지금
 * 이 컴포넌트를 쓰는 곳이 큰 쪽 하나만 쓰기에 값을 넣지 않고 축째로 뺐다.
 *
 * **점 개수의 상한.** 정의 노드는 온전한 크기의 점 슬롯을 다섯 개까지 두고, 그 너머는 가장자리
 * 점을 줄여 넣는 축소 슬롯으로 표현한다. 이 컴포넌트는 그 축소 규칙을 구현하지 않으므로 프리뷰도
 * 디자인에 실재하는 다섯 이하만 그린다.
 *
 * **헤딩 대응.** 첫 블록만 Figma 속성명이다 — 정의 노드가 점의 개수를 슬롯 하나하나를 켜고 끄는
 * 불리언으로 두고 있어, 켠 슬롯 수가 코드의 `count`에 대응한다. 나머지 두 블록은 대응하는 Figma
 * 속성이 없어 하는 일로 이름을 붙였다. 짙은 점은 정의 노드에서 늘 첫 슬롯 하나라 자리를 옮기는
 * 축이 아예 없고(코드 `selectedIndex`), 탭은 인터랙션이라 속성 축이 되지 않는다(코드
 * `onDotClick`).
 *
 * 선택 상태는 크기가 아니라 색의 진하기로만 갈리므로, 블록마다 짙은 점의 자리를 다르게 두어
 * 두 상태가 한 줄에 함께 보이게 했다.
 */
@UiModePreviews
@Composable
private fun PaginationDotsPreview() {
    PreviewPage {
        PreviewProperty(
            name = "Dot ●○○○○ … Dot ○○○○●",
            values = PreviewCounts.previewValues(),
        ) {
            PreviewCounts.forEach { count ->
                PreviewRow {
                    MinoPaginationDots(count = count, selectedIndex = 0)
                }
            }
        }
        PreviewProperty(name = "짙은 점의 자리", values = PreviewSelectedIndices.previewValues()) {
            PreviewSelectedIndices.forEach { selectedIndex ->
                PreviewRow {
                    MinoPaginationDots(count = PREVIEW_COUNT, selectedIndex = selectedIndex)
                }
            }
        }
        PreviewProperty(name = "탭 반응", values = "표시 전용 · 탭 가능") {
            PreviewRow {
                MinoPaginationDots(count = PREVIEW_COUNT, selectedIndex = 0)
            }
            PreviewRow {
                ClickablePaginationDotsPreview()
            }
        }
    }
}

/** 선택 인덱스를 스스로 들고 있는 인스턴스 하나. 점을 누르면 그 자리로 옮겨간다. */
@Composable
private fun ClickablePaginationDotsPreview(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    MinoPaginationDots(
        count = PREVIEW_COUNT,
        selectedIndex = selectedIndex,
        modifier = modifier,
        onDotClick = { selectedIndex = it },
    )
}

private const val PREVIEW_COUNT = 5
private val PreviewCounts = listOf(3, 4, 5)
private val PreviewSelectedIndices = listOf(0, 2, 4)
