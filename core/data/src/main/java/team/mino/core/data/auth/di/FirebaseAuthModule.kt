package team.mino.core.data.auth.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FirebaseAuthModule {
    // 기본 FirebaseApp은 :app이 처리한 google-services.json 산출물로 초기화된다.
    // 라이브러리 모듈은 아티팩트만 의존하므로 여기서 초기화를 수행하지 않는다.
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
