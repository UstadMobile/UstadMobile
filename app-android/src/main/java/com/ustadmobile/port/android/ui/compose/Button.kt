package com.example.ustadmobile.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Shadow(
    @Stable val offsetX: Dp,
    @Stable val offsetY: Dp,
    @Stable val radius: Dp,
    @Stable val color: Color,
)

@Composable
fun ButtonWithRoundCornerShape(text: String, onClick: () -> Unit) {
    Button(onClick = {onClick()}, shape = RoundedCornerShape(20.dp),
    modifier = Modifier.height(35.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun ButtonWithElevation() {
    Button(onClick = {
        //your onclick code here
    },elevation =  ButtonDefaults.elevation(
        defaultElevation = 10.dp,
        pressedElevation = 15.dp,
        disabledElevation = 0.dp
    ),
    modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(5.dp))) {
        Text(text = "Button with elevation")
    }
}