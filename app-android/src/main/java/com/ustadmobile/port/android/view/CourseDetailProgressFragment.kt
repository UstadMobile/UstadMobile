package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.util.ext.defaultScreenPadding

@Composable
private fun CourseDetailProgressScreen(
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            Text(text = stringResource(id = R.string.courses),
                modifier = Modifier.rotate(-90F))
        }
    }
}

@Composable
@Preview
fun CourseDetailProgressScreenPreview() {

    MdcTheme {
        CourseDetailProgressScreen()
    }
}