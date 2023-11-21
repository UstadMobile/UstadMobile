package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Preview
@Composable
private fun UstadExposedDropDownMenuFieldPreview() {
    var selectedOption by remember {
        mutableStateOf("Coffee")
    }

    UstadExposedDropDownMenuField<String>(
        value = selectedOption,
        label = "Drink",
        options = listOf("Coffee", "Tea"),
        onOptionSelected = {
            selectedOption = it
        },
        itemText =  { it },
    )
}