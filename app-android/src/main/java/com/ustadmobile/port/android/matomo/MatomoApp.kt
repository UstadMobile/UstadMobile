package com.ustadmobile.port.android.matomo

import android.app.Application
import com.toughra.ustadmobile.BuildConfig
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

abstract class MatomoApp : Application() {
    private var tracker: Tracker? = null

    abstract fun onCreateTrackerConfig(): TrackerBuilder

    @Synchronized
    fun getTracker(): Tracker {
        if (tracker == null) {
            tracker = onCreateTrackerConfig().build(Matomo.getInstance(this))
        }
        return tracker!!
    }

    override fun onLowMemory() {
        tracker?.dispatch()
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        if ((level == TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_COMPLETE) && tracker != null) {
            tracker?.dispatch()
        }
        super.onTrimMemory(level)
    }
}
