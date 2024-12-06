package com.ustadmobile.core.domain.matomo

import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * An Android-specific implementation of the `RecordMatomoTrackingUseCase` interface.
 * This implementation uses the Matomo `Tracker` library to record tracking events,
 * such as page views, on an Android platform.
 *
 * @param tracker An instance of the Matomo `Tracker` that facilitates tracking events.
 */
class RecordMatomoTrackingUseCaseAndroidImpl(private val tracker: Tracker) :
    RecordMatomoTrackingUseCase {

    /**
     * Sends a tracking event to the Matomo server using the provided `Tracker` instance.
     *
     * This function logs a page view with the specified URL path and title, using the
     * Matomo `TrackHelper` utility. The tracking event includes details about the
     * screen path and its corresponding title, making it available for analytics reports.
     *
     * @param path The URL path of the page or feature being tracked. This is typically
     *             a relative path (e.g., "/profile") but can also be an absolute URL.
     * @param title The title of the page or feature being tracked, providing a human-readable
     *              identifier for the event in Matomo's analytics dashboard.
     *
     */
    override suspend fun invoke(path: String, title: String) {
        TrackHelper.track().screen(path).title(title).with(tracker)
    }
}
