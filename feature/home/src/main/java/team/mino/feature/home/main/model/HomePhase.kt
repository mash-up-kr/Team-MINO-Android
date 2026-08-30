package team.mino.feature.home.main.model

/**
 * 카드 자리에 무엇이 놓이는지. 상단(방 뱃지·인사 문구·정렬 칩)은 어느 값에서도 그대로 남는다.
 *
 * 가이드는 이 값과 직교한다 — 볼 카드가 하나도 없어도 가이드를 먼저 띄운다(spec EC-016).
 * 그래서 가이드 노출 여부는 여기 갈래로 들어오지 않고 `HomeUiState`가 따로 들고 있다.
 *
 * [AllExhausted]와 [Empty]를 한 갈래로 합치지 않는다. 둘은 서로 다른 화면이다(spec EC-011).
 */
internal enum class HomePhase {
    /** 첫 덱을 받아오는 중. */
    LOADING,

    /** 넘길 카드가 있다. */
    DECK,

    /** 볼 것이 있었고 전부 둘러봤다. 스스로 다시 순환하지 않는다(spec EC-010). */
    ALL_EXHAUSTED,

    /** 애초에 볼 수 있는 장소가 하나도 없었다. 여기에만 공동방 생성 CTA가 붙는다(spec FR-020). */
    EMPTY,

    /**
     * 주 데이터를 받아 오지 못했다. 무엇이 실패했는지는 `HomeUiState.loadError`가 들고,
     * 문구 매핑은 그리는 쪽이 한다(`docs/conventions/error_handling.md` §5 1행).
     *
     * 실패를 스낵바로만 흘리면 [LOADING]이 걷히지 않아 화면이 영영 로딩으로 남는다 — 그래서 갈래가 있다.
     */
    ERROR,
}
