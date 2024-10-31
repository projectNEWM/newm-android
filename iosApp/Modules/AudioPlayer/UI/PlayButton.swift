import SwiftUI
import SharedUI
import Resolver
import ModuleLinker
import shared
import Analytics

public struct PlayButton: View {
	@InjectedObject private var audioPlayer: VLCAudioPlayer
	
	public init() {}
	
	public var body: some View {
		Button {
			switch audioPlayer.state {
			case .playing:
				audioPlayer.pause()
				logPauseTapped()
			case .paused, .stopped:
				audioPlayer.play()
				logPlayTapped()
			case .buffering:
				break
			}
		} label: {
			switch audioPlayer.state {
			case .buffering:
				ProgressView()
			case .paused, .stopped:
				playButton
			case .playing:
				pauseButton
			}
		}
		.tint(.white)
		.disabled(isDisabled)
	}
	
	@ViewBuilder
	private var playButton: some View {
		Image(systemName: "play.fill")
	}
	
	@ViewBuilder
	private var pauseButton: some View {
		Image(systemName: "pause.fill")
	}
	
	private var isDisabled: Bool {
		audioPlayer.state == .buffering
	}
}

extension PlayButton {
	private func logPauseTapped() {
		NEWMAnalytics.trackClickEvent(
			buttonName: AppScreens.MusicPlayerScreen().PAUSE_BUTTON,
			screenName: AppScreens.MusicPlayerScreen().name,
			properties:
				[
					"song_id": audioPlayer.currentTrack?.id ?? "",
				]
		)
	}
	
	private func logPlayTapped() {
		NEWMAnalytics.trackClickEvent(
			buttonName: AppScreens.MusicPlayerScreen().PLAY_BUTTON,
			screenName: AppScreens.MusicPlayerScreen().name,
			properties:
				[
					"song_id": audioPlayer.currentTrack?.id ?? "",
				]
		)
	}
}

#Preview {
	PlayButton()
		.preferredColorScheme(.dark)
}
