package com.horiz.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.horiz.ui.theme.BaseColor

private const val TRANSITION_DURATION = 150

@Composable
fun AppNavigation(
    baseColor: BaseColor,
    startDestinationOverride: String? = null,
    openTasksSubjectId: String? = null,
    navigationEventId: Int = 0,
    onDestinationConsumed: () -> Unit = {},
    onTasksSubjectConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val storage = remember {
        ScheduleStorage(context)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()

    val refreshFlag = backStackEntry?.destination?.route != Routes.Scanner.route

    LaunchedEffect(navigationEventId) {
        val targetRoute = startDestinationOverride ?: return@LaunchedEffect

        val routeToNavigate = when (targetRoute) {
            "today" -> Routes.Today.route
            "subjects" -> Routes.Subjects.route
            "schedules" -> Routes.Schedules.route
            "settings" -> Routes.Settings.route
            else -> targetRoute
        }

        navController.navigate(routeToNavigate) {
            launchSingleTop = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            restoreState = true
        }

        onDestinationConsumed()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION
                )
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION
                )
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION
                )
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION
                )
            )
        }
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                baseColor = baseColor,
                onTodayClick = {
                    navController.navigate(Routes.Today.route)
                },
                onSubjectsClick = {
                    navController.navigate(Routes.Subjects.route)
                },
                onSchedulesClick = {
                    navController.navigate(Routes.Schedules.route)
                },
                onSettingsClick = {
                    navController.navigate(Routes.Settings.route)
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
                navArgument("subjectName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val scheduleName = backStackEntry.arguments
                ?.getString("scheduleName")
                ?: return@composable

            val subjectName = backStackEntry.arguments
                ?.getString("subjectName")
                ?: return@composable

            val schedule = storage.getSchedule(scheduleName)
                ?: return@composable

            val subject = schedule.subjects.firstOrNull {
                it.name.equals(
                    subjectName,
                    ignoreCase = true
                )
            } ?: return@composable

            TaskScreen(
                schedule = schedule,
                subjectId = subject.id,
                storage = storage,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Subjects.route) {
            SubjectScreen(
                initialTaskSubjectId = openTasksSubjectId,
                onInitialTaskOpened = {
                    onTasksSubjectConsumed()
                },
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
            val scheduleName = backStackEntry.arguments
                ?.getString("scheduleName")
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
            val scheduleName = backStackEntry.arguments
                ?.getString("scheduleName")
                ?: return@composable

            val schedule = storage.getSchedule(scheduleName)

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