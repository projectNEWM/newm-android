import SwiftUI
import Home

struct MainView: View {
	@StateObject private var viewModel = MainViewModel()
	
	var body: some View {
		TabView(selection: $viewModel.selectedTab) {
			HomeView()
				.tabItem {
					Image(MainViewModel.Tab.home)
					Text(MainViewModel.Tab.home)
				}
				.tag(MainViewModel.Tab.home)
			TribeView()
				.tabItem {
					Image(MainViewModel.Tab.tribe)
					Text(MainViewModel.Tab.tribe)
				}
				.tag(MainViewModel.Tab.tribe)
			StarsView()
				.tabItem {
					Image(MainViewModel.Tab.stars)
					Text(MainViewModel.Tab.stars)
				}
				.tag(MainViewModel.Tab.stars)
			WalletView()
				.tabItem {
					Image(MainViewModel.Tab.wallet)
					Text(MainViewModel.Tab.wallet)
				}
				.tag(MainViewModel.Tab.wallet)
			MoreTabView()
				.tabItem {
					Image(MainViewModel.Tab.more)
					Text(MainViewModel.Tab.more)
				}
				.tag(MainViewModel.Tab.more)
		}
		.preferredColorScheme(.dark)
	}
}

struct MoreTabView: View {
	var body: some View {
		NavigationView {
			List {
				NavigationLink(MainViewModel.MoreTab.playlists.description) {
					PlaylistListView(id: "1")
				}
				NavigationLink(MainViewModel.MoreTab.artists.description) {
					ArtistListView()
				}
				NavigationLink(MainViewModel.MoreTab.profile.description) {
					ProfileView()
				}
				NavigationLink(MainViewModel.MoreTab.search.description) {
					SearchView()
				}
				NavigationLink(MainViewModel.MoreTab.genres.description) {
					GenresView()
				}
			}
		}
	}
}

private extension Text {
	init(_ tab: MainViewModel.Tab) {
		self = Text(tab.description)
	}
}

private extension Text {
	init(_ tab: MainViewModel.MoreTab) {
		self = Text(tab.description)
	}
}

struct MainView_Previews: PreviewProvider {
	static var previews: some View {
		MainView()
	}
}
