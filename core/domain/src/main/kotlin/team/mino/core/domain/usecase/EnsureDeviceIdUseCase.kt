package team.mino.core.domain.usecase

import team.mino.core.domain.repository.DeviceRepository
import javax.inject.Inject

class EnsureDeviceIdUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke() = deviceRepository.ensureDeviceId()
}
