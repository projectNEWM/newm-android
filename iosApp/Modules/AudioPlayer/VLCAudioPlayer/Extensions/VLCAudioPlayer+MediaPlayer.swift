import Foundation
import MediaPlayer
import Kingfisher

extension VLCAudioPlayer {
	func updateIOSNowPlayingInfo() async {
		var nowPlayingInfo = [String: Any]()
		
		if let title = title {
			nowPlayingInfo[MPMediaItemPropertyTitle] = title
		} else {
			errorReporter.logError("Couldn't get audio player title for track: \(currentTrack.debugDescription)")
		}
		
		if let artworkUrl {
			do {
				let image = try await withCheckedThrowingContinuation { continuation in
					KingfisherManager.shared.retrieveImage(with: KF.ImageResource(downloadURL: artworkUrl)) { result in
						continuation.resume(with: result)
					}
				}.image
				
				nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: image.size) { size in
					return KingfisherWrapper(image).image(withRoundRadius: 0, fit: size)
				}
			} catch {
				errorReporter.logError("Couldn't get artwork for track: \(currentTrack.debugDescription)")
			}
		}
		
		if let artist {
			nowPlayingInfo[MPMediaItemPropertyArtist] = artist
		}
	
		if let duration {
			nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration
		}
		
		if let seconds = currentTime?.seconds {
			nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = seconds
		}
				
		MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
	}
	
	func setupRemoteTransportControls() {
		let commandCenter = MPRemoteCommandCenter.shared()
		commandCenter.playCommand.addTarget { [weak self] _ in
			self?.play()
			return .success
		}
		commandCenter.pauseCommand.addTarget { [weak self] _ in
			self?.pause()
			return .success
		}
		commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
			guard let self, let event = event as? MPChangePlaybackPositionCommandEvent else {
				return .commandFailed
			}
			seek(toTime: event.positionTime)
			return .success
		}
		commandCenter.changeRepeatModeCommand.addTarget { [weak self] event in
			guard let self, let event = (event as? MPChangeRepeatModeCommandEvent) else {
				return .commandFailed
			}
			repeatMode = event.repeatType.repeatMode ?? .none
			return .success
		}
		commandCenter.changeShuffleModeCommand.addTarget { [weak self] event in
			guard let self, let event = (event as? MPChangeShuffleModeCommandEvent) else {
				return .commandFailed
			}
			shuffle = event.shuffleType.shuffle
			return .success
		}
		commandCenter.nextTrackCommand.addTarget { [weak self] _ in
			guard let self else {
				return .commandFailed
			}
			next()
			return .success
		}
		commandCenter.previousTrackCommand.addTarget { [weak self] _ in
			guard let self else {
				return .commandFailed
			}
			self.prev()
			return .success
		}
		commandCenter.togglePlayPauseCommand.addTarget { [weak self] _ in
			guard let self else {
				return .commandFailed
			}
			if self.isPlaying {
				self.pause()
			} else {
				self.play()
			}
			return .success
		}
		commandCenter.stopCommand.addTarget { [weak self] _ in
			self?.stop()
			return .success
		}
	}
}

private extension MPRepeatType {
	var repeatMode: RepeatMode? {
		return switch self {
		case .all: .all
		case .off: nil
		case .one: .one
		default: nil
		}
	}
}

private extension MPShuffleType {
	var shuffle: Bool {
		return switch self {
		case .items, .collections: true
		case .off: false
		default: false
		}
	}
}

