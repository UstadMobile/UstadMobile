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

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SplashScreenPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SplashScreenView


class SplashScreenActivity : AppCompatActivity(), SplashScreenView  {

    override val viewContext: Any
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {

        //add translucent effect on toolbar - full screen
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        super.onCreate(savedInstanceState)

        setTheme(R.style.ThemeSplash)
        setContentView(R.layout.activity_splash_screen)

        val presenter = SplashScreenPresenter(this, UMAndroidUtil.bundleToMap(intent.extras),
                this, UstadMobileSystemImpl.instance)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

}
