package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.CommentRemoteDataSource
import team.mino.core.data.datasource.CommentRemoteDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CommentDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindCommentRemoteDataSource(impl: CommentRemoteDataSourceImpl): CommentRemoteDataSource
}
