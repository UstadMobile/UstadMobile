package com.ustadmobile.port.android.view.clazz.detailoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.paging.compose.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.util.rememberFormattedDateRange
import com.ustadmobile.libuicompose.util.rememberFormattedTime
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.clazzassignment.UstadClazzAssignmentListItem
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.port.android.view.contententry.UstadContentEntryListItem
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants.DAY_STRING_RESOURCES
import com.ustadmobile.core.viewmodel.clazz.ClazzScheduleConstants.SCHEDULE_FREQUENCY_STRING_RESOURCES
import com.ustadmobile.libuicompose.view.clazz.detailoverview.ClazzDetailOverviewScreenForViewModel
import com.ustadmobile.port.android.util.compose.stringIdMapResource
import com.ustadmobile.core.R as CR

interface ClazzDetailOverviewEventListener {
    fun onClickClassCode(code: String?)

    fun onClickShare()

    fun onClickDownloadAll()

    fun onClickPermissions()
}

class ClazzDetailOverviewFragment: UstadBaseMvvmFragment() {

    private val viewModel: ClazzDetailOverviewViewModel by ustadViewModels { di, savedStateHandle ->
        ClazzDetailOverviewViewModel(di, savedStateHandle, ClazzDetailViewModel.DEST_NAME)
    }

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
                    ClazzDetailOverviewScreenForViewModel(viewModel)
                }
            }
        }


    }

    companion object {

        val BLOCK_ICON_MAP = mapOf(
            CourseBlock.BLOCK_MODULE_TYPE to R.drawable.ic_baseline_folder_open_24,
            CourseBlock.BLOCK_ASSIGNMENT_TYPE to R.drawable.baseline_assignment_turned_in_24,
            CourseBlock.BLOCK_CONTENT_TYPE to R.drawable.video_youtube,
            CourseBlock.BLOCK_TEXT_TYPE to R.drawable.ic_baseline_title_24,
            CourseBlock.BLOCK_DISCUSSION_TYPE to R.drawable.ic_baseline_forum_24
        )

    }

}

