@file:OptIn(ExperimentalCoroutinesApi::class)

package io.newm.shared.internal.implementations

import io.newm.shared.internal.implementations.utilities.mapErrorsSuspend
import io.newm.shared.internal.repositories.NFTRepository
import io.newm.shared.internal.repositories.WalletRepository
import io.newm.shared.public.models.error.KMMException
import io.newm.shared.public.usecases.DisconnectWalletUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent
import shared.Notification
import shared.postNotification
import kotlin.coroutines.cancellation.CancellationException

internal class DisconnectWalletUseCaseImpl(
    private val walletRepository: WalletRepository,
    private val nftRepository: NFTRepository,
) : DisconnectWalletUseCase, KoinComponent {

    @Throws(KMMException::class, CancellationException::class)
    override suspend fun disconnect(walletConnectionId: String?) {
        mapErrorsSuspend {
            if (walletConnectionId != null) {
                walletRepository.disconnectWallet(walletConnectionId)
            } else {
                val connections = walletRepository.syncWalletConnectionsFromNetworkToDB()

                coroutineScope {
                    connections.map { connection ->
                        async {
                            walletRepository.disconnectWallet(connection.id)
                        }
                    }.awaitAll()
                }

            }
            nftRepository.deleteAllTracksNFTsCache()
            postNotification(Notification.walletConnectionStateChanged)
        }
    }
}
