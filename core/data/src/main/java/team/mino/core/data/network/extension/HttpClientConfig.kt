package team.mino.core.data.network.extension

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

internal fun HttpClientConfig<*>.convertDomainException() {
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            throw when (exception) {
                is ResponseException ->
                    MinoDomainException.Http(
                        code = exception.response.status.value,
                        cause = exception,
                    )
                is IOException -> MinoDomainException.Network(exception)
                // CancellationException은 여기로 와 원본 그대로 rethrow되어야 코루틴 취소가 보존된다
                // — 상위 타입(RuntimeException 등) 분기를 추가해 위에서 가로채지 말 것
                else -> exception
            }
        }
    }
}
