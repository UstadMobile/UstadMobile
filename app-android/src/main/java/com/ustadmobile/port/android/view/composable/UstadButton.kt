package com.ustadmobile.port.android.view.composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R

enum class UstadButtonVariant {
    OUTLINED,
    TEXT,
    CONTAINED
}

@Composable
fun UstadButton(
    labelText: String,
    variant: UstadButtonVariant,
    disabled: Boolean = false,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    colorId: Int? = null
) {
    if (variant == UstadButtonVariant.CONTAINED){
        ContainedButton(
            labelText,
            disabled,
            onClick,
            modifier,
            startImageId
        )
    } else if (variant == UstadButtonVariant.TEXT){
        TextButton(
            labelText,
            disabled,
            onClick,
            modifier,
            startImageId,
        )
    }
}


@Composable
private fun TextButton(
    labelText: String,
    disabled: Boolean = false,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    colorId: Int? = null
){
    TextButton(
        modifier = modifier,
        onClick = onClick
    ){
        Row{

            if (startImageId != null){
                Image(
                    painter = painterResource(id = startImageId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                    ),
                    modifier = Modifier
                        .size(24.dp))

                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = labelText,
                style = MaterialTheme.typography.subtitle1,
                color = contentColorFor(MaterialTheme.colors.secondary)
            )
        }
    }
}

@Composable
private fun ContainedButton(
    labelText: String,
    disabled: Boolean = false,
    onClick: (() -> Unit) = {  },
    modifier: Modifier = Modifier,
    startImageId: Int? = null,
    colorId: Int? = null
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id =
                colorId ?: R.color.primaryColor
            )
        )
    ) {
        Text(
            labelText.uppercase(),
            color = contentColorFor(
                colorResource(id =
                   colorId ?: R.color.primaryColor
                )
            )
        )
    }
}


@Composable
@Preview
private fun UstadButtonPreview() {
    MdcTheme {
        UstadButton(
            labelText = "Birthday",
            variant = UstadButtonVariant.CONTAINED,
            onClick = {}
        )
    }
}