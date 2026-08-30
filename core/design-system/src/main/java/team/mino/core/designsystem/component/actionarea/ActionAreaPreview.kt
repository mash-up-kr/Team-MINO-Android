package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Link
import team.mino.core.designsystem.foundation.icons.icons.Pencil
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * Figma `Action Area` 문서 페이지(16215:35517)의 **컴포넌트 단위** 속성 블록을 순서대로 옮긴 프리뷰.
 *
 * 배경은 대체 배경을 쓴다 — `sticky=false`인 기본형은 액션 영역 자신이 배경을 그리지 않아
 * 흰 배경 위에서는 컨테이너 경계가 보이지 않기 때문이다.
 *
 * Figma에 있으나 블록을 만들지 않은 축:
 * - `variant=Compact`과 그 하위 `compactContent` — Figma가 변형 이름에 **(Web Only)** 를 달아 뒀다
 * - `customize` — 커스터마이즈 가이드라 API 축이 아니다
 * - `safeArea` — 하단 인셋은 컴포넌트가 아니라 호출부 `modifier`가 얹는다([MinoActionArea] KDoc 참조)
 */
@UiModePreviews
@Composable
private fun ActionAreaPreview() {
    PreviewPage(background = ColorAccessKeyToken.BackgroundNormalAlternative) {
        // 붙이면 상단 모서리 12dp 라운드 + Background/Elevated/Normal 배경 + 여백이 함께 생긴다
        PreviewProperty(name = "extra", values = "False · True") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "결제하기", onClick = {}),
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "결제하기", onClick = {}),
                extra = { ActionAreaPreviewExtra() },
            )
        }
        // extra가 있을 때 그 위에 긋는 1dp 선. Figma가 루트에 inset-0으로 얹은 별도 레이어라
        // 상단 라운드를 따라가지 않는 곧은 선이다.
        PreviewProperty(name = "divider", values = "True · False") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "결제하기", onClick = {}),
                extra = { ActionAreaPreviewExtra() },
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "결제하기", onClick = {}),
                divider = false,
                extra = { ActionAreaPreviewExtra() },
            )
        }
        // 강조 계열이 아니라 배치 축이다 — Strong·Cancel은 세로, Neutral은 가로로 늘어놓는다.
        // 같은 "보조 액션"이라도 세로 계열은 텍스트 버튼, 가로 계열은 테두리 버튼이다.
        PreviewProperty(name = "variant", values = ActionAreaVariant.entries.previewValues()) {
            ActionAreaVariant.entries.forEach { variant ->
                MinoActionArea(
                    modifier = Modifier.fillMaxWidth(),
                    variant = variant,
                    mainAction = ActionAreaAction(text = "메인", onClick = {}),
                    alternativeAction = ActionAreaAction(text = "대체", onClick = {}),
                    subAction = ActionAreaAction(text = "보조", onClick = {}),
                )
            }
        }
        // 액션 묶음 위에 gap 16으로 놓이는 가운데 정렬 문구
        PreviewProperty(name = "caption", values = "False · True") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                caption = "결제 시 이용약관에 동의하게 됩니다.",
            )
        }
        // Figma가 배경 레이어를 이 속성에 묶어 둬 꺼져 있으면 배경이 없다.
        // 켜면 배경과 함께 콘텐츠가 딱 잘리지 않도록 상단 페이드가 그려진다.
        PreviewProperty(name = "sticky", values = "False · True") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                sticky = true,
            )
        }
    }
}

/**
 * Figma `Action Area/Resource/Actions`(16215:35697)의 독립 불리언 축.
 *
 * `alternativeAction`·`subAction`은 `variant`와 **직교하는 독립 불리언**이라 메인·대체·보조를
 * 동시에 세울 수 있다. 아이콘은 세 자리 모두에 열려 있다.
 */
@UiModePreviews
@Composable
private fun ActionAreaActionsPreview() {
    PreviewPage(background = ColorAccessKeyToken.BackgroundNormalAlternative) {
        PreviewProperty(name = "alternativeAction", values = "False · True") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                alternativeAction = ActionAreaAction(text = "대체 액션", onClick = {}),
            )
        }
        PreviewProperty(name = "subAction", values = "False · True") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                subAction = ActionAreaAction(text = "보조 액션", onClick = {}),
            )
        }
        // 자리마다 각각 끌 수 있다
        PreviewProperty(name = "disable", values = "False · True") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}),
                subAction = ActionAreaAction(text = "보조 액션", onClick = {}),
            )
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(text = "메인 액션", onClick = {}, enabled = false),
                subAction = ActionAreaAction(text = "보조 액션", onClick = {}, enabled = false),
            )
        }
        PreviewProperty(name = "icon") {
            MinoActionArea(
                modifier = Modifier.fillMaxWidth(),
                mainAction = ActionAreaAction(
                    text = "방 편집",
                    onClick = {},
                    leadingIcon = { Icon(imageVector = MinoIcons.Pencil, contentDescription = null) },
                ),
                subAction = ActionAreaAction(
                    text = "장소 추가",
                    onClick = {},
                    leadingIcon = { Icon(imageVector = MinoIcons.Link, contentDescription = null) },
                ),
            )
        }
    }
}

/** Figma `Extra/Preset`은 만들지 않고 슬롯에 호출부가 조립한다. 프리뷰는 Summary 프리셋을 흉내 낸다. */
@Composable
private fun ActionAreaPreviewExtra(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(28.dp)) {
        Text(
            text = "총 결제금액 24,000원",
            style = TypographyAccessKeyToken.Body1NormalBold.value,
            color = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
