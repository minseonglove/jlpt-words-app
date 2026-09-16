import UIKit
import FirebaseCore
import GoogleMobileAds
import Shared

/// Compose 화면을 자식으로 두고 상태바 스타일을 책임지는 루트 컨트롤러.
///
/// ComposeUIViewController 는 상태바 스타일을 상위 컨트롤러가 정하도록 위임하는데, SwiftUI 의
/// UIHostingController 는 자식 컨트롤러의 preferredStatusBarStyle 을 묻지 않는다. 그래서 앱 루트를
/// SwiftUI 대신 UIKit 컨트롤러로 두고 여기서 스타일을 결정한다.
final class RootViewController: UIViewController, StatusBarStyleListener {
    /// 런치 화면과 첫 Compose 화면(스플래시)이 모두 밝은 배경이라 어두운 아이콘으로 시작한다.
    private var darkStatusBarIcons = true

    override var preferredStatusBarStyle: UIStatusBarStyle {
        darkStatusBarIcons ? .darkContent : .lightContent
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        let composeViewController = Shared.MainViewControllerKt.MainViewController()
        addChild(composeViewController)
        composeViewController.view.frame = view.bounds
        composeViewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(composeViewController.view)
        composeViewController.didMove(toParent: self)
    }

    func onDarkIconsChanged(darkIcons: Bool) {
        darkStatusBarIcons = darkIcons
        setNeedsStatusBarAppearanceUpdate()
    }
}

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let rootViewController = RootViewController()
        Shared.StatusBarAppearance.shared.listener = rootViewController

        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = rootViewController
        window.makeKeyAndVisible()
        self.window = window
    }
}

@main
final class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Firebase 초기화
        FirebaseApp.configure()

        // Google Mobile Ads 초기화
        MobileAds.shared.start(completionHandler: nil)

        // Koin DI 초기화
        Shared.KoinInitializerKt.doInitKoinModules(appDeclaration: { _ in })

        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(
            name: nil,
            sessionRole: connectingSceneSession.role
        )
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }
}
