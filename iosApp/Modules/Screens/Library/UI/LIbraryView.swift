import Foundation
import SwiftUI
import Resolver
import SharedUI
import shared
import Fonts
import Colors
import ModuleLinker
import Kingfisher
import AudioPlayer

struct LibraryView: View {
	@StateObject private var viewModel = LibraryViewModel()
	@State var showFilter = false
	
	public var body: some View {
		NavigationView {
			Group {
				if viewModel.showLoading {
					loadingView
				} else if viewModel.showNoSongsMessage {
					noSongsMessage
				} else {
					loadedView
				}
			}
			.refreshable {
				await viewModel.refresh()
			}
			.sheet(isPresented: .constant(viewModel.showXPubScanner), onDismiss: {
				viewModel.scannerDismissed()
			}) {
				XPubScannerView {
					viewModel.xPubScanned()
				}
			}
			.sheet(isPresented: $showFilter) {
				filterView
					.presentationDetents([.medium])
			}
			.alert(isPresented: .constant(viewModel.errors.currentError != nil), error: viewModel.errors.currentError) {
				Button {
					viewModel.errors.popFirstError()
				} label: {
					Text("Ok")
				}
			}
			.navigationBarTitleDisplayMode(.inline)
			.toolbar {
				ToolbarItem(placement: .topBarLeading) {
					Text(String.library)
						.font(.newmTitle1)
						.foregroundStyle(Gradients.libraryGradient.gradient)
				}
				ToolbarItem(placement: .topBarTrailing) {
					Button(action: {
						showFilter = true
					}, label: {
						Image("Filter Icon")
							.resizable()
							.renderingMode(.template)
							.tint(.white)
							.frame(width: 30, height: 30)
					})
				}
			}
		}
	}
	
	@ViewBuilder
	private var noSongsMessage: some View {
		ZStack {
			ScrollView {}
				.background {
					VStack {
						Text("Your library is empty.")
							.font(
								Font.custom("Inter", size: 24)
									.weight(.bold)
							)
							.multilineTextAlignment(.center)
							.foregroundColor(.white)
							.frame(width: 358, alignment: .top)
						
						Text("Time to rescue it with your epic music stash! 🎶")
							.font(
								Font.custom("Inter", size: 14)
									.weight(.medium)
							)
							.multilineTextAlignment(.center)
							.foregroundColor(Color(red: 0.56, green: 0.56, blue: 0.57))
							.frame(width: 358, alignment: .top)
					}
				}
			if viewModel.walletIsConnected == false {
				VStack {
					Spacer()
					ConnectWalletAlertView {
						viewModel.connectWallet()
					}
					.padding()
				}
			}
		}
	}
	
	@ViewBuilder
	private var loadingView: some View {
		ProgressView()
			.progressViewStyle(.circular)
	}
	
	@ViewBuilder
	private var loadedView: some View {
		NavigationView {
			List {
				ForEach(viewModel.filteredSortedNFTTracks, id: \.id) { audioTrack in
					row(for: audioTrack)
						.frame(height: 40)
						.padding(.leading, -6)
						.padding([.bottom, .top], -1)
						.swipeActions(allowsFullSwipe: false) {
							Button {
								viewModel.swipeAction(for: audioTrack)
							} label: {
								Text(viewModel.swipeText(for: audioTrack))
							}
						}
				}
				.listRowSeparator(.hidden)
			}
		}
		.searchable(text: $viewModel.searchText, prompt: "Search")
	}
	
	@ViewBuilder
	fileprivate func row(for track: NFTTrack) -> some View {
		Button(action: {
			viewModel.trackTapped(track)
		}) {
			HStack {
				KFImage(URL(string: track.imageUrl))
					.placeholder {
						Image.placeholder
							.resizable()
							.frame(width: 40, height: 40)
							.clipShape(RoundedRectangle(cornerRadius: 4))
					}
					.setProcessor(DownsamplingImageProcessor(size: CGSize(width: 40, height: 40)))
					.clipShape(RoundedRectangle(cornerRadius: 4))
				
				VStack(alignment: .leading, spacing: 3) {
					Text(track.title)
						.font(Font.interMedium(ofSize: 14))
						.foregroundStyle(viewModel.trackIsPlaying(track) ? NEWMColor.pink() : .white)
					HStack(alignment: .center, spacing: 4) {
						if viewModel.trackIsDownloaded(track) {
							Asset.Media.checkboxCircleFill.swiftUIImage
						}
						Text(track.artists.first ?? "")
							.font(Font.inter(ofSize: 12))
							.foregroundStyle(try! Color(hex: "8F8F91"))
					}
				}
				.padding(.leading, 4)
				
				Spacer()
				
				progressView(for: track)
			}
			.foregroundStyle(.white)
			.tag(track.id)
		}
	}
	
