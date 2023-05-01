import Foundation
import SwiftUI
import Colors

extension LandingView {
	@ViewBuilder
	var nicknameView: some View {
		VStack {
			Text(verbatim: .whatShouldWeCallYou)
				.font(.ralewayExtraBold(ofSize: 40))
				.multilineTextAlignment(.center)

			TextEditor(text: $viewModel.nickname)
				.foregroundStyle(Gradients.loginGradient.gradient)
				.multilineTextAlignment(.center)
				.font(.dmSerifItalic(ofSize: 40))
				.padding()
				.frame(height: 300)
				.accentColor(NEWMColor.pink.swiftUIColor)
				.focused($isTextFieldFocused)
				.autocorrectionDisabled()
				.autocapitalization(.none)
				.italic()

			Spacer()
			
			nextButton(title: .next) {
				viewModel.registerUser()
			}
			.disabled(!viewModel.nicknameIsValid)
		}
		.padding()
		.onAppear {
			isTextFieldFocused = true
		}
	}
}

struct UsernameView_Previews: PreviewProvider {
	static var previews: some View {
		LandingView()
			.nicknameView
			.preferredColorScheme(.dark)
	}
}
