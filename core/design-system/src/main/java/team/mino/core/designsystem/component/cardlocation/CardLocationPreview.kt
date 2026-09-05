package team.mino.core.designsystem.component.cardlocation

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.avatar.MinoAvatarGroup
import team.mino.core.designsystem.component.avatar.MinoAvatarGroupSize
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews

private const val PREVIEW_TITLE = "성수동 카페거리"
private const val PREVIEW_ADDRESS = "서울 성동구 성수이로 100"
private const val PREVIEW_COMMENT_COUNT = 3

/**
 * `Card_Location A` — [MinoCardLocationList]의 속성 축 프리뷰.
 *
 * 만들지 않은 축:
 * - `interaction`의 hover·press — Android는 리플로 대체된다
 */
@UiModePreviews
@Composable
private fun CardLocationListPreview() {
    PreviewPage {
        PreviewProperty(name = "thumbnail", values = "empty · filled") {
            PreviewRow {
                MinoCardLocationList(
                    title = PREVIEW_TITLE,
                    address = PREVIEW_ADDRESS,
                    commentCount = PREVIEW_COMMENT_COUNT,
                    onClick = {},
                    onMoreClick = {},
                )
            }
            PreviewRow {
                MinoCardLocationList(
                    title = PREVIEW_TITLE,
                    address = PREVIEW_ADDRESS,
                    commentCount = PREVIEW_COMMENT_COUNT,
                    onClick = {},
                    onMoreClick = {},
                    thumbnailUrl = "https://example.com/thumbnail.jpg",
                )
            }
        }
        PreviewProperty(name = "avatarGroup", values = "null · present") {
            PreviewRow {
                MinoCardLocationList(
                    title = PREVIEW_TITLE,
                    address = PREVIEW_ADDRESS,
                    commentCount = PREVIEW_COMMENT_COUNT,
                    onClick = {},
                    onMoreClick = {},
                    avatarGroup = {
                        MinoAvatarGroup(
                            imageUrls = SampleAvatarUrls,
                            size = MinoAvatarGroupSize.XSmall,
                        )
                    },
                )
            }
        }
    }
}

/**
 * `Card_Location B` — [MinoCardLocationCollage]의 속성 축 프리뷰.
 *
 * 만들지 않은 축:
 * - `interaction`의 hover·press — Android는 리플로 대체된다
 */
@UiModePreviews
@Composable
private fun CardLocationCollagePreview() {
    PreviewPage {
        PreviewProperty(name = "thumbnail", values = "empty · filled") {
            PreviewRow {
                MinoCardLocationCollage(
                    title = PREVIEW_TITLE,
                    address = PREVIEW_ADDRESS,
                    commentCount = PREVIEW_COMMENT_COUNT,
                    onClick = {},
                    onMoreClick = {},
                )
            }
            PreviewRow {
                MinoCardLocationCollage(
                    title = PREVIEW_TITLE,
                    address = PREVIEW_ADDRESS,
                    commentCount = PREVIEW_COMMENT_COUNT,
                    onClick = {},
                    onMoreClick = {},
                    thumbnailUrls = persistentListOf("https://example.com/thumbnail-1.jpg", null),
                )
            }
        }
        PreviewProperty(name = "avatarGroup", values = "null · present") {
            PreviewRow {
                MinoCardLocationCollage(
                    title = PREVIEW_TITLE,
                    address = PREVIEW_ADDRESS,
                    commentCount = PREVIEW_COMMENT_COUNT,
                    onClick = {},
                    onMoreClick = {},
                    thumbnailUrls = persistentListOf("https://example.com/thumbnail-1.jpg", null),
                    avatarGroup = {
                        MinoAvatarGroup(
                            imageUrls = SampleAvatarUrls,
                            size = MinoAvatarGroupSize.XSmall,
                        )
                    },
                )
            }
        }
    }
}

private val SampleAvatarUrls = persistentListOf<String?>(null, null, null)
