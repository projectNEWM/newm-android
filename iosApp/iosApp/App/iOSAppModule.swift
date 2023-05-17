import Foundation
import Resolver
import ModuleLinker
import Main
//import Wallet
import Home
import SharedUI
import SwiftUI
import Fonts
import Login
import NowPlaying
import PlaylistList
import Tips
import Artist
import Library
import AudioPlayer
//import Marketplace

struct iOSAppModule: ModuleProtocol {
	static var shared = iOSAppModule()
	
	let modules: [ModuleProtocol] = {
		[
//			WalletModule.shared,
			HomeModule.shared,
            LibraryModule.shared,
			SharedUIModule.shared,
			MainModule.shared,
			FontsModule.shared,
			LoginModule.shared,
			NowPlayingModule.shared,
			PlaylistModule.shared,
			TipsModule.shared,
			ArtistModule.shared,
			AudioPlayerModule.shared,
//			MarketplaceModule.shared
		]
	}()
	
	func registerAllServices() {
		modules.forEach { $0.registerAllServices() }
	}
}

extension Resolver: ResolverRegistering {
	public static func registerAllServices() {
		iOSAppModule.shared.modules.forEach { $0.registerAllServices() }
#if DEBUG
		iOSAppModule.shared.registerAllMockedServices(mockResolver: .mock)
#endif
	}
}

#if DEBUG
extension iOSAppModule {
	func registerAllMockedServices(mockResolver: Resolver) {
		Resolver.root.add(child: .mock)
		modules.forEach { $0.registerAllMockedServices(mockResolver: .mock) }
	}
}
#endif
