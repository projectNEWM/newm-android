import Foundation
import SwiftUI
import ModuleLinker

extension LibraryModule: LibraryViewProviding {
	@MainActor
    public func libraryView() -> AnyView {
        LibraryView().erased
    }
}
