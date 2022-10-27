package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.ustadmobile.ui.ImageCompose
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.ui.compose.TextHeader1
import com.ustadmobile.port.android.ui.compose.TextBody1
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.gray
import com.ustadmobile.port.android.util.AppPreferences
import kotlinx.coroutines.delay

class SplashScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            delay(2000L)
            startActivity(this@SplashScreenActivity)
            finish()
        }
        setContent {
            UstadMobileTheme {
                splashScreen()
            }
        }
    }
}

@Composable
@Preview
private fun splashScreen(){
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

private fun startActivity(context: Context) {
    val appPreferences = AppPreferences()
    var intent = Intent(context, OnboardingActivity::class.java)
    if(appPreferences.getIsLoggedIn(context) == true) {
        intent = Intent(context, MainActivity::class.java)
    }
    context.startActivity(intent)
}