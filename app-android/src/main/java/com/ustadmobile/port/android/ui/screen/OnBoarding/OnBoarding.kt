package com.ustadmobile.port.android.ui.screen.OnBoarding

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.ustadmobile.port.android.ui.compose.TextBody2
import com.ustadmobile.port.android.ui.theme.ui.theme.UstadMobileTheme
import com.ustadmobile.port.android.ui.theme.ui.theme.gray
import com.ustadmobile.port.android.util.ext.getUstadLocaleSetting
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import java.util.*

class OnBoarding : ComponentActivity() {

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


    OnboardingScreen(uiState.languageList, uiState.currentLanguage,
        onSetLanguage = { viewModel.onLanguageSelected(it) }
    )
}

@Composable
@Preview
fun OnboardingScreen(
    langList: List<UstadMobileSystemCommon.UiLanguage> = listOf(),
    currentLanguage: UstadMobileSystemCommon.UiLanguage
        = UstadMobileSystemCommon.UiLanguage("en", "English"),
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
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
            bottomRow()
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
                SetLanguageMenu(
                    langList = langList,
                    currentLanguage,
                    onItemSelected = onSetLanguage,
                )
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