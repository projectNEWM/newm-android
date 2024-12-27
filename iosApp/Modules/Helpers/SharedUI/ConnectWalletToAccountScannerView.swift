import SwiftUI
@preconcurrency import shared
import Resolver
import ModuleLinker
import Utilities
import Colors
import Analytics
import QRCodeReader

@MainActor
public struct ConnectWalletToAccountScannerView: View {
	private let completion: () -> ()
	@State private var manuallyEnteredCode: String = ""
	@Injected private var connectWalletToAccountUseCase: any ConnectWalletUseCase
	@State private var isLoading = false
	@State private var showHelpSheet = false
	@State private var showCopiedToast = false
	
	@State private var error: Error? {
		didSet {
			let errorReporter = Resolver.resolve(ErrorReporting.self)
			if errorIsCameraPermissionDenied {
				errorReporter.logBreadcrumb("Camera permission denied", level: .info)
			} else {
				error.flatMap(errorReporter.logError)
			}
		}
	}

	private let toolsUrl = "https://tools.newm.io/"

	public init(completion: @escaping () -> Void) {
		self.completion = completion
	}

	public var body: some View {
		VStack {
			HStack {
				title.padding()
				Spacer()
			}
			
			VStack {
				QRCodeScannerView { result in
					switch result {
					case .success(let code):
						success(id: code)
					case .failure(let error):
						self.error = error
					}
				}
				.frame(width: 346, height: 337)
				.clipShape(RoundedRectangle(cornerRadius: 32))
			}
			
			manualEntry
			Spacer()
			bottomButtons
		}
		.onAppear {
			checkScanPermissions()
		}
		.sheet(isPresented: $showHelpSheet) {
			ScannerHelpSheet(showSheet: $showHelpSheet, showCopiedToast: $showCopiedToast)
				.presentationDetents([.height(322)])
		}
		.alert(errorTitle, isPresented: isPresent($error)) {
			if errorIsCameraPermissionDenied {
				Button("Cancel") {
					error = nil
					showHelpSheet = true
				}
				Button("Settings") {
					if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
						UIApplication.shared.open(settingsURL)
					}
					error = nil
					showHelpSheet = true
				}
			} else {
				Button("OK") {
					error = nil
					showHelpSheet = true
				}
			}
		} message: {
			if errorIsCameraPermissionDenied {
				Text("This app is not authorized to use the camera.  Please go to Settings and allow access.")
			} else {
				Text("An unknown error occurred.")
			}
		}
		.loadingToast(shouldShow: $isLoading)
		.toast(shouldShow: $showCopiedToast, type: .copied)
		.background(.black)
		.analyticsScreen(name: AppScreens.ConnectWalletScannerScreen().name)
	}

	@ViewBuilder
	private var bottomButtons: some View {
		VStack {
			Button(action: {
				UIPasteboard.general.string = toolsUrl
				Task {
					showCopiedToast = true
					try? await Task.sleep(for: .seconds(1))
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

			Button(action: {
				showHelpSheet = true
			}) {
				Text("How can I connect a wallet?")
					.foregroundStyle(Gradients.libraryGradient.gradient)
					.padding()
					.frame(maxWidth: .infinity, idealHeight: 40)
					.background(Gradients.mainPrimaryLight)
					.cornerRadius(12)
			}
		}
		.padding()
	}

	@ViewBuilder
	private var title: some View {
		Text("Connect Wallet")
			.font(
				Font.custom("Inter", size: 24)
					.weight(.bold)
			)
			.foregroundStyle(Gradients.mainSecondary)
	}
	
	@ViewBuilder
	private var manualEntry: some View {
		VStack(alignment: .leading, spacing: 4) {
			Text("OR PASTE QR CODE")
				.font(
					Font.custom("Inter", size: 12)
						.weight(.bold)
				)
				.foregroundColor(Color(red: 0.44, green: 0.44, blue: 0.44))
			
			HStack {
				TextField("", text: $manuallyEnteredCode, prompt: Text("Paste here"))
					.padding(.leading, 12)
					.padding(.trailing, 5)
					.padding(.vertical, 12)
					.frame(maxWidth: .infinity, minHeight: 40, maxHeight: 40, alignment: .center)
					.background(Color(red: 0.14, green: 0.14, blue: 0.14))
					.cornerRadius(8)
				
				let disabled = manuallyEnteredCode.isEmpty
				Button {
					success(id: manuallyEnteredCode)
				} label: {
					Text("Connect")
						.foregroundColor(
							disabled ?
								.gray :
								NEWMColor.midCrypto()
						)
				}
				.disabled(disabled)
			}
		}
		.padding(.top)
		.frame(width: 358, alignment: .topLeading)
	}

	private func success(id: String) {
		isLoading = true
		Task {
			defer { isLoading = false }
			do {
				try await connectWalletToAccountUseCase.connect(walletConnectionId: id)
				completion()
			} catch {
				self.error = error
			}
		}
	}

	private func checkScanPermissions() {
		do {
			_ = try QRCodeReader.supportsMetadataObjectTypes()
			showHelpSheet = true
		} catch {
			self.error = error
		}
	}
	
	private var errorIsCameraPermissionDenied: Bool {
		(error as? NSError)?.code == -11852
	}
	
	private var errorTitle: String {
		if errorIsCameraPermissionDenied {
			"Camera Access Required"
		} else {
			"Error"
		}
	}
}

#Preview {
	ConnectWalletToAccountScannerView { }
		.preferredColorScheme(.dark)
}
