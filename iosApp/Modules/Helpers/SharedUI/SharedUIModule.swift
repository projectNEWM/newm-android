import Foundation
import Resolver
import ModuleLinker
import SwiftUI

public final class SharedUIModule: Module {
	public static let shared = SharedUIModule()
	
	public func registerAllServices() {
		Resolver.register {
			self as GradientTagProviding
		}
		
		Resolver.register {
			self as CircularProviding
		}
	}
}

extension SharedUIModule: GradientTagProviding {
	public func gradientTag(title: String) -> AnyView {
		GradientTag(title: title).erased
	}
}

extension SharedUIModule: CircularProviding {
	public func circular<D : RandomAccessCollection, I : Hashable, C: View>(@ViewBuilder content: () -> ForEach<D, I, C>) -> AnyView {
		Circular(content: content).erased
	}
}

//#if DEBUG
extension SharedUIModule {
	public func registerAllMockedServices(mockResolver: Resolver) {
	}
}
//#endif
