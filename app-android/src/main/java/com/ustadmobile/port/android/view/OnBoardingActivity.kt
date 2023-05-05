package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.OnboardingUiState
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.ext.getActivityContext
import com.ustadmobile.port.android.util.ext.getUstadLocaleSetting
import kotlinx.coroutines.delay
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import java.util.*

class OnBoardingActivity : ComponentActivity() {

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
            MdcTheme {
                OnboardingScreenViewModel(viewModel = viewModel)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.reloadCommandFlow.collect {
                delay(100)
                recreate()
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
private fun OnboardingScreenViewModel(viewModel: OnBoardingViewModel) {
    val uiState: OnboardingUiState by viewModel.uiState.collectAsState(initial = OnboardingUiState())
    val context = LocalContext.current.getActivityContext()
    OnboardingScreen(uiState.languageList, uiState.currentLanguage,
        onSetLanguage = { viewModel.onLanguageSelected(it) },
//        onClickNext = {
//            viewModel.onClickNext()
//            val intent = Intent(context, MainActivity::class.java)
//            context.getActivityContext().startActivity(intent)
//        }
    )
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
        Box {
            TopRow(langList, currentLanguage, onSetLanguage = {
                onSetLanguage(it)
            })
        }

        Spacer(modifier = Modifier.height(15.dp))

        Box(modifier = Modifier.weight(1f)){
            PagerView()
        }

        Box {
            BottomRow(onClickNext)
        }
    }

}

@Composable
private fun TopRow(
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
        Box{
            SetLanguageMenu(langList = langList,
                currentLanguage,
                onItemSelected = onSetLanguage,)
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Box {
            Image(
                painter = painterResource(id = R.drawable.expo2020_logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(80.dp))
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
                focusedIndicatorColor = colorResource(R.color.primaryColor),
                unfocusedIndicatorColor = colorResource(R.color.primaryColor),
                disabledIndicatorColor = colorResource(R.color.primaryColor),
            )
        )

        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {

            langList.forEachIndexed { index, uiLanguage ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        expanded = false
                        onItemSelected(uiLanguage)
                    }
                ) {
                    Text(text = uiLanguage.langDisplay)
                }
            }
        }
    }

}

data class OnboardingItem(
    val headerId: Int,
    val subheaderId: Int,
    val illustrationId: Int
)

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun PagerView() {

    val onboardingItems: List<OnboardingItem> = remember {
        listOf(OnboardingItem(R.string.onboarding_no_internet_headline,
            R.string.onboarding_no_internet_subheadline, R.drawable.illustration_offline_usage),
        OnboardingItem(R.string.onboarding_offline_sharing,
            R.string.onboarding_offline_sharing_subheading, R.drawable.illustration_offline_sharing),
        OnboardingItem(R.string.onboarding_stay_organized_headline,
            R.string.onboarding_stay_organized_subheading, R.drawable.illustration_organized)
        )
    }

    val state = rememberPagerState(0)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(0.76f)) {
            HorizontalPager(
                state = state,
                count = onboardingItems.size
            ) { page ->
                ItemView(onboardingItems[page])
            }
        }

        Box() {
            HorizontalPagerIndicator(
                pagerState = state,
                pageCount = 3,
                activeColor = colorResource(R.color.primaryColor),
                inactiveColor = Color.LightGray,
                indicatorWidth = 12.dp,
                indicatorShape = CircleShape,
                spacing = 8.dp,
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun ItemView(onboardingItem: OnboardingItem) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 0.dp, 10.dp, 0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(0.9f)) {
            Image(
                painter = painterResource(id = onboardingItem.illustrationId),
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp))
        }

        Box(modifier = Modifier.weight(0.4f)){
            PagerBottomRow(onboardingItem)
        }

    }
}

@Composable
private fun PagerBottomRow(onboardingItem: OnboardingItem){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text =  stringResource(onboardingItem.headerId),
            style = Typography.h3)

        Text(text =  stringResource(onboardingItem.subheaderId),
            style = Typography.body1)

    }
}

@Composable
private fun BottomRow(onClickNext: () -> Unit = { }){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Button(
            onClick = onClickNext,
        ) {
            Text(stringResource(R.string.onboarding_get_started_label))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = stringResource(R.string.created_partnership),
            style = Typography.body2,
            color = Color.DarkGray)

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_irc),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp))

    }
}

