package com.example.watchlist.base

import android.os.Bundle
import androidx.navigation.NavDirections

/**
 * Sealed class used with the live data to navigate between the fragments
 */
sealed class NavigationCommand {
    /**
     * navigate to a direction
     */
    data class To(val directions: NavDirections, val bundle: Bundle? = null) : NavigationCommand()

    /**
     * navigate back to the previous fragment
     */
    object Back : NavigationCommand()

    /**
     * navigate back to a destination in the back stack
     */
    data class BackTo(val destinationId: Int) : NavigationCommand()
}