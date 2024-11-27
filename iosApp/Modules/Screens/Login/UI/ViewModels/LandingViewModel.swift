import Foundation
import Resolver
import GoogleSignIn
import AuthenticationServices
import ModuleLinker
import shared
import Utilities
import RecaptchaEnterprise

@MainActor
class LandingViewModel: ObservableObject {
	@Published var navPath = [LandingRoute]()
	
	@Published var errors = ErrorSet()
	@Published var isLoading: Bool = false
	
	@Published var confirmationCode = ""
	@Published var email: String = ""
	@Published var password: String = ""
	@Published var confirmPassword: String = ""
	@Published var nickname: String = ""
			
	@Injected private var logInUseCase: any LoginUseCase
	@Injected private var signUpUseCase: any SignupUseCase
	@Injected private var resetPasswordUseCase: any ResetPasswordUseCase
	@Injected private var errorLogger: ErrorReporting
	private var recaptcha: RecaptchaClient!
	
	private let loginFieldValidator = LoginFieldValidator()
	
	init() {
		Task { [weak self] in
			guard let self else { return }
			do {
				recaptcha = try await Recaptcha.fetchClient(withSiteKey: EnvironmentVariable.recaptchaKey.value)
			} catch let error as RecaptchaError {
				errorLogger.logError("RecaptchaClient creation error: \(String(describing: error.errorMessage)).")
			} catch {
				errorLogger.logError("Unknown RecaptchaClient creation error: \(String(describing: error)).")
			}
		}
	}

	var nicknameIsValid: Bool {
		nickname.count > 0
	}
		
	var createAccountFieldsAreValid: Bool {
		password == confirmPassword &&
		loginFieldValidator.validate(email: email, password: password)
	}
	
	var loginFieldsAreValid: Bool {
		loginFieldValidator.validate(email: email, password: password)
	}
	
	var confirmationCodeIsValid: Bool {
		confirmationCode.count == 6
	}
	
	func goToLogin() {
		navPath.append(.login)
	}
	
	func login() {
		isLoading = true
		Task {
			do {
				try await logInUseCase.logIn(email: email, password: password, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.loginEmail.recaptchaAction))
			} catch {
				handleError(error)
			}
			isLoading = false
		}
	}
	
	func forgotPassword() {
		navPath.append(.forgotPassword)
		password = ""
		confirmPassword = ""
	}
	
	func requestPasswordResetCode() {
		navPath.append(.enterNewPassword)
		Task {
			do {
				try await signUpUseCase.requestEmailConfirmationCode(email: email, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.authCode.recaptchaAction), mustExists: true)
			} catch {
				handleError(error)
			}
		}
	}
	
	func resetPassword() {
		isLoading = true
		Task {
			do {
				try await resetPasswordUseCase.resetPassword(email: email, code: confirmationCode, newPassword: password, confirmPassword: confirmPassword, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.resetPassword.recaptchaAction))
				try await logInUseCase.logIn(email: email, password: password, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.loginEmail.recaptchaAction))
			} catch {
				handleError(error)
			}
			isLoading = false
		}
	}
	
	func createAccount() {
		navPath.append(.createAccount)
	}
	
	func requestVerificationCode() {
		if !navPath.contains(.codeConfirmation) {
			navPath.append(.codeConfirmation)
		}
		Task {
			do {
				try await signUpUseCase.requestEmailConfirmationCode(email: email, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.authCode.recaptchaAction), mustExists: false)
			} catch {
				handleError(error)
			}
		}
	}
	
	func registerUser() {
		isLoading = true
		Task {
			do {
				try await signUpUseCase.registerUser(
					email: email,
					password: password,
					passwordConfirmation: confirmPassword,
					verificationCode: confirmationCode,
					humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.register.recaptchaAction)
				)
				try await logInUseCase.logIn(
					email: email,
					password: password,
					humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.loginEmail.recaptchaAction)
				)
			} catch {
				handleError(error)
			}
			
			isLoading = false
		}
	}
	
	func navigateBackTo(_ navRoute: LandingRoute) {
		navPath = Array(navPath.prefix(through: navPath.firstIndex(of: navRoute)!))
	}
	
	func reset() {
		navPath = []
	}
		
	func handleGoogleSignIn(result: GIDSignInResult?, error: Error?) {
		guard let idToken = result?.user.idToken?.tokenString else {
			handleError("Failed to sign in with Google"); return
		}
		
		guard error == nil else {
			handleError(error!); return
		}
		
		isLoading = true
		Task {
			do {
				try await logInUseCase.logInWithGoogle(idToken: idToken, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.loginGoogle.recaptchaAction))
			} catch {
				handleError(error)
			}
			isLoading = false
		}
	}
	
	func handleAppleSignIn(result: Result<ASAuthorization, Error>) {
		switch result {
		case .success(let authResults):
			guard
				let identityToken = (authResults.credential as? ASAuthorizationAppleIDCredential)?.identityToken,
				let token = String(data: identityToken, encoding: .utf8)
			else {
				handleError("Could not sign in with Apple")
				return
			}
			isLoading = true
			Task {
				do {
					try await logInUseCase.logInWithApple(idToken: token, humanVerificationCode: try await recaptcha.execute(withAction: HumanVerificationAction.loginApple.recaptchaAction))
				} catch {
					handleError(error)
				}
				isLoading = false
			}
		case .failure(let error):
			handleError(error)
		}
	}
}

enum HumanVerificationAction {
	case loginEmail
	case loginApple
	case loginGoogle
	case register
	case authCode
	case resetPassword
	
	var recaptchaAction: RecaptchaAction {
		return switch self {
		case .loginEmail:
			RecaptchaAction.login
		case .loginApple:
				RecaptchaAction(customAction: "login_apple")
		case .loginGoogle:
			RecaptchaAction(customAction: "login_google")
		case .register:
			RecaptchaAction.signup
		case .authCode:
			RecaptchaAction(customAction: "auth_code")
		case .resetPassword:
			RecaptchaAction(customAction: "password_reset")
		}
	}
}
