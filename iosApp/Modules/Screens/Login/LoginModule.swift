import Foundation
import ModuleLinker
import Resolver
import SwiftUI
import shared

public final class LoginModule: ModuleProtocol {
	public static var shared = LoginModule()
	
	public func registerAllServices() {
		Resolver.register {
			self as LoginViewProviding
		}
		
		Resolver.register {
			LoginUseCaseProvider().get() as LoginUseCase
		}
	}
}

#if DEBUG
extension LoginModule {
	public func registerAllMockedServices(mockResolver: Resolver) {
//		mockResolver.register {
//			MockLogInLogOutUseCase.shared as LoggedInUserUseCaseProtocol
//		}
		
//		mockResolver.register {
//			MockLogInLogOutUseCase.shared as LoginRepo
//		}
		
//		mockResolver.register {
//			MockLogInLogOutUseCase.shared as LogOutUseCaseProtocol
//		}
	}
}
#endif
