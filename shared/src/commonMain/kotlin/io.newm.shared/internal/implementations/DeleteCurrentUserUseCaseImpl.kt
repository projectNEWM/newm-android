package io.newm.shared.internal.implementations

import io.newm.shared.internal.repositories.UserRepository
import io.newm.shared.public.usecases.DeleteCurrentUserUseCase
import io.newm.shared.public.usecases.LoginUseCase
import org.koin.core.component.KoinComponent

internal class DeleteCurrentUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val logoutUseCase: LoginUseCase
): DeleteCurrentUserUseCase, KoinComponent {
    override suspend fun delete() {
        userRepository.deleteCurrentUser()
        logoutUseCase.logout()
    }
}