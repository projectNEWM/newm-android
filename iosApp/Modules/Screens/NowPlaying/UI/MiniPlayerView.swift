import SwiftUI
import SharedUI
import AudioPlayer
import Resolver
import ModuleLinker
import Colors
import Kingfisher

struct MiniPlayerView: View {
	@InjectedObject private var audioPlayer: VLCAudioPlayer
	
	private let iconSize: CGFloat = 28
	
	var body: some View {
		VStack(alignment: .leading, spacing: 0) {
			progress.frame(height: 2)
			HStack(alignment: .center) {
				image.fixedSize()
				songInfo
				Spacer()
				PlayButton().frame(width: iconSize, height: iconSize)
			}
			.padding()
		}
		.background(.black)
	}
	
	@ViewBuilder
	private var progress: some View {
		GeometryReader { geometry in
			LinearGradient(colors: [
				try! Color(hex: "DC3CAA")
			],
						   startPoint: .leading,
						   endPoint: UnitPoint(x: (geometry.size.width / CGFloat(audioPlayer.percentPlayed ?? 0)) / geometry.size.width, y: 0.5))
			.frame(width: geometry.size.width * CGFloat(audioPlayer.percentPlayed ?? 0))
		}
	}
	
	@ViewBuilder
	private var songInfo: some View {
		VStack(alignment: .leading) {
			Text(audioPlayer.title ?? "")
				.font(.inter(ofSize: 14).bold())
			Text(audioPlayer.artist ?? "")
				.font(.inter(ofSize: 12))
		}
	}
	
	@ViewBuilder
	private var image: some View {
		if let image = audioPlayer.artworkUrl {
			KFImage(image)
				.setProcessor(DownsamplingImageProcessor(size: CGSize(width: iconSize, height: iconSize)))
				.appendProcessor(RoundCornerImageProcessor(radius: Radius.point(4)))
				.placeholder {
					Image.placeholder
						.resizable()
						.frame(width: iconSize, height: iconSize)
						.clipShape(RoundedRectangle(cornerRadius: 4))
				}
				.padding(.trailing, 8)
		}
	}
}

import shared

struct MiniPlayerView_Previews: PreviewProvider {
	static var previews: some View {
		AudioPlayerModule.shared.registerAllServices()
		Resolver.resolve(VLCAudioPlayer.self).setTracks([NFTTrack.mocks.first!])
		return ZStack {
			Color.white
			MiniPlayerView()
				.preferredColorScheme(.dark)
		}
	}
}
