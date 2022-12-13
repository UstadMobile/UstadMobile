package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseTerminologyOverviewBinding
import com.ustadmobile.core.controller.CourseTerminologyEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.core.viewmodel.CourseTerminologyEditUiState
import com.ustadmobile.core.viewmodel.LoginUiState
import com.ustadmobile.core.viewmodel.PersonDetailUiState
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.lib.db.entities.TerminologyEntry
import com.ustadmobile.port.android.view.composable.UstadTextEditField


class CourseTerminologyEditFragment: UstadEditFragment<CourseTerminology>(), CourseTerminologyEditView {

    private var mBinding: FragmentCourseTerminologyOverviewBinding? = null

    private var mPresenter: CourseTerminologyEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseTerminology>?
        get() = mPresenter

    private var headerAdapter: CourseTerminologyHeaderAdapter? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null
    private var terminologyEntryAdapter: TerminologyEntryAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseTerminologyOverviewBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_terminology_edit_overview)

        headerAdapter = CourseTerminologyHeaderAdapter()

        terminologyEntryAdapter = TerminologyEntryAdapter()

        detailMergerRecyclerAdapter = ConcatAdapter(headerAdapter,terminologyEntryAdapter)
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = CourseTerminologyEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null

        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null
        detailMergerRecyclerAdapter = null
        headerAdapter = null
        terminologyEntryAdapter = null
    }

    override var entity: CourseTerminology? = null
        get() = headerAdapter?.courseTerminology
        set(value) {
            field = value
            headerAdapter?.courseTerminology = value
        }

    override var titleErrorText: String? = null
        set(value) {
            field = value
            headerAdapter?.titleErrorText = value
        }

    override var terminologyTermList: List<TerminologyEntry>? = null
        get() = terminologyEntryAdapter?.currentList ?: listOf()
        set(value) {
            field = value
            terminologyEntryAdapter?.submitList(value)
            terminologyEntryAdapter?.notifyDataSetChanged()
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CourseTerminologyEditScreen(
    uiState: CourseTerminologyEditUiState = CourseTerminologyEditUiState(),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item{
            ListItem(
                text = {
                    UstadTextEditField(
                        value = uiState.entity?.ctTitle ?: "",
                        label = stringResource(id = R.string.name),
                        error = uiState.titleError,
                        enabled = uiState.fieldsEnabled,
                        onValueChange = {
                        },
                    )
                },
                secondaryText = { Text(stringResource(id = R.string.your_words_for)) }
            )
        }
    }
}

@Composable
@Preview
fun CourseTerminologyEditScreenPreview() {
    val uiState = CourseTerminologyEditUiState(
        courseTerminologyList = listOf(
            CourseTerminology().apply {
                ctTitle = "Course Terminology 1"
            },
            CourseTerminology().apply {
                ctTitle = "Course Terminology 2"
            },
            CourseTerminology().apply {
                ctTitle = "Course Terminology 3"
            }
        )
    )
    MdcTheme {
        CourseTerminologyEditScreen(uiState)
    }
}