import Foundation
import shared
import Resolver
import ModuleLinker

public struct NEWMError: LocalizedError {
	public var errorDescription: String?
	public var failureReason: String?
	public var recoverySuggestion: String?
	public var underlyingError: Error?
	
	public init(errorDescription: String? = nil, failureReason: String? = nil, recoverySuggestion: String? = nil, underlyingError: Error? = nil) {
		self.errorDescription = errorDescription
		self.failureReason = failureReason
		self.recoverySuggestion = recoverySuggestion
		self.underlyingError = underlyingError
	}
}

public extension Error {
	var newmError: NEWMError {
		NEWMError(errorDescription: localizedDescription, underlyingError: self)
	}
}

public extension String {
	var newmError: NEWMError {
		NEWMError(errorDescription: self)
	}
}

public extension KMMException {
	var newmError: NEWMError {
		NEWMError(errorDescription: message, underlyingError: self as? Error)
	}
}

public struct ErrorSet {
	private var errors: [NEWMError] = []
	
	public var currentError: NEWMError? {
		errors.first
	}
	
	public var hasError: Bool {
		currentError != nil
	}
	
	public init() {}
	
	mutating
	public func append(_ error: NEWMError) {
		errors.append(error)
	}
	
	mutating
	public func append(_ error: Error) {
		errors.append(NEWMError(errorDescription: "\(error.localizedDescription)", failureReason: nil, recoverySuggestion: nil, underlyingError: error))
	}
	
	mutating
	public func popFirstError() {
		guard errors.count > 0 else { return Resolver.resolve(ErrorReporting.self).logError("No error to pop") }
		errors.removeFirst()
	}
}
