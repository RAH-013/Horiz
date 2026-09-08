package com.horiz.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class BaseColor {
    PURPLE,
    BLUE,
    GREEN,
    ORANGE,
    RED,
    PINK
}

private fun lightColorSchemeFor(
    baseColor: BaseColor
): ColorScheme {
    return when (baseColor) {

        BaseColor.PURPLE -> lightColorScheme(
            primary = PurplePrimary,
            onPrimary = Color.White,
            primaryContainer = PurpleContainer,
            onPrimaryContainer = PurpleOnContainer,
            secondary = PurpleLight,
            onSecondary = Color.White
        )

        BaseColor.BLUE -> lightColorScheme(
            primary = BluePrimary,
            onPrimary = Color.White,
            primaryContainer = BlueContainer,
            onPrimaryContainer = BlueOnContainer,
            secondary = BlueLight,
            onSecondary = Color.White
        )

        BaseColor.GREEN -> lightColorScheme(
            primary = GreenPrimary,
            onPrimary = Color.White,
            primaryContainer = GreenContainer,
            onPrimaryContainer = GreenOnContainer,
            secondary = GreenLight,
            onSecondary = Color.White
        )

        BaseColor.ORANGE -> lightColorScheme(
            primary = OrangePrimary,
            onPrimary = Color.White,
            primaryContainer = OrangeContainer,
            onPrimaryContainer = OrangeOnContainer,
            secondary = OrangeLight,
            onSecondary = Color.White
        )

        BaseColor.RED -> lightColorScheme(
            primary = RedPrimary,
            onPrimary = Color.White,
            primaryContainer = RedContainer,
            onPrimaryContainer = RedOnContainer,
            secondary = RedLight,
            onSecondary = Color.White
        )

        BaseColor.PINK -> lightColorScheme(
            primary = PinkPrimary,
            onPrimary = Color.White,
            primaryContainer = PinkContainer,
            onPrimaryContainer = PinkOnContainer,
            secondary = PinkLight,
            onSecondary = Color.White
        )
    }
}

private fun darkColorSchemeFor(
    baseColor: BaseColor
): ColorScheme {
    return when (baseColor) {

        BaseColor.PURPLE -> darkColorScheme(
            primary = PurpleLight,
            onPrimary = Color.White,
            primaryContainer = PurpleDark,
            onPrimaryContainer = Color.White,
            secondary = PurplePrimary,
            onSecondary = Color.White
        )

        BaseColor.BLUE -> darkColorScheme(
            primary = BlueLight,
            onPrimary = Color.White,
            primaryContainer = BlueDark,
            onPrimaryContainer = Color.White,
            secondary = BluePrimary,
            onSecondary = Color.White
        )

        BaseColor.GREEN -> darkColorScheme(
            primary = GreenLight,
            onPrimary = Color.White,
            primaryContainer = GreenDark,
            onPrimaryContainer = Color.White,
            secondary = GreenPrimary,
            onSecondary = Color.White
        )

        BaseColor.ORANGE -> darkColorScheme(
            primary = OrangeLight,
            onPrimary = Color.White,
            primaryContainer = OrangeDark,
            onPrimaryContainer = Color.White,
            secondary = OrangePrimary,
            onSecondary = Color.White
        )

        BaseColor.RED -> darkColorScheme(
            primary = RedLight,
            onPrimary = Color.White,
            primaryContainer = RedDark,
            onPrimaryContainer = Color.White,
            secondary = RedPrimary,
            onSecondary = Color.White
        )

        BaseColor.PINK -> darkColorScheme(
            primary = PinkLight,
            onPrimary = Color.White,
            primaryContainer = PinkDark,
            onPrimaryContainer = Color.White,
            secondary = PinkPrimary,
            onSecondary = Color.White
        )
    }
}

@Composable
fun HorizTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    baseColor: BaseColor = BaseColor.PURPLE,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val targetColorScheme = if (darkTheme) {
        darkColorSchemeFor(baseColor)
    } else {
        lightColorSchemeFor(baseColor)
    }

    /*
     * Smooth color transition.
     */
    val primary by animateColorAsState(
        targetValue = targetColorScheme.primary,
        animationSpec = tween(500),
        label = "primary"
    )

    val onPrimary by animateColorAsState(
        targetValue = targetColorScheme.onPrimary,
        animationSpec = tween(400),
        label = "onPrimary"
    )

    val primaryContainer by animateColorAsState(
        targetValue = targetColorScheme.primaryContainer,
        animationSpec = tween(500),
        label = "primaryContainer"
    )

    val onPrimaryContainer by animateColorAsState(
        targetValue = targetColorScheme.onPrimaryContainer,
        animationSpec = tween(400),
        label = "onPrimaryContainer"
    )

    val secondary by animateColorAsState(
        targetValue = targetColorScheme.secondary,
        animationSpec = tween(500),
        label = "secondary"
    )

    val onSecondary by animateColorAsState(
        targetValue = targetColorScheme.onSecondary,
        animationSpec = tween(400),
        label = "onSecondary"
    )

    val surface by animateColorAsState(
        targetValue = targetColorScheme.surface,
        animationSpec = tween(500),
        label = "surface"
    )

    val onSurface by animateColorAsState(
        targetValue = targetColorScheme.onSurface,
        animationSpec = tween(400),
        label = "onSurface"
    )

    val background by animateColorAsState(
        targetValue = targetColorScheme.background,
        animationSpec = tween(500),
        label = "background"
    )

    val onBackground by animateColorAsState(
        targetValue = targetColorScheme.onBackground,
        animationSpec = tween(400),
        label = "onBackground"
    )

    val animatedColorScheme = targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        surface = surface,
        onSurface = onSurface,
        background = background,
        onBackground = onBackground
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}