package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.navigation.fragment.findNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material.MdcTheme
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.core.viewmodel.SettingsUiState
import com.ustadmobile.core.viewmodel.SettingsViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.port.android.view.clazz.detailoverview.ICON_SIZE
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.paddingCourseBlockIndent
import org.kodein.di.LazyDI
import org.kodein.di.instance

class SettingsFragment : UstadBaseMvvmFragment(){

    private val viewModel: SettingsViewModel by ustadViewModels(::SettingsViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    SettingsScreenForViewModel(viewModel)
                }
            }
        }
    }

}
@Composable
private fun SettingsScreenForViewModel(
    viewModel: SettingsViewModel
) {
    val uiState: SettingsUiState by viewModel.uiState.collectAsState(initial = SettingsUiState())
    SettingsScreen(
        uiState = uiState,
        onClickAppLanguage = viewModel::onClickAppLanguage,
        onClickGoToHolidayCalendarList = viewModel::onClickGoToHolidayCalendarList,
        onClickWorkspace = viewModel::onClickWorkspace,
        onClickLeavingReason = viewModel::onClickLeavingReason,
        onClickLangList = viewModel::onClickLangList
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SettingsScreen(
    uiState: SettingsUiState = SettingsUiState(),
    onClickAppLanguage: () -> Unit = {},
    onClickGoToHolidayCalendarList: () -> Unit = {},
    onClickWorkspace: () -> Unit = {},
    onClickLeavingReason: () -> Unit = {},
    onClickLangList: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )  {

        ListItem(
            modifier = Modifier
                .clickable(onClick = onClickAppLanguage),
            text = { Text(stringResource(id = R.string.app_language)) },
            secondaryText = { Text("English") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_language_blue_grey_600_24dp),
                    contentDescription = "")
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.holidayCalendarVisible){
            ListItem(
                modifier = Modifier
                    .clickable(onClick = onClickGoToHolidayCalendarList),
                text = { Text(text = stringResource(id = R.string.holiday_calendar)) },
                secondaryText = { Text(text = stringResource(id = R.string.holiday_calendars_desc)) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_empty),
                        contentDescription = "")
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.workspaceSettingsVisible){
            ListItem(
                modifier = Modifier
                    .clickable(onClick = onClickWorkspace),
                text = { Text(text = stringResource(id = R.string.site)) },
                secondaryText = { Text(text = stringResource(id = R.string.manage_site_settings)) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.workspace_join_24px),
                        contentDescription = "")
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.reasonLeavingVisible){
            ListItem(
                modifier = Modifier
                    .clickable(onClick = onClickLeavingReason),
                text = { Text(text = stringResource(id = R.string.leaving_reason)) },
                secondaryText = { Text(text = stringResource(id = R.string.leaving_reason_manage)) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_logout_24),
                        contentDescription = "")
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.langListVisible){
            ListItem(
                modifier = Modifier
                    .clickable(onClick = onClickLangList),
                text = { Text(text = stringResource(id = R.string.languages)) },
                secondaryText = { Text(text = stringResource(id = R.string.languages_description)) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_language_blue_grey_600_24dp),
                        contentDescription = "")
                }
            )
        }

    }
}


@Composable
@Preview
fun SettingsPreview() {
    MdcTheme{
        SettingsScreen()
    }
}