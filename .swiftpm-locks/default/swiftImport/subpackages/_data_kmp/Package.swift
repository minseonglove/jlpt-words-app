// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_kmp",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_data_kmp",
      type: .none,
      targets: ["_data_kmp"]
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
      name: "_data_kmp",
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
