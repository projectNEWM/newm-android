import SwiftUI
import SharedUI
import Colors
import ModuleLinker
import GoogleSignInSwift
import GoogleSignIn
import AuthenticationServices
import Resolver

public struct LandingView: View {
	@StateObject var viewModel = LandingViewModel()
	@FocusState var isTextFieldFocused: Bool
	
	private let socialSignInButtonHeight: CGFloat = 40
	@Injected private var logger: ErrorReporting
		
	public var body: some View {
		ZStack {
			NavigationStack(path: $viewModel.navPath) {
				landingView
					.padding()
					.navigationDestination(for: LandingRoute.self) { route in
						switch route {
						case .createAccount:
							createAccountView.backButton()
						case .codeConfirmation:
							codeConfirmationView.backButton()
						case .nickname:
							nicknameView.backButton()
						case .done:
							doneView
						case .login:
							loginView.backButton()
						case .forgotPassword:
							forgotPasswordView.backButton()
						case .enterNewPassword:
							enterNewPasswordView.backButton()
						}
					}
			}
			.alert(String.error, isPresented: .constant(viewModel.errors.hasError), presenting: viewModel.errors.currentError, actions: { error in
				Button {
					viewModel.errors.popFirstError()
				} label: {
					Text("Ok")
				}
			}, message: { error in
				Text(error.errorDescription ?? error.localizedDescription)
			})
			
			if viewModel.isLoading {
				LoadingToast()
			}
		}
	}
}

//Landing View
extension LandingView {
	@ViewBuilder
	private var landingView: some View {
		VStack {
			Asset.Media.logo.swiftUIImage.padding().padding(.top, 40)
			title
			Spacer()
			Group {
				loginButton
				createAccountButton
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
		actionButton(title: .login) {
			viewModel.goToLogin()
		}
	}
	
	@ViewBuilder
	private var createAccountButton: some View {
		Button {
			viewModel.createAccount()
		} label: {
			buttonText(.createNewAccount)
		}
		.background(.clear)
		.foregroundColor(NEWMColor.pink())
		.borderOverlay(color: NEWMColor.grey500(), radius: 4, width: 2)
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
		GoogleSignInButton {
			if let rootVC = rootViewController {
				GIDSignIn.sharedInstance.signIn(withPresenting: rootVC, completion: viewModel.handleGoogleSignIn)
			}
		}
		.frame(height: socialSignInButtonHeight)
	}
}

struct LandingView_Previews: PreviewProvider {
	static var previews: some View {
		let vm = LandingViewModel()
		vm.errors.append(NEWMError(errorDescription: "You fucked up big time."))
		return Group {
			LandingView()
			LandingView(viewModel: vm)
				.previewDisplayName("Error")
		}
		.preferredColorScheme(.dark)
	}
}
