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
@available(*, deprecated, renamed: "ImageAsset.Image", message: "This typealias will be removed in SwiftGen 7.0")
public typealias AssetImageTypeAlias = ImageAsset.Image

// swiftlint:disable superfluous_disable_command file_length implicit_return

// MARK: - Asset Catalogs

// swiftlint:disable identifier_name line_length nesting type_body_length type_name
public enum Asset {
  public enum Media {
    public static let earningsIcon = ImageAsset(name: "EarningsIcon")
    public static let heartIcon = ImageAsset(name: "HeartIcon")
    public enum PlayerIcons {
      public static let heartAdd = ImageAsset(name: "heart-add")
      public static let heartAddHighlighted = ImageAsset(name: "heart-add_highlighted")
      public static let heart = ImageAsset(name: "heart")
      public static let heartHighlighted = ImageAsset(name: "heart_highlighted")
      public static let heartSelected = ImageAsset(name: "heart_selected")
      public static let next = ImageAsset(name: "next")
      public static let nextHighlighted = ImageAsset(name: "next_highlighted")
      public static let pause = ImageAsset(name: "pause")
      public static let pauseSelected = ImageAsset(name: "pause_selected")
      public static let play = ImageAsset(name: "play")
      public static let playHighlighted = ImageAsset(name: "play_highlighted")
      public static let previous = ImageAsset(name: "previous")
      public static let previousHighlighted = ImageAsset(name: "previous_highlighted")
      public enum Repeat {
        public static let repeatFill = ImageAsset(name: "repeat-fill")
        public static let repeatOneFill = ImageAsset(name: "repeat-one-fill")
        public static let `repeat` = ImageAsset(name: "repeat")
      }
      public static let shareHighlighted = ImageAsset(name: "share-highlighted")
      public static let share = ImageAsset(name: "share")
      public static let shuffle = ImageAsset(name: "shuffle")
      public static let shuffleSelected = ImageAsset(name: "shuffle_selected")
    }
    public static let royaltiesIcon = ImageAsset(name: "RoyaltiesIcon")
    public static let arrowSmallDown = ImageAsset(name: "arrow-small-down")
    public static let backArrow = ImageAsset(name: "back-arrow")
    public static let checkmark = ImageAsset(name: "checkmark")
    public static let download = ImageAsset(name: "download")
    public static let fileDownloadFill = ImageAsset(name: "file-download-fill")
    public static let logo = ImageAsset(name: "logo")
    public static let placeholder = ImageAsset(name: "placeholder")
    public static let playMiniFill = ImageAsset(name: "play-mini-fill")
    public static let priceTag = ImageAsset(name: "price-tag")
    public static let starIcon = ImageAsset(name: "star-icon")
  }
  public enum MockAssets {
    public static let artist0 = ImageAsset(name: "artist0")
    public static let artist1 = ImageAsset(name: "artist1")
    public static let artist2 = ImageAsset(name: "artist2")
    public static let artist3 = ImageAsset(name: "artist3")
    public static let artist4 = ImageAsset(name: "artist4")
    public static let artist5 = ImageAsset(name: "artist5")
    public static let artist6 = ImageAsset(name: "artist6")
    public static let artist7 = ImageAsset(name: "artist7")
    public static let artist8 = ImageAsset(name: "artist8")
    public static let artist9 = ImageAsset(name: "artist9")
  }
}
// swiftlint:enable identifier_name line_length nesting type_body_length type_name

// MARK: - Implementation Details

public struct ImageAsset {
  public fileprivate(set) var name: String

  #if os(macOS)
  public typealias Image = NSImage
  #elseif os(iOS) || os(tvOS) || os(watchOS)
  public typealias Image = UIImage
  #endif

  @available(iOS 8.0, tvOS 9.0, watchOS 2.0, macOS 10.7, *)
  public var image: Image {
    let bundle = BundleToken.bundle
    #if os(iOS) || os(tvOS)
    let image = Image(named: name, in: bundle, compatibleWith: nil)
    #elseif os(macOS)
    let name = NSImage.Name(self.name)
    let image = (bundle == .main) ? NSImage(named: name) : bundle.image(forResource: name)
    #elseif os(watchOS)
    let image = Image(named: name)
    #endif
    guard let result = image else {
      fatalError("Unable to load image asset named \(name).")
    }
    return result
  }

  #if os(iOS) || os(tvOS)
  @available(iOS 8.0, tvOS 9.0, *)
  public func image(compatibleWith traitCollection: UITraitCollection) -> Image {
    let bundle = BundleToken.bundle
    guard let result = Image(named: name, in: bundle, compatibleWith: traitCollection) else {
      fatalError("Unable to load image asset named \(name).")
    }
    return result
  }
  #endif

  #if canImport(SwiftUI)
  @available(iOS 13.0, tvOS 13.0, watchOS 6.0, macOS 10.15, *)
  public var swiftUIImage: SwiftUI.Image {
    SwiftUI.Image(asset: self)
  }
  #endif
}

public extension ImageAsset.Image {
  @available(iOS 8.0, tvOS 9.0, watchOS 2.0, *)
  @available(macOS, deprecated,
    message: "This initializer is unsafe on macOS, please use the ImageAsset.image property")
  convenience init?(asset: ImageAsset) {
    #if os(iOS) || os(tvOS)
    let bundle = BundleToken.bundle
    self.init(named: asset.name, in: bundle, compatibleWith: nil)
    #elseif os(macOS)
    self.init(named: NSImage.Name(asset.name))
    #elseif os(watchOS)
    self.init(named: asset.name)
    #endif
  }
}

#if canImport(SwiftUI)
@available(iOS 13.0, tvOS 13.0, watchOS 6.0, macOS 10.15, *)
public extension SwiftUI.Image {
  init(asset: ImageAsset) {
    let bundle = BundleToken.bundle
    self.init(asset.name, bundle: bundle)
  }

  init(asset: ImageAsset, label: Text) {
    let bundle = BundleToken.bundle
    self.init(asset.name, bundle: bundle, label: label)
  }

  init(decorative asset: ImageAsset) {
    let bundle = BundleToken.bundle
    self.init(decorative: asset.name, bundle: bundle)
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
