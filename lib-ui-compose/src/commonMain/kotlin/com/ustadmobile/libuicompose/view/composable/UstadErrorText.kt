package com.ustadmobile.libuicompose.view.composable

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UstadErrorText(
    modifier: Modifier = Modifier,
    error: String,
){
    Text(text = error,
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.error,
        modifier = modifier,
    )
}

//@Composable
//@Preview
//private fun UstadErrorTextPreview() {
//    UstadErrorText(
//        error = "Email is not valid"
//    )
//}