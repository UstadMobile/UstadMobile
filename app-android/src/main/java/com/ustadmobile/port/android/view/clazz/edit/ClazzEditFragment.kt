package com.ustadmobile.port.android.view.clazz.edit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.view.clazz.edit.ClazzEditScreenForViewModel
import com.ustadmobile.port.android.view.contententry.list.ContentEntryList2Fragment
import com.ustadmobile.port.android.view.TitleDescBottomSheetOption
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.core.R as CR

class ClazzEditFragment : UstadBaseMvvmFragment() {

    private var bottomSheetOptionList: List<TitleDescBottomSheetOption> = listOf()

    private val viewModel: ClazzEditViewModel by ustadViewModels(::ClazzEditViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzEditScreenForViewModel(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetOptionList = listOf(
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.module),
                        requireContext().getString(CR.string.course_module),
                        CourseBlock.BLOCK_MODULE_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.text),
                        requireContext().getString(CR.string.formatted_text_to_show_to_course_participants),
                        CourseBlock.BLOCK_TEXT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.content),
                        requireContext().getString(CR.string.add_course_block_content_desc),
                        CourseBlock.BLOCK_CONTENT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.clazz_assignment),
                        requireContext().getString(CR.string.add_assignment_block_content_desc),
                        CourseBlock.BLOCK_ASSIGNMENT_TYPE),
                TitleDescBottomSheetOption(
                        requireContext().getString(CR.string.discussion_board),
                        requireContext().getString(CR.string.add_discussion_board_desc),
                        CourseBlock.BLOCK_DISCUSSION_TYPE),
        )

    }


    companion object {

        @JvmField
        val BLOCK_ICON_MAP = mapOf(
            CourseBlock.BLOCK_MODULE_TYPE to R.drawable.ic_baseline_folder_open_24,
            CourseBlock.BLOCK_ASSIGNMENT_TYPE to R.drawable.baseline_assignment_turned_in_24,
            CourseBlock.BLOCK_CONTENT_TYPE to R.drawable.video_youtube,
            CourseBlock.BLOCK_TEXT_TYPE to R.drawable.ic_baseline_title_24,
            CourseBlock.BLOCK_DISCUSSION_TYPE to R.drawable.ic_baseline_forum_24
        )

        @JvmField
        val BLOCK_AND_ENTRY_ICON_MAP = BLOCK_ICON_MAP + ContentEntryList2Fragment.CONTENT_ENTRY_TYPE_ICON_MAP

        val ADD_COURSE_BLOCK_OPTIONS: (Context) ->  List<TitleDescBottomSheetOption> = { context ->
            listOf(
                TitleDescBottomSheetOption(
                    context.getString(CR.string.module),
                    context.getString(CR.string.course_module),
                    CourseBlock.BLOCK_MODULE_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.text),
                    context.getString(CR.string.formatted_text_to_show_to_course_participants),
                    CourseBlock.BLOCK_TEXT_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.content),
                    context.getString(CR.string.add_course_block_content_desc),
                    CourseBlock.BLOCK_CONTENT_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.assignments),
                    context.getString(CR.string.add_assignment_block_content_desc),
                    CourseBlock.BLOCK_ASSIGNMENT_TYPE),
                TitleDescBottomSheetOption(
                    context.getString(CR.string.discussion_board),
                    context.getString(CR.string.add_discussion_board_desc),
                    CourseBlock.BLOCK_DISCUSSION_TYPE),
            )
        }

    }


}