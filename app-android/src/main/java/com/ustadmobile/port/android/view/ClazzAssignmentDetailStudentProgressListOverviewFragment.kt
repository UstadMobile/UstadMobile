package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemAssignmentDetailAttemptBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressOverviewListPresenter
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.controller.SubmissionSummaryListener
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailStudentProgressListOverviewUiState
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzAssignmentDetailStudentProgressListOverviewFragment(): UstadListViewFragment<AssignmentSubmitterSummary, AssignmentSubmitterSummary>(),
        ClazzAssignmentDetailStudentProgressOverviewListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var progressSummaryAdapter: AssignmentProgressSummaryRecyclerAdapter? = null
    private var mPresenter: ClazzAssignmentDetailStudentProgressOverviewListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in AssignmentSubmitterSummary>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false


    class PersonWithAssignmentStatementDisplayListRecyclerAdapter(var listener: SubmissionSummaryListener?):
            SelectablePagedListAdapter<AssignmentSubmitterSummary,
                    PersonWithAssignmentStatementDisplayListRecyclerAdapter
                    .PersonWithStatementDisplayListViewHolder>(DIFF_CALLBACK) {

        class PersonWithStatementDisplayListViewHolder(val itemBinding: ItemAssignmentDetailAttemptBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithStatementDisplayListViewHolder {
            val itemBinding = ItemAssignmentDetailAttemptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.listener = listener
            itemBinding.selectablePagedListAdapter = this
            return PersonWithStatementDisplayListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonWithStatementDisplayListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.tag = item?.submitterUid
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            listener = null
        }

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        progressSummaryAdapter = AssignmentProgressSummaryRecyclerAdapter(null)

        mPresenter = ClazzAssignmentDetailStudentProgressOverviewListPresenter(requireContext(),
                arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonWithAssignmentStatementDisplayListRecyclerAdapter(mPresenter)

        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,progressSummaryAdapter, mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        super.onClick(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzAssignmentDao

    private val progressSummaryObserver = Observer<AssignmentProgressSummary?> {
        t ->  progressSummaryAdapter?.assignmentProgressSummaryVal = t
    }

    override var progressSummary: LiveData<AssignmentProgressSummary?>? = null
        get() = field
        set(value) {
            field?.removeObserver(progressSummaryObserver)
            field = value
            value?.observe(viewLifecycleOwner, progressSummaryObserver)
        }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<AssignmentSubmitterSummary> = object
            : DiffUtil.ItemCallback<AssignmentSubmitterSummary>() {
            override fun areItemsTheSame(oldItem: AssignmentSubmitterSummary,
                                         newItem: AssignmentSubmitterSummary): Boolean {
                return oldItem.submitterUid == newItem.submitterUid
            }

            override fun areContentsTheSame(oldItem: AssignmentSubmitterSummary,
                                            newItem: AssignmentSubmitterSummary): Boolean {
                return oldItem.name == newItem.name
                        && oldItem.latestPrivateComment == newItem.latestPrivateComment
                        && oldItem.fileSubmissionStatus == newItem.fileSubmissionStatus
            }
        }
    }

}

@Composable
private fun ClazzAssignmentDetailStudentProgressListOverviewScreen(
    uiState: ClazzAssignmentDetailStudentProgressListOverviewUiState =
        ClazzAssignmentDetailStudentProgressListOverviewUiState(),
    onClickAssignment: (AssignmentSubmitterSummary) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberRow(uiState.progressSummary?.calculateNotSubmittedStudents() ?: 0,
                    stringResource(R.string.not_started)
                )

                Divider(
                    color = contentColorFor(
                        colorResource(id = R.color.grey)
                    ),
                    modifier = Modifier
                        .height(45.dp)
                        .width(1.dp)
                )

                NumberRow(uiState.progressSummary?.submittedStudents ?: 0,
                stringResource(R.string.submitted_cap)
                )

                Divider(
                    color = contentColorFor(
                        colorResource(id = R.color.grey)
                    ),
                    modifier = Modifier
                        .height(45.dp)
                        .width(1.dp)
                )

                NumberRow(uiState.progressSummary?.markedStudents ?: 0,
                stringResource(R.string.marked_cap)
                )

                Divider(
                    color = contentColorFor(
                        colorResource(id = R.color.grey)
                    ),
                    modifier = Modifier
                        .height(45.dp)
                        .width(1.dp)
                )
            }
        }

        items(
            items = uiState.assignmentSubmitterList,
            key = { person -> person.submitterUid }
        ){ person ->
            AssignmentDetailAttemptListItem(person, onClickAssignment)
        }

    }
}

@Composable
private fun NumberRow(
    number: Int = 0,
    text: String = ""
){
    Column(
        modifier = Modifier.padding(20.dp)
    ){
        Text(number.toString())
        Text(text)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AssignmentDetailAttemptListItem (
    person: AssignmentSubmitterSummary,
    onClick: (AssignmentSubmitterSummary) -> Unit = {},
){

    val personUiState = person.listItemUiState

    ListItem(
        modifier = Modifier.clickable {
            onClick(person)
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_person_black_24dp),
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text(person.name ?: "") },
        secondaryText = {
            if (personUiState.latestPrivateCommentVisible){
                Row{
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_comment_24),
                        contentDescription = "",
                        modifier = Modifier.size(12.dp)
                    )
                    Text(person.latestPrivateComment ?: "")
                }
            }
        },
        trailing = {
            Row{
                if (personUiState.fileSubmissionStatusIconVisible){
                    Icon(
                        painter = painterResource(
                            ClazzAssignmentDetailOverviewFragment.ASSIGNMENT_STATUS_MAP[
                                    person.fileSubmissionStatus] ?: R.drawable.ic_done_white_24dp
                        ),
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (personUiState.fileSubmissionStatusTextVisible){
                    Text(messageIdResource(
                        SubmissionConstants.STATUS_MAP[person.fileSubmissionStatus]
                            ?: MessageID.not_submitted_cap
                    ))
                }
            }

        }
    )
}

@Composable
@Preview
fun ClazzAssignmentDetailStudentProgressListOverviewScreenPreview() {
    val uiStateVal = ClazzAssignmentDetailStudentProgressListOverviewUiState(
        progressSummary = AssignmentProgressSummary().apply {
            totalStudents = 10
            submittedStudents = 2
            markedStudents = 3
        },
        assignmentSubmitterList = listOf(
            AssignmentSubmitterSummary().apply {
                submitterUid = 1
                name = "Bob Dylan"
                latestPrivateComment = "Here is private comment"
            }
        ),
    )

    MdcTheme {
        ClazzAssignmentDetailStudentProgressListOverviewScreen(uiStateVal)
    }
}