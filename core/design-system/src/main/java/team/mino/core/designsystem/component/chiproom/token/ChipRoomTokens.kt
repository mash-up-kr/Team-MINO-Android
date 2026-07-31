package team.mino.core.designsystem.component.chiproom.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Chip Room 컴포넌트 슬롯 → 크기 매핑. Figma `Chip_Room`(15852:88538) 기준.
 */
internal object ChipRoomTokens {
    val ContentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp)

    /** 정렬 버튼·카테고리 칩 리스트 사이 간격. */
    val SortCategorySpacing = 12.dp

    /**
     * 정렬 버튼 콘텐츠 패딩. `Button/Button` Medium 기본값(수평 20dp)을 이 인스턴스에서
     * 수평 16dp로 덮어쓴 Figma 실측값이다(15852:88559).
     */
    val SortButtonContentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)

    /**
     * 정렬 드롭다운(`Menu/Menu`, 15852:88543) 고정 너비. `MinoMenu`는 `widthIn(min=140.dp)`만
     * 걸어 두고 실제 너비는 호출부가 정하는 컴포넌트라, 셀이 `MinoMenuItem`의 `fillMaxWidth()`를
     * 통해 상위에서 내려오는 제약을 그대로 채운다 — 너비를 명시하지 않으면 화면(또는 Popup의
     * 기본 최대 너비)까지 넓어진다. 여기서는 Figma가 고정한 140dp를 그대로 강제한다.
     */
    val SortDropdownWidth = 140.dp
}
