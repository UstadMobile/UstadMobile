package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R

/**
 * Main field layout for detail fields in DetailViews
 */
@Composable
fun UstadDetailField(
    modifier: Modifier = Modifier,
    imageId: Int = 0,
    valueText: String,
    labelText: String,
    onClick: () -> Unit = {}
){
    TextButton(
        modifier = modifier,
        onClick = onClick
    ){
        Row{
            if (imageId != 0){
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = contentColorFor(backgroundColor = MaterialTheme.colors.background)),
                    modifier = Modifier
                        .size(24.dp))
            } else {
                Box(modifier = Modifier.width(24.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.body1,
                    color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                )

                Text(
                    text = labelText,
                    style = MaterialTheme.typography.subtitle1,
                    color = colorResource(id = R.color.list_subheader),
                )
            }
        }
    }
}

@Composable
@Preview
private fun DetailFieldPreview() {
    UstadDetailField(
        imageId = R.drawable.ic_date_range_black_24dp,
        valueText = "01/Jan/1980",
        labelText = "Birthday"
    )
}

