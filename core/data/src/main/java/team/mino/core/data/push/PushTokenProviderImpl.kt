package team.mino.core.data.push

import com.google.firebase.messaging.FirebaseMessaging
import team.mino.core.data.push.extension.awaitDomain
import javax.inject.Inject

internal class PushTokenProviderImpl @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
) : PushTokenProvider {
    override suspend fun currentToken(): String = firebaseMessaging.token.awaitDomain()
}
