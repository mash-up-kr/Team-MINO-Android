package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonSize
import team.mino.core.designsystem.component.button.TextButtonStyle
import team.mino.core.designsystem.component.contentbadge.ContentBadgeSize
import team.mino.core.designsystem.component.contentbadge.MinoContentBadge
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ArrowRight
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Menu` 문서 페이지(16215:18387)의 **메뉴 단위** 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * `scroll`은 항목 영역만 스크롤하고 액션 영역은 고정해야 하는데, 그러려면 컨테이너가 두 자식 사이에서
 * 높이를 배분하는 커스텀 `Layout`이 필요해 아직 미지원이다. 그래서 블록을 만들지 않았다.
 *
 * Figma는 `Cell Padding`을 메뉴 단위 속성으로 두지만 코드는 아이템마다 받는다. 그래서 그 축은
 * 셀 리소스 프리뷰(`MenuItemCellPreview`)에 두고, 한 메뉴 안에서는 같은 값을 준다.
 */
@UiModePreviews
@Composable
private fun MenuPreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = MenuItemVariant.entries.previewValues()) {
            MenuItemVariant.entries.forEach { variant ->
                MinoMenu(modifier = Modifier.fillMaxWidth()) {
                    MinoMenuItem(text = "첫 번째 항목", onClick = {}, variant = variant, active = true)
                    MinoMenuItem(text = "두 번째 항목", onClick = {}, variant = variant)
                    MinoMenuItem(text = "세 번째 항목", onClick = {}, variant = variant)
                }
            }
        }
        // 구분선이 메뉴 폭을 가로질러야 해서 아이템 목록의 좌우 여백 바깥에 놓인다.
        // 리딩·트레일링 프리셋은 슬롯으로 열어 호출부가 조립한다.
        PreviewProperty(name = "menuActionArea", values = "False · True") {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuItem(text = "첫 번째 항목", onClick = {}, variant = MenuItemVariant.Checkbox, active = true)
                MinoMenuItem(text = "두 번째 항목", onClick = {}, variant = MenuItemVariant.Checkbox)
            }
            MinoMenu(
                modifier = Modifier.fillMaxWidth(),
                actionArea = {
                    MinoMenuActionArea(
                        leadingContent = {
                            MinoTextButton(
                                text = "초기화",
                                onClick = {},
                                size = TextButtonSize.Small,
                                style = TextButtonStyle.Assistive,
                            )
                        },
                        trailingContent = {
                            MinoButton(text = "적용", onClick = {}, size = ButtonSize.Small)
                        },
                    )
                },
            ) {
                MinoMenuItem(text = "첫 번째 항목", onClick = {}, variant = MenuItemVariant.Checkbox, active = true)
                MinoMenuItem(text = "두 번째 항목", onClick = {}, variant = MenuItemVariant.Checkbox)
            }
        }
    }
}

/**
 * Figma `Menu/Resource/Item/Cell` 컴포넌트셋(16215:18569)과 `Item/Title` 리소스의 축.
 *
 * 선택돼도 **라벨 색·굵기는 그대로다** — Normal 변형만 라벨이 강조된다(Figma 실측).
 * Leading 자리는 변형이 소유하므로 슬롯으로 열지 않았다(Figma도 Normal 셀에는 Leading 프레임이 없다).
 */
@UiModePreviews
@Composable
private fun MenuItemCellPreview() {
    PreviewPage {
        PreviewProperty(name = "active", values = "False · True") {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MenuItemVariant.entries.forEach { variant ->
                    MinoMenuItem(text = "${variant.name} 비선택", onClick = {}, variant = variant)
                    MinoMenuItem(text = "${variant.name} 선택", onClick = {}, variant = variant, active = true)
                }
            }
        }
        PreviewProperty(name = "caption", values = "False · True") {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuItem(text = "캡션 없음", onClick = {})
                MinoMenuItem(text = "캡션 있음", onClick = {}, caption = "설명")
                MinoMenuItem(text = "선택 + 캡션", onClick = {}, active = true, caption = "설명")
            }
        }
        PreviewProperty(name = "disable", values = "False · True") {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuItem(text = "기본", onClick = {})
                MinoMenuItem(text = "비활성", onClick = {}, enabled = false)
                MinoMenuItem(text = "비활성 + 캡션", onClick = {}, enabled = false, caption = "설명")
            }
        }
        PreviewProperty(name = "Vertical Padding", values = "8px · 12px") {
            MenuCellPaddingSample(contentPadding = MinoMenuDefaults.ItemContentPaddingCompact)
            MenuCellPaddingSample(contentPadding = MinoMenuDefaults.ItemContentPadding)
        }
        // 좌우 여백을 셀과 맞춰 라벨 시작점이 정렬된다
        PreviewProperty(name = "Item/Title") {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuTitle(text = "그룹 제목")
                MinoMenuItem(text = "첫 번째 항목", onClick = {})
                MinoMenuTitle(text = "다른 그룹")
                MinoMenuItem(text = "두 번째 항목", onClick = {})
            }
        }
        PreviewProperty(name = "Item/Cell → Trailing Content") {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MinoMenuItem(
                    text = "아이콘",
                    onClick = {},
                    trailingContent = {
                        Icon(
                            imageVector = MinoIcons.ArrowRight,
                            contentDescription = null,
                            tint = ColorAccessKeyToken.LabelAlternative.value,
                        )
                    },
                )
                MinoMenuItem(
                    text = "배지",
                    onClick = {},
                    trailingContent = {
                        MinoContentBadge(text = "NEW", size = ContentBadgeSize.XSmall)
                    },
                )
            }
        }
    }
}

@Composable
private fun MenuCellPaddingSample(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    MinoMenu(modifier = modifier.fillMaxWidth()) {
        MinoMenuItem(text = "첫 번째 항목", onClick = {}, active = true, contentPadding = contentPadding)
        MinoMenuItem(text = "두 번째 항목", onClick = {}, contentPadding = contentPadding)
    }
}
