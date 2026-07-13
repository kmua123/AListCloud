package com.kmuaz.alistcloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kmuaz.alistcloud.navigation.AppNavigation
import com.kmuaz.alistcloud.ui.theme.AListCloudTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AListCloudTheme {
                AppNavigation()
            }
        }
    }

}