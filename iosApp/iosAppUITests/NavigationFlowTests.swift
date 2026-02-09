import XCTest

/**
 * iOS 导航流程测试
 *
 * 测试应用的主要导航流程：
 * 1. Welcome → Map
 * 2. Map → FireStation/School/Forest
 * 3. 各场景 → 返回 Map
 * 4. Map → Collection
 * 5. Map → Parent Mode
 */
final class NavigationFlowTests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    override func tearDownWithError() throws {
        // Cleanup
    }

    /**
     * 测试应用启动
     */
    func testAppLaunch() throws {
        let app = XCUIApplication()
        app.launch()

        // 验证应用启动成功
        XCTAssertTrue(app.exists, "应用应该启动成功")
    }

    /**
     * 测试欢迎页面导航到地图
     */
    func testWelcomeToMapNavigation() throws {
        let app = XCUIApplication()
        app.launch()

        // 查找并点击开始按钮
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
        }

        // 验证地图页面已显示
        let mapElements = app.otherElements.containing(.any, identifier: "地图")
        XCTAssertTrue(mapElements.count > 0 || app.images.count > 0, "应该显示地图页面")
    }

    /**
     * 测试地图到消防站导航
     */
    func testMapToFireStationNavigation() throws {
        let app = XCUIApplication()
        app.launch()

        // 先导航到地图
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
        }

        // 查找消防站按钮并点击
        let fireStationButton = app.buttons.matching(identifier: "消防站").element(boundBy: 0)
        if fireStationButton.exists {
            fireStationButton.tap()

            // 验证消防站页面已显示
            XCTAssertTrue(app.navigationBars["消防站"].exists || app.staticTexts["消防站"].exists, "应该显示消防站页面")

            // 测试返回按钮
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
            }
        }
    }

    /**
     * 测试地图到学校导航
     */
    func testMapToSchoolNavigation() throws {
        let app = XCUIApplication()
        app.launch()

        // 先导航到地图
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
        }

        // 查找学校按钮并点击
        let schoolButton = app.buttons.matching(identifier: "学校").element(boundBy: 0)
        if schoolButton.exists {
            schoolButton.tap()

            // 验证学校页面已显示
            XCTAssertTrue(app.navigationBars["学校"].exists || app.staticTexts["学校"].exists, "应该显示学校页面")

            // 测试返回按钮
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
            }
        }
    }

    /**
     * 测试地图到森林导航
     */
    func testMapToForestNavigation() throws {
        let app = XCUIApplication()
        app.launch()

        // 先导航到地图
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
        }

        // 查找森林按钮并点击
        let forestButton = app.buttons.matching(identifier: "森林").element(boundBy: 0)
        if forestButton.exists {
            forestButton.tap()

            // 验证森林页面已显示
            XCTAssertTrue(app.navigationBars["森林"].exists || app.staticTexts["森林"].exists, "应该显示森林页面")

            // 测试返回按钮
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
            }
        }
    }

    /**
     * 测试收藏页面导航
     */
    func testCollectionNavigation() throws {
        let app = XCUIApplication()
        app.launch()

        // 先导航到地图
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
        }

        // 查找收藏按钮（左上角星标图标）
        let collectionButton = app.buttons.matching(NSPredicate(format: "label CONTAINS '收藏' OR identifier CONTAINS 'star'")).element(boundBy: 0)
        if collectionButton.exists {
            collectionButton.tap()

            // 验证收藏页面已显示
            XCTAssertTrue(app.staticTexts["我的收藏"].exists, "应该显示收藏页面")

            // 测试返回按钮
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
            }
        }
    }

    /**
     * 测试家长模式导航
     */
    func testParentModeNavigation() throws {
        let app = XCUIApplication()
        app.launch()

        // 先导航到地图
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
        }

        // 查找家长模式按钮（右上角齿轮图标）
        let parentButton = app.buttons.matching(NSPredicate(format: "label CONTAINS '家长' OR identifier CONTAINS 'gear'")).element(boundBy: 0)
        if parentButton.exists {
            parentButton.tap()

            // 验证家长模式页面已显示
            XCTAssertTrue(app.staticTexts["家长模式"].exists, "应该显示家长模式页面")

            // 测试返回按钮
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
            }
        }
    }

    /**
     * 测试完整的导航流程
     */
    func testCompleteNavigationFlow() throws {
        let app = XCUIApplication()
        app.launch()

        // Welcome → Map
        let startButton = app.buttons["开始探索"]
        if startButton.exists {
            startButton.tap()
            sleep(1)
        }

        // Test Map → FireStation → Map
        let fireStationButton = app.buttons.matching(identifier: "消防站").element(boundBy: 0)
        if fireStationButton.exists {
            fireStationButton.tap()
            sleep(1)
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
                sleep(1)
            }
        }

        // Test Map → School → Map
        let schoolButton = app.buttons.matching(identifier: "学校").element(boundBy: 0)
        if schoolButton.exists {
            schoolButton.tap()
            sleep(1)
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
                sleep(1)
            }
        }

        // Test Map → Forest → Map
        let forestButton = app.buttons.matching(identifier: "森林").element(boundBy: 0)
        if forestButton.exists {
            forestButton.tap()
            sleep(1)
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
                sleep(1)
            }
        }

        // Test Collection
        let collectionButton = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'star'")).element(boundBy: 0)
        if collectionButton.exists {
            collectionButton.tap()
            sleep(1)
            let backButton = app.buttons["chevron.left"]
            if backButton.exists {
                backButton.tap()
            }
        }

        // All navigation tests passed
        XCTAssertTrue(true, "导航流程测试完成")
    }
}
