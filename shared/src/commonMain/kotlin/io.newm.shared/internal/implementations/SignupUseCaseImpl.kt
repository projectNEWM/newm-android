package io.newm.shared.internal.implementations

import io.newm.shared.login.models.NewUser
import io.newm.shared.login.repository.LogInRepository
import io.newm.shared.public.models.error.KMMException
import io.newm.shared.public.usecases.SignupUseCase
import kotlin.coroutines.cancellation.CancellationException

internal class SignupUseCaseImpl(private val repository: LogInRepository) : SignupUseCase {

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun requestEmailConfirmationCode(email: String) {
        return repository.requestEmailConfirmationCode(email)
    }

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun registerUser(
        nickname: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        verificationCode: String
    ) {
        val newUser = NewUser(
            nickname = nickname,
            email = email,
            newPassword = password,
            confirmPassword = passwordConfirmation,
            authCode = verificationCode
        )
        repository.registerUser(newUser)
    }
}