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
		guard reportError(error) else { return }
		print("ERROR: \(error.kmmException?.description() ?? error)")
		SentrySDK.capture(error: error.kmmException ?? error)
#endif
	}
	
	private func reportError(_ error: Error) -> Bool {
		if let error = error as? KMMException {
			return switch error.message {
			case "Invalid login.  Please try again.": false
			default: true
			}
		}
		return true
	}
	
	func logBreadcrumb(_ crumb: String, level: LoggingLevel) {
#if !DEBUG
		let breadcrumb = Breadcrumb()
		breadcrumb.level = level.sentryLevel
		SentrySDK.addBreadcrumb(breadcrumb)
#endif
	}
}

extension String: Error {}
extension KMMException: Error {}

extension Error {
	var kmmException: KMMException? { (self as NSError).kotlinException as? KMMException }
}

extension LoggingLevel {
	var sentryLevel: SentryLevel {
		switch self {
		case .debug: return .debug
		case .info: return .info
		case .warning: return .warning
		case .error: return .error
		case .fatal: return .fatal
		}
	}
}
