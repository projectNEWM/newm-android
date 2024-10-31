import Foundation
import SwiftUI

public protocol LibraryViewProviding {
	@MainActor
	func libraryView() -> AnyView
}
