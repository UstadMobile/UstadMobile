package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ustadmobile.ui.ButtonWithRoundCorners
import com.example.ustadmobile.ui.ImageCompose
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.OnboardingUiState
import com.ustadmobile.port.android.ui.compose.TextBody1
import com.ustadmobile.port.android.ui.compose.TextBody2
import com.ustadmobile.port.android.ui.compose.TextHeader3
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.gray
import com.ustadmobile.port.android.ui.theme.ui.theme.primary
import com.ustadmobile.port.android.util.ext.getActivityContext
import com.ustadmobile.port.android.util.ext.getUstadLocaleSetting
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import java.util.*

class OnboardingActivity : ComponentActivity() {

    val di: DI by closestDI()
    private val viewModel: OnBoardingViewModel by viewModels {
        provideFactory(di, this, null)
    }

    override fun attachBaseContext(newBase: Context) {
        val res = newBase.resources
        val config = res.configuration
        val languageSetting = newBase.getUstadLocaleSetting()

        val locale = if (languageSetting == UstadMobileSystemCommon.LOCALE_USE_SYSTEM)
            Locale.getDefault()
        else
            Locale(languageSetting)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UstadMobileTheme {
                OnboardingScreen(viewModel = viewModel)
            }
        }
    }

    companion object {
        fun provideFactory(
            di: DI,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory = object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return OnBoardingViewModel(di, SavedStateHandleAdapter(handle)) as T
            }
        }
    }
}

@Composable
private fun OnboardingScreen(viewModel: OnBoardingViewModel) {
    val uiState: OnboardingUiState by viewModel.uiState.collectAsState(initial = OnboardingUiState())
    val context = LocalContext.current.getActivityContext()
    OnboardingScreen(uiState.languageList, uiState.currentLanguage,
        onSetLanguage = { viewModel.onLanguageSelected(it) },
        onClickNext = { onClickNext({ viewModel.onClickNext() }, context)  }
    )
}

private fun onClickNext(onClickNext: () -> Unit = {}, context: Context){
    val intent = Intent(context, MainActivity::class.java)
    onClickNext()
    context.startActivity(intent)
}

@Composable
@Preview
private fun OnboardingScreen(
    langList: List<UstadMobileSystemCommon.UiLanguage> = listOf(),
    currentLanguage: UstadMobileSystemCommon.UiLanguage
        = UstadMobileSystemCommon.UiLanguage("en", "English"),
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
    onClickNext: () -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .background(Color.White)
    ) {

        Box(modifier = Modifier.weight(0.13F)){
            topRow(langList, currentLanguage, onSetLanguage = {
                onSetLanguage(it)
            })
        }

        Spacer(modifier = Modifier.height(15.dp))

        Box(modifier = Modifier.weight(0.74F)){
            PagerView()
        }

        Box(modifier = Modifier.weight(0.13F)){
            bottomRow(onClickNext)
        }
    }

}

@Composable
private fun topRow(
    langList: List<UstadMobileSystemCommon.UiLanguage> = listOf(),
    currentLanguage: UstadMobileSystemCommon.UiLanguage
    = UstadMobileSystemCommon.UiLanguage("en", "English"),
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
){
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier
            .weight(1f)){
            SetLanguageMenu(langList = langList,
                currentLanguage,
                onItemSelected = onSetLanguage,)
        }

        Box(modifier = Modifier
            .weight(1f),){
            ImageCompose(image = R.drawable.expo2020_logo,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(110.dp),)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SetLanguageMenu(
    langList: List<UstadMobileSystemCommon.UiLanguage>,
    currentLanguage: UstadMobileSystemCommon.UiLanguage,
    onItemSelected: (UstadMobileSystemCommon.UiLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = currentLanguage.langDisplay,
            onValueChange = { },
            label = { stringResource(R.string.language) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = primary,
                unfocusedIndicatorColor = primary,
                disabledIndicatorColor = primary,
            )
        )

        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {
            val activity = LocalContext.current.getActivityContext()
            langList.forEachIndexed { index, uiLanguage ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        expanded = false
                        onItemSelected(uiLanguage)
                        activity.recreate()
                    }
                ) {
                    Text(text = uiLanguage.langDisplay)
                }
            }
        }
    }

}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun PagerView() {

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
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .width(screenWidth),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(modifier = Modifier.weight(0.95F)){
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = listState,
            ) {
                itemsIndexed(pagesList) { _, item ->
                    ItemView(item)
                }
            }
        }

        currentIndex = listState.firstVisibleItemIndex

        Box(modifier = Modifier.weight(0.05F)){
            threeDots(currentIndex)
        }
    }
}

@Composable
private fun ItemView(item: Array<Int>) {

    val configuration = LocalConfiguration.current
    val screenWidth = (configuration.screenWidthDp-20).dp

    Column(
        modifier = Modifier
            .width(screenWidth)
            .height(IntrinsicSize.Max)
            .padding(20.dp, 0.dp, 10.dp,0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(0.9f)) {
            ImageCompose(
                item[2] as Int,
                modifier = Modifier
                    .padding(10.dp))
        }

        Box(modifier = Modifier.weight(0.4f)){
            pagerBottomRow(item)
        }

    }
}

@Composable
private fun pagerBottomRow(item: Array<Int>){
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

@Composable
private fun bottomRow(onClickNext: () -> Unit = { }){
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
            { onClickNext() }
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
