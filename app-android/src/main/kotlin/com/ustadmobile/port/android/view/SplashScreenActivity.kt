/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.view


import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.work.WorkManager
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.SplashScreenView
import com.ustadmobile.staging.port.android.impl.ClazzLogScheduleWorker
import java.util.concurrent.TimeUnit


class SplashScreenActivity : SplashScreenView, UstadBaseActivity(), DialogInterface.OnClickListener{


    override fun onClick(dialog: DialogInterface?, which: Int) {
        checkPermissions()
    }

    override fun startUi(delay: Boolean, animate: Boolean) {
        Handler().postDelayed({
            UstadMobileSystemImpl.instance.startUI(this@SplashScreenActivity)
        }, if(delay) TimeUnit.SECONDS.toMillis(if(animate) 3 else 2) else 0)
    }

    private lateinit var constraintLayout: ConstraintLayout

    override fun animateOrganisationIcon(animate: Boolean, delay: Boolean) {

        organisationIcon.setOnClickListener{
            val constraint = ConstraintSet()
            val transition = AutoTransition()
            transition.duration = 1000
            constraint.clone(this, R.layout.activity_splash_screen_zoom)
            TransitionManager.beginDelayedTransition(constraintLayout, transition)
            constraint.applyTo(constraintLayout)
        }

        if(delay){
            Handler().postDelayed({
                organisationIcon.performClick()
            }, TimeUnit.MILLISECONDS.toMillis(if(animate) 200 else 0))
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id==R.id.action_leavecontainer){
            return true;
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        var allGranted = permissions.size == 2
        for (i in grantResults.indices) {
            allGranted = allGranted and (grantResults[i] == PackageManager.PERMISSION_GRANTED)
        }

        if (allGranted) {
            Handler().postDelayed(
                    {
                        UstadMobileSystemImpl.instance.startUI(this@SplashScreenActivity)
                    }, 0)
        } else {
            /* avoid possibly getting into an infinite loop if we had no user interaction
                and permission was denied
             */
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg voids: Void): Void? {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                    }

                    return null
                }

                override fun onPostExecute(o: Void) {
                    this@SplashScreenActivity.checkPermissions()
                }
            }.execute()
        }

    }

    private lateinit var organisationIcon : ImageView

    private val impl =  UstadMobileSystemImpl.instance

    val EXTERNAL_STORAGE_REQUESTED = 1

    val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    internal var rationalesShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLogout = false

        setTheme(R.style.ThemeOnboarding)

        setContentView(R.layout.activity_splash_screen)

        constraintLayout = findViewById(R.id.constraint_layout)

        val launched = impl.getAppPref(OnBoardingView.PREF_TAG, "false",this).toBoolean()

        val showSplash = impl.getAppConfigString(
                AppConfig.KEY_SHOW_SPASH_SCREEN, null, this)!!.toBoolean()

        queueClazzLogScheduleWorker()

        Handler().postDelayed({
            impl.startUI(this@SplashScreenActivity)
        }, if(showSplash || !launched) TimeUnit.SECONDS.toMillis(2) else 0)



    }

    private fun queueClazzLogScheduleWorker(){
        WorkManager.getInstance().cancelAllWorkByTag(ClazzLogScheduleWorker.TAG)
        ClazzLogScheduleWorker.queueClazzLogScheduleWorker(
                ClazzLogScheduleWorker.getNextClazzLogScheduleDueTime())
    }


    /**
     * Checks for permissions and alerts the user to give permissions.
     */
    fun checkPermissions() {
        var hasRequiredPermissions = true
        for (i in REQUIRED_PERMISSIONS.indices) {
            hasRequiredPermissions = hasRequiredPermissions and
                    (ContextCompat.checkSelfPermission(this,
                    REQUIRED_PERMISSIONS[i]) === PackageManager.PERMISSION_GRANTED)
        }

        if (!hasRequiredPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) && !rationalesShown) {
                //show an alert
                val builder = AlertDialog.Builder(this)
                builder.setTitle("File permissions required")
                        .setMessage("This app requires file permissions " +
                                "on the SD card to download and save content")
                builder.setPositiveButton("OK", this)
                val dialog = builder.create()
                dialog.show()
                rationalesShown = true
                return
            } else {
                rationalesShown = false
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        EXTERNAL_STORAGE_REQUESTED)
                return
            }
        }
    }


}
