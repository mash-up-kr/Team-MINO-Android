package team.mino.core.data.network.extension

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

internal fun HttpClientConfig<*>.convertDomainException() {
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            throw when (exception) {
                is CancellationException -> exception
                is ResponseException ->
                    MinoDomainException.Http(
                        code = exception.response.status.value,
                        cause = exception,
                    )
                is IOException -> MinoDomainException.Network(exception)
                else -> exception
            }
        }
    }
}
