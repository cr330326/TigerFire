package com.cryallen.tigerfire.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cryallen.tigerfire.factory.ViewModelFactory
import com.cryallen.tigerfire.ui.firestation.FireStationScreen
import com.cryallen.tigerfire.ui.map.MapScreen
import com.cryallen.tigerfire.ui.welcome.WelcomeScreen

/**
 * 应用导航框架
 *
 * 使用 Jetpack Compose Navigation 实现页面导航
 *
 * @param navController 导航控制器
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val viewModelFactory = remember { ViewModelFactory(context) }

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

            MapScreen(
                viewModel = viewModel,
                onNavigateToFireStation = { navController.navigate(Route.FIRE_STATION) },
                onNavigateToSchool = { navController.navigate(Route.SCHOOL) },
                onNavigateToForest = { navController.navigate(Route.FOREST) },
                onNavigateToCollection = { navController.navigate(Route.COLLECTION) },
                onNavigateToParent = { navController.navigate(Route.PARENT) }
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
            // TODO: SchoolScreen 将在 Task 4.5 中实现
            // SchoolScreen(
            //     viewModel = viewModel,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // 森林
        composable(Route.FOREST) {
            // TODO: ForestScreen 将在 Task 4.6 中实现
            // ForestScreen(
            //     viewModel = viewModel,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // 我的收藏
        composable(Route.COLLECTION) {
            // TODO: CollectionScreen 将在 Task 4.7 中实现
            // CollectionScreen(
            //     viewModel = viewModel,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }

        // 家长模式
        composable(Route.PARENT) {
            // TODO: ParentScreen 将在 Task 4.8 中实现
            // ParentScreen(
            //     viewModel = viewModel,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
    }
}
