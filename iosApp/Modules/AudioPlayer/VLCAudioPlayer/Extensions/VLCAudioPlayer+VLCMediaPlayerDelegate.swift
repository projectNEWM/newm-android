import Foundation
import VLCKitSPM

class VLCAudioPlayerDelegate: NSObject, VLCMediaPlayerDelegate {
	var stream: AsyncStream<Void> {
		AsyncStream { [weak self] continuation in
			self?.continuation = continuation
		}
	}

	private var continuation: AsyncStream<Void>.Continuation!
	private var lastYieldTime: Date?
	private let throttleInterval: TimeInterval = 1.0// Throttle interval in seconds
	
	func mediaPlayerStateChanged(_ aNotification: Foundation.Notification) {
		continuation.yield()
	}
	
	func mediaPlayerTimeChanged(_ aNotification: Foundation.Notification) {
		// Throttle timeChanged calls based on the last yield time
		let now = Date()
		
		if let lastTime = lastYieldTime, now.timeIntervalSince(lastTime) < throttleInterval {
			return // Skip this event if it's within the throttle interval
		}
		
		lastYieldTime = now
		continuation.yield()
	}
	
	func mediaPlayerTitleChanged(_ aNotification: Foundation.Notification) {
		continuation.yield()
	}
	
	func mediaPlayerChapterChanged(_ aNotification: Foundation.Notification) {
		continuation.yield()
	}
	
	deinit {
		continuation.finish()
	}
}
