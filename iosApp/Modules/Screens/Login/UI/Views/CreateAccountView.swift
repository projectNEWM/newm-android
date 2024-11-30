import SwiftUI
import SharedUI
import Colors

extension LandingView {
	@ViewBuilder
	var createAccountView: some View {
		VStack {
			Asset.Media.logo.swiftUIImage.resizable().frame(width: 100, height: 100).padding(.top, 50).padding(.bottom, 25)
			VStack {
				NEWMTextField(title: .email, prompt: .emailPrompt, isSecure: false, text: $viewModel.email)
					.textContentType(.emailAddress)
					.padding(.bottom)
				NEWMTextField(title: .password, prompt: .minimum8Characters, isSecure: true, text: $viewModel.password)
					.textContentType(.password)
					.padding(.bottom)
				NEWMTextField(title: .confirmPassword, prompt: .confirmYourPassword, isSecure: true, text: $viewModel.confirmPassword)
					.textContentType(.password)
					.padding(.bottom)
			}.padding()
			
			actionButton(title: .next, backgroundGradient: Gradients.loginGradient.gradient) {
				viewModel.requestVerificationCode()
			}
			.padding([.leading, .trailing])
			
			Spacer()
		}
		.autocorrectionDisabled()
		.autocapitalization(.none)
	}
}

import Mocks

#Preview {
	LandingView()
		.createAccountView
		.preferredColorScheme(.dark)
}
