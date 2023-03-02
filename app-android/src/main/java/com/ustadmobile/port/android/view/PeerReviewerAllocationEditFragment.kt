package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPeerReviewerAllocationEditBinding
import com.ustadmobile.core.controller.PeerReviewerAllocationEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PeerReviewerAllocationEditView
import com.ustadmobile.core.viewmodel.PeerReviewerAllocationEditUIState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.PeerReviewerAllocationList
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadExposedDropDownMenuField


interface PeerReviewerAllocationEditFragmentEventHandler {

    fun handleAssignRandomAllocationClicked()
}

class PeerReviewerAllocationEditFragment: UstadEditFragment<PeerReviewerAllocationList>(), PeerReviewerAllocationEditView, PeerReviewerAllocationEditFragmentEventHandler {

    private var headerAdapter: PeerAllocationHeaderAdapter? = null
    private var detailMergerRecyclerView: RecyclerView? = null
    private var concatAdapter: ConcatAdapter? = null

    private var mBinding: FragmentPeerReviewerAllocationEditBinding? = null

    private var mPresenter: PeerReviewerAllocationEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PeerReviewerAllocationList>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPeerReviewerAllocationEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_assignment_peer_allocation_edit)

        headerAdapter = PeerAllocationHeaderAdapter(this)

        concatAdapter = ConcatAdapter(headerAdapter)
        detailMergerRecyclerView?.adapter = concatAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = PeerReviewerAllocationEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)
        ustadFragmentTitle = getString(R.string.assign_reviewers)
    }

    override fun handleAssignRandomAllocationClicked() {
        mPresenter?.handleRandomAssign()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: PeerReviewerAllocationList? = null
        get() = field
        set(value) {
            field = value
        }

    override var submitterListWithAllocations: List<AssignmentSubmitterWithAllocations>? = null
        get() = field
        set(value) {
            field = value

            concatAdapter = null
            concatAdapter = ConcatAdapter(headerAdapter)

            value?.forEach {

                val headerAdapter = SimpleHeadingRecyclerAdapter(
                    it.name ?: ""
                ).apply {
                    visible = true
                }

                concatAdapter?.addAdapter(headerAdapter)

                val submitterList: MutableList<AssignmentSubmitterSummary> = value.toMutableList()
                val self = value.find { submitter -> submitter.submitterUid == it.submitterUid }
                if(self != null){
                    submitterList.remove(self)
                }
                val peerAdapter = PeerAllocationSubmitterAdapter(submitterList, requireContext(), di)
                peerAdapter.submitList(it.allocations)

                concatAdapter?.addAdapter(peerAdapter)
                detailMergerRecyclerView?.adapter = concatAdapter

            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PeerReviewerAllocationEditScreen(
    uiState: PeerReviewerAllocationEditUIState,
    onAssignRandomReviewerClick: () -> Unit = {},
    onAllocationClick: (AssignmentSubmitterWithAllocations) -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ){
        item {
            Button(
                onClick = onAssignRandomReviewerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = stringResource(id = R.string.assign_random_reviewers).uppercase())
            }
        }

        items(
            uiState.submitterListWithAllocations,
            key = {it.submitterUid}
        ){ submitter ->
            val submitters = uiState.submitterListWithAllocations.filter { it.name != submitter.name }
            Text(
                text = submitter.name ?: "",
                style = Typography.body1,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            submitter.allocations?.forEachIndexed { index, allocation ->

                ListItem(
                    text = { Text(
                        text = stringResource(id = R.string.reviewer, (index+1).toString()),
                        style = Typography.body2
                    )},
                    trailing = {
                        UstadExposedDropDownMenuField(
                            value = submitters.first().name,
                            label = "",
                            options = submitters,
                            onOptionSelected = {
                                  onAllocationClick(submitter.shallowCopy{
                                      name = it.toString()
                                  })
                            },
                            itemText =  { "$it" },
                            modifier = Modifier
                                .width(120.dp)
                        )
                    }
                )
            }
        }

    }
}

@Composable
@Preview
fun PeerReviewerAllocationEditPreview(){
    PeerReviewerAllocationEditScreen(
        uiState = PeerReviewerAllocationEditUIState(
            submitterListWithAllocations = listOf(
                AssignmentSubmitterWithAllocations().apply {
                    name = "Maryam"
                    submitterUid = 3
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 380
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 400
                        }
                    )
                },
                AssignmentSubmitterWithAllocations().apply {
                    name = "Ahmad"
                    submitterUid = 1
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 38
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 40
                        }
                    )
                },
                AssignmentSubmitterWithAllocations().apply {
                    name = "Intelligent Students"
                    submitterUid = 2
                    allocations = listOf(
                        PeerReviewerAllocation().apply {
                            praUid = 99
                        },
                        PeerReviewerAllocation().apply {
                            praUid = 23
                        }
                    )
                }
            ),
        )
    )
}