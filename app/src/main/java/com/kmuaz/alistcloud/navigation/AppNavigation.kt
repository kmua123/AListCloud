package com.kmuaz.alistcloud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kmuaz.alistcloud.ui.home.HomeScreen
import com.kmuaz.alistcloud.ui.login.LoginScreen
import com.kmuaz.alistcloud.ui.setting.SettingScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onSettingClick = {
                    navController.navigate("setting")
                }
            )
        }
        composable("setting") {
            SettingScreen()
        }

    }

}