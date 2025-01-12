package com.ustadmobile.core.domain.matomo

/**
 * An interface for recording tracking events in Matomo analytics.
 * This use case encapsulates the logic for sending tracking information,
 * such as the URL path and title of a page or resource, to a Matomo server.
 */

interface RecordMatomoTrackingUseCase {
    suspend fun invoke(path: String, title: String)
}