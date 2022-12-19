import Foundation
import SwiftUI
import Resolver
import ModuleLinker
import SwiftUINavigation

extension LibraryView {
	struct Links: View {
		@Binding var route: LibraryRoute?
		@Injected private var playlistViewProvider: PlaylistViewProviding
		@Injected private var artistViewProvider: ArtistViewProviding
		
		var body: some View {
			ZStack {
				NavigationLink(unwrapping: $route,
							   case: /LibraryRoute.playlist,
							   destination: { $playlistId in
					playlistViewProvider.playlistView(id: playlistId)
				}, onNavigate: clearLinks, label: {})
				
				NavigationLink(unwrapping: $route,
							   case: /LibraryRoute.artist,
							   destination: { $artistId in
					artistViewProvider.artistView(id: artistId)
				}, onNavigate: clearLinks, label: {})
			}
		}
		
		private func clearLinks(isActive: Bool) {
			if isActive == false { route = nil }
		}
	}
}
