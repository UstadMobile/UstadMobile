package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ustadmobile.ui.ButtonWithRoundCorners
import com.example.ustadmobile.ui.ImageCompose
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.android.ui.compose.TextBody2
import com.ustadmobile.port.android.ui.screen.MainScreen
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.gray


class OnBoarding : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UstadMobileTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Box(Modifier.weight(0.2f)) {
                        topRow()
                    }

                    Box(Modifier.weight(1f)) {
                        OnBoardingView()
                    }

                    Box(Modifier.weight(0.2f)) {
                        bottomRow()
                    }
                }
            }
        }
    }

    @Composable
    private fun topRow(){
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier
                .weight(1f)){
                SelectLanguageMenu{
                    context.findActivity()?.recreate()
                }
            }

            Box(modifier = Modifier
                .weight(1f),){
                ImageCompose(
                    image = R.drawable.expo2020_logo,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(110.dp),)
            }
        }
    }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    @Composable
    private fun bottomRow(){
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Box(modifier = Modifier.weight(1f)){
            }

            Box(modifier = Modifier
                .weight(1f)){
                ButtonWithRoundCorners(context.getString(R.string.onboarding_get_started_label))
                { startMainActivity(context) }
            }

            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                TextBody2(text = context.getString(R.string.created_partnership), color = gray)
                ImageCompose(
                    image = R.drawable.ic_irc,
                    size = 90)
            }
        }
    }
}



private fun startMainActivity(context: Context){
    val intent = Intent(context, MainScreen::class.java)
    val systemImpl =  UstadMobileSystemImpl()
    systemImpl.setAppPref(com.ustadmobile.core.view.OnBoardingView.PREF_TAG, true.toString(), context)
    context.startActivity(intent)
}
