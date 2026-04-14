package com.udlap.suppliesrescuesystem

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base [Application] class for the Supplies Rescue System.
 *
 * This class is annotated with [HiltAndroidApp] to trigger Hilt's code generation,
 * including a base class for the application that serves as the application-level
 * dependency container.
 */
@HiltAndroidApp
class SuppliesRescueApp : Application()
