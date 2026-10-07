package com.hussain.learningdashboard.di

import android.content.Context
import androidx.room.Room
import com.hussain.learningdashboard.core.database.AppDatabase
import com.hussain.learningdashboard.core.database.DATABASE_NAME
import com.hussain.learningdashboard.core.database.buildAppDatabase
import com.hussain.learningdashboard.core.network.AndroidNetworkMonitor
import com.hussain.learningdashboard.core.network.NetworkMonitor
import org.koin.dsl.module

fun initKoinAndroid(context: Context) {
    val appContext = context.applicationContext
    initKoin(
        module {
            single<NetworkMonitor> { AndroidNetworkMonitor(appContext) }
            single<AppDatabase> {
                Room.databaseBuilder<AppDatabase>(
                    context = appContext,
                    name = appContext.getDatabasePath(DATABASE_NAME).absolutePath,
                ).buildAppDatabase()
            }
            single {
                createSessionDataStore { appContext.filesDir.resolve(SESSION_DATASTORE_FILE).absolutePath }
            }
        },
    )
}
