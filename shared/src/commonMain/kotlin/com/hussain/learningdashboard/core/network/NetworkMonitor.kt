package com.hussain.learningdashboard.core.network

/** Platform connectivity check: ConnectivityManager on Android, NWPathMonitor on iOS. */
interface NetworkMonitor {
    fun isOnline(): Boolean
}
