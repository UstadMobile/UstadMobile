package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R

/**
 * Main field layout for message fields in DetailViews
 */
@Composable
fun UstadMessageField(
    mainText: String,
    secondText: String,
    thirdText: String,
    fourthText: String,
    fifthText: String,
    thirdTextIcon: Int = 0,
    modifier: Modifier = Modifier,
    imageId: Int = 0,
    onClick: (() -> Unit)? = null,
    secondaryActionContent: (@Composable () -> Unit)? = null,
    autoPadding: Boolean = true,
){
    val modifierToUse = if(autoPadding) {
        modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    }else {
        modifier
    }

    if(onClick != null) {
        TextButton(
            modifier = modifierToUse,
            onClick = onClick
        ){
            MessageFieldContent(
                firstText = mainText,
                secondText = secondText,
                thirdText = thirdText,
                fourthText = fourthText,
                fifthText = fifthText,
                thirdTextIcon = thirdTextIcon,
                imageId = imageId,
                secondaryActionContent = secondaryActionContent,
            )
        }
    }else {
        MessageFieldContent(
            modifier = modifierToUse,
            firstText = mainText,
            secondText = secondText,
            thirdText = thirdText,
            fourthText = fourthText,
            fifthText = fifthText,
            thirdTextIcon = thirdTextIcon,
            imageId = imageId,
            secondaryActionContent = secondaryActionContent,
        )
    }
}

@Composable
private fun MessageFieldContent(
    modifier: Modifier = Modifier,
    imageId: Int = 0,
    firstText: String,
    secondText: String,
    thirdText: String,
    fourthText: String,
    fifthText: String,
    thirdTextIcon: Int = 0,
    secondaryActionContent: (@Composable () -> Unit)? = null,
) {
    Row(modifier){
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

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(){
                Text(
                    text = firstText,
                    style = MaterialTheme.typography.body1,
                    color = contentColorFor(backgroundColor = MaterialTheme.colors.background),

                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "" + fourthText,
                    style = MaterialTheme.typography.subtitle1,
                    color = colorResource(id = R.color.list_subheader),

                )
            }


            Row(){


                Text(
                    text = secondText,
                    style = MaterialTheme.typography.body2,
                    color = contentColorFor(backgroundColor = MaterialTheme.colors.background),
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "" + fifthText,
                    style = MaterialTheme.typography.body2,
                    color = colorResource(id = R.color.list_subheader),
                    textAlign = TextAlign.Right

                )
            }



            Row () {
                if(thirdTextIcon != 0) {
                    Image(
                        painter = painterResource(id = thirdTextIcon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                        ),
                        modifier = Modifier
                            .size(24.dp)
                    )

                    Text(
                        text = thirdText,
                        style = MaterialTheme.typography.subtitle2,
                        color = colorResource(id = R.color.list_subheader),
                    )

                }
            }


        }

        if(secondaryActionContent != null) {
            secondaryActionContent()
        }
    }
}


@Composable
@Preview
private fun MesssageFieldWithSecondaryActionPreview() {
    UstadMessageField(
        mainText = "Eddy Grant",
        secondText = "Hi I'm from Electric Avenue",
        thirdText = "I am also from Electric Avenue and I want to take it higher",
        fourthText = "02/Feb/2022",
        fifthText = "7 replies",
        imageId = R.drawable.ic_person_black_24dp,
        thirdTextIcon = R.drawable.ic_baseline_sms_24,
        secondaryActionContent = {
            IconButton(
                onClick = {  },
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                )
            }
        }
    )
}
