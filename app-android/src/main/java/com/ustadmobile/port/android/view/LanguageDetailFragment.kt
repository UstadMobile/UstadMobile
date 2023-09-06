package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.LanguageDetailUiState
import com.ustadmobile.core.viewmodel.LanguageDetailViewModel
import com.ustadmobile.core.viewmodel.LanguageListViewModel
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.core.R as CR

class LanguageDetailFragment: UstadBaseMvvmFragment() {

    private val viewModel: LanguageDetailViewModel by ustadViewModels(::LanguageDetailViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    LanguageDetailScreenForViewModel(viewModel)
                }
            }
        }
    }
}

@Composable
fun LanguageDetailScreenForViewModel(
    viewModel: LanguageDetailViewModel
){
    val uiState: LanguageDetailUiState by viewModel.uiState.collectAsState(initial = LanguageDetailUiState())

    LanguageDetailScreen(
        uiState = uiState
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanguageDetailScreen(
    uiState: LanguageDetailUiState = LanguageDetailUiState()
){
    Column (
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ){

        ListItem(
            modifier = Modifier,
//                .clickable(onClick = onClickAppLanguage),
            text = { Text(stringResource(id = CR.string.name)) },
            secondaryText = { uiState.language?.name ?: "" },
//            icon = {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_language_blue_grey_600_24dp),
//                    contentDescription = "")
//            }
        )

        ListItem(
            text = {Text(stringResource(id = CR.string.two_letter_code))},
            secondaryText = { uiState.language?.iso_639_1_standard ?: "" }
        )

        ListItem(
            text = {Text(stringResource(id = CR.string.three_letter_code))},
            secondaryText = { uiState.language?.iso_639_2_standard ?: "" }
        )
    }
}

@Composable
@Preview
fun LanguageDetailScreenPreview(){
    MdcTheme {
        LanguageDetailScreen()
    }
}