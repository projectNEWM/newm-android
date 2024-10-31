// swiftlint:disable all
// Generated using SwiftGen — https://github.com/SwiftGen/SwiftGen

#if os(macOS)
  import AppKit
#elseif os(iOS)
  import UIKit
#elseif os(tvOS) || os(watchOS)
  import UIKit
#endif
#if canImport(SwiftUI)
  import SwiftUI
#endif

// Deprecated typealiases
@available(*, deprecated, renamed: "ColorAsset.Color", message: "This typealias will be removed in SwiftGen 7.0")
public typealias AssetColorTypeAlias = ColorAsset.Color

// swiftlint:disable superfluous_disable_command file_length implicit_return

// MARK: - Asset Catalogs

// swiftlint:disable identifier_name line_length nesting type_body_length type_name
public enum NEWMColor {
  public static let main = ColorAsset(name: "Main")
  public static let midMusic = ColorAsset(name: "MidMusic")
  public static let primary = ColorAsset(name: "Primary")
  public static let blue = ColorAsset(name: "blue")
  public static let blue2 = ColorAsset(name: "blue2")
  public static let green = ColorAsset(name: "green")
  public static let green2 = ColorAsset(name: "green2")
  public static let grey100 = ColorAsset(name: "grey100")
  public static let grey200 = ColorAsset(name: "grey200")
  public static let grey400 = ColorAsset(name: "grey400")
  public static let grey500 = ColorAsset(name: "grey500")
  public static let grey600 = ColorAsset(name: "grey600")
  public static let grey700 = ColorAsset(name: "grey700")
  public static let lightBlue = ColorAsset(name: "lightBlue")
  public static let midCrypto = ColorAsset(name: "midCrypto")
  public static let offPink = ColorAsset(name: "offPink")
  public static let orange = ColorAsset(name: "orange")
  public static let orange1 = ColorAsset(name: "orange1")
  public static let orange2 = ColorAsset(name: "orange2")
  public static let pink = ColorAsset(name: "pink")
  public static let pink2 = ColorAsset(name: "pink2")
  public static let purple = ColorAsset(name: "purple")
  public static let purple2 = ColorAsset(name: "purple2")
  public static let red = ColorAsset(name: "red")
  public static let yellow = ColorAsset(name: "yellow")
  public enum Background {
    public static let defaultLight = ColorAsset(name: "Background/defaultLight")
    public static let defaultLighter = ColorAsset(name: "Background/defaultLighter")
  }
  public static let company = ColorAsset(name: "Company")
  public static let music = ColorAsset(name: "Music")
  public static let error = ColorAsset(name: "Error")
  public static let success = ColorAsset(name: "Success")
}
// swiftlint:enable identifier_name line_length nesting type_body_length type_name

// MARK: - Implementation Details

public final class ColorAsset {
  public fileprivate(set) var name: String

  #if os(macOS)
  public typealias Color = NSColor
  #elseif os(iOS) || os(tvOS) || os(watchOS)
  public typealias Color = UIColor
  #endif

  @available(iOS 11.0, tvOS 11.0, watchOS 4.0, macOS 10.13, *)
  public private(set) lazy var color: Color = {
    guard let color = Color(asset: self) else {
      fatalError("Unable to load color asset named \(name).")
    }
    return color
  }()

  #if os(iOS) || os(tvOS)
  @available(iOS 11.0, tvOS 11.0, *)
  public func color(compatibleWith traitCollection: UITraitCollection) -> Color {
    let bundle = BundleToken.bundle
    guard let color = Color(named: name, in: bundle, compatibleWith: traitCollection) else {
      fatalError("Unable to load color asset named \(name).")
    }
    return color
  }
  #endif

  #if canImport(SwiftUI)
  @available(iOS 13.0, tvOS 13.0, watchOS 6.0, macOS 10.15, *)
  public private(set) lazy var swiftUIColor: SwiftUI.Color = {
    SwiftUI.Color(asset: self)
  }()
  #endif

  fileprivate init(name: String) {
    self.name = name
  }
}

public extension ColorAsset.Color {
  @available(iOS 11.0, tvOS 11.0, watchOS 4.0, macOS 10.13, *)
  convenience init?(asset: ColorAsset) {
    let bundle = BundleToken.bundle
    #if os(iOS) || os(tvOS)
    self.init(named: asset.name, in: bundle, compatibleWith: nil)
    #elseif os(macOS)
    self.init(named: NSColor.Name(asset.name), bundle: bundle)
    #elseif os(watchOS)
    self.init(named: asset.name)
    #endif
  }
}

#if canImport(SwiftUI)
@available(iOS 13.0, tvOS 13.0, watchOS 6.0, macOS 10.15, *)
public extension SwiftUI.Color {
  init(asset: ColorAsset) {
    let bundle = BundleToken.bundle
    self.init(asset.name, bundle: bundle)
  }
}
#endif

// swiftlint:disable convenience_type
private final class BundleToken {
  static let bundle: Bundle = {
    #if SWIFT_PACKAGE
    return Bundle.module
    #else
    return Bundle(for: BundleToken.self)
    #endif
  }()
}
// swiftlint:enable convenience_type
