package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R

@Composable
fun UstadContainedButton(
    labelText: String,
    enabled: Boolean = true,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    backgroundColorId: Int = R.color.primaryColor,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
){
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = backgroundColorId)
        )
    ) {

        ButtonContent(
            labelText,
            modifier,
            startImageId,
            backgroundColorId,
            horizontalArrangement
        )
    }
}

@Composable
fun UstadTextButton(
    labelText: String,
    enabled: Boolean = true,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
){
    TextButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ){

        ButtonContent(
            labelText,
            modifier,
            startImageId,
            null,
            horizontalArrangement
        )
    }
}

@Composable
fun UstadOutlinedButton(
    labelText: String,
    enabled: Boolean = true,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
){

    OutlinedButton(
        onClick = onClick,
        enabled = enabled
    ) {

        ButtonContent(
            labelText,
            modifier,
            startImageId,
            null,
            horizontalArrangement
        )
    }
}

@Composable
private fun ButtonContent(
    labelText: String,
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    textColorId: Int? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
){
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (startImageId != null) {
            Image(
                painter = painterResource(id = startImageId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    contentColorFor(MaterialTheme.colors.background)
                ),
                modifier = Modifier
                    .size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))
        }

        if (textColorId == null){
            Text(labelText.uppercase())
        } else {
            Text(
                labelText.uppercase(),
                color = contentColorFor(
                    colorResource(id = textColorId)
                )
            )
        }
    }
}

@Composable
@Preview
private fun UstadContainedButtonPreview() {
    MdcTheme{
        UstadContainedButton(
            labelText = "Birthday",
            enabled = true,
            onClick = { },
            modifier = Modifier.width(110.dp),
            backgroundColorId = R.color.secondaryColor,
            startImageId = R.drawable.ic_add_white_24dp,
            horizontalArrangement = Arrangement.Start
        )
    }
}

@Composable
@Preview
private fun UstadTextButtonPreview() {
    MdcTheme{
        UstadTextButton(
            labelText = "Birthday",
            onClick = {},
            startImageId = R.drawable.ic_add_white_24dp,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        )
    }
}

@Composable
@Preview
private fun UstadOutlinedButtonPreview() {
    MdcTheme {
        UstadOutlinedButton(
            labelText = "Birthday",
            onClick = {},
            startImageId = R.drawable.ic_add_white_24dp,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        )
    }
}