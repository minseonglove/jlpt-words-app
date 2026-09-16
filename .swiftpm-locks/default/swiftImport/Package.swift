// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_data-kmp"),
    .package(path: "subpackages/_data_kmp"),
    .package(path: "subpackages/_presentation-kmp"),
    .package(path: "subpackages/_presentation_kmp"),
    .package(path: "subpackages/_shared")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_data-kmp", package: "_data-kmp"),
        .product(name: "_data_kmp", package: "_data_kmp"),
        .product(name: "_presentation-kmp", package: "_presentation-kmp"),
        .product(name: "_presentation_kmp", package: "_presentation_kmp"),
        .product(name: "_shared", package: "_shared")
      ]
    )
  ]
)
