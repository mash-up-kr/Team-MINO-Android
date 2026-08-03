package team.mino.core.designsystem.util.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme

/**
 * 컴포넌트 프리뷰의 껍데기. Figma 디자인 시스템 문서 페이지의 **컴포넌트 섹션 한 장**에 대응한다.
 *
 * Figma 문서 페이지는 컴포넌트마다 `속성 = 값 · 값` 헤딩과 그 아래 예시 묶음(`List` 블록)을
 * 세로로 쌓아 만든다. 코드 프리뷰도 같은 구조를 [PreviewProperty]로 그대로 옮겨,
 * **Figma 페이지와 프리뷰를 나란히 놓고 축 단위로 대조**할 수 있게 한다.
 *
 * 블록 순서는 Figma 문서 페이지의 순서를 따른다. Figma에 있지만 Android에서 제외한 축
 * (웹 전용 `interaction`·`autoComplete` 등)은 블록을 만들지 않고, 왜 뺐는지만 주석으로 남긴다.
 *
 * ```
 * @UiModePreviews
 * @Composable
 * private fun ChipPreview() {
 *     PreviewPage {
 *         PreviewProperty(name = "size", values = ChipSize.entries.previewValues()) { ... }
 *     }
 * }
 * ```
 *
 * @param background 페이지 배경. 컴포넌트가 대체 배경 위에 놓이는 자리(액션 영역 등)만 바꾼다.
 */
@Composable
internal fun PreviewPage(
    modifier: Modifier = Modifier,
    background: ColorAccessKeyToken = ColorAccessKeyToken.BackgroundNormalNormal,
    content: @Composable ColumnScope.() -> Unit,
) {
    MinoAndroidAppTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(background.value)
                .padding(PagePadding),
            verticalArrangement = Arrangement.spacedBy(PropertySpacing),
            content = content,
        )
    }
}

/**
 * 문서 페이지의 `List` 블록 하나 — 속성 헤딩과 그 아래 예시 묶음.
 *
 * @param name 속성 이름. Figma 헤딩의 표기를 그대로 쓴다(코드 파라미터명이 아니라 **Figma 속성명**).
 * @param values 값 목록. 열거 축이면 [previewValues]로 잇고, 값이 없는 슬롯 축이면 null로 둔다 —
 *   Figma도 `icon`처럼 값이 없는 축은 `=` 없이 이름만 적는다.
 */
@Composable
internal fun PreviewProperty(
    name: String,
    modifier: Modifier = Modifier,
    values: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HeadingSpacing),
    ) {
        Text(
            text = if (values == null) name else "$name = $values",
            style = TypographyAccessKeyToken.Label2Medium.value,
            color = ColorAccessKeyToken.LabelAlternative.value,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(ContentSpacing),
            content = content,
        )
    }
}

/** [PreviewProperty] 안에서 값을 가로로 늘어놓는 줄. Figma `Content` 프레임의 가로 배치에 대응한다. */
@Composable
internal fun PreviewRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** 열거 축의 값 목록을 Figma 헤딩 표기(`A · B · C`)로 잇는다. */
internal fun Iterable<Any>.previewValues(): String = joinToString(separator = " · ")

private val PagePadding = 16.dp
private val PropertySpacing = 28.dp
private val HeadingSpacing = 12.dp
private val ContentSpacing = 12.dp
