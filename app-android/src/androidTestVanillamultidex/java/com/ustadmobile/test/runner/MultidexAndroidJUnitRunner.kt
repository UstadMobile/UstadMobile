package com.ustadmobile.test.runner
import android.app.Application
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.multidex.MultiDex
import android.content.Context
import android.util.Log

@Suppress("unused")
class MultidexAndroidJUnitRunner : androidx.test.runner.AndroidJUnitRunner() {

    override fun onCreate(args: Bundle) {
        Log.i("Multidexrun", "multidextestrunner onCreate")
        val context: Context = getApplicationContext()
        MultiDex.install(context)
        super.onCreate(args)
    }


}