package team.mino.core.data.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/**
 * JSON 본문 하나를 그대로 돌려주는 [MockEngine]. [onRequest]로 나간 요청을 붙잡을 수 있다.
 *
 * 엔진 조립을 테스트마다 다시 적으면 Ktor 설정이 바뀔 때 고칠 곳이 파일 수만큼 늘어난다.
 */
internal fun jsonEngine(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    onRequest: (HttpRequestData) -> Unit = {},
): MockEngine =
    MockEngine { request ->
        onRequest(request)
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }
