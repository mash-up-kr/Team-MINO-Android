package team.mino.feature.roomform.form.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import team.mino.feature.roomform.R

/**
 * 화면이 동시에 하나만 띄우는 확인 모달의 종류.
 *
 * 제목과 확인 버튼 라벨은 문자열 리소스로 두고, 컴포저블은 어느 모달인지 모른 채 그 둘만 받는다.
 *
 * 그 두 리소스를 종류마다 스스로 들고 있어, 화면이 종류별 분기를 나열하지 않는다. 이탈 2종은 확인 라벨이
 * 같지만 제목이 갈리는데, 제목을 각 구현이 직접 선언하므로 편집 폼에서 생성 문구가 뜨는 일은 여전히 없다.
 */
@Immutable
internal sealed interface RoomFormDialog {
    @get:StringRes
    val titleRes: Int

    @get:StringRes
    val confirmLabelRes: Int

    /** 생성 경로에서 CTA를 눌렀을 때의 저장 확인. 편집 경로는 이 모달을 거치지 않는다. */
    data object Save : RoomFormDialog {
        override val titleRes: Int = R.string.roomform_dialog_save_title
        override val confirmLabelRes: Int = R.string.roomform_dialog_save_confirm
    }

    /** 생성 폼에 잃을 입력이 남은 채 나갈 때. */
    data object ExitCreate : RoomFormDialog {
        override val titleRes: Int = R.string.roomform_dialog_exit_create_title
        override val confirmLabelRes: Int = R.string.roomform_dialog_exit_confirm
    }

    /** 편집 폼의 값이 진입 시점과 달라진 채 나갈 때. */
    data object ExitEdit : RoomFormDialog {
        override val titleRes: Int = R.string.roomform_dialog_exit_edit_title
        override val confirmLabelRes: Int = R.string.roomform_dialog_exit_confirm
    }
}
