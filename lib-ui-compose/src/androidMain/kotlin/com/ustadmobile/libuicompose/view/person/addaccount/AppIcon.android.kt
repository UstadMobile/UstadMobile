package com.ustadmobile.libuicompose.view.person.addaccount

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.R

@Composable
actual fun AppIcon() {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_icon),
        contentDescription = "app_icon",
        modifier = Modifier.height(100.dp).fillMaxWidth()
            .padding(horizontal = 20.dp),
    )
}