import Foundation

public protocol ErrorReporting: Sendable {
	func logError(_ error: String)
	func logError(_ error: Error)
	func logBreadcrumb(_ crumb: String, level: LoggingLevel)
}

public enum LoggingLevel {
	case fatal
	case error
	case warning
	case info
	case debug
}
