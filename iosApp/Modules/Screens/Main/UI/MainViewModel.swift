import Foundation
import Combine
import ModuleLinker
import Resolver
import Data
import API
import shared

@MainActor
class MainViewModel: ObservableObject {
	@Published var selectedTab: MainViewModelTab = .home
	//This value isn't used, it's just for triggering a view refresh.
	@Published var updateLoginState: Bool = false
	@Injected private var loginUseCase: LoginUseCase
	
	private var cancels = Set<AnyCancellable>()

	var shouldShowLogin: Bool {
		loginUseCase.userIsLoggedIn == false
	}
	
	public init() {
		NotificationCenter.default.publisher(for: shared.Notification().loginStateChanged)
			.receive(on: RunLoop.main)
			.sink { [weak self] _ in
				self?.updateLoginState.toggle()
			}
			.store(in: &cancels)
	}
}
