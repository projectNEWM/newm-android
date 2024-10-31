import Foundation
import AVFoundation.AVPlayer
import shared
import VLCKitSPM
import Resolver
import Files
import Combine
import ModuleLinker
import Utilities
import OrderedCollections

@MainActor
public class VLCAudioPlayer: ObservableObject {
	public enum PlaybackState {
		case playing
		case paused
		case stopped
		case buffering
	}
	static let sharedPlayer = VLCAudioPlayer()
	
	private var mediaPlayer: VLCMediaPlayer
	private var playQueue = PlayQueue() {
		didSet {
			update()
		}
	}
	@Published private var fileManager = FileManagerService()
	lazy private var delegate: VLCAudioPlayerDelegate = VLCAudioPlayerDelegate()
	private var cancels = Set<AnyCancellable>()
	
	@Published public private(set) var loadingProgress: [NFTTrack: Double] = [:]
	
	private var _errors = PassthroughSubject<Error, Never>()
	public var errors: AnyPublisher<Error, Never> { _errors.eraseToAnyPublisher() }

	@Published public var state: PlaybackState = .stopped
	@Published public var duration: TimeInterval?
	@Published public var currentTime: VLCTime?
	@Published public var percentPlayed: Float?
	@Published public var title: String?
	@Published public var artist: String?
	@Published public var artworkUrl: URL?
	@Published public var currentTrack: NFTTrack?
	@Published public var isPlaying: Bool = false

	@Injected var errorReporter: any ErrorReporting
		
	public var textFilter: String? {
		get { playQueue.textFilter }
		set { playQueue.textFilter = newValue }
	}
	
	public var durationFilter: Int? {
		get { playQueue.durationFilter }
		set { playQueue.durationFilter = newValue }
	}

	public var shuffle: Bool {
		get { playQueue.shuffle }
		set { playQueue.shuffle = newValue }
	}
	
	public var repeatMode: RepeatMode {
		get { playQueue.repeatMode }
		set { playQueue.repeatMode = newValue }
	}
	
	public var sort: Sort {
		get { playQueue.sortCriteria }
		set { playQueue.sortCriteria = newValue }
	}
	
	private init() {
		mediaPlayer = VLCMediaPlayer()
		mediaPlayer.delegate = delegate
		fileManager.objectWillChange
			.receive(on: DispatchQueue.main)
			.sink { [weak self] in
				self?.update()
			}.store(in: &cancels)
		
		NotificationCenter.default.publisher(for: Notification.Name(Notification().walletConnectionStateChanged)).sink { [weak self] _ in
			self?.handleWalletDisconnect()
		}.store(in: &cancels)
		
		setUpDelegateHandling()
		setupRemoteTransportControls()
		
		objectWillChange.sink { [weak self] _ in
			Task {
				await self?.updateIOSNowPlayingInfo()
			}
		}.store(in: &cancels)
	}
	
	private func setUpDelegateHandling() {
		Task {
			for await _ in delegate.stream {
				if mediaPlayer.state == .ended, let _ = playQueue.nextTrack() {
					playCurrentTrackInQueue()
				} else if mediaPlayer.state == .error {
					_errors.send("Unable to load \(currentTrack?.title ?? "song")")
				} else if mediaPlayer.time != currentTime {
					Task { @MainActor in
						update()
					}
				}
			}
		}
	}
	
	private func update() {
		title = mediaPlayer.media?.metaData.title
		duration = mediaPlayer.media?.length.seconds
		currentTime = mediaPlayer.time
		percentPlayed = mediaPlayer.position
		title = mediaPlayer.media?.metaData.title
		artist = mediaPlayer.media?.metaData.artist
		artworkUrl = mediaPlayer.media?.metaData.artworkURL
		currentTrack = try? playQueue.currentTrack()
		isPlaying = state == .playing
		state = if mediaPlayer.isPlaying {
			.playing
		} else if mediaPlayer.state == .buffering {
			.buffering
		} else if mediaPlayer.state == .paused {
			.paused
		} else {
			.stopped
		}
	}

	public var hasNextTrack: Bool {
		playQueue.hasNextTrack
	}
	
	public var hasPrevTrack: Bool {
		playQueue.hasPrevTrack
	}
	
	public func play() {
		mediaPlayer.play()
	}
		
	private func handleWalletDisconnect() {
		stop()
		playQueue.originalTracks = []
		removeDownloadedSongs()
	}
	
	private func playCurrentTrackInQueue() {
		stop()

		guard let currentTrack, let url = URL(string: currentTrack.audioUrl) else {
			mediaPlayer.media = nil
			return
		}
		
		do {
			let currentTrackVLC = currentTrack.vlcMedia(fileUrl: try fileManager.getPlaybackURL(for: url))
			mediaPlayer.media = currentTrackVLC
			play()
		} catch {
			_errors.send(error)
		}
	}
	
	public func downloadTrack(_ track: NFTTrack) async throws {
		guard trackIsDownloaded(track) == false, loadingProgress[track] == nil else {
			return
		}
		
		defer {
			loadingProgress.removeValue(forKey: track)
		}
		
		try await fileManager.download(track: track) { [weak self] progress in
			guard let self else { return }
			print("progress for [\(track.title)]: \(progress)")
			Task { @MainActor in
				self.loadingProgress[track] = progress
			}
		}
	}
	
	public func cancelDownload(_ track: NFTTrack) {
		fileManager.cancelDownload(track: track)
	}
	
	public func pause() {
		mediaPlayer.pause()
	}
	
	public func stop() {
		mediaPlayer.stop()
	}
	
	public func next() {
		playQueue.nextTrack(userInitiated: true)
		playCurrentTrackInQueue()
	}
	
	public func prev() {
		guard mediaPlayer.time.seconds ?? Double.infinity < 2 else {
			mediaPlayer.time = VLCTime(int: 0)
			return
		}
		playQueue.previousTrack()
		playCurrentTrackInQueue()
	}
	
	public func seek(toTrack track: NFTTrack) {
		guard track != currentTrack else {
			seek(toTime: 0)
			return
		}
		do {
			try playQueue.seekToTrack(track)
			playCurrentTrackInQueue()
		} catch {
			_errors.send(error)
		}
	}
	
	public func seek(toTime time: Double) {
		mediaPlayer.time = VLCTime(int: time.secondsToMilliseconds)
	}
	
	public var playQueueIsEmpty: Bool {
		playQueue.isEmpty
	}
	
	public func setTracks(_ tracks: Set<NFTTrack>, playFirstTrack: Bool = true) {
		playQueue.originalTracks = tracks
		if playFirstTrack, tracks.isEmpty == false {
			try? playQueue.seekToFirst()
			playCurrentTrackInQueue()
		}
	}
	
	public func trackIsDownloaded(_ track: NFTTrack) -> Bool {
		fileManager.fileExists(for: URL(string: track.audioUrl)!)
	}
	
	public func cycleRepeatMode() {
		playQueue.cycleRepeatMode()
	}
	
	public func removeDownloadedSongs() {
		fileManager.clearFiles()
	}
	
	public func removeDownloadedSong(_ song: NFTTrack) async {
		await fileManager.clearFile(at: URL(string: song.audioUrl)!)
	}
	
	deinit {
		Task { [weak self] in
			await self?.stop()
		}
	}
}
