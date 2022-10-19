package com.ustadmobile.port.android.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ustadmobile.ui.ButtonWithIcon
import com.example.ustadmobile.ui.ImageCompose
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.ui.TextInput
import com.ustadmobile.port.android.ui.compose.TextHeader1
import com.ustadmobile.port.android.ui.compose.TextBody1
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.black
import com.ustadmobile.port.android.ui.theme.ui.theme.gray
import com.ustadmobile.port.android.ui.theme.ui.theme.primary

class MainScreen : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UstadMobileTheme {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    ImageCompose(R.drawable.illustration_connect, 180)

                    TextBody1(getString(R.string.please_enter_the_linK), black)

                    TextInput(getString(R.string.site_link))

                    TextBody1(getString(R.string.or), gray)

                    ButtonWithIcon(
                        getString(R.string.create_a_new_learning_env),
                        R.drawable.ic_add_black_24dp)
                }
            }
        }
    }
}
