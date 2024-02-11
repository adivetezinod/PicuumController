package com.etezinod.picuumcontroller

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.etezinod.picuumcontroller.screen.PicuumControllerScreen
import com.etezinod.picuumcontroller.screen.PicuumDevicesScreen

interface PicuumScreen {
    val name: String
    val args get() = emptyList<NamedNavArgument>()
    val body: @Composable (NavHostController) -> Unit
    val route get() = "$name/${args.joinToString("/") { "{${it.name}}" }}"
}

fun NavHostController.navigate(
    screen: PicuumScreen,
    vararg args: String
) {
    val route = "${screen.name}/${args.joinToString("/")}"
    navigate(route)
}

private enum class PicuumScreens : PicuumScreen {
    DEVICE {
        override val body = @Composable { controller: NavHostController ->
            PicuumDevicesScreen { device ->
                controller.navigate(
                    CONTROLLER,
                    device.address
                )
            }
        }
    },
    CONTROLLER {
        override val body = @Composable { _: NavHostController ->
            PicuumControllerScreen()
        }

        override val args = listOf(
            navArgument("address") {
                type = NavType.StringType
            }
        )
    }
}

@Composable
fun MainNavigator() {
    val navController = rememberNavController()
    NavHost(
        navController,
        PicuumScreens.DEVICE.route
    ) {
        PicuumScreens.entries.forEach { screen ->
            composable(screen.route, screen.args) {
                screen.body(navController)
            }
        }
    }
}