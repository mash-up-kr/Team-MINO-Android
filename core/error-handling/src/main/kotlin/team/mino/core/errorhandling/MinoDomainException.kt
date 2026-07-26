package team.mino.core.errorhandling

sealed class MinoDomainException(
    message: String? = null,
    cause: Throwable,
) : RuntimeException(message, cause) {
    class Network(cause: Throwable) : MinoDomainException(cause = cause)

    class Http(val code: Int, cause: Throwable) : MinoDomainException(cause = cause)
}
