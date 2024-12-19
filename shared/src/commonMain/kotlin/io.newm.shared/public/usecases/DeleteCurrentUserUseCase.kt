package io.newm.shared.public.usecases

import io.newm.shared.public.models.error.KMMException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

interface DeleteCurrentUserUseCase {

    @Throws(KMMException::class, CancellationException::class)
    suspend fun delete()
}

class DeleteCurrentUserUseCaseProvider : KoinComponent {
    private val deleteCurrentUserUseCase: DeleteCurrentUserUseCase by inject()

    fun get(): DeleteCurrentUserUseCase {
        return this.deleteCurrentUserUseCase
    }
}
