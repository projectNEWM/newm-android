import Utilities
import Combine
import SwiftUI
import shared
import AudioPlayer
import Resolver

@MainActor
class TrackRowModel: ObservableObject, Identifiable {
	private enum DownloadState {
		case downloaded
		case downloading
	}
	
	private let track: NFTTrack
	@Injected private var audioPlayer: VLCAudioPlayer
	private var cancels: Set<AnyCancellable> = []
	
	@Published var isPlaying: Bool = false
	@Published var isDownloaded: Bool = false
	@Published var loadingProgress: Double?
	let didError: (Error) -> ()
	
	var imageUrl: URL? { URL(string: track.imageUrl) }
	var title: String { track.title }
	var artist: String { track.artists.first ?? "" }
	nonisolated var id: String { track.id }
	var swipeText: String {
		switch downloadState {
		case .downloaded:
			"Delete download"
		case .downloading:
			"Cancel"
		case nil:
			"Download"
		}
	}
	private var downloadState: DownloadState? {
		return if isDownloaded {
			.downloaded
		} else if loadingProgress != nil {
			.downloading
		} else {
			nil
		}
	}

	init(track: NFTTrack, didError: @escaping (Error) -> ()) {
		self.track = track
		self.didError = didError
		
		isPlaying = audioPlayer.currentTrack == track
		isDownloaded = audioPlayer.trackIsDownloaded(track)
		loadingProgress = audioPlayer.loadingProgress[track]
		
		audioPlayer.$currentTrack.sink { [weak self] currentTrack in
			guard let self else { return }
			let isCurrentTrackPlaying = currentTrack == track
			if isCurrentTrackPlaying != isPlaying {
				isPlaying = isCurrentTrackPlaying
			}
		}.store(in: &cancels)
		
		audioPlayer.$loadingProgress.sink { [weak self] loadingProgress in
			guard let self else { return }
			if self.loadingProgress != loadingProgress[track] {
				self.loadingProgress = loadingProgress[track]
				self.isDownloaded = audioPlayer.trackIsDownloaded(track)
			}
		}.store(in: &cancels)
	}
	
	func trackTapped() {
		audioPlayer.seek(toTrack: track)
	}
	
	func swipeAction() async {
		switch downloadState {
		case .downloaded:
			await audioPlayer.removeDownloadedSong(track)
			isDownloaded = false
		case .downloading:
			audioPlayer.cancelDownload(track)
		case nil:
			await downloadTrack()
		}
	}
	
	private func downloadTrack() async {
		do {
			try await audioPlayer.downloadTrack(track)
		} catch {
			let userDidCancelCode = -999
			if (error as NSError).code != userDidCancelCode {
				didError(NEWMError(errorDescription: "Could not download \"\(track.title)\".  Please try again later."))
			}
		}
	}
}

extension NFTTrack: Identifiable {}
