import Foundation
import Sentry
import ModuleLinker
import shared

class SentryErrorReporter: ErrorReporting {
	func logError(_ error: String) {
		print("ERROR: \(error)")
#if !DEBUG
		SentrySDK.capture(error: error)
#endif
	}
	
	func logError(_ error: Error) {
		print("ERROR: \(error.kmmException?.description() ?? error)")
#if !DEBUG
		SentrySDK.capture(error: error.kmmException ?? error)
#endif
	}
}

extension String: Error {}
extension KMMException: Error {}

extension Error {
	var kmmException: KMMException? { (self as NSError).kotlinException as? KMMException }
}
