package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.core.viewmodel.SettingsUiState
import com.ustadmobile.port.android.view.composable.UstadDetailField
import org.kodein.di.instance

interface SettingsFragmentEventListener {

    fun onClickAppLanguage()

    fun onClickPanicButton()

}

class SettingsFragment : UstadBaseFragment(), SettingsView, SettingsFragmentEventListener{

    var mPresenter: SettingsPresenter? = null

    private var mBinding: FragmentSettingsBinding? = null

    override var displayLanguage: String?
        get() = mBinding?.displayLanguage
        set(value) {
            mBinding?.displayLanguage = value
        }

    override var workspaceSettingsVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.workspaceSettingsVisible = value
        }
    override var holidayCalendarVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.holidayCalendarVisible = value
        }

    override var reasonLeavingVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.reasonLeavingVisible = value
        }

    override var langListVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.langListVisible = value
        }

    val uiState = SettingsUiState()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        mBinding = FragmentSettingsBinding.inflate(inflater, container, false).also {

        }

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        mBinding?.presenter = mPresenter
        mBinding?.fragmentEventListener = this

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    SettingsScreen(uiState)
                }
            }
        }
    }

    override fun onClickAppLanguage() {
        val systemImpl: UstadMobileSystemImpl by instance()
        val langList = systemImpl.getAllUiLanguagesList()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_language)
            .setItems(langList.map { it.langDisplay }.toTypedArray()) { _, which ->
                val lang = langList[which].langCode
                systemImpl.setLocale(lang)
                activity?.recreate()
            }
            .show()
    }

    override fun onClickPanicButton() {
        findNavController().navigate(R.id.panic_button_settings_dest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.presenter = null
        mBinding?.fragmentEventListener = null
        mBinding = null
        mPresenter = null
    }

}

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

        UstadDetailField(
            imageId = R.drawable.ic_language_blue_grey_600_24dp,
            valueText = stringResource(R.string.app_language),
            labelText = "English",
            onClick = onClickAppLanguage
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.holidayCalendarVisible){
            UstadDetailField(
                valueText = stringResource(R.string.holiday_calendars),
                labelText = stringResource(id = R.string.holiday_calendars_desc),
                imageId = 0,
                onClick = onClickGoToHolidayCalendarList,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.workspaceSettingsVisible){
            UstadDetailField(
                imageId = R.drawable.workspace_join_24px,
                valueText = stringResource(R.string.site),
                labelText = stringResource(id = R.string.manage_site_settings),
                onClick = onClickWorkspace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.reasonLeavingVisible){
            UstadDetailField(
                imageId = R.drawable.ic_baseline_logout_24,
                valueText = stringResource(R.string.leaving_reason),
                labelText = stringResource(id = R.string.leaving_reason_manage),
                onClick = onClickLeavingReason
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.langListVisible){
            UstadDetailField(
                imageId = R.drawable.ic_language_blue_grey_600_24dp,
                valueText = stringResource(R.string.languages),
                labelText = stringResource(id = R.string.languages_description),
                onClick = onClickLangList
            )
        }

    }
}


@Composable
@Preview
fun SettingsPreview() {
    val uiState = SettingsUiState(
        reasonLeavingVisible = true,
        holidayCalendarVisible = true,
        workspaceSettingsVisible = true,
        langListVisible = true
    )
    MdcTheme{
        SettingsScreen(uiState)
    }
}