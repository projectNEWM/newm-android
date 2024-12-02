import SwiftUI
import SharedUI
import Colors

extension LandingView {
	@ViewBuilder
	var loginView: some View {
		ZStack {
			VStack {
				Asset.Media.logo.swiftUIImage.resizable().frame(width: 112, height: 112).padding(30)
				NEWMTextField(title: .email, prompt: .emailPrompt, isSecure: false, text: $viewModel.email)
					.padding(.bottom)
					.keyboardType(.emailAddress)
					.autocorrectionDisabled()
					.textInputAutocapitalization(.never)
					.textContentType(.emailAddress)
				NEWMTextField(title: .password, prompt: .yourPassword, isSecure: true, text: $viewModel.password)
					.padding(.bottom)
					.textContentType(.password)
				actionButton(title: .next, backgroundGradient: Gradients.loginGradient.gradient) {
					viewModel.login()
				}
				Spacer()
			}
			.addSidePadding()
			.toolbar {
				Button("Forgot password?") {
					viewModel.forgotPassword()
				}
				.frame(alignment: .trailing)
				.foregroundColor(try! Color(hex: "DC3CAA"))
			}
		}
	}
}

#if DEBUG
struct LoginView_Previews: PreviewProvider {
	static var previews: some View {
		NavigationView {
			NavigationLink(isActive: .constant(true)) {
				LandingView()
					.loginView
					.preferredColorScheme(.dark)
			} label: {
				Text("")
			}
		}
	}
}
#endif
