import SwiftUI
import Foundation
import Fonts
import Colors
import SharedUI

//TODO: Come up with a better name for SupportButton.
struct SupportButton: View {
	let titleText: String
	let backgroundColor: any ShapeStyle
	let iconImage: Image
	let borderColor: Color?
	
	private let cornerRadius: CGFloat = 4
	
	init(titleText: String, backgroundColor: any ShapeStyle, iconImage: Image, borderColor: Color?) {
		self.titleText = titleText
		self.backgroundColor = backgroundColor
		self.iconImage = iconImage
		self.borderColor = borderColor
	}
	
	var body: some View {
		Button {
			print("\(titleText) tapped")
		} label: {
			HStack {
				icon
				title
			}
			.padding([.top, .bottom], 10)
			.frame(width: 120, height: 35)
			.background(backgroundColor)
			.cornerRadius(cornerRadius)
			.overlay(RoundedRectangle(cornerRadius: cornerRadius)
				.stroke(borderColor ?? .clear, lineWidth: 2))
			.fixedSize()
			.erased
		}
		
	}
	
	private var title: some View {
		Text(titleText)
			.font(.inter(ofSize: 12).bold())
			.frame(alignment: .center)
			.padding(.trailing, 10)
			.fixedSize()
	}
	
	private var icon: some View {
		iconImage
			.resizable()
			.aspectRatio(1, contentMode: .fit)
	}
}

extension SupportButton {
	static func supportButton() -> some View {
		SupportButton(
			titleText: "Support",
			backgroundColor: .black,
			iconImage: SharedUI.Asset.Media.PlayerIcons.heartAdd(),
			borderColor: NEWMColor.grey500()
		).foregroundColor(NEWMColor.pink())
	}
	
	static func followButton() -> some View {
		SupportButton(
			titleText: "Follow",
			backgroundColor: LinearGradient(colors: [NEWMColor.purple(), NEWMColor.pink()], startPoint: .bottomLeading, endPoint: .topTrailing),
			iconImage: Asset.Media.starIcon(),
			borderColor: nil
		).foregroundColor(.white)
	}
}

struct SupportButton_Previews: PreviewProvider {
	static var previews: some View {
		HStack {
			SupportButton.followButton()
			SupportButton.supportButton()
		}.preferredColorScheme(.dark)
	}
}
