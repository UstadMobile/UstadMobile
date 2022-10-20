package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ustadmobile.ui.ButtonWithRoundCorners
import com.example.ustadmobile.ui.ImageCompose
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.port.android.ui.compose.TextBody2
import com.ustadmobile.port.android.ui.screen.MainScreen
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.gray


class OnBoarding : ComponentActivity() {

    private val viewModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UstadMobileTheme {
                OnboardingViaViewModel(viewModel = viewModel)
            }
        }
    }

    @Composable
    private fun topRow(){
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier
                .weight(1f)){
//                SelectLanguageMenu{
//                    context.findActivity()?.recreate()
//                }
            }

            Box(modifier = Modifier
                .weight(1f),){
                ImageCompose(
                    image = R.drawable.expo2020_logo,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(110.dp),)
            }
        }
    }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    @Composable
    private fun bottomRow(){
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Box(modifier = Modifier.weight(1f)){
            }

            Box(modifier = Modifier
                .weight(1f)){
                ButtonWithRoundCorners(context.getString(R.string.onboarding_get_started_label))
                {  }
            }

            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                TextBody2(text = context.getString(R.string.created_partnership), color = gray)
                ImageCompose(
                    image = R.drawable.ic_irc,
                    size = 90)
            }
        }
    }
}

class OnboardingPage(
    val headlineText: Int,
    val subheaderText: Int,
    val imageId: Int
)

val ONBOARDING_PAGE_LIST = listOf(
    OnboardingPage(R.string.onboarding_no_internet_headline,
        R.string.onboarding_no_internet_subheadline,
        R.drawable.illustration_offline_usage),
    OnboardingPage(R.string.onboarding_offline_sharing,
        R.string.onboarding_offline_sharing_subheading,
        R.drawable.illustration_offline_sharing),
    OnboardingPage(R.string.onboarding_stay_organized_headline,
        R.string.onboarding_stay_organized_subheading,
        R.drawable.ic_logout))

@Composable
fun OnboardingViaViewModel(viewModel: OnBoardingViewModel) {
    val langList = viewModel.languageList.collectAsState(initial = listOf())
    val currentLang = viewModel.currentLanguage.collectAsState(
        initial = UstadMobileSystemCommon.UiLanguage("", ""))

    OnboardingView(langList.value, currentLang.value, { viewModel.onLanguageSelected(it) })
}

@Composable
@Preview
fun OnboardingView(
    langList: List<UstadMobileSystemCommon.UiLanguage> = listOf(),
    currentLanguage: UstadMobileSystemCommon.UiLanguage
        = UstadMobileSystemCommon.UiLanguage("en", "English"),
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
) {
    val configuration = LocalConfiguration.current

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier
            .weight(1f)){
//            SelectLanguageMenu{
//                context.findActivity()?.recreate()
//            }
        }

        Box(modifier = Modifier
            .weight(1f),){
            ImageCompose(
                image = R.drawable.expo2020_logo,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(110.dp),)
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = LazyListState(),
    ) {

        val screenWidth = configuration.screenWidthDp.dp

        itemsIndexed(ONBOARDING_PAGE_LIST) { index, item ->
            Column(
                modifier = Modifier
                    .width(screenWidth)
                    .height(IntrinsicSize.Max),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(Modifier.weight(1f)) {
                    ImageCompose(item.imageId, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = currentLanguage.langDisplay)
    }
}