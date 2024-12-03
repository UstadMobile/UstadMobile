package com.ustadmobile.core.domain.matomo

import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class RecordMatomoTrackingUseCaseAndroidImpl(private val tracker: Tracker) : RecordMatomoTrackingUseCase {
    override suspend fun invoke(path: String, title: String) {
        TrackHelper.track().screen(path).title(title).with(tracker)
    }
}
