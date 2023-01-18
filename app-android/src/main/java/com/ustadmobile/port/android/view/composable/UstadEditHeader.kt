package com.ustadmobile.port.android.view.composable

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.port.android.util.ext.defaultItemPadding

@Composable
fun UstadEditHeader(
    text: String,
    modifier: Modifier = Modifier,
){
    Text(text, modifier = modifier.defaultItemPadding())
}
