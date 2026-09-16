// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_presentation-kmp",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_presentation-kmp",
      type: .none,
      targets: ["_presentation-kmp"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/googleads/swift-package-manager-google-mobile-ads.git",
      exact: "12.12.0"
    )
  ],
  targets: [
    .target(
      name: "_presentation-kmp",
      dependencies: [
        .product(
          name: "GoogleMobileAds",
          package: "swift-package-manager-google-mobile-ads"
        )
      ]
    )
  ]
)
