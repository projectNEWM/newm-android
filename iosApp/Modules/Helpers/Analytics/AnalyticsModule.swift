import ModuleLinker
import Foundation
import Resolver
import FirebaseCore

final public class AnalyticsModule: Module {
	public static var shared = AnalyticsModule()
	private let newmAnalytics = NEWMAnalytics()
			
	init() {
		FirebaseApp.configure()
		NEWMAnalytics().setup()
	}
	
	public func registerAllServices() {
		
	}
	
#if DEBUG
	public func registerAllMockedServices(mockResolver: Resolver) {
		
	}
#endif
}
