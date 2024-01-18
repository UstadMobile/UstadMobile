package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.random.Random

@Preview
@Composable
fun UstadNumberTextEditFieldPreview() {

    var aNumber by remember {
        mutableStateOf(0.toFloat())
    }

    Column {
        UstadNumberTextField(
            modifier = Modifier.fillMaxWidth(),
            value = aNumber,
            label = { Text("Phone Number") },
            enabled = true,
            onValueChange = {
                aNumber = it
            },
            trailingIcon = {
                Text("points")
            }
        )

        //Test to ensure that the field will update when required by viewmodel
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                aNumber = Random.nextInt(1, 6).toFloat()
            }
        ) {
            Text("Roll Dice")
        }
    }



}