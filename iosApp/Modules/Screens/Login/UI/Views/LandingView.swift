import SwiftUI
import SharedUI
import Colors
import ModuleLinker
import GoogleSignInSwift
import GoogleSignIn
import AuthenticationServices
import Resolver
import Utilities

public struct LandingView: View {
	@StateObject var viewModel = LandingViewModel()
	@FocusState var isTextFieldFocused: Bool
	
	private let socialSignInButtonHeight: CGFloat = 40
	@Injected private var logger: ErrorReporting
	
	public var body: some View {
		NavigationStack(path: $viewModel.navPath) {
			landingView
				.padding()
				.navigationDestination(for: LandingRoute.self) { route in
					switch route {
					case .createAccount:
						createAccountView.backButton()
					case .codeConfirmation:
						codeConfirmationView.backButton()
					case .login:
						loginView.backButton()
					case .forgotPassword:
						forgotPasswordView.backButton()
					case .enterNewPassword:
						enterNewPasswordView.backButton()
					}
				}
		}
		.errorAlert(message: viewModel.errors.currentError?.errorDescription) {
			viewModel.errors.popFirstError()
		}
		.toast(shouldShow: $viewModel.isLoading, type: .loading)
	}
}

//Landing View
extension LandingView {
	@ViewBuilder
	fileprivate var landingView: some View {
		VStack {
			HStack {
				Spacer()
				createAccountButton
					.frame(alignment: .trailing)
			}
			Asset.Media.logo.swiftUIImage.padding().padding(.top, 40)
			title
			Spacer()
			Group {
				loginButton
				googleSignInButton
				signInWithAppleButton
			}
			.cornerRadius(4)
			.font(.inter(ofSize: 14).weight(.semibold))
		}
	}
	
	@ViewBuilder
	private var title: some View {
		Text(verbatim: .welcomeToNewm)
			.font(.ralewayExtraBold(ofSize: 30))
	}
	
	@ViewBuilder
	private var loginButton: some View {
		actionButton(title: .login, backgroundGradient: Gradients.loginGradient.gradient) {
			viewModel.goToLogin()
		}
	}
	
	@ViewBuilder
	private var createAccountButton: some View {
		Button {
			viewModel.createAccount()
		} label: {
			buttonText(.createAccount)
		}
		.fixedSize()
		.foregroundColor(try! Color(hex: "DC3CAA"))
	}
	
	@ViewBuilder
	private var signInWithAppleButton: some View {
		SignInWithAppleButton(.signIn) { request in
			request.requestedScopes = [.fullName, .email]
		} onCompletion: { result in
			viewModel.handleAppleSignIn(result: result)
		}
		.signInWithAppleButtonStyle(.white)
		.frame(height: socialSignInButtonHeight)
	}
	
	private var rootViewController: UIViewController? {
		guard let rootVC = (UIApplication.shared.connectedScenes.first as? UIWindowScene)?.windows.first?.rootViewController else {
			logger.logError("\(#file) could not get root view controller for SwiftUI")
			return nil
		}
		return rootVC
	}
	
	@ViewBuilder
	private var googleSignInButton: some View {
		actionButton(title: HStack {
			Image("google-fill")
			Text("Login with Google")
		}, backgroundGradient: Gradients.mainPrimaryLight) {
			if let rootVC = rootViewController {
				GIDSignIn.sharedInstance.signIn(withPresenting: rootVC, completion: viewModel.handleGoogleSignIn)
			}
		}
		.frame(height: socialSignInButtonHeight)
		.foregroundStyle(NEWMColor.midMusic())
	}
}

#if DEBUG

#Preview {
	LandingView().landingView
		.preferredColorScheme(.dark)
}

#Preview("Error") {
	let vm = LandingViewModel()
	vm.errors.append(NEWMError(errorDescription: "You messed up big time."))
	return LandingView(viewModel: vm)
		.preferredColorScheme(.dark)
}
#endif
