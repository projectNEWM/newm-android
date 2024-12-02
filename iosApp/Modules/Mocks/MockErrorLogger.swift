import Foundation
import ModuleLinker

public class MockErrorLogger: ErrorReporting {
	public var errorsLogged = [Error]()
	public var breadcrumbs = [String]()
	
	public init() {}
	
	public func logError(_ error: String) {
		errorsLogged.append(error)
	}
	
	public func logError(_ error: Error) {
		errorsLogged.append(error)
	}
	
	public func logBreadcrumb(_ crumb: String, level: ModuleLinker.LoggingLevel) {
		breadcrumbs.append(crumb)
	}
}

extension String: Error {}
