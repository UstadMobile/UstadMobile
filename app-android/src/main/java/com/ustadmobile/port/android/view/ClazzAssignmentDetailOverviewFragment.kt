package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentDetailOverviewBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady


interface ClazzAssignmentDetailOverviewFragmentEventHandler {

}

class ClazzAssignmentDetailOverviewFragment: UstadDetailFragment<ClazzAssignment>(), ClazzAssignmentDetailOverviewView, ClazzAssignmentDetailFragmentEventHandler {



    private var mBinding: FragmentClazzAssignmentDetailOverviewBinding? = null

    private var mPresenter: ClazzAssignmentDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null

    private var contentLiveData: LiveData<PagedList<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
    private val contentObserver = Observer<PagedList<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?> {
        t ->
        run {
            //contentHeadingRecyclerAdapter?.visible = t?.size ?: 0 > 0
            //contentRecyclerAdapter?.submitList(t)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzAssignmentDetailOverviewBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
                rootView.findViewById(R.id.fragment_clazz_assignment_detail_overview)

        /**
         *  TODO adapters
         *  1. clazz assignment detail - description and datetime
         *  2. list of contentEntry
         *  3. score
         *  4. class comments
         *  5. private comments
         */

        detailMergerRecyclerAdapter = ConcatAdapter()
        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = ClazzAssignmentDetailOverviewPresenter(requireContext(),
                arguments.toStringMap(), this, viewLifecycleOwner, di)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var clazzAssignmentContent: DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>? = null
        set(value) {
            contentLiveData?.removeObserver(contentObserver)
            contentLiveData = value?.asRepositoryLiveData(ClazzWorkDao)
            field = value
            contentLiveData?.observeIfFragmentViewIsReady(this, contentObserver)
        }


    override var timeZone: String? = null
        get() = field
        set(value) {
            field = value
        }

    override var clazzAssignmentClazzComments: DataSource.Factory<Int, CommentsWithPerson>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var clazzAssignmentPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?
        get() = TODO("Not yet implemented")
        set(value) {}


    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            field = value
        }

}