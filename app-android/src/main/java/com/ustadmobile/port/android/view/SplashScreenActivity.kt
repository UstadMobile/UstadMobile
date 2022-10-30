package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ustadmobile.ui.ImageCompose
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.port.android.ui.compose.TextHeader1
import com.ustadmobile.port.android.ui.compose.TextBody1
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.gray
import kotlinx.coroutines.delay
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

class SplashScreenActivity : ComponentActivity() {

    val di: DI by closestDI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            delay(2000L)
            startActivity(this@SplashScreenActivity, di)
            finish()
        }
        setContent {
            UstadMobileTheme {
                SplashScreen()
            }
        }
    }

    companion object {
        fun provideFactory(
            di: DI,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory = object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return OnBoardingViewModel(di, SavedStateHandleAdapter(handle)) as T
            }
        }
    }
}

@Composable
private fun SplashScreen(){
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImageCompose(R.drawable.ic_launcher_icon, 110)
            TextHeader1("Ustad Mobile", gray)
        }

        ImageCompose(R.drawable.expo2020_logo, 100)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextBody1("Created in partnership with", gray)
            ImageCompose(R.drawable.ic_irc, 90)
        }
    }
}

@Composable
@Preview
private fun SplashScreenPreview(){
    SplashScreen()
}

private fun startActivity(context: Context, di: DI) {

    val systemImpl: UstadMobileSystemImpl = di.direct.instance()

    val activityClass = if(systemImpl.getAppPref(OnBoardingView.PREF_TAG, "false").toBoolean()) {
        MainActivity::class.java
    }else {
        OnboardingActivity::class.java
    }

    var intent = Intent(context, activityClass)
    context.startActivity(intent)
}