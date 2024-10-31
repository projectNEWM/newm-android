import Foundation
import Combine
import ModuleLinker
import Resolver
import shared
import Network
import Analytics

@MainActor
class MainViewModel: ObservableObject {
	@Published var selectedTab: MainViewModelTab = .library
	//This value isn't used, it's just for triggering a view refresh.
	@Published var updateLoginState: Bool = false
	@Published var alertMessage: String?

	@Injected private var loginUseCase: UserSessionUseCase

	private var cancels = Set<AnyCancellable>()
	private let networkMonitor = NWPathMonitor()

	var shouldShowLogin: Bool {
		loginUseCase.isLoggedIn() == false
	}
	
	public init() {
		NotificationCenter.default.publisher(for: shared.Notification().loginStateChanged)
			.receive(on: RunLoop.main)
			.sink { [weak self] _ in
				self?.updateLoginState.toggle()
			}
			.store(in: &cancels)
		
		startNetworkMonitoring()
	}
	
	func startNetworkMonitoring() {
		networkMonitor.pathUpdateHandler = { path in
			if path.status == .unsatisfied {
				DispatchQueue.main.async { [weak self] in
					self?.alertMessage = "Your device does not appear to be connected to the internet."
				}
			}
		}
		let queue = DispatchQueue(label: "NetworkMonitor")
		networkMonitor.start(queue: queue)
	}
}
