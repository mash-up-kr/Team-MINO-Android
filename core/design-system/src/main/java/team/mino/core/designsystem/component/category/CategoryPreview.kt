package team.mino.core.designsystem.component.category

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

private val SampleItems = persistentListOf("전체", "카페", "맛집", "액티비티", "숙소", "공연·전시", "체험", "여행")

/**
 * Figma `Category` 문서 페이지(16215:22015)의 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma 헤딩의 `padding`은 컴포넌트셋 축 이름으로 `Horizontal Padding`이고,
 * 코드 파라미터는 `horizontalPadding`이다.
 *
 * `scroll`은 `LazyRow`가 이미 처리하고 양끝 페이드도 구현돼 있어 별도 블록을 만들지 않았다 —
 * 위 블록들이 모두 항목 8개짜리라 폭을 넘겨 그 동작이 그대로 보인다.
 */
@UiModePreviews
@Composable
private fun CategoryPreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = CategoryVariant.entries.previewValues()) {
            CategoryVariant.entries.forEach { variant ->
                CategoryPreviewItem(variant = variant)
            }
        }
        PreviewProperty(name = "size", values = CategorySize.entries.previewValues()) {
            CategorySize.entries.forEach { size ->
                CategoryPreviewItem(size = size)
            }
        }
        // 좌우 여백은 스크롤 콘텐츠 **안쪽**이라 Figma처럼 스크롤하면 여백까지 함께 밀려난다
        PreviewProperty(name = "padding", values = "False · True") {
            CategoryPreviewItem(horizontalPadding = false)
            CategoryPreviewItem(horizontalPadding = true)
        }
        PreviewProperty(name = "verticalPadding", values = "False · True") {
            CategoryPreviewItem(verticalPadding = false)
            CategoryPreviewItem(verticalPadding = true)
        }
        // 스크롤 영역 오른쪽에 고정으로 붙는 자리. 페이드 대상이 아니라 슬롯으로 열었다.
        PreviewProperty(name = "iconButton", values = "False · True") {
            CategoryPreviewItem(horizontalPadding = true)
            CategoryPreviewItem(
                horizontalPadding = true,
                trailingContent = {
                    Icon(
                        imageVector = MinoIcons.Plus,
                        contentDescription = "카테고리 더 보기",
                        tint = ColorAccessKeyToken.LabelNormal.value,
                    )
                },
            )
        }
    }
}

/** Figma 인스턴스 하나에 대응하는 프리뷰 항목. 선택 상태를 각자 들고 있어 눌러 볼 수 있다. */
@Composable
private fun CategoryPreviewItem(
    modifier: Modifier = Modifier,
    size: CategorySize = CategorySize.Medium,
    variant: CategoryVariant = CategoryVariant.Normal,
    horizontalPadding: Boolean = false,
    verticalPadding: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    MinoCategory(
        modifier = modifier.fillMaxWidth(),
        items = SampleItems,
        selectedIndex = selectedIndex,
        onItemClick = { selectedIndex = it },
        size = size,
        variant = variant,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        trailingContent = trailingContent,
    )
}
