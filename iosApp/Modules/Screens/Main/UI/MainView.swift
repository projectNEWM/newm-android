import SwiftUI
import Resolver
import ModuleLinker
import TabBar
import AudioPlayer
import SharedUI
import Profile

public struct MainView: View {
	@StateObject var viewModel = MainViewModel()
	
	@Injected private var libraryViewProvider: LibraryViewProviding
	@Injected private var loginViewProvider: LoginViewProviding
	@Injected private var miniPlayerViewProvider: MiniNowPlayingViewProviding
	@Injected private var nowPlayingViewProvider: NowPlayingViewProviding
	@InjectedObject private var audioPlayer: VLCAudioPlayer
	
	@State var route: MainViewRoute?
	
	public var body: some View {
		GeometryReader { geometry in
			if viewModel.shouldShowLogin {
				loginViewProvider.loginView().transition(.move(edge: .bottom))
			} else {
				TabBar(tabProviders: tabProviders, bottomPadding: miniPlayerHeight)
					.preferredColorScheme(.dark)
					.sheet(isPresented: isPresent($route), onDismiss: { route = nil }) {
						sheetView
					}
					.overlay {
						Spacer()
						miniPlayerView
							.offset(x: 0, y: -geometry.safeAreaInsets.bottom+1)
							.transition(.move(edge: .bottom))
					}
					.transition(.move(edge: .bottom))
			}
		}
		.animation(.easeInOut, value: viewModel.shouldShowLogin)
	}
	
	@ViewBuilder
	private var miniPlayerView: some View {
		if showAudioPlayer {
			VStack {
				Spacer()
				miniPlayerViewProvider.miniNowPlayingView()
					.onTapGesture {
						route = .nowPlaying
					}
					.padding(.bottom)
			}
		}
	}
	
	private var miniPlayerHeight: CGFloat {
		return audioPlayer.isPlaying ? 42 : 0
	}
	
	private var showAudioPlayer: Bool {
		switch audioPlayer.state {
		case .buffering, .ended, .playing, .paused, .opening:
			return true
		default:
			return false
		}
	}
	
	@ViewBuilder
	private var sheetView: some View {
		switch route {
		case .nowPlaying: nowPlayingViewProvider.nowPlayingView()
		default: EmptyView()
		}
	}
	
	private var tabProviders: [TabViewProvider] {
		[
			TabViewProvider(image: Image(MainViewModelTab.library), tabName: MainViewModelTab.library.description) {
				libraryViewProvider.libraryView()
			},
			TabViewProvider(image: Image(MainViewModelTab.profile), tabName: MainViewModelTab.profile.description) {
				ProfileView().erased
			}
		]
	}
}

//#if DEBUG
struct MainView_Previews: PreviewProvider {
	static var previews: some View {
		Resolver.root = Resolver.mock
		MainModule.shared.registerAllServices()
		AudioPlayerModule.shared.registerAllServices()
		return MainView()
	}
}
//#endif

extension MainViewRoute: Identifiable {
	var id: Self { self }
}

func url(for testImage: ImageAsset) -> URL {
	guard let imageURL = NSURL(fileURLWithPath: NSTemporaryDirectory()).appendingPathComponent("\(testImage.name).png") else {
		fatalError()
	}
	
	let pngData = testImage.image.pngData()
	do { try pngData?.write(to: imageURL) } catch { }
	return imageURL
}
