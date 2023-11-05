package com.ustadmobile.port.android.view.clazzlog.attendancelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.libuicompose.view.clazzlog.attendancelist.ClazzLogListAttendanceScreenForViewModel
import com.ustadmobile.port.android.view.BottomSheetOption
import com.ustadmobile.port.android.view.OptionsBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClazzLogListAttendanceFragment(): UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzLogListAttendanceViewModel)


    fun ClazzLogListAttendanceViewModel.RecordAttendanceOption.toBottomSheetOption(): BottomSheetOption {
        val systemImpl : UstadMobileSystemImpl = direct.instance()
        return BottomSheetOption(
            RECORD_ATTENDANCE_OPTIONS_ICON[this] ?: 0,
            systemImpl.getString(this.stringResource), this.commandId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(
            viewModel = viewModel,
            transform = { appUiState ->
                appUiState.copy(
                    fabState = appUiState.fabState.copy(
                        onClick = this::onClickFab
                    )
                )
            }
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzLogListAttendanceScreenForViewModel(viewModel)
                }
            }
        }
    }


    private fun onClickFab() {
        lifecycleScope.launch {
            val uiState = viewModel.uiState.first()
            if(uiState.recordAttendanceOptions.size == 1) {
                viewModel.onClickRecordAttendance(
                    uiState.recordAttendanceOptions.first()
                )
            }else {
                OptionsBottomSheetFragment(
                    optionsList = uiState.recordAttendanceOptions.map {
                        it.toBottomSheetOption()
                    },
                    onOptionSelected = {option ->
                        viewModel.onClickRecordAttendance(
                            ClazzLogListAttendanceViewModel.RecordAttendanceOption.forCommand(
                                option.optionCode
                            )
                        )
                    }
                ).show(requireActivity().supportFragmentManager, "attendance_options")
            }
        }
    }



    companion object {

        val RECORD_ATTENDANCE_OPTIONS_ICON = mapOf(
            ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE
                        to R.drawable.ic_calendar_today_24px_,
            ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE
                        to R.drawable.ic_add_black_24dp
        )

    }
}