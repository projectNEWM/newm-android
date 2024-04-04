import Foundation
import Combine
import Resolver
import ModuleLinker
import SharedUI
import shared
import AudioPlayer
import Utilities
import SharedExtensions

@MainActor
class LibraryViewModel: ObservableObject {
	@Published var searchText: String = "" {
		didSet {
			sortAndFilterTracks()
		}
	}
	@Published var errors = ErrorSet()
	var durationFilter: Int? {
		set {
			audioPlayer.durationFilter = newValue
			sortAndFilterTracks()
		}
		get { audioPlayer.durationFilter }
	}
	private(set) var sort: Sort {
		set {
			audioPlayer.sort = newValue
			tracks.sort(by: newValue.comparator)
		}
		get { audioPlayer.sort }
	}
	
	private func sortAndFilterTracks() {
		filteredSortedTracks = tracks
			.filter { track in
				return (durationFilter.flatMap { track.duration > $0 } ?? true) && 
				(searchText.isEmpty == false ? 
				 (track.title.localizedCaseInsensitiveContains(searchText) || track.artists.first { $0.localizedCaseInsensitiveContains(searchText) } != nil) : true)
			}
			.sorted(by: sort.comparator)
	}
	
	@Published private(set) var route: LibraryRoute?
	@Published private(set) var filteredSortedTracks: [NFTTrack] = []
	@Published private var tracks: [NFTTrack] = [] {
		didSet {
			sortAndFilterTracks()
		}
	}
	@Published private(set) var showLoading: Bool = true
	@Published private(set) var showXPubScanner: Bool = false
	var walletIsConnected: Bool { connectWalletXPubUseCase.isConnected() }
	
	private var cancels: Set<AnyCancellable> = []
	
	@Injected private var walletNFTTracksUseCase: any WalletNFTTracksUseCase
	@Injected private var connectWalletXPubUseCase: any ConnectWalletUseCase
	@InjectedObject private var audioPlayer: VLCAudioPlayer
	@Injected private var logger: any ErrorReporting
	
	init() {
		NotificationCenter.default.publisher(for: Notification().walletConnectionStateChanged)
			.sink { [weak self] _ in
				Task {
					guard let self else { return }
					if self.walletIsConnected {
						await self.refresh()
					} else {
						self.tracks = []
					}
				}
			}
			.store(in: &cancels)
		
		audioPlayer.objectWillChange
			.receive(on: DispatchQueue.main)
			.sink { [weak self] _ in
				self?.objectWillChange.send()
			}
			.store(in: &cancels)
		
		Task { [weak self] in
			guard let self else { return }
			for await error in audioPlayer.errors.values {
				errors.append(error)
			}
		}
			
		Task {
			await refresh()
		}
	}
	
	var showNoSongsMessage: Bool {
		tracks.isEmpty || walletIsConnected == false
	}
	
	func refresh() async {
		defer {
			showLoading = false
		}
		
		guard walletIsConnected else { return }
		
		do {
			try await walletNFTTracksUseCase.refresh()
			tracks = try await walletNFTTracksUseCase.getAllNFTTracks()
		} catch {
			logger.logError(error)
			errors.append(NEWMError(errorDescription: "Unable to fetch songs.  Please try again."))
		}
	}
	
	func xPubScanned() {
		showXPubScanner = false
		showLoading = true
		Task {
			await refresh()
			showLoading = false
		}
	}
	
	func scannerDismissed() {
		showXPubScanner = false
	}
	
	func connectWallet() {
		showXPubScanner = true
	}
	
	private func downloadTrack(_ track: NFTTrack) {
		Task {
			do {
				try await audioPlayer.downloadTrack(track)
			} catch {
				let userDidCancelCode = -999
				if (error as NSError).code != userDidCancelCode {
					self.errors.append(NEWMError(errorDescription: "Could not download \"\(track.title)\".  Please try again later."))
				}
			}
		}
	}
	
	func removeDownloadedTrack(_ track: NFTTrack) {
		audioPlayer.removeDownloadedSong(track)
	}
	
	func trackTapped(_ track: NFTTrack) {
		if audioPlayer.playQueueIsEmpty {
			audioPlayer.setTracks(Set(tracks), playFirstTrack: false)
		}
		audioPlayer.seek(toTrack: track)
	}
	
	func swipeAction(for track: NFTTrack) {
		switch downloadState(for: track) {
		case .downloaded:
			audioPlayer.removeDownloadedSong(track)
		case .downloading:
			audioPlayer.cancelDownload(track)
		case nil:
			downloadTrack(track)
		}
	}
	
	func swipeText(for track: NFTTrack) -> String {
		switch downloadState(for: track) {
		case .downloaded:
			"Delete download"
		case .downloading:
			"Cancel"
		case nil:
			"Download"
		}
	}
	
	func loadingProgress(for track: NFTTrack) -> Double? {
		audioPlayer.loadingProgress[track]
	}
	
	func trackIsPlaying(_ track: NFTTrack) -> Bool {
		audioPlayer.trackIsPlaying(track)
	}
	
	func trackIsDownloaded(_ track: NFTTrack) -> Bool {
		audioPlayer.trackIsDownloaded(track)
	}
	
	private enum DownloadState {
		case downloaded
		case downloading
	}
	
	private func downloadState(for track: NFTTrack) -> DownloadState? {
		return if trackIsDownloaded(track) {
			.downloaded
		} else if loadingProgress(for: track) == nil {
			nil
		} else {
			.downloading
		}
	}
	
	func cycleTitleSort() {
		sort = switch sort {
		case .title(let ascending) where ascending == true:
				.title(ascending: false)
		case .title(let ascending) where ascending == false:
				.title(ascending: true)
		default:
				.title(ascending: true)
		}
	}
	
	func cycleArtistSort() {
		sort = switch sort {
		case .artist(let ascending) where ascending == true:
				.artist(ascending: false)
		case .artist(let ascending) where ascending == false:
				.artist(ascending: true)
		default:
				.artist(ascending: true)
		}
	}
	
	func cycleDurationSort() {
		sort = switch sort {
		case .duration(let ascending) where ascending == true:
				.duration(ascending: false)
		case .duration(let ascending) where ascending == false:
				.duration(ascending: true)
		default:
				.duration(ascending: true)
		}
	}

}
