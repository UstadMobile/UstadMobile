package com.ustadmobile.port.android.view.clazz.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.libuicompose.view.clazz.list.ClazzListScreenForViewModel
import com.ustadmobile.port.android.view.BottomSheetOption
import com.ustadmobile.port.android.view.OptionsBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.ustadmobile.core.R as CR


class ClazzListFragment(): UstadBaseMvvmFragment() {

    private val viewModel: ClazzListViewModel by ustadViewModels { di, savedStateHandle ->
        ClazzListViewModel(di, savedStateHandle, "FOO")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(
            viewModel = viewModel,
            transform = {
                it.copy(
                    fabState = it.fabState.copy(
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
                    ClazzListScreenForViewModel(viewModel)
                }
            }
        }
    }

    fun onClickFab() {
        viewLifecycleOwner.lifecycleScope.launch {
            val uiState = viewModel.uiState.first()
            val optionList = if(uiState.canAddNewCourse) {
                listOf(
                    BottomSheetOption(R.drawable.ic_add_black_24dp,
                        requireContext().getString(CR.string.add_a_new_course), NEW_CLAZZ
                    )
                )
            }else {
                listOf()
            } + listOf(
                BottomSheetOption(R.drawable.ic_login_24px,
                requireContext().getString(CR.string.join_existing_course), JOIN_CLAZZ
                )
            )

            val sheet = OptionsBottomSheetFragment(optionList) {
                when(it.optionCode) {
                    NEW_CLAZZ -> viewModel.onClickAdd()
                    JOIN_CLAZZ -> viewModel.onClickJoinExistingClazz()
                }
            }

            sheet.show(childFragmentManager, sheet.tag)
        }
    }

    companion object {

        const val NEW_CLAZZ = 2

        const val JOIN_CLAZZ = 3


        @JvmStatic
        val FOREIGNKEYADAPTER_COURSE = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.coursePictureDao.findByClazzUidAsync(foreignKey)?.coursePictureUri
            }
        }

    }


}