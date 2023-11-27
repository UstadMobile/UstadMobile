package com.ustadmobile.libuicompose.components

import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//Dummy HTML edit function that will be handled with find/replaced
@Composable
fun UstadHtmlEditPlaceholder(
    htmlTextTmp: String,
    onChangeHtmlTmp: (String) -> Unit,
    editInNewScreenTmp: Boolean = false,
    onClickEditInNewScreenTmp: () -> Unit = { },
    placeholderText: String? = null,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {

    if(editInNewScreenTmp) {
        UstadClickableTextField(
            value = htmlTextTmp,
            onValueChange =  { },
            onClick = onClickEditInNewScreenTmp,
            placeholder = if(placeholderText != null) {
                { Text(placeholderText) }
            }else {
                null
            },
            modifier = modifier,
            isError =  isError,
        )
    }else {
        OutlinedTextField(
            value = htmlTextTmp,
            onValueChange = onChangeHtmlTmp,
            placeholder = if(placeholderText != null) {
                { Text(placeholderText) }
            }else {
                null
            },
            modifier = modifier,
        )
    }

}