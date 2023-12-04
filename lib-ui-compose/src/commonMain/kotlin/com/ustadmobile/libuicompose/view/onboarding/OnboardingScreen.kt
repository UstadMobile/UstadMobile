package com.ustadmobile.libuicompose.view.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.OnboardingUiState
import com.ustadmobile.libuicompose.components.UstadHorizontalPagingIndicator
import com.ustadmobile.libuicompose.components.isDesktop
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnBoardingViewModel,
    showArrows: Boolean = isDesktop(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(OnboardingUiState())

    if(uiState.showWaitForRestart) {
        Dialog(
            onDismissRequest = {
                //Do nothing - this dialog goes away once the restart happens
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(MR.strings.loading)
                )
            }

        }
    }

    OnboardingScreen(
        uiState = uiState,
        onSetLanguage = viewModel::onLanguageSelected,
        onClickNext = viewModel::onClickNext,
        showArrows = showArrows,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    showArrows: Boolean,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
    onClickNext: () -> Unit = { }
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { onboardingItems.size },
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Box {
            TopRow(
                uiState,
                onSetLanguage = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.height(15.dp))


        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(showArrows) {
                IconButton(
                    enabled = pagerState.currentPage >= 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack, contentDescription = stringResource(MR.strings.previous)
                    )
                }
            }

            PagerView(
                modifier = Modifier.weight(1.0f),
                pagerState = pagerState,
            )

            if(showArrows) {
                IconButton(
                    enabled = pagerState.currentPage < (onboardingItems.size - 1),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowForward, contentDescription = stringResource(MR.strings.next)
                    )
                }
            }
        }

        Box {
            BottomRow(onClickNext)
        }
    }
}

@Composable
private fun TopRow(
    uiState: OnboardingUiState,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
){
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Box{
            SetLanguageMenu(
                langList = uiState.languageList,
                currentLanguage = uiState.currentLanguage,
                onItemSelected = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = currentLanguage.langDisplay,
            onValueChange = { },
            label = { stringResource(MR.strings.language) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )

        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false

            }
        ) {
            langList.forEach { uiLanguage ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        expanded = false
                        onItemSelected(uiLanguage)
                    },
                    text = { Text(text = uiLanguage.langDisplay) }
                )
            }
        }
    }
}

val onboardingItems: List<OnboardingItem> = listOf(
    OnboardingItem(
        MR.strings.onboarding_no_internet_headline,
        MR.strings.onboarding_no_internet_subheadline,
        UstadImage.ILLUSTRATION_OFFLINE_USAGE
    ),
    OnboardingItem(
        MR.strings.onboarding_offline_sharing,
        MR.strings.onboarding_offline_sharing_subheading,
        UstadImage.ILLUSTRATION_OFFLINE_SHARING,
    ),
    OnboardingItem(
        MR.strings.onboarding_stay_organized_headline,
        MR.strings.onboarding_stay_organized_subheading,
        UstadImage.ILLUSTRATION_ORGANIZED,
    )
)


data class OnboardingItem(
    val headerId: StringResource,
    val subheaderId: StringResource,
    val imgPath: UstadImage,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerView(
    modifier: Modifier,
    pagerState: PagerState,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(0.76f)) {
            HorizontalPager(
                state = pagerState,
            ) { page ->
                ItemView(onboardingItems[page])
            }
        }

        Box() {
            UstadHorizontalPagingIndicator(
                modifier = Modifier.padding(vertical = 8.dp),
                pageCount = onboardingItems.size,
                activePage = pagerState.currentPage,
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = MaterialTheme.colorScheme.outline,
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
                painter = ustadAppImagePainter(onboardingItem.imgPath),
                contentDescription = null,
                modifier = Modifier.padding(10.dp))
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

        Text(
            text =  stringResource(onboardingItem.headerId),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )


        Text(
            text =  stringResource(onboardingItem.subheaderId),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

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
            Text(stringResource(MR.strings.onboarding_get_started_label))
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}