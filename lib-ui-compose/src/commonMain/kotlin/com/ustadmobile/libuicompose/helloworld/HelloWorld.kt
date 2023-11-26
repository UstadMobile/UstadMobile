package com.ustadmobile.libuicompose.helloworld

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.components.UstadHtmlText

@Composable
fun HelloWorld() {

    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {


        UstadHtmlText(
            html = "<p style=\"text-align: left;\">Sallam, I am a<span style=\"font-weight: 700; font-style: italic;\">Developer </span>in <span style=\"text-decoration: underline;\">UstadMobile</span></p>"
        )

    }
}


