package team.mino.core.errorhandling

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object UncaughtErrorHandler {
    private val channel = Channel<Throwable>(Channel.BUFFERED)

    val errors: Flow<Throwable> = channel.receiveAsFlow()

    fun dispatch(throwable: Throwable) {
        channel.trySend(throwable)
    }
}
