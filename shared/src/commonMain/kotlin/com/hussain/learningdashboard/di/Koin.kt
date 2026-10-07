package com.hussain.learningdashboard.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.hussain.learningdashboard.AppViewModel
import com.hussain.learningdashboard.core.network.createHttpClient
import com.hussain.learningdashboard.core.network.mock.createMockLearningEngine
import com.hussain.learningdashboard.feature.auth.data.AuthRepositoryImpl
import com.hussain.learningdashboard.feature.auth.data.local.DataStoreSessionStorage
import com.hussain.learningdashboard.feature.auth.data.local.SessionStorage
import com.hussain.learningdashboard.feature.auth.data.remote.AuthRemoteDataSource
import com.hussain.learningdashboard.feature.auth.data.remote.KtorAuthRemoteDataSource
import com.hussain.learningdashboard.feature.auth.domain.AuthRepository
import com.hussain.learningdashboard.feature.auth.domain.LogoutUseCase
import com.hussain.learningdashboard.feature.auth.presentation.LoginViewModel
import com.hussain.learningdashboard.feature.course.data.CourseRepositoryImpl
import com.hussain.learningdashboard.feature.course.data.local.CourseLocalDataSource
import com.hussain.learningdashboard.feature.course.data.local.RoomCourseLocalDataSource
import com.hussain.learningdashboard.feature.course.data.remote.CourseRemoteDataSource
import com.hussain.learningdashboard.feature.course.data.remote.KtorCourseRemoteDataSource
import com.hussain.learningdashboard.feature.course.domain.CourseRepository
import com.hussain.learningdashboard.feature.course.presentation.dashboard.DashboardViewModel
import com.hussain.learningdashboard.feature.course.presentation.detail.CourseDetailViewModel
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

const val SESSION_DATASTORE_FILE = "session.preferences_pb"

private val networkModule = module {
    // Swap the mock engine for OkHttp (Android) / Darwin (iOS) to talk to a real backend.
    single { createHttpClient(createMockLearningEngine(networkMonitor = get())) }
}

private val authModule = module {
    single<AuthRemoteDataSource> { KtorAuthRemoteDataSource(get()) }
    single<SessionStorage> { DataStoreSessionStorage(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LogoutUseCase(get(), get()) }
    viewModelOf(::LoginViewModel)
}

private val courseModule = module {
    single<CourseRemoteDataSource> { KtorCourseRemoteDataSource(get()) }
    single<CourseLocalDataSource> { RoomCourseLocalDataSource(get()) }
    single<CourseRepository> { CourseRepositoryImpl(get(), get()) }
    viewModelOf(::DashboardViewModel)
    viewModel { (courseId: Int) -> CourseDetailViewModel(courseId, get()) }
}

private val appModule = module {
    viewModelOf(::AppViewModel)
}

/** [platformModule] provides NetworkMonitor, AppDatabase and the session DataStore. */
fun initKoin(platformModule: Module) {
    startKoin {
        modules(platformModule, networkModule, authModule, courseModule, appModule)
    }
}

fun createSessionDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
