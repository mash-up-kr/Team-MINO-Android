package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton

/**
 * 메인 액션 하나만 두는 액션 영역(Figma `Action Area/Action Area`).
 *
 * 버튼 자체는 [MinoButton]이 그린다. 이 컴포넌트는 Figma가 정한 컨테이너 패딩·배경과 메인 액션의
 * 배치·스타일을 소유한다. 그래서 액션이 하나뿐이어도 [MinoButton]을 직접 쓰는 것과 다르다 —
 * 20dp 컨테이너 패딩, 너비(`fillMaxWidth`), 스타일([ButtonStyle.SolidPrimary])을 호출부가
 * 화면마다 다시 정하지 않는다.
 *
 * 보조 액션이 필요하면 배치가 달라 컴포넌트가 나뉜다.
 * - [MinoSubActionArea]: 메인 액션 옆에 가로로 붙는 저강조 보조 액션.
 * - [MinoAlternativeActionArea]: 메인 액션 아래 세로로 쌓이는 대체 액션.
 *
 * Figma 컴포넌트셋의 `Extra=False` 변형만 구현한다. 컴포넌트셋에 `Divider` 속성이 선언돼 있지만
 * 실제 레이어는 `Extra=True` 변형에만 있고(`Semantic/Line/Normal/Neutral` 1px 상단 실선),
 * 이 변형에는 구분선이 없어 파라미터를 두지 않았다. `Extra=True`(설명 영역이 얹힌 형태)를
 * 구현하게 되면 그때 디폴트 값을 가진 파라미터로 더한다.
 *
 * @param sticky 스크롤되는 콘텐츠 위에 떠서 고정되는 상태인지 여부(Figma `Sticky` 속성).
 *   Figma에서 액션 영역의 배경을 그리는 레이어가 이 속성에 묶여 있어, **꺼져 있으면 배경이 없다.**
 *   콘텐츠 흐름 안에 놓이는 기본형은 화면 배경이 그대로 비쳐야 하므로 이쪽이 기본값이다.
 *   켜면 [MinoActionAreaDefaults.stickyContainerColor] 배경과, 콘텐츠가 딱 잘리지 않도록
 *   상단 페이드가 함께 그려진다.
 *
 * 하단 시스템 인셋(제스처 내비게이션 바 등) 대응은 **컴포넌트가 하지 않고 호출부에 맡긴다.**
 * `Scaffold`가 `contentWindowInsets`로 하단 인셋을 이미 소비하는 화면에서 컴포넌트가 한 번 더
 * 얹으면 여백이 두 겹으로 들어가는데, 그때 호출부가 끌 방법이 없기 때문이다. 화면 구조에 맞춰
 * [modifier]에 직접 얹는다.
 *
 * ```
 * MinoActionArea(
 *     modifier = Modifier.navigationBarsPadding(),
 *     mainAction = ActionAreaAction(text = "확인", onClick = { }),
 * )
 * ```
 *
 * Figma는 이 자리에 `Safe Area/Bottom`을 두고 `Type=Gesture`일 때 14px 고정 여백을 주지만,
 * 디자인 툴이 기기 인셋을 읽지 못해 박아둔 값이다. 안드로이드에서는 실제 인셋을 쓴다.
 */
@Composable
fun MinoActionArea(
    mainAction: ActionAreaAction,
    modifier: Modifier = Modifier,
    sticky: Boolean = false,
) {
    ActionAreaContainer(modifier = modifier, sticky = sticky) {
        MinoButton(
            modifier = Modifier.fillMaxWidth(),
            text = mainAction.text,
            onClick = mainAction.onClick,
            enabled = mainAction.enabled,
            style = ButtonStyle.SolidPrimary,
            leadingIcon = mainAction.leadingIcon,
        )
    }
}

/**
 * 액션 영역에 놓이는 액션 하나. 액션 영역 3종이 자리마다 이 타입을 받는다.
 *
 * 크기는 담지 않는다. Figma 액션 영역은 모든 자리가 `Size=Large`로 고정이라, 크기를 열면
 * 디자인에 없는 조합이 만들어진다. 스타일도 담지 않는다 — 어느 자리에 놓이는지가 스타일을
 * 결정하고, 그 결정은 각 액션 영역 컴포넌트가 소유한다.
 *
 * @param leadingIcon 글자 앞 아이콘(Figma `Leading Icon`). 색은 [MinoButton]이 스타일에 맞춰
 *   `LocalContentColor`로 깔아주므로 `tint`를 따로 넘기지 않아도 된다.
 */
@Immutable
data class ActionAreaAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val leadingIcon: (@Composable () -> Unit)? = null,
)
