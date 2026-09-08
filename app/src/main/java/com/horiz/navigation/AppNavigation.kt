package com.horiz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.screens.home.HomeScreen
import com.horiz.ui.screens.qr.QrScreen
import com.horiz.ui.screens.scanner.ScannerScreen
import com.horiz.ui.screens.schedule.ScheduleScreen
import com.horiz.ui.screens.schedule.ScheduleViewScreen
import com.horiz.ui.screens.settings.SettingsScreen
import com.horiz.ui.screens.subjects.SubjectScreen
import com.horiz.ui.screens.tasks.TaskScreen
import com.horiz.ui.screens.today.TodayScreen
import com.horiz.ui.theme.AppTheme
import com.horiz.ui.theme.BaseColor

@Composable
fun AppNavigation(
    appTheme: AppTheme,
    baseColor: BaseColor,
    onThemeChanged: (AppTheme) -> Unit,
    onBaseColorChanged: (BaseColor) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val storage = remember {
        ScheduleStorage(context)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()

    val refreshFlag =
        backStackEntry?.destination?.route != Routes.Scanner.route

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                onTodayClick = {
                    navController.navigate(
                        Routes.Today.route
                    )
                },
                onSubjectsClick = {
                    navController.navigate(
                        Routes.Subjects.route
                    )
                },
                onSchedulesClick = {
                    navController.navigate(
                        Routes.Schedules.route
                    )
                },
                onSettingsClick = {
                    navController.navigate(
                        Routes.Settings.route
                    )
                }
            )
        }

        composable(Routes.Today.route) {
            val kingName = storage.getKing()

            val schedule = kingName?.let {
                storage.getSchedule(it)
            }

            if (schedule != null) {
                TodayScreen(
                    schedule = schedule,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onManageTasks = { entry ->
                        navController.navigate(
                            Routes.Tasks.createRoute(
                                scheduleName = schedule.name,
                                scheduleEntryId = entry.id
                            )
                        )
                    }
                )
            }
        }

        composable(
            route = Routes.Tasks.route,
            arguments = listOf(
                navArgument("scheduleName") {
                    type = NavType.StringType
                },
                navArgument("scheduleEntryId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val scheduleName =
                backStackEntry.arguments
                    ?.getString("scheduleName")
                    ?: return@composable

            val scheduleEntryId =
                backStackEntry.arguments
                    ?.getLong("scheduleEntryId")
                    ?: return@composable

            val schedule =
                storage.getSchedule(scheduleName)
                    ?: return@composable

            TaskScreen(
                schedule = schedule,
                scheduleEntryId = scheduleEntryId,
                storage = storage,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Subjects.route) {
            SubjectScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Schedules.route) {
            ScheduleScreen(
                storage = storage,
                refreshFlag = refreshFlag,
                onBackClick = {
                    navController.popBackStack()
                },
                onScheduleClick = { schedule ->
                    navController.navigate(
                        Routes.ScheduleView.createRoute(
                            schedule.name
                        )
                    )
                },
                onScannerClick = {
                    navController.navigate(
                        Routes.Scanner.route
                    )
                },
                onQrClick = { scheduleName ->
                    navController.navigate(
                        Routes.Qr.createRoute(
                            scheduleName
                        )
                    )
                }
            )
        }

        composable(Routes.Scanner.route) {
            ScannerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onImported = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.Qr.route,
            arguments = listOf(
                navArgument("scheduleName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val scheduleName =
                backStackEntry.arguments?.getString("scheduleName")
                    ?: return@composable

            QrScreen(
                scheduleName = scheduleName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ScheduleView.route,
            arguments = listOf(
                navArgument("scheduleName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val scheduleName =
                backStackEntry.arguments?.getString("scheduleName")
                    ?: return@composable

            val schedule = storage.getSchedule(
                scheduleName
            )

            if (schedule != null) {
                ScheduleViewScreen(
                    schedule = schedule,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}