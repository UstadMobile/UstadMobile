package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPeerReviewerAllocationEditBinding
import com.ustadmobile.core.controller.PeerReviewerAllocationEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PeerReviewerAllocationEditView
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocationList


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