package com.ustadmobile.port.android.viewModel

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.ustadmobile.core.tincan.Activity
import com.ustadmobile.port.android.util.AppPreferences
import com.ustadmobile.port.android.view.MainActivity
import com.ustadmobile.port.android.view.OnBoardingActivity

class SplashScreenViewModel: ViewModel() {
    private val appPreferences = AppPreferences()

    fun startActivity(context: Context) {
        var intent = Intent(context, OnBoardingActivity::class.java)
        if(appPreferences.getIsLoggedIn(context) == true) {
            intent = Intent(context, MainActivity::class.java)
        }
        context.startActivity(intent)
    }
}