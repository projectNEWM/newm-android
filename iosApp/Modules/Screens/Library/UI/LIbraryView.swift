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
import Mocks
import Analytics
import UIKit

@MainActor
struct LibraryView: View {
	@StateObject private var viewModel = LibraryViewModel()
	@State private var showFilter = false
	static var count = 0

	public var body: some View {
		ZStack {
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
					trackRefresh()
					await viewModel.refresh()
				}
				.sheet(isPresented: .constant(viewModel.showCodeScanner), onDismiss: {
					viewModel.scannerDismissed()
				}) {
					ConnectWalletToAccountScannerView {
						viewModel.codeScanned()
					}
				}
				.sheet(isPresented: $showFilter) {
					filterView
						.presentationDetents([.height(350)])
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
					if viewModel.filteredSortedTracks.isEmpty == false {
						ToolbarItem(placement: .topBarTrailing) {
							Button(action: {
								showFilter = true
							}, label: {
								Image("Filter Icon")
									.resizable()
									.renderingMode(.template)
									.frame(width: 40, height: 40)
									.foregroundStyle(Gradients.mainPrimary)
							})
						}
					}
				}
			}
			
			Color.black
				.opacity(showFilter ? 0.5 : 0)
				.animation(.easeInOut, value: showFilter)
				.ignoresSafeArea()
		}
	}
	
	private func trackRefresh() {
		NEWMAnalytics.trackClickEvent(
			buttonName: AppScreens.NFTLibraryScreen().REFRESH_BUTTON,
			screenName: AppScreens.NFTLibraryScreen().name,
			properties: nil
		)
	}
	
	@ViewBuilder
	fileprivate var noSongsMessage: some View {
		ScrollView {
			ZStack {
				VStack {
					Text("Your library is empty.")
						.font(
							Font.custom("Inter", size: 24)
								.weight(.bold)
						)
						.multilineTextAlignment(.center)
						.foregroundColor(.white)
						.frame(width: 358, alignment: .top)
						.padding(.bottom)
					
					Text("Time to rescue it with your epic music stash!\nLet’s fill this up. 🎶")
						.font(
							Font.custom("Inter", size: 14)
								.weight(.medium)
						)
						.multilineTextAlignment(.center)
						.foregroundColor(Color(red: 0.56, green: 0.56, blue: 0.57))
						.frame(width: 358, alignment: .top)
						.padding(.bottom)
					
					if viewModel.walletIsConnected {
						Button {
							trackRecordStore()
							UIApplication.shared.open(URL(string: "https://newm.io/recordstore")!)
						} label: {
							Text("Visit Record Store")
								.frame(maxWidth: .infinity)
								.padding()
								.background(Gradients.mainPrimary.opacity(0.08))
								.foregroundColor(NEWMColor.midMusic())
								.cornerRadius(8)
						}
					}
				}
				if viewModel.walletIsConnected == false {
					VStack {
						Spacer()
						ConnectWalletAlertView {
							viewModel.connectWallet()
						}
					}
					.padding()
				}
			}
		}
		.analyticsScreen(name: AppScreens.NFTLibraryEmptyWalletScreen().name)
	}
	
	private func trackRecordStore() {
		NEWMAnalytics.trackClickEvent(
			buttonName: AppScreens.NFTLibraryEmptyWalletScreen().VISIT_RECORDS_BUTTON,
			screenName: AppScreens.NFTLibraryEmptyWalletScreen().name,
			properties: nil
		)
	}
	
	@ViewBuilder
	private var loadingView: some View {
		ProgressView()
			.progressViewStyle(.circular)
	}
	
	@ViewBuilder
	private var loadedView: some View {
		List(viewModel.filteredSortedTracks) { track in
			TrackRow(viewModel: TrackRowModel(track: track) { error in
				viewModel.errors.append(error)
			})
		}
		.listRowSeparator(.hidden)
		.searchable(text: $viewModel.searchText, prompt: "Search")
		.analyticsScreen(name: AppScreens.NFTLibraryScreen().name)
	}
	
	@ViewBuilder
	var filterView: some View {
		ZStack {
			Color.black
			VStack(alignment: .leading, spacing: 20) {
				Text("Filter songs under")
					.foregroundColor(.white)
					.font(.inter(ofSize: 14))
				
				Button(action: {
					viewModel.toggleLengthFilter()
				}) {
					HStack {
						Text("30 seconds")
							.foregroundColor(viewModel.durationFilter == 30 ? .black : NEWMColor.midMusic())
						Spacer()
						Image(systemName: "checkmark")
							.foregroundColor(.black)
					}
					.padding()
					.background(viewModel.durationFilter == 30 ? NEWMColor.midMusic().erased : Gradients.mainPrimaryLight.erased)
					.cornerRadius(8)
				}
				.frame(height: 40)
				
				Text("Sort by")
					.foregroundColor(.white)
					.padding(.top)
					.font(.inter(ofSize: 14))

				VStack(alignment: .leading, spacing: 10) {
					Button(action: {
						viewModel.titleSortTapped()
					}) {
						Text(viewModel.titleSortButtonTitle)
							.frame(maxWidth: .infinity, alignment: .leading)
							.padding()
							.background(viewModel.titleSortSelected ? NEWMColor.midMusic().erased : Gradients.mainPrimaryLight.erased)
							.foregroundColor(viewModel.titleSortSelected ? .black : NEWMColor.midMusic.swiftUIColor)
							.cornerRadius(8)
							.font(.interMedium(ofSize: 14))
					}
					
					Button(action: {
						viewModel.artistSortTapped()
					}) {
						Text(viewModel.artistSortButtonTitle)
							.frame(maxWidth: .infinity, alignment: .leading)
							.padding()
							.background(viewModel.artistSortSelected ? NEWMColor.midMusic().erased : Gradients.mainPrimaryLight.erased)
							.foregroundColor(viewModel.artistSortSelected ? .black : NEWMColor.midMusic.swiftUIColor)
							.cornerRadius(8)
							.font(.interMedium(ofSize: 14))
					}
					
					Button(action: {
						viewModel.durationSortTapped()
					}) {
						Text(viewModel.durationSortButtonTitle)
							.frame(maxWidth: .infinity, alignment: .leading)
							.padding()
							.background(viewModel.durationSortSelected ? NEWMColor.midMusic().erased : Gradients.mainPrimaryLight.erased)
							.foregroundColor(viewModel.durationSortSelected ? .black : NEWMColor.midMusic.swiftUIColor)
							.cornerRadius(8)
							.font(.interMedium(ofSize: 14))
					}
				}
			}
		}
		.padding()
		.padding(.top, 15)
		.background(Color.black)
		.clipShape(RoundedRectangle(cornerSize: CGSize(width: 20, height: 20)))
		.ignoresSafeArea()
		.analyticsScreen(name: AppScreens.NFTLibraryFilterScreen().name)
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
	private func rightView(for sort: Sort?) -> some View {
		switch sort {
		case
				.artist(let ascending) where ascending == true,
				.duration(let ascending) where ascending == true,
				.title(let ascending) where ascending == true:
			Image(systemName: "chevron.up")
		case
				.artist(let ascending) where ascending == false,
				.duration(let ascending) where ascending == false,
				.title(let ascending) where ascending == false:
			Image(systemName: "chevron.down")
		default:
			EmptyView()
		}
	}
}



#if DEBUG
#Preview {
	Resolver.root = .mock
	LibraryModule.shared.registerAllMockedServices(mockResolver: .mock)
	return LibraryView()//.noSongsMessage
		.preferredColorScheme(.dark)
		.tint(.white)
		.background(.black)
}
#endif
