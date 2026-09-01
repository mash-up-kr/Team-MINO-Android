package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import team.mino.core.data.network.dto.response.AvatarResponse
import team.mino.core.data.network.dto.response.ProfileResponse
import team.mino.core.domain.model.Profile
import team.mino.core.domain.model.ProfileAvatar

/**
 * 아바타 13종(선택 12종 + 기본 1종)과 서버 색 문자열의 대응을 양방향으로 고정한다.
 *
 * 표의 단일 출처는 `docs/specs/profile/contracts/profile-api-contract.md` §2「아바타 값 표」이며,
 * 아래 기대값은 그 표를 **손으로 옮겨 적은 리터럴**이다. 대응이 `ProfileAvatar`·`RoomColor`의 선언 순서와
 * 어긋나므로(`Person4`·`Person5`·`Person6`이 `green`·`purple`·`lime`), `ordinal`이나 `entries.zip(...)`으로
 * 기대값을 만들면 구현과 같은 방식으로 틀려 테스트가 자기 자신을 증명한다
 * (`docs/specs/profile/research.md` D44). 그래서 루프를 쓰지 않고 13종을 한 줄씩 적는다.
 *
 * 기본 아바타는 목록의 마지막 항목인 [ProfileAvatar.Basic]이고 서버 `gray`에 대응한다. **`gray`는 이제
 * 보내지도 않는 값이 아니라 표의 한 행이다** — 아바타를 고르지 않고 저장한 프로필이 이 값으로 나간다
 * (`docs/specs/profile/spec.md` FR-015 · EC-002 · research D53). 모르는 문자열과 `avatar == null`도 같은 항목으로 모인다
 * (API 계약 §2 협의 항목 ⑥).
 */
class ProfileMapperTest {
    @Test
    fun `아바타 13종이 표에 적힌 서버 색 문자열로 나간다`() {
        assertEquals("red", colorSentFor(ProfileAvatar.Person1))
        assertEquals("red_orange", colorSentFor(ProfileAvatar.Person2))
        assertEquals("orange", colorSentFor(ProfileAvatar.Person3))
        assertEquals("green", colorSentFor(ProfileAvatar.Person4))
        assertEquals("purple", colorSentFor(ProfileAvatar.Person5))
        assertEquals("lime", colorSentFor(ProfileAvatar.Person6))
        assertEquals("cyan", colorSentFor(ProfileAvatar.Person7))
        assertEquals("pink", colorSentFor(ProfileAvatar.Person8))
        assertEquals("blue", colorSentFor(ProfileAvatar.Person9))
        assertEquals("brown", colorSentFor(ProfileAvatar.Person10))
        assertEquals("light_blue", colorSentFor(ProfileAvatar.Person11))
        assertEquals("violet", colorSentFor(ProfileAvatar.Person12))
        assertEquals("gray", colorSentFor(ProfileAvatar.Basic))
    }

    @Test
    fun `표에 적힌 서버 색 문자열 13종이 대응하는 아바타로 읽힌다`() {
        assertEquals(ProfileAvatar.Person1, avatarReadFrom("red"))
        assertEquals(ProfileAvatar.Person2, avatarReadFrom("red_orange"))
        assertEquals(ProfileAvatar.Person3, avatarReadFrom("orange"))
        assertEquals(ProfileAvatar.Person4, avatarReadFrom("green"))
        assertEquals(ProfileAvatar.Person5, avatarReadFrom("purple"))
        assertEquals(ProfileAvatar.Person6, avatarReadFrom("lime"))
        assertEquals(ProfileAvatar.Person7, avatarReadFrom("cyan"))
        assertEquals(ProfileAvatar.Person8, avatarReadFrom("pink"))
        assertEquals(ProfileAvatar.Person9, avatarReadFrom("blue"))
        assertEquals(ProfileAvatar.Person10, avatarReadFrom("brown"))
        assertEquals(ProfileAvatar.Person11, avatarReadFrom("light_blue"))
        assertEquals(ProfileAvatar.Person12, avatarReadFrom("violet"))
        assertEquals(ProfileAvatar.Basic, avatarReadFrom("gray"))
    }

    /**
     * 위 왕복 테스트의 `gray` 줄은 **폴백에 가려 실패할 수 없다** — `toDomain()`이 모르는 색을
     * [ProfileAvatar.Default]로 메우고 그 값이 곧 [ProfileAvatar.Basic]이라, 표에서 `gray` 행을 지워도
     * 통과한다. `gray`가 폴백이 아니라 **표의 한 행**이라는 이번 개정의 주장은 폴백을 우회해야 고정된다.
     */
    @Test
    fun `gray는 폴백이 아니라 표에 실제로 있는 행이다`() {
        assertEquals(ProfileAvatar.Basic, AvatarResponse(color = "gray").toProfileAvatarOrNull())
        assertNull(AvatarResponse(color = "chartreuse").toProfileAvatarOrNull())
    }

    /**
     * 짝을 한 줄씩 적은 위 두 테스트는 지금 있는 13종만 덮는다. 아바타가 늘었는데 표에 줄을 더하지 않으면
     * 그 항목은 어느 테스트에도 걸리지 않으므로, 표가 열거를 빠짐없이·중복 없이 덮는지를 따로 고정한다.
     * 여기서는 **어느 아바타가 어느 색인지를 판정하지 않는다** — 개수와 중복만 본다.
     */
    @Test
    fun `모든 아바타가 서로 다른 색 문자열을 갖는다`() {
        val colors = ProfileAvatar.entries.map(::colorSentFor)

        assertEquals(
            "표가 덮지 못한 아바타가 있거나 두 아바타가 같은 색을 보낸다",
            ProfileAvatar.entries.size,
            colors.toSet().size,
        )
    }

    /**
     * 서버가 아바타 색을 넓혔다는 이유로 프로필 조회가 실패하면 안 된다
     * (`docs/specs/profile/research.md` D37).
     */
    @Test
    fun `표에 없는 문자열은 기본 아바타로 읽는다`() {
        assertEquals(ProfileAvatar.Basic, avatarReadFrom("chartreuse"))
    }

    @Test
    fun `avatar가 null이면 기본 아바타로 읽는다`() {
        val response = profileResponse(avatar = null)

        assertEquals(ProfileAvatar.Basic, response.toDomain().avatar)
    }

    @Test
    fun `닉네임은 응답에서 그대로 읽힌다`() {
        val response = profileResponse(nickname = "꾹이", avatar = AvatarResponse(color = "red"))

        assertEquals("꾹이", response.toDomain().nickname)
    }

    @Test
    fun `닉네임은 요청에 그대로 실린다`() {
        val request = Profile(nickname = "꾹이", avatar = ProfileAvatar.Person1).toRequest()

        assertEquals("꾹이", request.nickname)
    }

    private fun colorSentFor(avatar: ProfileAvatar): String =
        Profile(nickname = NICKNAME, avatar = avatar)
            .toRequest()
            .avatar
            .color

    private fun avatarReadFrom(color: String): ProfileAvatar =
        profileResponse(avatar = AvatarResponse(color = color))
            .toDomain()
            .avatar

    private fun profileResponse(
        nickname: String = NICKNAME,
        avatar: AvatarResponse?,
    ): ProfileResponse =
        ProfileResponse(
            id = "3f0b0d8e-6d1a-4d2f-9c1e-2b7a5c9f0f11",
            nickname = nickname,
            avatar = avatar,
            createdAt = "2026-08-28T01:12:44+09:00",
        )

    private companion object {
        const val NICKNAME = "미노"
    }
}
