import Foundation
import Sentry
import ModuleLinker
import shared

class SentryErrorReporter: ErrorReporting {
	func logError(_ error: String) {
#if !DEBUG
		print("ERROR: \(error)")
		SentrySDK.capture(error: error)
#endif
	}
	
	func logError(_ error: Error) {
#if !DEBUG
		print("ERROR: \(error.kmmException?.description() ?? error)")
		SentrySDK.capture(error: error.kmmException ?? error)
#endif
	}
}

extension String: Error {}
extension KMMException: Error {}

extension Error {
	var kmmException: KMMException? { (self as NSError).kotlinException as? KMMException }
}
