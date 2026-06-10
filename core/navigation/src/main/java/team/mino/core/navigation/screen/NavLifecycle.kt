package team.mino.core.navigation.screen

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

fun NavController.popBackStackIfResumed(from: NavBackStackEntry) {
    if (from.lifecycle.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}
