package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R

@Composable
fun UstadQuickActionButton(
    imageId: Int = 0,
    labelText: String,
    onClick: (() -> Unit) = {  },
){
    TextButton(
        modifier = Modifier.width(110.dp),
        onClick = onClick
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Image(
                painter = painterResource(id = imageId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = contentColorFor(backgroundColor = MaterialTheme.colors.background)),
                modifier = Modifier
                    .size(24.dp))

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = labelText,
                style = MaterialTheme.typography.subtitle1,
                color = contentColorFor(MaterialTheme.colors.secondary)
            )
        }
    }
}


@Composable
@Preview
private fun QuickActionButtonPreview() {
    MdcTheme {
        UstadQuickActionButton(
            imageId = R.drawable.ic_date_range_black_24dp,
            labelText = "Birthday"
        )
    }
}
