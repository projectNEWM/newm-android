import SwiftUI
import ModuleLinker
import Resolver
import SharedUI
import Fonts
import AudioPlayer
import shared
import Kingfisher
import Colors

public struct NowPlayingView: View {
	typealias Seconds = Int
	
	@InjectedObject private var audioPlayer: VLCAudioPlayer
	
	public var body: some View {
		ZStack {
			VStack {
				Spacer()
				title
				controls
			}
			.padding()
		}
		.background(background)
		.backButton()
		//TODO: make an "ErrorProviding" protocol, replace all these.
//		.alert(isPresented: .constant(audioPlayer.errors.currentError != nil), error: audioPlayer.errors.currentError) {
//			Button {
//				audioPlayer.errors.popFirstError()
//			} label: {
//				Text("Ok")
//			}
//		}
	}
}

//MARK: Subviews
extension NowPlayingView {
	@ViewBuilder
	private var background: some View {
		KFImage(audioPlayer.artworkUrl)
			.resizable(resizingMode: .stretch)
			.scaledToFill()
	}
	
	@ViewBuilder
	private var title: some View {
		VStack(alignment: .center) {
			Text(audioPlayer.title ?? "--")
				.font(Font.ralewayExtraBold(ofSize: 24))
			Text(audioPlayer.artist ?? "--")
				.font(Font.interMedium(ofSize: 14))
		}
	}
	
	private var playbackTimeBinding: Binding<Float> {
		Binding<Float>(get: {
			Float(audioPlayer.currentTime ?? 0)
		}, set: { playbackTime in
			audioPlayer.seek(toTime: Double(playbackTime))
		})
	}
	
	@ViewBuilder
	fileprivate var controls: some View {
		VStack(spacing: 0) {
			Slider(value: playbackTimeBinding, in: 0.0...Float(audioPlayer.duration ?? 300))
				.tint(Gradients.loginGradient.gradient)
				.zIndex(1)
			
			VStack {
				HStack {
					Text("\(audioPlayer.currentTime.playbackTimeString)")
					Spacer()
					Text("\(audioPlayer.duration.playbackTimeString)")
				}
				.foregroundStyle(try! Color(hex: "8F8F91"))
				.font(Font.inter(ofSize: 12))
				
				Spacer()
				
				HStack {
					repeatButton
					Spacer()
					prevButton
					playButton
					nextButton
					Spacer()
					shuffleButton
				}
				.padding()
			}
			.padding()
			.background(Color.black)
			.clipShape(UnevenRoundedRectangle(cornerRadii: RectangleCornerRadii(bottomLeading: 8, bottomTrailing: 8), style: .continuous))
			.frame(height: 108)
		}
	}
	
	@ViewBuilder
	private var playButton: some View {
		PlayButton().padding([.trailing, .leading], 26)
			.scaleEffect(CGSize(width: 1.5, height: 1.5))
			.frame(width: 50, height: 50)
	}
	
	@ViewBuilder
	private var shuffleButton: some View {
		Button {
			audioPlayer.shuffle.toggle()
		} label: {
			if audioPlayer.shuffle {
				Image(systemName: "shuffle")
					.foregroundStyle(Gradients.mainPrimary)
			} else {
				Image(systemName: "shuffle")
			}
		}
		.tint(audioPlayer.shuffle ? NEWMColor.pink() : .white)
		.scaleEffect(CGSize(width: 1.5, height: 1.5))
	}
	
	@ViewBuilder
	private var prevButton: some View {
		Button {
			audioPlayer.prev()
		} label: {
			Image(systemName: "backward.end.fill")
				.tint(.white)
		}
		.scaleEffect(CGSize(width: 1.5, height: 1.5))
		.disabled(audioPlayer.hasPrevTrack == false)
	}
	
	@ViewBuilder
	private var nextButton: some View {
		Button {
			audioPlayer.next()
		} label: {
			Image(systemName: "forward.end.fill")
				.tint(.white)
		}
		.scaleEffect(CGSize(width: 1.5, height: 1.5))
		.disabled(audioPlayer.hasNextTrack == false)
	}
	
	@ViewBuilder
	private var repeatButton: some View {
		Button {
			audioPlayer.cycleRepeatMode()
		} label: {
			switch audioPlayer.repeatMode {
			case .all:
				Image(systemName: "repeat")
					.tint(Gradients.mainPrimary)
			case .one:
				Image(systemName: "repeat.1")
					.tint(Gradients.mainPrimary)
			case .none:
				Image(systemName: "repeat")
					.tint(.white)
			}
		}
		.scaleEffect(CGSize(width: 1.5, height: 1.5))
	}
}

#Preview {
	Resolver.root = Resolver(child: .main)
	AudioPlayerModule.shared.registerAllMockedServices(mockResolver: .root)
	AudioPlayerModule.shared.registerAllServices()
	@InjectedObject var audioPlayer: VLCAudioPlayer
	audioPlayer.setTracks(Set(NFTTrack.mocks), playFirstTrack: true)
	return Group {
		NowPlayingView()
			.preferredColorScheme(.dark)
	}
	.padding()
}

func url(for testImage: ImageAsset) -> URL {
	guard let imageURL = NSURL(fileURLWithPath: NSTemporaryDirectory()).appendingPathComponent("\(testImage.name).png") else {
		fatalError()
	}
	
	let pngData = testImage.image.pngData()
	do { try pngData?.write(to: imageURL) } catch { }
	return imageURL
}
