package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonSize
import team.mino.core.designsystem.component.button.TextButtonStyle
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Avatar` 문서 페이지(16215:25543)의 `Person` 섹션 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * Figma에 있으나 블록을 만들지 않은 축:
 * - `interaction`의 하위 hover·press·focus 4단계 — Android는 리플로 대체된다
 * - `customize` — 커스터마이즈 가이드라 API 축이 아니다
 *
 * 리소스 레벨 변형(`Image/Person`의 Official·Deactivate, `Image/Company`의 원티드)은
 * 전용 이미지 애셋이 필요해 아직 없다.
 */
@UiModePreviews
@Composable
private fun AvatarPreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = MinoAvatarVariant.entries.previewValues()) {
            PreviewRow {
                MinoAvatarVariant.entries.forEach { variant ->
                    MinoAvatar(variant = variant, size = MinoAvatarSize.Medium)
                }
            }
        }
        PreviewProperty(name = "size", values = MinoAvatarSize.entries.previewValues()) {
            PreviewRow {
                MinoAvatarSize.entries.forEach { size ->
                    MinoAvatar(size = size)
                }
            }
        }
        // 코드는 placeholder를 별도 축이 아니라 `imageUrl = null`로 표현한다.
        // 프리뷰에는 네트워크가 없어 URL을 넘겨도 폴백(=placeholder)이 그려진다.
        PreviewProperty(name = "placeholder", values = "False · True") {
            PreviewRow {
                MinoAvatarVariant.entries.forEach { variant ->
                    MinoAvatar(variant = variant, size = MinoAvatarSize.Medium, imageUrl = null)
                }
            }
        }
        // onClick이 있으면 리플을 받는다. Figma 인터랙션 레이어는 아바타 바깥으로 8dp 튀어나오지만
        // Compose는 리플을 바운즈 밖으로 그리지 못해 안쪽에 머문다.
        PreviewProperty(name = "interaction", values = "False · True") {
            PreviewRow {
                MinoAvatar(size = MinoAvatarSize.Medium)
                MinoAvatar(size = MinoAvatarSize.Medium, onClick = {})
            }
        }
        // 배지 프레임의 중심을 우상단 모서리에 맞춰 절반이 밖으로 나가고, 크기는 아바타와 무관하게 20dp 고정이다
        PreviewProperty(name = "pushBadge", values = "False · True") {
            MinoAvatarSize.entries.forEach { size ->
                PreviewRow {
                    MinoAvatar(size = size)
                    MinoAvatar(size = size, pushBadge = { AvatarPreviewBadge() })
                }
            }
        }
    }
}

/**
 * Figma `Avatar` 문서 페이지의 `Avatar Group` 섹션(16215:26113) 속성 블록.
 *
 * DS 그룹은 pill 컨테이너가 없고 링을 아바타 크기 **안쪽**에 그린다. 크기는 XSmall·Small 둘뿐이며
 * 겹침 폭(6/8dp)과 트레일링 간격(8/10dp)까지 함께 가른다.
 */
@UiModePreviews
@Composable
private fun AvatarGroupPreview() {
    PreviewPage {
        PreviewProperty(name = "variant", values = MinoAvatarVariant.entries.previewValues()) {
            PreviewRow {
                MinoAvatarVariant.entries.forEach { variant ->
                    MinoAvatarGroup(imageUrls = SampleImageUrls, variant = variant)
                }
            }
        }
        PreviewProperty(name = "size", values = MinoAvatarGroupSize.entries.previewValues()) {
            PreviewRow {
                MinoAvatarGroupSize.entries.forEach { size ->
                    MinoAvatarGroup(imageUrls = SampleImageUrls, size = size)
                }
            }
        }
        // Figma 기본 프리셋이 `Button/Text`(Assistive·Small)라 MinoTextButton을 그대로 넣는다
        PreviewProperty(name = "trailingContent", values = "False · True") {
            MinoAvatarGroupSize.entries.forEach { size ->
                PreviewRow {
                    MinoAvatarGroup(imageUrls = SampleImageUrls, size = size)
                    MinoAvatarGroup(
                        imageUrls = SampleImageUrls,
                        size = size,
                        trailingContent = {
                            MinoTextButton(
                                text = "외 3명",
                                onClick = {},
                                size = TextButtonSize.Small,
                                style = TextButtonStyle.Assistive,
                            )
                        },
                    )
                }
            }
        }
    }
}

/** 푸시 배지 그래픽은 화면마다 달라 슬롯으로 열려 있다. 프리뷰는 가장 단순한 점으로 채운다. */
@Composable
private fun AvatarPreviewBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(ColorAccessKeyToken.StatusNegative.value),
    )
}

private val SampleImageUrls = persistentListOf<String?>(null, null, null, null, null)
