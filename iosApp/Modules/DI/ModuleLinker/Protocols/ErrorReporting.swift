import Foundation

public protocol ErrorReporting: Sendable {
	func logError(_ error: String)
	func logError(_ error: Error)
}
