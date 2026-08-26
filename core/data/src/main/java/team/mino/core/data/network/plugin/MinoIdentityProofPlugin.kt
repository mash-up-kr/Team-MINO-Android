package team.mino.core.data.network.plugin

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import team.mino.core.data.BuildConfig
import team.mino.core.data.auth.IdTokenProvider

/**
 * Mino 서버로 나가는 요청에만 신원 증명을 `Authorization: Bearer`로 싣는다.
 *
 * 계약 A-1~A-6은 docs/specs/anonymous-auth-session/contracts/identity-proof-attachment.md §3이 소유한다.
 * 신원 증명을 캐시하지 않고 매 요청 시점에 얻는다 (research.md R-010).
 *
 * A-2의 리다이렉트 갈래는 Ktor의 `HttpRedirect`가 담당한다 — 그쪽이 authority(host:port)가 바뀌는 리다이렉트에서
 * `Authorization`을 떼어내므로 토큰이 다른 host로 따라가지 않는다. 이 플러그인이 다시 판정하지 않는 이유이며,
 * `followRedirects`를 끄거나 `HttpRedirect`를 걷어내면 그 보증이 사라진다.
 */
internal fun minoIdentityProofPlugin(idTokenProvider: IdTokenProvider): ClientPlugin<Unit> =
    createClientPlugin(name = PLUGIN_NAME) {
        // 요청 파이프라인 단계에서 판정한다 — A-3 위반 시 엔진에 요청이 도달해선 안 된다.
        onRequest { request, _ ->
            if (request.url.host != minoHost) return@onRequest

            val identityProof =
                checkNotNull(idTokenProvider.getIdToken()) {
                    // 호출자 계약 C-1(세션 확보 후 요청) 위반이므로 도메인 예외로 감싸지 않는다 (A-3).
                    "Mino 서버 요청에 실을 신원 증명이 없다. 세션을 확보한 뒤 요청해야 한다."
                }
            request.headers.append(HttpHeaders.Authorization, "Bearer $identityProof")
        }
    }

private const val PLUGIN_NAME = "MinoIdentityProof"

/** 판정 기준은 host 하나다 — scheme·port·path는 보지 않는다 (research.md R-009). */
private val minoHost: String = Url(BuildConfig.API_BASE_URL).host
