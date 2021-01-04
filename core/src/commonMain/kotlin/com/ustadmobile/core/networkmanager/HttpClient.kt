package com.ustadmobile.core.networkmanager

import io.ktor.client.HttpClient

/**
 * Get default Http client to avoid creating extra thread pool. The client must have the JsonFeature
 * installed.
 */
@Deprecated("Use dependency injection instead!")
expect fun defaultHttpClient(): HttpClient
