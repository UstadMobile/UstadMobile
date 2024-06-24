package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun UstadTitleDescriptionButton(
    title: String,
    description: String,
    titleColor: Color = Color.Black,
    titleSize: Float = 16f,
    descriptionSize: Float = 12f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp).clickable(onClick = onClick)
    ) {
        androidx.compose.material.Text(
            text = title,
            color = titleColor,
            fontSize = titleSize.sp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        androidx.compose.material.Text(
            text = description,
            fontSize = descriptionSize.sp
        )
    }
}