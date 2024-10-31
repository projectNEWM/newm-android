import Foundation
import Resolver
import ModuleLinker
import Main
import SharedUI
import SwiftUI
import Fonts
import Login
import NowPlaying
import Library
import AudioPlayer
import Files
import shared
import Logging
import Profile
import Mocks
import Analytics

#if DEBUG
enum MockMode {
	case use
	case fallback
	case dontUse
}
let mockMode: MockMode = .dontUse
#endif

struct iOSAppModule: Module {
	static var shared = iOSAppModule()
	
	private let modules: [Module] = {
		[
            LibraryModule.shared,
			SharedUIModule.shared,
			MainModule.shared,
			FontsModule.shared,
			LoginModule.shared,
			NowPlayingModule.shared,
			AudioPlayerModule.shared,
			FilesModule.shared,
			LoggingModule.shared,
			ProfileModule.shared,
			MocksModule.shared,
			AnalyticsModule.shared
		]
	}()
	
	init() {
		KoinKt.doInitKoin(enableNetworkLogs: true)
	}
}

extension Resolver: ResolverRegistering {
	public static func registerAllServices() {
		iOSAppModule.shared.registerAllServices()
#if DEBUG
		switch mockMode {
		case .use:
			Resolver.root = Resolver(child: .main)
			iOSAppModule.shared.registerAllMockedServices(mockResolver: .root)
		case .fallback:
			let mock = Resolver()
			Resolver.root.add(child: mock)
			iOSAppModule.shared.registerAllMockedServices(mockResolver: mock)
		case .dontUse:
			break
		}
#endif
	}
}

extension iOSAppModule {
	func registerAllServices() {
		Resolver.register {
			UserDetailsUseCaseProvider().get() as UserDetailsUseCase
		}
				
		modules.forEach { $0.registerAllServices() }
	}

#if DEBUG
	func registerAllMockedServices(mockResolver: Resolver) {
		modules.forEach { $0.registerAllMockedServices(mockResolver: mockResolver) }
	}
#endif
}
