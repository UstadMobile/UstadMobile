package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.config.AppConfig
import com.ustadmobile.core.util.ext.appMetaData
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
    val context = LocalContext.current
    val showPoweredBy = remember {
        context.appMetaData?.getString(AppConfig.KEY_CONFIG_SHOW_POWERED_BY) == "true"
    }


    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp))

                Text(text = stringResource(CR.string.app_name),
                    style = Typography.h1,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if(showPoweredBy) {
            Text(stringResource(CR.string.powered_by))
        }
    }
}

@Composable
@Preview
private fun SplashScreenPreview(){
    SplashScreen()
}