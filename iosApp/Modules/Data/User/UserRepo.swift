//import Foundation
//import API
//import Models
//import ModuleLinker
//import Resolver
//
//class UserRepo: UserManaging, ObservableObject {
//	private let api = UserAPI()
//	@Injected private var loginRepo: LoginRepo
//	
//	@Published public var currentUser: User?
//	
//	//TODO: make actor
//	public static let shared = UserRepo()
//	
//	private init() {}
//	
//	public func deleteUser() async throws {
//		do {
//			try await api.delete()
//		} catch {
//			try handleError(error)
//		}
//		loginRepo.logOut()
//	}
//	
//	public func createUser(nickname: String, email: String, password: String, passwordConfirmation: String, verificationCode: String) async throws {
//		do {
//			try await api.create(nickname: nickname, email: email, password: password, passwordConfirmation: passwordConfirmation, verificationCode: verificationCode)
//		} catch {
//			try handleError(error)
//		}
//	}
//	
//	public func resetPassword(email: String, password: String, confirmPassword: String, authCode: String) async throws {
//		do {
//			try await api.resetPassword(email: email, password: password, confirmPassword: confirmPassword, authCode: authCode)
//		} catch {
//			try handleError(error)
//		}
//	}
//	
//	public func fetchCurrentUser() async throws {
//		do {
//			currentUser = try await api.getCurrentUser()
//		} catch {
//			try handleError(error)
//		}
//	}
//	
//	public func updateUserInfo(firstName: String?, lastName: String?, currentPassword: String?, newPassword: String?, confirmNewPassword: String?) async throws {
//		let updatedUser = try UpdatedUser(
//			firstName: firstName?.nilIfEmpty,
//			lastName: lastName?.nilIfEmpty,
//			newPassword: newPassword?.nilIfEmpty,
//			confirmPassword: confirmNewPassword?.nilIfEmpty,
//			currentPassword: currentPassword?.nilIfEmpty
//		)
//		do {
//			try await api.update(user: updatedUser)
//			try await fetchCurrentUser()
//		} catch {
//			try handleError(error)
//		}
//	}
//	
//	private func handleError(_ error: Error) throws {
//		if let error = error as? UserAPI.Error {
//			switch error {
//			case .accountExists:
//				throw UserRepoError.accountExists
//			case .twoFAFailed:
//				throw UserRepoError.twoFAFailed
//			case .unprocessableEntity(let cause):
//				throw UserRepoError.displayError(cause)
//			}
//		} else if let error = error as? APIError {
//			switch error {
//			case .invalidResponse:
//				throw UserRepoError.dataUnavailable
//			case .httpError(_, let cause):
//				throw UserRepoError.displayError(cause ?? "An unknown error occurred.")
//			}
//		}
//	}
//}
