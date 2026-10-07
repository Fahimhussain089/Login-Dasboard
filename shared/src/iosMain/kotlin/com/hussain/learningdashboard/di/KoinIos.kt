package com.hussain.learningdashboard.di

import androidx.room.Room
import com.hussain.learningdashboard.core.database.AppDatabase
import com.hussain.learningdashboard.core.database.DATABASE_NAME
import com.hussain.learningdashboard.core.database.buildAppDatabase
import com.hussain.learningdashboard.core.network.IosNetworkMonitor
import com.hussain.learningdashboard.core.network.NetworkMonitor
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** Called from Swift as `KoinIosKt.doInitKoinIos()`. */
fun initKoinIos() {
    initKoin(
        module {
            single<NetworkMonitor> { IosNetworkMonitor() }
            single<AppDatabase> {
                Room.databaseBuilder<AppDatabase>(name = "${documentDirectory()}/$DATABASE_NAME")
                    .buildAppDatabase()
            }
            single { createSessionDataStore { "${documentDirectory()}/$SESSION_DATASTORE_FILE" } }
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "Documents directory unavailable" }
}
