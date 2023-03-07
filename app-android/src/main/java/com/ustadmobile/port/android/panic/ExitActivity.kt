package com.ustadmobile.port.android.panic

import android.annotation.TargetApi
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import com.ustadmobile.port.android.panic.ExitActivity

class ExitActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishAndRemoveTask()
        System.exit(0)
    }

    companion object {
        fun exitAndRemoveFromRecentApps(activity: Activity) {
            val intent = Intent(activity, ExitActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    or Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
            activity.startActivity(intent)
        }
    }
}