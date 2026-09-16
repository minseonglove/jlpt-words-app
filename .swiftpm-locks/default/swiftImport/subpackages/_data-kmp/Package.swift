// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data-kmp",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_data-kmp",
      type: .none,
      targets: ["_data-kmp"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      exact: "12.4.0"
    )
  ],
  targets: [
    .target(
      name: "_data-kmp",
      dependencies: [
        .product(
          name: "FirebaseCore",
          package: "firebase-ios-sdk"
        ),
        .product(
          name: "FirebaseFirestore",
          package: "firebase-ios-sdk"
        ),
        .product(
          name: "FirebaseAnalytics",
          package: "firebase-ios-sdk"
        ),
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
