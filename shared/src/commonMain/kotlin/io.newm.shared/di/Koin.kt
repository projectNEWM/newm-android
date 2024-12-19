package io.newm.shared.di

import io.ktor.client.engine.HttpClientEngine
import io.newm.shared.NewmAppLogger
import io.newm.shared.config.NewmSharedBuildConfig
import io.newm.shared.config.NewmSharedBuildConfigImpl
import io.newm.shared.internal.TokenManager
import io.newm.shared.internal.api.CardanoWalletAPI
import io.newm.shared.internal.api.GenresAPI
import io.newm.shared.internal.api.LoginAPI
import io.newm.shared.internal.api.NEWMWalletConnectionAPI
import io.newm.shared.internal.api.PlaylistAPI
import io.newm.shared.internal.api.RemoteConfigAPI
import io.newm.shared.internal.api.UserAPI
import io.newm.shared.internal.implementations.ChangePasswordUseCaseImpl
import io.newm.shared.internal.implementations.ConnectWalletUseCaseImpl
import io.newm.shared.internal.implementations.DeleteCurrentUserUseCaseImpl
import io.newm.shared.internal.implementations.DisconnectWalletUseCaseImpl
import io.newm.shared.internal.implementations.ForceAppUpdateUseCaseImpl
import io.newm.shared.internal.implementations.GetGenresUseCaseImpl
import io.newm.shared.internal.implementations.GetWalletConnectionsUseCaseImpl
import io.newm.shared.internal.implementations.HasWalletConnectionsUseCaseImpl
import io.newm.shared.internal.implementations.LoginUseCaseImpl
import io.newm.shared.internal.implementations.ResetPasswordUseCaseImpl
import io.newm.shared.internal.implementations.SignupUseCaseImpl
import io.newm.shared.internal.implementations.SyncWalletConnectionsUseCaseImpl
import io.newm.shared.internal.implementations.UserDetailsUseCaseImpl
import io.newm.shared.internal.implementations.UserSessionUseCaseImpl
import io.newm.shared.internal.implementations.WalletNFTTracksUseCaseImpl
import io.newm.shared.internal.repositories.GenresRepository
import io.newm.shared.internal.repositories.LogInRepository
import io.newm.shared.internal.repositories.NFTRepository
import io.newm.shared.internal.repositories.PlaylistRepository
import io.newm.shared.internal.repositories.RemoteConfigRepository
import io.newm.shared.internal.repositories.RemoteConfigRepositoryImpl
import io.newm.shared.internal.repositories.UserRepository
import io.newm.shared.internal.repositories.WalletRepository
import io.newm.shared.internal.services.cache.NFTCacheService
import io.newm.shared.internal.services.cache.WalletConnectionCacheService
import io.newm.shared.internal.services.network.NFTNetworkService
import io.newm.shared.internal.services.network.WalletConnectionNetworkService
import io.newm.shared.internal.store.NftTrackStore
import io.newm.shared.public.analytics.NewmAppEventLogger
import io.newm.shared.public.usecases.ChangePasswordUseCase
import io.newm.shared.public.usecases.ConnectWalletUseCase
import io.newm.shared.public.usecases.DeleteCurrentUserUseCase
import io.newm.shared.public.usecases.DisconnectWalletUseCase
import io.newm.shared.public.usecases.ForceAppUpdateUseCase
import io.newm.shared.public.usecases.GetGenresUseCase
import io.newm.shared.public.usecases.GetWalletConnectionsUseCase
import io.newm.shared.public.usecases.HasWalletConnectionsUseCase
import io.newm.shared.public.usecases.LoginUseCase
import io.newm.shared.public.usecases.ResetPasswordUseCase
import io.newm.shared.public.usecases.SignupUseCase
import io.newm.shared.public.usecases.SyncWalletConnectionsUseCase
import io.newm.shared.public.usecases.UserDetailsUseCase
import io.newm.shared.public.usecases.UserSessionUseCase
import io.newm.shared.public.usecases.WalletNFTTracksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import shared.platformModule

