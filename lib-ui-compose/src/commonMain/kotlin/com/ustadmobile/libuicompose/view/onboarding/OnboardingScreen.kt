package com.ustadmobile.libuicompose.view.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.OnboardingUiState
import com.ustadmobile.libuicompose.components.UstadHorizontalPagingIndicator
import com.ustadmobile.libuicompose.components.UstadSetLanguageDropDown
import com.ustadmobile.libuicompose.components.UstadWaitForRestartDialog
import com.ustadmobile.libuicompose.components.isDesktop
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    viewModel: OnBoardingViewModel,
    showArrows: Boolean = isDesktop(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(OnboardingUiState())

    if (uiState.showWaitForRestart) {
        UstadWaitForRestartDialog()
    }

    OnboardingScreen(
        uiState = uiState,
        onSetLanguage = viewModel::onLanguageSelected,
        onClickNext = viewModel::onClickExistJoining,
        onClickIndividual = viewModel::onClickIndividual,
        onClickAddNewOrganization = viewModel::onClickAddNewOrganization,
        showArrows = showArrows,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    showArrows: Boolean,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
    onClickNext: () -> Unit = { },
    onClickIndividual: () -> Unit = { },
    onClickAddNewOrganization: () -> Unit = { },
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { onboardingItems.size },
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp)
    ) {
        Box {
            TopRow(
                uiState, onSetLanguage = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showArrows) {
                IconButton(enabled = pagerState.currentPage >= 1, onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(MR.strings.previous)
                    )
                }
            }

            PagerView(
                modifier = Modifier.weight(1.0f),
                pagerState = pagerState,
            )

            if (showArrows) {
                IconButton(
                    enabled = pagerState.currentPage < (onboardingItems.size - 1),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = stringResource(MR.strings.next)
                    )
                }
            }
        }

        Box {
            BottomRow(
                onClickNext,
                onClickIndividual,
                onClickAddNewOrganization,
            )
        }
    }
}

@Composable
private fun TopRow(
    uiState: OnboardingUiState,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
) {
    Row(
        modifier = Modifier.wrapContentHeight().fillMaxWidth()
    ) {
        Box {
            UstadSetLanguageDropDown(
                langList = uiState.languageList,
                currentLanguage = uiState.currentLanguage,
                onItemSelected = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}


val onboardingItems: List<OnboardingItem> = listOf(
    OnboardingItem(
        MR.strings.onboarding_headline1,
        MR.strings.onboarding_subheadline1,
        UstadImage.ILLUSTRATION_ONBOARDING1
    ), OnboardingItem(
        MR.strings.onboarding_headline2,
        MR.strings.onboarding_subheadline2,
        UstadImage.ILLUSTRATION_ONBOARDING2,
    ), OnboardingItem(
        MR.strings.onboarding_headline2,
        MR.strings.onboarding_subheadline3,
        UstadImage.ILLUSTRATION_ONBOARDING3,
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
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(0.76f)) {
            HorizontalPager(
                state = pagerState,
            ) { page ->
                ItemView(onboardingItems[page])
            }
        }

        Box {
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
        modifier = Modifier.fillMaxWidth().padding(20.dp, 0.dp, 10.dp, 0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(Modifier.weight(0.9f)) {
            Image(
                painter = ustadAppImagePainter(onboardingItem.imgPath),
                contentDescription = null,
                modifier = Modifier.padding(10.dp)
            )
        }

        Box(modifier = Modifier.weight(0.4f)) {
            PagerBottomRow(onboardingItem)
        }

    }
}

@Composable
private fun PagerBottomRow(onboardingItem: OnboardingItem) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(onboardingItem.headerId),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )


        Text(
            text = stringResource(onboardingItem.subheaderId),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

    }
}

@Composable
private fun BottomRow(
    onClickJoinOrganization: () -> Unit = {},
    onClickIndividual: () -> Unit = {},
    onClickAddOrganization: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClickJoinOrganization, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(MR.strings.i_want_to_join_my_organization_school))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onClickAddOrganization, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(MR.strings.i_want_to_add_my_organization_school))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onClickIndividual, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(MR.strings.im_an_individual_learner))
        }

    }
}


