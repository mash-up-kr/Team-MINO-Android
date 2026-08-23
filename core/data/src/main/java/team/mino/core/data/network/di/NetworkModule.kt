package team.mino.core.data.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import team.mino.core.data.BuildConfig
import team.mino.core.data.auth.IdTokenProvider
import team.mino.core.data.network.extension.convertDomainException
import team.mino.core.data.network.plugin.minoIdentityProofPlugin
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(idTokenProvider: IdTokenProvider): HttpClient =
        HttpClient(OkHttp) {
            expectSuccess = true
            convertDomainException()
            defaultRequest {
                url(BuildConfig.API_BASE_URL)
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                level = if (BuildConfig.FLAVOR == "qa") LogLevel.BODY else LogLevel.NONE
            }
            // defaultRequest가 Before 단계에서 base URL을 채운 뒤 이 플러그인의 host 판정이 돈다
            // — 상대 경로 호출도 Mino host로 확정된 상태에서 A-1 판정을 받는다.
            install(minoIdentityProofPlugin(idTokenProvider))
        }
}
