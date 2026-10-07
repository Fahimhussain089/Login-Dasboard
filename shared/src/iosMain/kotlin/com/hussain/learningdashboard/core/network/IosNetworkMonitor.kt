package com.hussain.learningdashboard.core.network

import kotlin.concurrent.Volatile
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

class IosNetworkMonitor : NetworkMonitor {

    @Volatile
    private var online = true

    private val monitor = nw_path_monitor_create()

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            online = nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, dispatch_queue_create("com.hussain.learningdashboard.network", null))
        nw_path_monitor_start(monitor)
    }

    override fun isOnline(): Boolean = online
}
