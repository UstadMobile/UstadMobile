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

import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SplashPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SplashView
import com.ustadmobile.port.android.impl.DbInitialEntriesInserter
import java.util.concurrent.TimeUnit


class SplashScreenActivity : SplashView, UstadBaseActivity() {

    private lateinit var organisationIcon : ImageView

    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.ThemeOnboarding)
        setContentView(R.layout.activity_splash_screen)

        organisationIcon = findViewById(R.id.organisation_icon)
        constraintLayout = findViewById(R.id.constraint_layout)

        val presenter = SplashPresenter(this, UMAndroidUtil.bundleToMap(intent.extras),
                this, UstadMobileSystemImpl.instance)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

    }

    override fun startUi(delay: Boolean, animate: Boolean) {
        Handler().postDelayed({
            UstadMobileSystemImpl.instance.startUI(this@SplashScreenActivity)
        }, if(delay) TimeUnit.SECONDS.toMillis(if(animate) 3 else 2) else 0)
    }

    override fun preloadData() {
        val dbWork = OneTimeWorkRequest.Builder(
                DbInitialEntriesInserter.DbInitialEntriesInserterWorker::class.java)
                .build()
        WorkManager.getInstance().enqueue(dbWork)
    }

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


}