fun initKoin(enableNetworkLogs: Boolean = true, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(enableNetworkLogs = enableNetworkLogs), platformModule())
    }

// called by iOS etc
//https://johnoreilly.dev/posts/kotlinmultiplatform-koin/
fun initKoin(enableNetworkLogs: Boolean) = initKoin(enableNetworkLogs = enableNetworkLogs) {}

fun commonModule(enableNetworkLogs: Boolean) = module {
    single { createJson() }
    single {
        createHttpClient(
            httpClientEngine = get(),
            json = get(),
            logInRepository = get(),
            tokenManager = get(),
            buildConfig = get(),
            enableNetworkLogs = enableNetworkLogs,
            appLogger = get()
        )
    }
    single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    single { createJson() }
    // Internal Configurations
    single<NewmSharedBuildConfig> { NewmSharedBuildConfigImpl() }
    single { NewmAppLogger() }
    single { NewmAppEventLogger() }
    // Internal API Services
    single { CardanoWalletAPI(get()) }
    single { GenresAPI(get()) }
    single { LoginAPI(get(), get()) }
    single { NEWMWalletConnectionAPI(get()) }
    single { PlaylistAPI(get()) }
    single { RemoteConfigAPI(get()) }
    single { UserAPI(get(), get()) }
    // Internal Services
    single { WalletConnectionNetworkService(get()) }
    single { WalletConnectionCacheService(get()) }
    single { NFTNetworkService(get()) }
    single { NFTCacheService(get()) }
    single { NftTrackStore(get(), get()) }
    // Internal Repositories
    single { WalletRepository(get(), get(), get()) }
    single { NFTRepository(get()) }
    single { GenresRepository() }
    single { LogInRepository() }
    single { PlaylistRepository() }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get()) }
    single { UserRepository(get(), get(), get()) }
    // External Use Cases to be consumed outside of KMM
    single<ChangePasswordUseCase> { ChangePasswordUseCaseImpl(get()) }
    single<ConnectWalletUseCase> { ConnectWalletUseCaseImpl(get(), get()) }
    single<ForceAppUpdateUseCase> { ForceAppUpdateUseCaseImpl(get()) }
    single<GetGenresUseCase> { GetGenresUseCaseImpl(get()) }
    single<LoginUseCase> { LoginUseCaseImpl(get(), get()) }
    single<ResetPasswordUseCase> { ResetPasswordUseCaseImpl(get()) }
    single<SignupUseCase> { SignupUseCaseImpl(get()) }
    single<UserDetailsUseCase> { UserDetailsUseCaseImpl(get()) }
    single<GetGenresUseCase> { GetGenresUseCaseImpl(get()) }
    single<WalletNFTTracksUseCase> { WalletNFTTracksUseCaseImpl(get()) }
    single<ConnectWalletUseCase> { ConnectWalletUseCaseImpl(get(), get()) }
    single<UserSessionUseCase> { UserSessionUseCaseImpl(get()) }
    single<ConnectWalletUseCase> { ConnectWalletUseCaseImpl(get(), get()) }
    single<DisconnectWalletUseCase> { DisconnectWalletUseCaseImpl(get(), get()) }
    single<SyncWalletConnectionsUseCase> { SyncWalletConnectionsUseCaseImpl(get()) }
    single<GetWalletConnectionsUseCase> { GetWalletConnectionsUseCaseImpl(get()) }
    single<HasWalletConnectionsUseCase> { HasWalletConnectionsUseCaseImpl(get()) }
    single<DeleteCurrentUserUseCase> { DeleteCurrentUserUseCaseImpl(get(), get()) }
}

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun createHttpClient(
    httpClientEngine: HttpClientEngine,
    json: Json,
    logInRepository: LogInRepository,
    tokenManager: TokenManager,
    enableNetworkLogs: Boolean,
    buildConfig: NewmSharedBuildConfig,
    appLogger: NewmAppLogger
): NetworkClientFactory =
    NetworkClientFactory(
        httpClientEngine,
        json,
        logInRepository,
        tokenManager,
        enableNetworkLogs,
        buildConfig,
        appLogger
    )
