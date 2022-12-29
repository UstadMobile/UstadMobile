package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.core.viewmodel.CourseTerminologyEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.messageIdResource
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

@Composable
private fun CourseTerminologyEditScreen(
    uiState: CourseTerminologyEditUiState = CourseTerminologyEditUiState(),
    onTerminologyTermChanged: (TerminologyEntry?) -> Unit = {},
    onCtTitleChanged: (String?) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item{
            UstadTextEditField(
                value = uiState.entity?.ctTitle ?: "",
                label = stringResource(id = R.string.name),
                error = uiState.titleError,
                enabled = uiState.fieldsEnabled,
                onValueChange = { onCtTitleChanged(it) },
            )
        }

        item {
            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = stringResource(id = R.string.your_words_for)
            )
        }

        items(
            items = uiState.terminologyTermList,
            key = {terminologyTerm -> terminologyTerm.id}
        ){ terminologyTerm ->

            UstadTextEditField(
                value = terminologyTerm.term ?: "",
                label = messageIdResource(id = terminologyTerm.messageId),
                error = terminologyTerm.errorMessage,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onTerminologyTermChanged(terminologyTerm.copy(
                        term = it
                    ))
                },)
        }
    }
}

@Composable
@Preview
fun CourseTerminologyEditScreenPreview() {
    val uiState = CourseTerminologyEditUiState(
        terminologyTermList = listOf(
            TerminologyEntry(
                id = "1",
                term = "First",
                messageId = MessageID.teacher
            ),
            TerminologyEntry(
                id = "2",
                term = "Second",
                messageId = MessageID.student
            ),
            TerminologyEntry(
                id = "3",
                term = "Third",
                messageId = MessageID.add_a_teacher
            )
        )
    )
    MdcTheme {
        CourseTerminologyEditScreen(uiState)
    }
}