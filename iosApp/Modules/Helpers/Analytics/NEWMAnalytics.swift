@preconcurrency import shared
import FirebaseAnalytics
import Combine
import Resolver
import Foundation
import SharedExtensions

public class NEWMAnalytics {
	private var cancels: Set<AnyCancellable> = []

	func setup() {
		NotificationCenter.default.publisher(for: shared.Notification().loginStateChanged)
			.receive(on: RunLoop.main)
			.sink { _ in
				Task { @MainActor in
					let user = Resolver.resolve(UserDetailsUseCase.self)
					NEWMAnalytics.setUserId(userId: (try? await user.fetchLoggedInUserDetails().id) ?? "")
				}
			}
			.store(in: &cancels)
		
		Task { @MainActor in
			let user = Resolver.resolve(UserDetailsUseCase.self)
			NEWMAnalytics.setUserId(userId: (try? await user.fetchLoggedInUserDetails().id) ?? "")
		}
	}
	
	static public func trackClickEvent(buttonName: String, screenName: String, properties: [String : Any]?) {
		var properties = properties
		properties?["button_name"] = buttonName
		properties?["screen_name"] = screenName
		Analytics.logEvent("button_click", parameters: properties)
	}
	
	static public func trackEvent(eventName: String, properties: [String : Any]?) {
		Analytics.logEvent(eventName, parameters: properties)
	}
		
	static public func setUserId(userId: String) {
		Analytics.setUserID(userId)
	}
	
	static public func setUserProperty(propertyName: String, value: String) {
		Analytics.setUserProperty(value, forName: propertyName)
	}
}
