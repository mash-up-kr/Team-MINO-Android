package team.mino.core.designsystem.component.scrollbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * ScrollBar 프리뷰.
 *
 * 이 컴포넌트는 문서 페이지가 아니라 화면 안의 인스턴스로만 확인할 수 있어(컴포넌트 정의 노드가
 * 라이브러리 파일에 있어 열리지 않는다) 속성 축 대신 **쓰이는 맥락** 두 가지로 블록을 나눴다.
 * 썸 길이는 목록의 스크롤 여지에서 나오므로 목록 없이 단독으로 보면 아무것도 확인할 수 없다.
 */
@UiModePreviews
@Composable
private fun ScrollBarPreview() {
    PreviewPage {
        PreviewProperty(name = "스크롤 여지 있음") {
            PreviewRow {
                ScrollBarPreviewList(itemCount = 8)
            }
        }
        PreviewProperty(name = "스크롤 여지 없음") {
            PreviewRow {
                ScrollBarPreviewList(itemCount = 2)
            }
        }
    }
}

/** 스크롤 상태를 들고 있는 목록 하나. 스크롤바는 목록 오른쪽 끝에 겹쳐 놓인다. */
@Composable
private fun ScrollBarPreviewList(
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberLazyListState()

    Box(modifier = modifier.width(PreviewListWidth).height(PreviewListHeight)) {
        LazyColumn(state = scrollState) {
            items(count = itemCount) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PreviewItemHeight)
                        .padding(vertical = PreviewItemGap)
                        .background(
                            color = ColorAccessKeyToken.FillNormal.value,
                            shape = RoundedCornerShape(PreviewItemRadius),
                        ),
                )
            }
        }
        MinoScrollBar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = PreviewScrollBarInset),
        )
    }
}

private val PreviewListWidth = 335.dp
private val PreviewListHeight = 416.dp
private val PreviewItemHeight = 104.dp
private val PreviewItemGap = 6.dp
private val PreviewItemRadius = 12.dp
private val PreviewScrollBarInset = 12.dp
