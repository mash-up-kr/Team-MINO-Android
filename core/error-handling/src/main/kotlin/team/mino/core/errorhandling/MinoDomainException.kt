package team.mino.core.errorhandling

sealed class MinoDomainException(
    message: String? = null,
    cause: Throwable,
) : RuntimeException(message, cause) {
    class Network(cause: Throwable) : MinoDomainException(cause = cause)

    class Http(val code: Int, cause: Throwable) : MinoDomainException(cause = cause)

    /** 연결은 됐으나 인증 제공자가 세션·신원 증명을 발급하지 못했다. */
    class Auth(cause: Throwable) : MinoDomainException(cause = cause)
}
