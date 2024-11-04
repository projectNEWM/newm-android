import Foundation
import VLCKitSPM

public extension VLCTime {
	var seconds: Double? {
		value.flatMap { $0.doubleValue / 1_000 }
	}
}

extension Double {
	var secondsToMilliseconds: Int32 {
		Int32(self * 1000.0)
	}
}
