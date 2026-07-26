package team.mino.core.errorhandling

inline fun <T> runCatchingDomain(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: MinoDomainException) {
        Result.failure(e)
    }

inline fun <T> Result<T>.onDomainFailure(action: (MinoDomainException) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it as MinoDomainException) }
    return this
}
