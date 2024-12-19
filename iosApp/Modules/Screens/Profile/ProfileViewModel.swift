import Foundation
import Resolver
import ModuleLinker
import Combine
import shared
import AudioPlayer
import SharedExtensions
import Utilities

@MainActor
final class ProfileViewModel: ObservableObject {
	@Injected private var getCurrentUser: UserDetailsUseCase
	@LazyInjected private var hasWalletConnectionUseCase: HasWalletConnectionsUseCase
	@LazyInjected private var changePasswordUseCase: ChangePasswordUseCase
	@LazyInjected private var disconnectWalletUseCase: DisconnectWalletUseCase
	@LazyInjected private var logOutUseCase: LoginUseCase
	@LazyInjected private var deleteUserUseCase: DeleteCurrentUserUseCase

	@Injected private var logger: ErrorReporting
	
	@Published private var user: User?
	
	var fullName: String { "\((user?.firstName).emptyIfNil) \((user?.lastName).emptyIfNil)" }
	var nickname: String { (user?.nickname?.nilIfEmpty.flatMap { "@\($0)" }).emptyIfNil }
	var email: String { (user?.email).emptyIfNil }
	var bannerURL: URL? { user?.bannerUrl.flatMap(URL.init) }
	var pictureURL: URL? { user?.pictureUrl.flatMap(URL.init) }
	
	@Published var currentPassword: String = ""
	@Published var newPassword: String = ""
	@Published var confirmPassword: String = ""
	@Published private var errors = ErrorSet()
	@Published var showLoadingToast: Bool = true
	@Published var showCompletionToast: Bool = false
	@Published var isWalletConnected: Bool = false
	@Published var showDeleteUserConfirmation: Bool = false
	var errorAlert: String? { errors.currentError?.errorDescription }
		
	var enableSaveButon: Bool {
		let hasNewPassword =
		currentPassword.isEmpty == false &&
		newPassword.isEmpty == false &&
		confirmPassword.isEmpty == false
		
		let newPasswordsMatch = newPassword == confirmPassword
		
		return hasNewPassword && newPasswordsMatch && showLoadingToast == false && showCompletionToast == false
	}
	
	var showSaveButton: Bool {
		currentPassword.isEmpty == false ||
		newPassword.isEmpty == false ||
		confirmPassword.isEmpty == false
	}
	
	init() {
		Task.detached {
			for try await _ in NotificationCenter.default.publisher(for: shared.Notification().walletConnectionStateChanged).compactMap({ $0 }).values {
				Task { @MainActor [weak self] in
					self?.isWalletConnected = try await self?.hasWalletConnectionUseCase.hasWalletConnections() == true
				}
			}
		}
		
		Task {
			await loadUser()
			showLoadingToast = false
		}
	}
	
	func loadUser() async {
		// don't set loading state here, since this might be called from the view's "refreshable"
		do {
			user = try await getCurrentUser.fetchLoggedInUserDetails()
			isWalletConnected = try await hasWalletConnectionUseCase.hasWalletConnections().boolValue
		} catch {
			logger.logError(error)
			errors.append(error.newmError)
		}
	}
	
	func save() async {
		defer {
			currentPassword = ""
			newPassword = ""
			confirmPassword = ""
		}
		
		showLoadingToast = true
		do {
			try await changePasswordUseCase.changePassword(
				oldPassword: currentPassword,
				newPassword: newPassword,
				confirmPassword: confirmPassword
			)
			showLoadingToast = false
			showCompletionToast = true
			try? await Task.sleep(for: .seconds(1))
			showCompletionToast = false
		} catch {
			showLoadingToast = false
			logger.logError(error)
			errors.append(error.newmError)
		}
	}
	
	func disconnectWallet() async {
		do {
			try await disconnectWalletUseCase.disconnect(walletConnectionId: nil)
			isWalletConnected = false
		} catch {
			errors.append(error)
		}
	}
	
	func alertDismissed() {
		errors.popFirstError()
	}
	
	func logOut() {
		do {
			try logOutUseCase.logout()
			NotificationCenter.default.post(name: Notification.Name(shared.Notification().loginStateChanged), object: nil)
		} catch {
			errors.append(error)
		}
	}
	
	func deleteUser() async {
		do {
			showLoadingToast = true
			try await deleteUserUseCase.delete()
		} catch {
			errors.append(error)
		}
	}
}
