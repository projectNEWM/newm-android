import Foundation
import shared

class MockWalletNFTTracksUseCase: WalletNFTTracksUseCase {
	func getAllNFTTracks() async throws -> [NFTTrack] {
		NFTTrack.mocks
	}
	
	func getAllNFTTracksFlow() -> any Kotlinx_coroutines_coreFlow {
		fatalError()
	}
	
	func getAllStreamTokens() async throws -> [NFTTrack] {
		NFTTrack.mocks
	}
	
	func getAllStreamTokensFlow() -> any Kotlinx_coroutines_coreFlow {
		fatalError()
	}
	
	func getNFTTrack(id: String) throws -> NFTTrack {
		NFTTrack.mocks.first { $0.id == id }!
	}
	
	func refresh() async throws {
		// no-op
	}
}