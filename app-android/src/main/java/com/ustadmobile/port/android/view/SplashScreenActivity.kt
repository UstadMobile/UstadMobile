package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.libuicompose.theme.UstadAppTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.ext.getUstadDeepLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ustadmobile.core.R as CR

class SplashScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                delay(2000L)
                val intent = Intent(this@SplashScreenActivity, AppActivity::class.java)
                intent.getUstadDeepLink()?.also {
                    intent.putExtra(UstadViewModel.ARG_OPEN_LINK, it)
                }
                startActivity(intent)
                finish()
            }
        }

        setContent {
            UstadAppTheme {
                SplashScreen()
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

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp))

            Text(text = "Ustad Mobile",
                style = Typography.h1,
                color = Color.Gray
            )
        }

        Image(
            painter = painterResource(id = R.drawable.expo2020_logo),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(text = stringResource(CR.string.created_partnership),
                style = Typography.body1,
                color = Color.DarkGray)

            Image(
                painter = painterResource(id = R.drawable.ic_irc),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp))
        }
    }
}

@Composable
@Preview
private fun SplashScreenPreview(){
    SplashScreen()
}