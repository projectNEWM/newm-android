package io.newm.shared.internal.repositories

import co.touchlab.kermit.Logger
import io.newm.shared.internal.services.cache.WalletConnectionCacheService
import io.newm.shared.internal.services.network.WalletConnectionNetworkService
import io.newm.shared.public.models.WalletConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent

internal class WalletRepository(
    private val networkService: WalletConnectionNetworkService,
    private val cacheService: WalletConnectionCacheService,
) {
    fun getWalletConnectionsCache(): Flow<List<WalletConnection>> =
        cacheService.getWalletConnections()

    suspend fun syncWalletConnectionsFromNetworkToDB(): List<WalletConnection> {
        return try {
            val connections = networkService.getWalletConnections()
            cacheService.cacheWalletConnections(connections)
            connections
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching wallet connections from network ${e.cause}" }
            throw e
        }
    }

    suspend fun connectWallet(newmCode: String): WalletConnection? {
        return try {
            val newConnection = networkService.connectWallet(newmCode.removePrefix("newm-"))
            cacheService.cacheWalletConnections(listOf(newConnection))
            newConnection
        } catch (e: Exception) {
//TODO: throw error, don't return false
            Logger.e(e) { "Error connecting wallet ${e.cause}" }
            null
        }
    }

    suspend fun disconnectWallet(walletConnectionId: String): Boolean {
        return try {
            val success = networkService.disconnectWallet(walletConnectionId)
            if (success) {
                cacheService.deleteAllWalletConnections(walletConnectionId)
            }
            success
        } catch (e: Exception) {
//TODO: throw error, don't return false
            Logger.e(e) { "Error disconnecting wallet ${e.cause}" }
            false
        }
    }
}