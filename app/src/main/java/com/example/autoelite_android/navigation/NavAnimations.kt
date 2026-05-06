package com.example.autoelite_android.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

// Duración estándar
private const val ANIM_DURATION = 300
private const val FADE_DURATION = 250

val tabEnter: EnterTransition = fadeIn(animationSpec = tween(FADE_DURATION))

val tabExit: ExitTransition = fadeOut(animationSpec = tween(FADE_DURATION))

val slideInFromRight: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIM_DURATION)
    ) + fadeIn(animationSpec = tween(ANIM_DURATION))

val slideOutToRight: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIM_DURATION)
    ) + fadeOut(animationSpec = tween(ANIM_DURATION))

// Cuando se hace push a una pantalla interna, la pantalla de origen
// hace un ligero slide hacia la izquierda y se desvanece
val slideOutToLeft: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(ANIM_DURATION)
    ) + fadeOut(animationSpec = tween(ANIM_DURATION))

// Cuando se vuelve atrás (pop), la pantalla de origen reaparece
// desde la izquierda
val slideInFromLeft: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(ANIM_DURATION)
    ) + fadeIn(animationSpec = tween(ANIM_DURATION))

val authEnter: EnterTransition = fadeIn(animationSpec = tween(400))

val authExit: ExitTransition = fadeOut(animationSpec = tween(300))
