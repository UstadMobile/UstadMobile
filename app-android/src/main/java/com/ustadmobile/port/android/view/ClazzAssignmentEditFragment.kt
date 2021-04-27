package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentEditBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady


interface ClazzAssignmentEditFragmentEventHandler {
    fun onClickNewContent()
}

class ClazzAssignmentEditFragment: UstadEditFragment<ClazzAssignment>(), ClazzAssignmentEditView, ClazzAssignmentEditFragmentEventHandler {

    private var mBinding: FragmentClazzAssignmentEditBinding? = null

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzAssignment>?
        get() = mPresenter

    // TODO make recylerview
    private var contentRecyclerAdapter: ContentEntryListAdapterRA? = null
    private var contentRecyclerView: RecyclerView? = null
    private val contentObserver = Observer<List<
            ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?>{
        t -> contentRecyclerAdapter?.submitList(t)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzAssignmentEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
        }

        // TODO add contentRecyclerView

        mPresenter = ClazzAssignmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_assignment, R.string.edit_assignment)
        val navController = findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java){
            val contentEntrySelected = it.firstOrNull()?:return@observeResult
            mPresenter?.handleAddOrEditContent(contentEntrySelected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        contentRecyclerView?.adapter = null
        contentRecyclerAdapter = null
        contentRecyclerView = null
    }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzAssignment = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone =
                    getText(R.string.class_timezone).toString() + " " + value
            field = value
        }

    override var clazzAssignmentContent: DoorMutableLiveData
            <List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
        set(value) {
            field?.removeObserver(contentObserver)
            field = value
            value?.observeIfFragmentViewIsReady(this, contentObserver)
        }

    override fun onClickNewContent() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer::class.java,
                R.id.content_entry_list_dest,
                bundleOf(ContentEntryList2View.ARG_CLAZZWORK_FILTER to
                        entity?.caUid.toString(),
                        ContentEntryList2View.ARG_CONTENT_FILTER to
                                ContentEntryList2View.ARG_LIBRARIES_CONTENT,
                        UstadView.ARG_PARENT_ENTRY_UID to UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString()))

    }

}