package team.mino.core.designsystem.component.profileavatar

import androidx.compose.runtime.Composable
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * 프로필 아바타의 속성 축 프리뷰.
 *
 * 이 아바타의 Figma 문서 페이지는 디자인 시스템 라이브러리 파일에 있어 화면 파일에서 열리지 않는다.
 * 그래서 블록을 Figma 헤딩이 아니라 공개 파라미터 축으로 세웠고, 화면 파일에 실제로 그려진 두 자리
 * (선택 그리드 한 칸·상단 썸네일)를 `size` 블록이 대신 대조한다.
 *
 * 블록을 만들지 않은 축:
 * - `selected` — 디자인에 선택 표시가 없어 참·거짓의 그림이 같다. 차이는 접근성 시맨틱뿐이라 눈으로 볼 것이 없다
 * - `contentDescription` — 시각 축이 아니다
 *
 * `avatar` 블록을 네 개씩 끊어 쌓은 것은 프리뷰 폭에 맞추기 위해서다. 실제 그리드 배치는
 * 이 컴포넌트가 아니라 화면이 소유한다.
 */
@UiModePreviews
@Composable
private fun ProfileAvatarPreview() {
    PreviewPage {
        PreviewProperty(name = "avatar", values = MinoProfileAvatar.entries.previewValues()) {
            MinoProfileAvatar.entries.chunked(PREVIEW_COLUMN_COUNT).forEach { row ->
                PreviewRow {
                    row.forEach { avatar ->
                        MinoProfileAvatarImage(avatar = avatar)
                    }
                }
            }
        }
        PreviewProperty(name = "size", values = MinoProfileAvatarSize.entries.previewValues()) {
            PreviewRow {
                MinoProfileAvatarSize.entries.forEach { size ->
                    MinoProfileAvatarImage(avatar = MinoProfileAvatar.Person1, size = size)
                }
            }
        }
        // onClick이 있을 때만 리플과 선택 시맨틱을 받는다. 정적 프리뷰에는 리플이 그려지지 않는다.
        PreviewProperty(name = "onClick", values = "False · True") {
            PreviewRow {
                MinoProfileAvatarImage(avatar = MinoProfileAvatar.Person1)
                MinoProfileAvatarImage(avatar = MinoProfileAvatar.Person1, onClick = {})
            }
        }
    }
}

private const val PREVIEW_COLUMN_COUNT = 4
