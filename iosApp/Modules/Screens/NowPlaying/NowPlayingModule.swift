import Foundation
import ModuleLinker
import Resolver
import SwiftUI

public final class NowPlayingModule: Module {
	public static var shared = NowPlayingModule()
	
	public func registerAllServices() {
		Resolver.register {
			self as NowPlayingViewProviding
		}
		
		Resolver.register {
			self as MiniNowPlayingViewProviding
		}
	}
}

extension NowPlayingModule: NowPlayingViewProviding {
	public func nowPlayingView() -> AnyView {
		NowPlayingView().erased
	}
}

extension NowPlayingModule: MiniNowPlayingViewProviding {
	@MainActor
	public func miniNowPlayingView() -> AnyView {
		MiniPlayerView().erased
	}
}

#if DEBUG
extension NowPlayingModule {
	public func registerAllMockedServices(mockResolver: Resolver) {
	}
}
#endif
