package team.mino.core.data.datasource

/**
 * 프로필 로컬 캐시의 표현. 서버 계약이 아니라 DataStore에 담긴 두 값을 그대로 든다.
 *
 * [avatarName]은 `ProfileAvatar`의 이름이지 서버 문자열이 아니다 — 캐시가 서버 표현을 들면
 * 서버가 표현을 바꿀 때 고칠 곳이 매퍼 밖으로 하나 더 생긴다(`docs/specs/profile/data-model.md` §3).
 * 도메인 `Profile`과의 변환은 `repository/mapper/ProfileMapper`가 맡고, 경계는 `ProfileRepositoryImpl`이다.
 */
internal data class ProfileEntry(
    val nickname: String,
    val avatarName: String,
)
