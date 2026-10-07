package com.hussain.learningdashboard.feature.course.presentation.dashboard

import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.feature.auth.domain.AuthRepository
import com.hussain.learningdashboard.feature.auth.domain.LogoutUseCase
import com.hussain.learningdashboard.feature.course.data.CourseRepositoryImpl
import com.hussain.learningdashboard.feature.course.fakes.FakeCourseLocalDataSource
import com.hussain.learningdashboard.feature.course.fakes.FakeCourseRemoteDataSource
import com.hussain.learningdashboard.feature.course.fakes.TestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val remote = FakeCourseRemoteDataSource(courses = listOf(TestData.pythonCourse))
    private val repository = CourseRepositoryImpl(remote, FakeCourseLocalDataSource())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `offline launch shows cached courses with an offline warning`() = runTest {
        repository.refreshCourses()
        remote.isOnline = false

        val state = createViewModel().uiState.first { it !is DashboardUiState.Loading }

        assertEquals(
            DashboardUiState.Content(
                courses = repository.observeCourses().first(),
                isRefreshing = false,
                refreshError = AppError.NoInternet,
            ),
            state,
        )
    }

    @Test
    fun `offline first launch with no cache shows the error state`() = runTest {
        remote.isOnline = false

        val state = createViewModel().uiState.first { it !is DashboardUiState.Loading }

        assertEquals(DashboardUiState.Error(AppError.NoInternet), state)
    }

    @Test
    fun `empty course list from the API shows the empty state`() = runTest {
        remote.courses = emptyList()

        val state = createViewModel().uiState.first { it !is DashboardUiState.Loading }

        assertEquals(DashboardUiState.Empty, state)
    }

    private fun createViewModel() = DashboardViewModel(
        courseRepository = repository,
        logoutUseCase = LogoutUseCase(NoOpAuthRepository, repository),
    )

    private object NoOpAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String) = AppResult.Success(Unit)
        override suspend fun isLoggedIn() = true
        override suspend fun logout() = Unit
    }
}
