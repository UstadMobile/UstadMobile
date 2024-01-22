package com.ustadmobile.libuicompose.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Composable
fun UstadEditHeader(
    text: String,
    modifier: Modifier = Modifier,
){
    Text(text, modifier = modifier.defaultItemPadding())
}
