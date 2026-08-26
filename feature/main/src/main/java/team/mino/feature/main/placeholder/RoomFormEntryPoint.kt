package team.mino.feature.main.placeholder

/**
 * 방 폼(`:feature:roomform`)을 눌러 볼 임시 검증 진입점의 조작과 결과를 셸 계층에 관통시키는 묶음.
 *
 * 진입점 feature가 아직 하나도 없어 폼을 여는 경로가 저장소 어디에도 없기 때문에 둔다
 * (→ `docs/specs/group-room-form/plan.md` §범위 경계). 실제 진입점 feature가 생기면
 * 이 파일과 [team.mino.feature.main.placeholder.screen.RoomFormEntryPlaceholderScreen],
 * 그리고 이 타입을 관통시키는 파라미터를 함께 걷어낸다.
 *
 * 결과는 표시만 한다 — 도착점 이동과 완료 스낵바는 진입점 feature의 몫이다(같은 문서).
 */
internal data class RoomFormEntryPoint(
    /** 폼이 돌려준 마지막 결과. 아직 한 번도 돌아오지 않았으면 `null`이다. */
    val lastResult: String?,
    val onCreate: () -> Unit,
    val onCreateWithOnboarding: () -> Unit,
    val onEditSeedRoom: () -> Unit,
)