	@ViewBuilder
	private func progressView(for track: NFTTrack) -> some View {
		if let progress = viewModel.loadingProgress(for: track) {
			if 0 < progress, progress < 1 {
				Gauge(value: progress, in: 0...1) { }
					.gaugeStyle(.accessoryCircularCapacity)
					.scaleEffect(0.5)
			} else {
				ProgressView()
			}
		} else {
			EmptyView()
		}
	}
	
	@ViewBuilder
	var filterView: some View {
		List {
			Group {
				Picker(selection: Binding<Int>(get: {
					viewModel.durationFilter
				}, set: { value in
					viewModel.durationFilter = value
				})) {
					ForEach(Array(stride(from: 5, through: 600, by: 5)), id: \.self) { value in
						Text("\(value.playbackTimeString)").tag(value)
					}
				} label: {
					Text("Filter songs under")
				}
				.foregroundStyle(NEWMColor.midMusic.swiftUIColor)
				
				Section("Sort by") {
					Button {
						viewModel.cycleTitleSort()
					} label: {
						filterRow(title: "Title") {
							if case .title = viewModel.sort {
								return rightView(for: viewModel.sort)
							}
							return EmptyView()
						}
					}
					
					Button {
						viewModel.cycleArtistSort()
					} label: {
						filterRow(title: "Artist") {
							if case .artist = viewModel.sort {
								return rightView(for: viewModel.sort)
							}
							return EmptyView()
						}
					}
					
					Button {
						viewModel.cycleLengthSort()
					} label: {
						filterRow(title: "Length") {
							if case .length = viewModel.sort {
								return rightView(for: viewModel.sort)
							}
							return EmptyView()
						}
					}
				}
			}
			.listRowBackground(try! Color(hex: "#1C0707"))
			.listRowSeparator(.hidden)
		}
	}
	
	@ViewBuilder
	func filterRow(title: String, rightView: () -> any View = { EmptyView() }) -> some View {
		HStack {
			Text(title)
			Spacer()
			rightView().erased
		}
		.foregroundStyle(NEWMColor.midMusic.swiftUIColor)
	}
	
	@ViewBuilder
	private func rightView(for sort: AudioPlayerSort?) -> some View {
		switch sort {
		case
				.artist(let ascending) where ascending == true,
				.length(let ascending) where ascending == true,
				.title(let ascending) where ascending == true:
			Image(systemName: "chevron.up")
		case
				.artist(let ascending) where ascending == false,
				.length(let ascending) where ascending == false,
				.title(let ascending) where ascending == false:
			Image(systemName: "chevron.down")
		default:
			EmptyView()
		}
	}
}

#if DEBUG
struct LibraryView_Previews: PreviewProvider {
	static var previews: some View {
		LibraryModule.shared.registerAllMockedServices(mockResolver: .mock)
		Resolver.root = Resolver.mock
		let mockResolver = Resolver(child: .root)
		mockResolver.register {
			let useCase = Resolver.mock.resolve(ConnectWalletUseCase.self)
			useCase.connect(xpub: "xpub3833")
			return useCase as ConnectWalletUseCase
		}
		Resolver.root = mockResolver
		return Group {
			LibraryView()
			LibraryView()
				.row(for: NFTTrackMocksKt.mockTracks.first!)
				.padding()
				.previewDisplayName("Row")
		}
		.preferredColorScheme(.dark)
		.tint(.white)
	}
}

struct LibraryView_Previews2: PreviewProvider {
	static var previews: some View {
		LibraryModule.shared.registerAllMockedServices(mockResolver: .mock)
		Resolver.root = Resolver.mock
		return LibraryView(showFilter: true)
			.preferredColorScheme(.dark)
			.tint(.white)
			.background(.black)
	}
}
#endif
