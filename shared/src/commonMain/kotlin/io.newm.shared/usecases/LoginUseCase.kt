package io.newm.shared.usecases

import io.newm.shared.login.repository.KMMException
import io.newm.shared.login.repository.LogInRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

interface LoginUseCase {
    @Throws(KMMException::class, CancellationException::class)
    suspend fun logIn(email: String, password: String)
    fun logOut()
    val userIsLoggedIn: Boolean
}

internal class LoginUseCaseImpl(private val repository: LogInRepository) : LoginUseCase {
    @Throws(KMMException::class, CancellationException::class)
    override suspend fun logIn(email: String, password: String) {
        return repository.logIn(email = email, password = password)
    }

    override fun logOut() {
        repository.logOut()
    }

    override val userIsLoggedIn: Boolean
        get() = repository.userIsLoggedIn()
}

class LoginUseCaseProvider(): KoinComponent {
    private val loginUseCase: LoginUseCase by inject()

    fun get(): LoginUseCase {
        return this.loginUseCase
    }
}