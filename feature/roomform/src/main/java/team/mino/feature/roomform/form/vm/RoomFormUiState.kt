package team.mino.feature.roomform.form.vm

import androidx.compose.runtime.Immutable
import team.mino.core.common.android.architecture.UiState
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomNameValidation
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.roomform.form.model.RoomFormDialog
import team.mino.feature.roomform.form.model.RoomFormMode

/**
 * 폼이 편집하는 세 입력값.
 *
 * 현재 값과 진입 시점 스냅샷이 같은 타입이라 값 비교만으로 변경 여부가 판정된다 —
 * 고쳤다가 원래대로 되돌린 폼은 "변경 없음"으로 돌아온다.
 *
 * @property color `null`은 "고르지 않음"이다. 미선택을 회색으로 채워 두지 않는다 —
 *  그러면 고르지 않은 폼과 회색을 고른 폼이 같아진다.
 */
@Immutable
internal data class RoomFormValues(
    val name: String = "",
    val description: String = "",
    val color: RoomColor? = null,
)

/**
 * 공동방 생성·편집 폼의 상태.
 *
 * 화면이 그리려고 계산하는 값(CTA 활성·이탈 확인 필요 여부)은 필드가 아니라 아래의 파생 프로퍼티다.
 * 필드로 두면 [values]가 바뀔 때 함께 갱신하는 것을 빠뜨려 두 출처가 갈린다.
 *
 * @property initial 편집 진입 시점의 스냅샷. 생성 진입에는 비교 대상이 없어 `null`이다.
 * @property nameValidation 필드의 오류 표시와 CTA를 가른다. 미리보기가 읽는 [values]에는 관여하지 않는다.
 * @property loadError 편집 진입 조회 실패. 채워지면 화면 전체가 재시도 가능한 오류로 바뀐다.
 * @property dialog 확인 모달의 단일 슬롯. 종류별 플래그를 나열하지 않아 둘 이상이 동시에 뜰 수 없다.
 */
@Immutable
internal data class RoomFormUiState(
    val mode: RoomFormMode = RoomFormMode.Create,
    val isOnboarding: Boolean = false,
    val values: RoomFormValues = RoomFormValues(),
    val initial: RoomFormValues? = null,
    val nameValidation: RoomNameValidation = RoomNameValidation.Blank,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadError: MinoDomainException? = null,
    val dialog: RoomFormDialog? = null,
) : UiState {
    /** 방 설명과 대표 색상은 선택 입력이라 이름만 유효하면 저장할 수 있다. */
    val canSubmit: Boolean
        get() = nameValidation is RoomNameValidation.Valid && !isSubmitting

    /** 나가도 잃을 것이 없는 폼. 세 입력이 모두 손대지 않은 상태다. */
    val isBlankForm: Boolean
        get() = values.name.isEmpty() && values.description.isEmpty() && values.color == null

    /** 편집 진입에서만 성립한다. 되돌린 값은 다시 같아지므로 변경으로 세지 않는다. */
    val isChanged: Boolean
        get() = initial != null && initial != values

    /** 나갈 때 확인을 받아야 하는지. 생성은 채운 것이, 편집은 바꾼 것이 기준이다. */
    val needsExitConfirm: Boolean
        get() = when (mode) {
            RoomFormMode.Create -> !isBlankForm
            is RoomFormMode.Edit -> isChanged
        }
}
