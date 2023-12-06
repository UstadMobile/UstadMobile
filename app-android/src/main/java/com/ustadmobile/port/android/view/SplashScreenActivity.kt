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
import androidx.lifecycle.lifecycleScope
import com.toughra.ustadmobile.R
import com.ustadmobile.libuicompose.theme.AppTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import kotlinx.coroutines.delay
import com.ustadmobile.core.R as CR

class SplashScreenActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            delay(2000L)
            val intent = Intent(this@SplashScreenActivity, AppActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContent {
            AppTheme {
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
                painter = painterResource(id = R.drawable.ic_launcher_maktab2),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
            )

            Text(
                text = stringResource(com.ustadmobile.core.R.string.app_name),
                style = Typography.h1,
                color = Color.Gray
            )
        }


    }
}

@Composable
@Preview
private fun SplashScreenPreview(){
    SplashScreen()
}