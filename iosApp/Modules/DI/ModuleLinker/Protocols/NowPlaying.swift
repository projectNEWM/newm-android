import Foundation
import SwiftUI

public protocol NowPlayingViewProviding {
	func nowPlayingView() -> AnyView
}

@MainActor
public protocol MiniNowPlayingViewProviding {
	func miniNowPlayingView() -> AnyView
}
