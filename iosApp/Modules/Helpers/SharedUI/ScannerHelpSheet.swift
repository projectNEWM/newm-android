import SwiftUI
import Colors
import Fonts

struct ScannerHelpSheet: View {
	@Binding var showSheet: Bool
	@Binding var showCopiedToast: Bool
	private let toolsUrl = "https://tools.newm.io/"

	var body: some View {
		VStack {
			VStack {
				VStack {
					HStack {
						Text("HOW TO CONNECT")
							.font(Font.inter(ofSize: 12).bold())
							.padding(.bottom, 16)
							.foregroundStyle(try! Color(hex: "6F6F70"))
						Spacer()
					}
					
					VStack(alignment: .leading, spacing: 10) {
						Group {
							Text("1. Open your Cardano supported web3 wallet app")
							Text("2. Use this URL to connect your wallet:")
						}
						.foregroundColor(.white)
						.font(Font.inter(ofSize: 14))
						
						Button(action: {
							UIPasteboard.general.string = toolsUrl
							Task {
								showCopiedToast = true
								try! await Task.sleep(for: .seconds(1))
								showCopiedToast = false
							}
						}) {
							HStack {
								Text(verbatim: toolsUrl)
								Image("Copy Icon")
							}
							.padding([.top, .bottom])
							.frame(maxWidth: .infinity, idealHeight: 40)
							.background(Gradients.mainSecondaryLight)
							.foregroundStyle(NEWMColor.midCrypto())
							.cornerRadius(12)
						}
						
						HStack {
							Spacer()
							Text(verbatim: "or continue on desktop at the same address (https://tools.newm.io)")
								.foregroundColor(.gray)
								.multilineTextAlignment(.center)
								.font(.inter(ofSize: 14))
							Spacer()
						}
					}
				}
				.padding()
				.background(NEWMColor.Background.defaultLight())
				.cornerRadius(8)
			}
			.presentationBackground(Color.black.opacity(0.9))
			.presentationDetents([.medium])

			Button(action: {
				showSheet = false
			}) {
				Text("Got it")
					.foregroundStyle(Gradients.libraryGradient.gradient)
					.padding()
					.frame(maxWidth: .infinity, idealHeight: 40)
					.background(Gradients.mainPrimaryLight)
					.cornerRadius(12)
			}
		}
		.background(.black)
		.padding([.leading, .trailing])
	}
}

#Preview {
//	VStack {}
//		.sheet(isPresented: .constant(true)) {
	ScannerHelpSheet(showSheet: .constant(true), showCopiedToast: .constant(false))
//		}
}
