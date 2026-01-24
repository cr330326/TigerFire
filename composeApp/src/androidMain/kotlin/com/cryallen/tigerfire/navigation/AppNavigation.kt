package com.cryallen.tigerfire.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cryallen.tigerfire.factory.ViewModelFactory
import com.cryallen.tigerfire.ui.collection.CollectionScreen
import com.cryallen.tigerfire.ui.firestation.FireStationScreen
import com.cryallen.tigerfire.ui.forest.ForestScreen
import com.cryallen.tigerfire.ui.map.MapScreen
import com.cryallen.tigerfire.ui.parent.ParentScreen
import com.cryallen.tigerfire.ui.school.SchoolScreen
import com.cryallen.tigerfire.ui.welcome.WelcomeScreen

/**
 * 应用导航框架
 *
 * 使用 Jetpack Compose Navigation 实现页面导航
 *
 * @param navController 导航控制器
 * @param viewModelFactory ViewModel 工厂（从外部传入）
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModelFactory: ViewModelFactory
) {

    NavHost(
        navController = navController,
        startDestination = Route.WELCOME
    ) {
        // 启动页/欢迎页
        composable(Route.WELCOME) {
            val viewModel = remember { viewModelFactory.createWelcomeViewModel() }

            WelcomeScreen(
                viewModel = viewModel,
                onNavigateToMap = {
                    navController.navigate(Route.MAP) {
                        popUpTo(Route.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // 主地图
        composable(Route.MAP) {
            val viewModel = remember { viewModelFactory.createMapViewModel() }
            val appSessionManager = remember { viewModelFactory.getAppSessionManager() }

            MapScreen(
                viewModel = viewModel,
                onNavigateToFireStation = { navController.navigate(Route.FIRE_STATION) },
                onNavigateToSchool = { navController.navigate(Route.SCHOOL) },
                onNavigateToForest = { navController.navigate(Route.FOREST) },
                onNavigateToCollection = { navController.navigate(Route.COLLECTION) },
                onNavigateToParent = { navController.navigate(Route.PARENT) },
                appSessionManager = appSessionManager
            )
        }

        // 消防站
        composable(Route.FIRE_STATION) {
            val viewModel = remember { viewModelFactory.createFireStationViewModel() }

            FireStationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 学校
        composable(Route.SCHOOL) {
            val viewModel = remember { viewModelFactory.createSchoolViewModel() }

            SchoolScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 森林
        composable(Route.FOREST) {
            val viewModel = remember { viewModelFactory.createForestViewModel() }

            ForestScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 我的收藏
        composable(Route.COLLECTION) {
            // 使用 remember 创建 ViewModel，每次进入时都会创建新实例
            val viewModel = remember { viewModelFactory.createCollectionViewModel() }

            // 页面离开时清理 ViewModel，防止内存泄漏
            DisposableEffect(Unit) {
                onDispose {
                    viewModel.cleanup()
                }
            }

            CollectionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 家长模式
        composable(Route.PARENT) {
            val viewModel = remember { viewModelFactory.createParentViewModel() }

            ParentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
