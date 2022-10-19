package com.example.ustadmobile.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R

@Composable
fun ImageCompose(image: Int, size: Int) {
    Image(
        painter = painterResource(id = image),
        contentDescription = null,
        modifier = Modifier
            .size(size.dp))
}

@Composable
fun ImageCompose(image: Int, modifier: Modifier) {
    Image(
        painter = painterResource(id = image),
        contentDescription = null,
        modifier = modifier)
}


@Composable
fun CircleImageView() {
    Image(
        painter = painterResource(R.drawable.expo2020_logo),
        contentDescription = "avatar",
        contentScale = ContentScale.Crop,            // crop the image if it's not a square
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)                       // clip to the circle shape
            .border(2.dp, Color.Gray, CircleShape)   // add a border (optional)
    )
}
