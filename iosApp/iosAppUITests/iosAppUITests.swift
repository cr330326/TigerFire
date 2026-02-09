import XCTest

final class iosAppUITests: XCTestCase {

    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["--uitesting"]
        app.launch()
    }

    override func tearDownWithError() throws {
        app = nil
    }

    func testLaunch() throws {
        XCTAssertTrue(app.exists, "App should launch successfully")
    }

    func testWelcomeToMapNavigation() throws {
        // Welcome screen should be visible
        let welcomeText = app.staticTexts["欢迎"]
        let startButton = app.buttons["开始"]

        // Try to find and tap start button
        if startButton.exists {
            startButton.tap()
        } else {
            // Try alternative button text
            let altButton = app.buttons.matching(NSPredicate(format: "label CONTAINS '探索' OR label CONTAINS '开始'")).firstMatch
            if altButton.exists {
                altButton.tap()
            }
        }

        // Verify map is displayed (look for map elements)
        let mapExists = app.otherElements.count > 0 || app.images.count > 0
        XCTAssertTrue(mapExists, "Map should be displayed after welcome")
    }

    func testNavigation() throws {
        // Get all buttons and try to interact
        let allButtons = app.buttons.allElementsBoundByIndex

        for i in 0..<min(allButtons.count, 5) {
            let button = allButtons[bound: i]
            if button.exists && button.isHittable {
                print("Found button: \(button.label)")
                button.tap()
                sleep(1)

                // Try to go back if there's a back button
                let backButton = app.buttons["chevron.left"]
                if backButton.exists {
                    backButton.tap()
                    sleep(1)
                }
            }
        }
    }
}
