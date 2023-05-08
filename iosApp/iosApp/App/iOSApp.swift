import SwiftUI
import Resolver
import ModuleLinker
import FacebookCore
import Auth

@main
struct iOSApp: App {
	let mainViewProvider: MainViewProviding
	
	init() {
#if DEBUG
		UserDefaults.standard.set(false, forKey: "_UIConstraintBasedLayoutLogUnsatisfiable")
#endif
		mainViewProvider = Resolver.resolve()
		
		setUpAppearance()
		
		ApplicationDelegate.shared.application(
			UIApplication.shared,
			didFinishLaunchingWithOptions: [:]
		)
	}
	
	var body: some Scene {
		WindowGroup {
			VStack {
				mainViewProvider.mainView()
			}
			.preferredColorScheme(.dark)
			.onOpenURL { url in
				ApplicationDelegate.shared.application(
							UIApplication.shared,
							open: url,
							sourceApplication: nil,
							annotation: [UIApplication.OpenURLOptionsKey.annotation]
						)
			}
		}
	}
	
	private func setUpAppearance() {
		UINavigationBar.appearance().tintColor = .white
		let barAppearance = UIBarAppearance()
		barAppearance.configureWithOpaqueBackground()
		UITabBar.appearance().standardAppearance = UITabBarAppearance(barAppearance: barAppearance)
	}
}

extension UIWindow {
	open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
		if motion == .motionShake {
			Task {
				do {
					try await LoginManager().logOut()
				} catch {
					print(error)
				}
			}
		}
	}
}
