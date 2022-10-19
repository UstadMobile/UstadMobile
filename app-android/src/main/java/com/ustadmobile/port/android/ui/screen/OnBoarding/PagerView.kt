package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import androidx.compose.ui.unit.sp
import com.example.ustadmobile.ui.ImageCompose
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.android.ui.compose.TextBody1
import com.ustadmobile.port.android.ui.compose.TextHeader3
import com.ustadmobile.port.android.ui.theme.ui.theme.gray

@Composable
fun OnBoardingView() {

    val viewModel by lazy { OnBoardingViewModel() }

    viewModel.items = arrayOf(
        arrayOf(
            R.string.onboarding_no_internet_headline,
            R.string.onboarding_no_internet_subheadline, R.drawable.illustration_offline_usage),
        arrayOf(R.string.onboarding_offline_sharing,
            R.string.onboarding_offline_sharing_subheading, R.drawable.illustration_offline_sharing),
        arrayOf(R.string.onboarding_stay_organized_headline,
            R.string.onboarding_stay_organized_subheading, R.drawable.ic_logout))

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = LazyListState(),
    ) {
        itemsIndexed(viewModel.items) { index, item ->
            View(index, item)
        }
    }
}


@Composable
private fun View(index: Int, item: Array<Any>) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Column(
        modifier = Modifier
            .width(screenWidth)
            .height(IntrinsicSize.Max),
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
private fun bottomRow(index: Int, item: Array<Any>){
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

        Spacer(modifier = Modifier.height(20.dp))

        threeDots(index)
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