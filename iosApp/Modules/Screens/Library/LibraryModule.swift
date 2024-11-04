import Foundation
import Resolver
import ModuleLinker
import shared

public final class LibraryModule: Module {
	public static let shared = LibraryModule()
		
	public func registerAllServices() {
		Resolver.register {
			self as LibraryViewProviding
		}
		
		Resolver.register {
			WalletNFTTracksUseCaseProvider().get() as WalletNFTTracksUseCase
		}
		
		Resolver.register {
			ConnectWalletUseCaseProvider().get() as ConnectWalletUseCase
		}
		
		Resolver.register {
			HasWalletConnectionsUseCaseProvider().get() as HasWalletConnectionsUseCase
		}
		
		Resolver.register {
			DisconnectWalletUseCaseProvider().get() as DisconnectWalletUseCase
		}
		
		Resolver.register {
			SyncWalletConnectionsUseCaseProvider().get() as SyncWalletConnectionsUseCase
		}
    }
}

#if DEBUG
import AudioPlayer
import Mocks
extension LibraryModule {
    public func registerAllMockedServices(mockResolver: Resolver) {
		mockResolver.register {
			MockWalletNFTTracksUseCase() as WalletNFTTracksUseCase
		}
		
		mockResolver.register {
			MockConnectWalletUseCase() as ConnectWalletUseCase
		}
		
		mockResolver.register {
			MockErrorLogger() as ErrorReporting
		}
		
		MocksModule.shared.registerAllMockedServices(mockResolver: .mock)
		AudioPlayerModule.shared.registerAllServices()
	}
}
#endif
