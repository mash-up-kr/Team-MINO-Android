package team.mino.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.datasource.OnboardingProgressEntry
import team.mino.core.data.datasource.OnboardingProgressLocalDataSource
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep

/**
 * 저장된 원시 값(문자열·불리언)이 [OnboardingProgress]로 조립되는 경계다. 이 테스트가 보는 것은
 * DataStore가 아니라 **조립과 폴백**이라 로컬 저장은 Fake로 대체한다.
 *
 * 계약은 `docs/specs/onboarding-flow/contracts/onboarding-progress.md` §2, 키별 기본값은
 * `docs/specs/onboarding-flow/data-model.md` §4.1이 소유한다.
 */
class OnboardingProgressRepositoryImplTest {
    private val localDataSource = FakeOnboardingProgressLocalDataSource()
    private val repository = OnboardingProgressRepositoryImpl(localDataSource)

    /** 키가 하나도 저장된 적 없는 첫 설치. 네 필드 모두 모델의 기본값이다(data-model §4.1). */
    @Test
    fun `저장된 값이 없으면 기본값을 돌려준다`() =
        runTest {
            val progress = repository.getProgress()

            assertEquals(OnboardingStep.PROFILE, progress.lastStep)
            assertNull(progress.createdRoomId)
            assertFalse(progress.isCompleted)
            assertEquals(OnboardingProgress(), progress)
        }

    /** 저장 포맷이 `OnboardingStep.name`이라는 것까지 고정한다 — 이 문자열이 다음 실행의 입력이다. */
    @Test
    fun `기록한 스텝을 이름으로 저장하고 그대로 되읽는다`() =
        runTest {
            repository.setCurrentStep(OnboardingStep.ROOM_FORM)

            assertEquals("ROOM_FORM", localDataSource.entry.lastStepName)
            assertEquals(OnboardingStep.ROOM_FORM, repository.getProgress().lastStep)
        }

    /** 어느 스텝에서 중단해도 그 스텝으로 재개해야 하므로 네 값 전부가 왕복해야 한다. */
    @Test
    fun `모든 스텝이 왕복한다`() =
        runTest {
            OnboardingStep.entries.forEach { step ->
                repository.setCurrentStep(step)

                assertEquals(step, repository.getProgress().lastStep)
            }
        }

    @Test
    fun `기록한 공동방 id를 그대로 되읽는다`() =
        runTest {
            repository.setCreatedRoomId("room-1")

            assertEquals("room-1", repository.getProgress().createdRoomId)
        }

    @Test
    fun `기록한 초대 참여 방 id를 그대로 되읽는다`() =
        runTest {
            repository.setInvitedRoomId("room-9")

            assertEquals("room-9", repository.getProgress().invitedRoomId)
        }

    @Test
    fun `완료를 기록하면 완료로 읽힌다`() =
        runTest {
            assertFalse("사전 조건: 완료 표시가 서 있지 않다", repository.getProgress().isCompleted)

            repository.markCompleted()

            assertTrue(repository.getProgress().isCompleted)
        }

    /** 네 쓰기는 서로 다른 키다. 하나를 쓴다고 나머지가 덮이면 재개 지점이나 방 id를 잃는다. */
    @Test
    fun `네 쓰기가 서로를 덮지 않는다`() =
        runTest {
            repository.setCurrentStep(OnboardingStep.INVITE)
            repository.setCreatedRoomId("room-1")
            repository.setInvitedRoomId("room-9")
            repository.markCompleted()

            assertEquals(
                OnboardingProgress(
                    lastStep = OnboardingStep.INVITE,
                    createdRoomId = "room-1",
                    invitedRoomId = "room-9",
                    isCompleted = true,
                ),
                repository.getProgress(),
            )
        }

    /**
     * 낡거나 손상된 값이다. 던지지 않고 [OnboardingStep.PROFILE]로 떨어뜨린다 — 온보딩을 처음부터
     * 태우는 편이 홈으로 튕기는 것보다 안전하다(계약 §2 · SC-002).
     */
    @Test
    fun `알 수 없는 스텝 이름은 던지지 않고 PROFILE로 떨어진다`() =
        runTest {
            localDataSource.entry = OnboardingProgressEntry(
                lastStepName = "GARDEN",
                createdRoomId = "room-1",
                invitedRoomId = null,
                isCompleted = false,
            )

            val progress = repository.getProgress()

            assertEquals(OnboardingStep.PROFILE, progress.lastStep)
            assertEquals("스텝만 떨어질 뿐 나머지 값은 유지된다", "room-1", progress.createdRoomId)
        }

    /** 빈 문자열도 어느 스텝 이름과도 맞지 않는다. 같은 폴백을 탄다. */
    @Test
    fun `빈 스텝 이름도 PROFILE로 떨어진다`() =
        runTest {
            localDataSource.entry = OnboardingProgressEntry(
                lastStepName = "",
                createdRoomId = null,
                invitedRoomId = null,
                isCompleted = false,
            )

            assertEquals(OnboardingStep.PROFILE, repository.getProgress().lastStep)
        }

    /** 대소문자가 다른 값은 다른 값이다. 관대하게 해석하면 손상된 값을 정상으로 오인한다. */
    @Test
    fun `대소문자가 다른 스텝 이름은 PROFILE로 떨어진다`() =
        runTest {
            localDataSource.entry = OnboardingProgressEntry(
                lastStepName = "invite",
                createdRoomId = "room-1",
                invitedRoomId = null,
                isCompleted = false,
            )

            assertEquals(OnboardingStep.PROFILE, repository.getProgress().lastStep)
        }
}

/** 저장된 적 없는 키는 `null`, 완료 표시만 `false`로 채워지는 DataStore 쪽 규약을 그대로 흉내 낸다. */
private class FakeOnboardingProgressLocalDataSource : OnboardingProgressLocalDataSource {
    var entry =
        OnboardingProgressEntry(lastStepName = null, createdRoomId = null, invitedRoomId = null, isCompleted = false)

    override suspend fun getProgress(): OnboardingProgressEntry = entry

    override suspend fun setLastStepName(stepName: String) {
        entry = entry.copy(lastStepName = stepName)
    }

    override suspend fun setCreatedRoomId(roomId: String) {
        entry = entry.copy(createdRoomId = roomId)
    }

    override suspend fun setInvitedRoomId(roomId: String) {
        entry = entry.copy(invitedRoomId = roomId)
    }

    override suspend fun markCompleted() {
        entry = entry.copy(isCompleted = true)
    }
}
