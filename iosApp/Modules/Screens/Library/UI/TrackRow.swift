import SwiftUI
import Kingfisher
import Colors
import SharedUI

struct TrackRow: View {
	@ObservedObject private var viewModel: TrackRowModel
	
	init(viewModel: TrackRowModel) {
		self.viewModel = viewModel
	}
	
	var body: some View {
		Button(action: {
			viewModel.trackTapped()
		}) {
			HStack {
				KFImage(viewModel.imageUrl)
					.placeholder {
						Image.placeholder
							.resizable()
							.frame(width: 40, height: 40)
							.clipShape(RoundedRectangle(cornerRadius: 4))
					}
					.setProcessor(DownsamplingImageProcessor(size: CGSize(width: 40, height: 40)))
					.clipShape(RoundedRectangle(cornerRadius: 4))

				VStack(alignment: .leading, spacing: 3) {
					Text(viewModel.title)
						.font(Font.interMedium(ofSize: 14))
						.foregroundStyle(viewModel.isPlaying ? NEWMColor.success() : .white)
					Text(viewModel.artist)
						.font(Font.inter(ofSize: 12))
						.foregroundStyle(try! Color(hex: "8F8F91"))
				}
				.padding(.leading, 4)
				
				Spacer()
				
				downloadView()
					.tint(NEWMColor.success())
			}
			.padding()
		}
		.frame(height: 40)
		.padding(.leading, -6)
		.padding([.bottom, .top], -1)
		.swipeActions {
			Button {
				Task {
					await viewModel.swipeAction()
				}
			} label: {
				VStack(alignment: .center) {
					Text(viewModel.swipeText)
						.font(.interMedium(ofSize: 12))
				}
			}
			.tint(NEWMColor.success())
		}
		.id(viewModel.id)
	}
	
	@ViewBuilder
	private func downloadView() -> some View {
		if viewModel.isDownloaded {
			Asset.Media.fileDownloadFill()
				.renderingMode(.template)
				.foregroundStyle(NEWMColor.success())
		} else if let progress = viewModel.loadingProgress {
			if 0 < progress, progress < 1 {
				Gauge(value: progress, in: 0...1) { }
					.gaugeStyle(.accessoryCircularCapacity)
					.scaleEffect(0.5)
					.padding(.trailing, -20)
			} else {
				ProgressView()
			}
		} else {
			EmptyView()
		}
	}
}
