package com.horiz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horiz.data.preferences.AppPreferences
import com.horiz.navigation.AppNavigation
import com.horiz.ui.theme.HorizTheme
import kotlinx.coroutines.launch

@Composable
fun App() {
    val context = LocalContext.current

    val preferences = remember {
        AppPreferences(context.applicationContext)
    }

    val scope = rememberCoroutineScope()

    val appTheme by preferences.theme.collectAsStateWithLifecycle(
        initialValue = null
    )

    val baseColor by preferences.baseColor.collectAsStateWithLifecycle(
        initialValue = null
    )

    if (appTheme == null || baseColor == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    HorizTheme(
        appTheme = appTheme!!,
        baseColor = baseColor!!
    ) {
        AppNavigation(
            appTheme = appTheme!!,
            baseColor = baseColor!!,
            onThemeChanged = { newTheme ->
                scope.launch {
                    preferences.saveTheme(newTheme)
                }
            },
            onBaseColorChanged = { newColor ->
                scope.launch {
                    preferences.saveBaseColor(newColor)
                }
            }
        )
    }
}