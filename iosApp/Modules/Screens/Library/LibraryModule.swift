import Foundation
import Resolver
import ModuleLinker
import SwiftUI

public final class LibraryModule: ModuleProtocol {
	public static let shared = LibraryModule()
		
	public func registerAllServices() {
		Resolver.register {
			self as LibraryViewProviding
		}
    }
}

#if DEBUG
extension LibraryModule {
    public func registerAllMockedServices(mockResolver: Resolver) {

	}
}
#endif
