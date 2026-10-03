package com.kmuaz.alistcloud.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kmuaz.alistcloud.viewmodel.CloudToolsViewModel
import com.kmuaz.alistcloud.ui.upload.UploadScreen
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.kmuaz.alistcloud.ui.home.HomeScreen
import com.kmuaz.alistcloud.ui.download.DownloadScreen
import com.kmuaz.alistcloud.ui.login.LoginScreen
import com.kmuaz.alistcloud.ui.setting.SettingScreen

@Composable
fun AppNavigation() {
    val tools: CloudToolsViewModel = viewModel()
    var transferTab by rememberSaveable { mutableIntStateOf(0) }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "login"
    fun navigate(target: String) {
        nav.navigate(target) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route != "login") NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(Triple("home", "文件", Icons.Default.Folder),
                    Triple("downloads", "传输", Icons.Default.Download),
                    Triple("setting", "我的", Icons.Default.Person)).forEach { (target, label, icon) ->
                    NavigationBarItem(selected = route == target, onClick = { navigate(target) },
                        icon = { Icon(icon, label) }, label = { Text(label) })
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "login", modifier = Modifier.padding(padding)) {
            composable("login") {
                LoginScreen(onLoginSuccess = { nav.navigate("home") { popUpTo("login") { inclusive = true } } })
            }
            composable("home") {
                HomeScreen(onSettingClick = { navigate("setting") }, onDownloadsClick = { transferTab = 0; navigate("downloads") }, tools = tools, onUploadTasksClick = { transferTab = 1; navigate("downloads") })
            }
            composable("downloads") {
                Column {
                    TabRow(selectedTabIndex = transferTab) {
                        Tab(selected = transferTab == 0, onClick = { transferTab = 0 }, text = { Text("下载") })
                        Tab(selected = transferTab == 1, onClick = { transferTab = 1 }, text = { Text("上传") })
                    }
                    if (transferTab == 0) DownloadScreen(onBackClick = { navigate("home") })
                    else UploadScreen(tools, onBrowse = { navigate("home") })
                }
            }
            composable("setting") { SettingScreen() }
        }
    }
}
