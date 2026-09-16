// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_presentation_kmp",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_presentation_kmp",
      type: .none,
      targets: ["_presentation_kmp"]
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
      name: "_presentation_kmp",
      dependencies: [
        .product(
          name: "GoogleMobileAds",
          package: "swift-package-manager-google-mobile-ads"
        )
      ]
    )
  ]
)
