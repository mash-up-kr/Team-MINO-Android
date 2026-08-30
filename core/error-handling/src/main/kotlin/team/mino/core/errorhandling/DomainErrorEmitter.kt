package team.mino.core.errorhandling

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface DomainErrorEmitter {
    val domainErrors: Flow<MinoDomainException>

    fun emitDomainError(error: MinoDomainException)
}

fun domainErrorEmitter(): DomainErrorEmitter = DomainErrorEmitterImpl()

private class DomainErrorEmitterImpl : DomainErrorEmitter {
    private val channel = Channel<MinoDomainException>(Channel.BUFFERED)

    override val domainErrors: Flow<MinoDomainException> = channel.receiveAsFlow()

    override fun emitDomainError(error: MinoDomainException) {
        channel.trySend(error)
    }
}
