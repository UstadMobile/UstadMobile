package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * Main field layout for detail fields in DetailViews
 */
@Composable
fun UstadDetailField(
    valueText: String,
    labelText: String?,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    secondaryActionContent: (@Composable () -> Unit)? = null,
) {
    UstadDetailField(
        valueText = AnnotatedString(valueText),
        labelText = labelText?.let { AnnotatedString(it) },
        modifier = modifier,
        icon = icon,
        onClick = onClick,
        secondaryActionContent = secondaryActionContent,
    )
}

@Composable
fun UstadDetailField(
    valueText: String,
    labelText: String?,
    modifier: Modifier = Modifier,
    imageId: Int = 0,
    onClick: (() -> Unit)? = null,
    secondaryActionContent: (@Composable () -> Unit)? = null,
) {
    UstadDetailField(
        valueText = AnnotatedString(valueText),
        labelText = labelText?.let { AnnotatedString(it) },
        modifier = modifier,
        imageId = imageId,
        onClick = onClick,
        secondaryActionContent = secondaryActionContent,
    )
}

@Composable
fun UstadDetailField(
    valueText: AnnotatedString,
    labelText: AnnotatedString?,
    modifier: Modifier = Modifier,
    imageId: Int = 0,
    onClick: (() -> Unit)? = null,
    secondaryActionContent: (@Composable () -> Unit)? = null,
) {
    UstadDetailField(
        valueText = valueText,
        labelText = labelText,
        modifier = modifier,
        onClick = onClick,
        secondaryActionContent = secondaryActionContent,
        icon = {
            if(imageId != 0) {
//                Image(
//                    // TODO error
//                    painter = painterResource(id = imageId),
//                    contentDescription = null,
//                    colorFilter = ColorFilter.tint(
//                        color = contentColorFor(backgroundColor = MaterialTheme.colors.background)),
//                    modifier = Modifier
//                        .size(24.dp))
            }else {
                Spacer(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

/**
 * Main field layout for detail fields in DetailViews
 */
@Composable
fun UstadDetailField(
    valueText: AnnotatedString,
    labelText: AnnotatedString?,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    secondaryActionContent: (@Composable () -> Unit)? = null,
){

    if(onClick != null) {
        TextButton(
            modifier = modifier,
            onClick = onClick
        ){
            DetailFieldContent(
                valueText = valueText,
                labelText = labelText,
                icon = icon,
                secondaryActionContent = secondaryActionContent,
            )
        }
    }else {
        DetailFieldContent(
            modifier = modifier,
            valueText = valueText,
            labelText = labelText,
            icon = icon,
            secondaryActionContent = secondaryActionContent,
        )
    }
}

@Composable
private fun DetailFieldContent(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    valueText: AnnotatedString,
    labelText: AnnotatedString?,
    secondaryActionContent: (@Composable () -> Unit)? = null,
) {
    Row(modifier){
        if (icon != null){
            icon()
        } else {
            Box(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.body1,
                color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
            )

            if(labelText != null) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.subtitle1,
                    // TODO error
//                    color = colorResource(R.color.list_subheader),
                )
            }

        }

        if(secondaryActionContent != null) {
            secondaryActionContent()
        }
    }
}
