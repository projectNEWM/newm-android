package io.newm.shared.internal.implementations

import io.newm.shared.internal.db.PreferencesDataStore
import io.newm.shared.internal.implementations.utilities.mapErrors
import io.newm.shared.internal.implementations.utilities.mapErrorsSuspend
import io.newm.shared.internal.repositories.LogInRepository
import io.newm.shared.internal.repositories.models.OAuthData
import io.newm.shared.public.models.error.KMMException
import io.newm.shared.public.usecases.LoginUseCase
import kotlin.coroutines.cancellation.CancellationException

internal class LoginUseCaseImpl(
    private val repository: LogInRepository,
    private val dataStore: PreferencesDataStore
) : LoginUseCase {
    @Throws(KMMException::class, CancellationException::class)
    override suspend fun logIn(email: String, password: String, humanVerificationCode: String) {
        return mapErrorsSuspend {
            return@mapErrorsSuspend repository.logIn(email.trim(), password.trim(), humanVerificationCode)
        }
    }

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun logInWithGoogle(idToken: String, humanVerificationCode: String) {
        return mapErrorsSuspend {
            return@mapErrorsSuspend repository.oAuthLogin(OAuthData.Google(idToken), humanVerificationCode)
        }
    }

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun logInWithFacebook(accessToken: String) {
        return mapErrorsSuspend {
            return@mapErrorsSuspend repository.oAuthLogin(OAuthData.Facebook(accessToken), "")
        }
    }

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun logInWithLinkedIn(accessToken: String) {
        return mapErrorsSuspend {
            return@mapErrorsSuspend repository.oAuthLogin(OAuthData.LinkedIn(accessToken), "")
        }
    }

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun logInWithApple(idToken: String, humanVerificationCode: String) {
        return mapErrorsSuspend {
            return@mapErrorsSuspend repository.oAuthLogin(OAuthData.Apple(idToken), humanVerificationCode)
        }
    }

    @Throws(KMMException::class, CancellationException::class)
    override fun logout() {
        mapErrors {
            repository.logout()
            dataStore.clearAll()
        }
        fun crashApp() {
            crashApp()
        }
        crashApp()
    }
}
