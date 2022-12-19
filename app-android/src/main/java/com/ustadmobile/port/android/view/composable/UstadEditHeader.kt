package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UstadEditHeader(
    text: String,
    modifier: Modifier = Modifier,
    autoPadding: Boolean = true,
){
    val effectiveModifier = if(autoPadding) {
        modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    }else {
        modifier
    }
    Text(text, modifier = effectiveModifier)
}
