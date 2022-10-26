package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.example.ustadmobile.ui.ImageCompose
import com.ustadmobile.port.android.ui.compose.TextBody1
import com.ustadmobile.port.android.ui.compose.TextHeader3
import com.ustadmobile.port.android.ui.theme.ui.theme.gray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PagerView() {

    val pagesList = arrayOf(
        arrayOf(
            R.string.onboarding_no_internet_headline,
            R.string.onboarding_no_internet_subheadline, R.drawable.illustration_offline_usage),
        arrayOf(R.string.onboarding_offline_sharing,
            R.string.onboarding_offline_sharing_subheading, R.drawable.illustration_offline_sharing),
        arrayOf(R.string.onboarding_stay_organized_headline,
            R.string.onboarding_stay_organized_subheading, R.drawable.illustration_organized))

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var currentIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .width(screenWidth),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val listState = rememberLazyListState()
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
        ) {
            itemsIndexed(pagesList) { index, item ->
                ItemView(index, item)
            }
        }

        currentIndex = listState.firstVisibleItemIndex

        threeDots(currentIndex)
    }
}

@Composable
private fun ItemView(index: Int, item: Array<Int>) {

    val configuration = LocalConfiguration.current
    val screenWidth = (configuration.screenWidthDp-20).dp

    Column(
        modifier = Modifier
            .width(screenWidth)
            .height(IntrinsicSize.Max)
            .padding(20.dp, 0.dp, 0.dp,0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(1f)) {
            ImageCompose(
                item[2] as Int,
                modifier = Modifier
                    .padding(10.dp))
        }

        Box(modifier = Modifier.weight(0.4f)){
            bottomRow(index, item)
        }

    }
}

@Composable
private fun bottomRow(index: Int, item: Array<Int>){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextHeader3(
            text = stringResource(item[0] as Int),
            color = Color.DarkGray
        )
        TextBody1(
            text = stringResource(item[1] as Int),
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun threeDots(index: Int){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(15.dp)
    ) {
        for (i in 0..2){
            if (i == index){
                dotShape(size = 15)
            } else {
                dotShape(size = 8)
            }
        }
    }
}

@Composable
private fun dotShape(size: Int){
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(gray)
    )
    Spacer(modifier = Modifier.width(5.dp))
}