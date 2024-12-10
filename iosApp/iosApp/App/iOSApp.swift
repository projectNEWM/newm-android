import SwiftUI
import Resolver
import ModuleLinker
import UIKit
import shared
import Kingfisher
import Utilities
@preconcurrency import RecaptchaEnterprise
import Logging

@main
struct iOSApp: App {
	private let mainViewProvider: MainViewProviding
	private var imageErrorPlugin = KingfisherErrorHandler()
	@Injected private var forceAppUpdateUseCase: ForceAppUpdateUseCase
	@Injected private var errorLogger: ErrorReporting
	
	@State private var recaptcha: RecaptchaClient!
	@State private var showUpdateAlert: Bool = false

	init() {
#if DEBUG
		UserDefaults.standard.set(false, forKey: "_UIConstraintBasedLayoutLogUnsatisfiable")
#endif
		mainViewProvider = Resolver.resolve()
		
		setUpAppearance()
		setUpKingfisherErrorHandling()
	}
	
	var body: some Scene {
		WindowGroup {
			Group {
				if showUpdateAlert {
					ForceAppUpdateView()
				} else {
					VStack {
						mainViewProvider.mainView()
					}
				}
			}
			.preferredColorScheme(.dark)
			.task {
				do {
					recaptcha = try await Recaptcha.fetchClient(withSiteKey: EnvironmentVariable.recaptchaKey.value)
				} catch let error as RecaptchaError {
					errorLogger.logError("RecaptchaClient creation error: \(String(describing: error.errorMessage)).")
				} catch {
					errorLogger.logError("Unknown RecaptchaClient creation error: \(String(describing: error)).")
				}
				
				await checkAppUpdate()
			}
		}
	}
	
	@MainActor
	private func setUpAppearance() {
		UINavigationBar.appearance().tintColor = .white
		let barAppearance = UIBarAppearance()
		barAppearance.configureWithOpaqueBackground()
		UITabBar.appearance().standardAppearance = UITabBarAppearance(barAppearance: barAppearance)
	}
	
	private func checkAppUpdate() async {
	    do {
            showUpdateAlert = (try await forceAppUpdateUseCase.isiOSUpdateRequired(
                currentAppVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as! String,
                humanVerificationCode: try await recaptcha.execute(
                    withAction: RecaptchaAction(customAction: "mobile_config")
                )
            ).boolValue) == true
		} catch {
            errorLogger.logError("Error checking for app update: \(error.localizedDescription)")
        }
	}
}

extension iOSApp {
	func setUpKingfisherErrorHandling() {
		KingfisherManager.shared.downloader.delegate = imageErrorPlugin
	}
}
