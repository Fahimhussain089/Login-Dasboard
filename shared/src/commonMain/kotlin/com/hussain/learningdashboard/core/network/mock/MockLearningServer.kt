package com.hussain.learningdashboard.core.network.mock

import com.hussain.learningdashboard.core.network.AppJson
import com.hussain.learningdashboard.core.network.NetworkMonitor
import com.hussain.learningdashboard.core.network.NoInternetException
import com.hussain.learningdashboard.feature.auth.data.remote.LoginRequestDto
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Stands in for the real backend. It honours device connectivity, so switching the
 * device offline makes requests fail exactly like a real network call would.
 * Replacing it with OkHttp/Darwin engines is a one-line DI change.
 */
fun createMockLearningEngine(
    networkMonitor: NetworkMonitor,
    latency: Duration = 800.milliseconds,
): HttpClientEngine = MockEngine { request ->
    delay(latency)
    if (!networkMonitor.isOnline()) throw NoInternetException()

    val path = request.url.encodedPath.removePrefix("/v1/")
    val lessonsPath = Regex("""courses/(\d+)/lessons""").matchEntire(path)
    when {
        request.method == HttpMethod.Post && path == "auth/login" -> handleLogin(request)
        request.method == HttpMethod.Get && path == "courses" -> respondJson(MockResponses.courses)
        request.method == HttpMethod.Get && lessonsPath != null -> {
            val lessons = MockResponses.lessonsFor(lessonsPath.groupValues[1].toInt())
            if (lessons != null) respondJson(lessons) else respondJson("""{"message":"Course not found"}""", HttpStatusCode.NotFound)
        }
        else -> respondJson("""{"message":"Not found"}""", HttpStatusCode.NotFound)
    }
}

private suspend fun MockRequestHandleScope.handleLogin(request: HttpRequestData): HttpResponseData {
    val body = AppJson.decodeFromString<LoginRequestDto>(request.body.toByteArray().decodeToString())
    val validCredentials = body.email.equals(MockResponses.DEMO_EMAIL, ignoreCase = true) &&
        body.password == MockResponses.DEMO_PASSWORD
    return if (validCredentials) {
        respondJson("""{"token":"mock-jwt-token","userName":"Demo Learner"}""")
    } else {
        respondJson("""{"message":"Invalid email or password"}""", HttpStatusCode.Unauthorized)
    }
}

private fun MockRequestHandleScope.respondJson(
    content: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = content,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)
