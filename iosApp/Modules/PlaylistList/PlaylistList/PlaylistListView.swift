import SwiftUI
import SharedUI

public struct PlaylistListView: DataView {
	@ObservedObject var viewModel: PlaylistListViewModel
	
	public init(id: String) {
		viewModel = PlaylistListViewModel(id: id)
	}
	
	public var body: some View {
		ScrollView(.vertical, showsIndicators: false) {
			VStack {
				ForEach(viewModel.playlists) { playlist in
					PlaylistListViewCell(playlist: playlist)
						.padding(5)
						.padding([.leading, .trailing], 5)
				}
			}
		}
		.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
		.background(Color.black)
	}
}

struct PlaylistView_Previews: PreviewProvider {
	static var previews: some View {
		PlaylistListView(id: "1")
	}
}
