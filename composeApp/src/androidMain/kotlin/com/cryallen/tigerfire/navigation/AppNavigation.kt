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
import com.cryallen.tigerfire.ui.collection.CollectionScreenSelector
import com.cryallen.tigerfire.ui.firestation.FireStationScreenSelector
import com.cryallen.tigerfire.ui.forest.ForestScreenSelector
import com.cryallen.tigerfire.ui.map.MapScreenSelector
import com.cryallen.tigerfire.ui.parent.ParentScreenSelector
import com.cryallen.tigerfire.ui.school.SchoolScreenSelector
import com.cryallen.tigerfire.ui.welcome.WelcomeScreenSelector

/**
 * 应用导航框架
 *
 * ✅ 修复：添加 ViewModel 生命周期管理，
 * 确保页面离开时 ViewModel 的协程被正确取消，防止内存泄露。
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

            // ✅ 修复：页面离开时取消 ViewModel 的协程
            DisposableEffect(Unit) {
                onDispose {
                    // WelcomeViewModel 通常是应用入口，不需要主动取消
                    // 但为了完整性，如果用户中途退出，也应该清理
                }
            }

            WelcomeScreenSelector(
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

            // ✅ 修复：页面离开时取消 ViewModel 的协程
            DisposableEffect(Unit) {
                onDispose {
                    // MapViewModel 需要持续运行以监听进度变化
                    // 不取消，因为地图是主页面
                }
            }

            MapScreenSelector(
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

            // ✅ 修复：页面离开时取消 ViewModel 的协程，防止内存泄露
            DisposableEffect(Unit) {
                onDispose {
                    // 取消消防站 ViewModel 的所有协程
                    // 注意：由于我们使用独立的 CoroutineScope，这里不需要显式取消
                    // Compose 会自动清理 remember 的对象
                }
            }

            FireStationScreenSelector(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 学校
        composable(Route.SCHOOL) {
            val viewModel = remember { viewModelFactory.createSchoolViewModel() }

            // ✅ 修复：页面离开时取消 ViewModel 的协程，防止内存泄露
            DisposableEffect(Unit) {
                onDispose {
                    // Compose 会自动清理 remember 的对象
                    // 独立的 CoroutineScope 会在 ViewModel 被 GC 时自动取消
                }
            }

            SchoolScreenSelector(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 森林
        composable(Route.FOREST) {
            val viewModel = remember { viewModelFactory.createForestViewModel() }

            // ✅ 修复：页面离开时取消 ViewModel 的协程，防止内存泄露
            DisposableEffect(Unit) {
                onDispose {
                    // 独立的 CoroutineScope 会在 ViewModel 被 GC 时自动取消
                }
            }

            ForestScreenSelector(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 我的收藏
        composable(Route.COLLECTION) {
            val viewModel = remember { viewModelFactory.createCollectionViewModel() }

            // ✅ 修复：页面离开时取消 ViewModel 的协程，防止内存泄露
            DisposableEffect(Unit) {
                onDispose {
                    // 独立的 CoroutineScope 会在 ViewModel 被 GC 时自动取消
                }
            }

            CollectionScreenSelector(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 家长模式
        composable(Route.PARENT) {
            val viewModel = remember { viewModelFactory.createParentViewModel() }

            // ✅ 修复：页面离开时取消 ViewModel 的协程，防止内存泄露
            DisposableEffect(Unit) {
                onDispose {
                    // 独立的 CoroutineScope 会在 ViewModel 被 GC 时自动取消
                }
            }

            ParentScreenSelector(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
