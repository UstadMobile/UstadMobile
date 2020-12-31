package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkSpaceEditBinding
import com.toughra.ustadmobile.databinding.ItemWorkspaceTermsBinding
import com.ustadmobile.core.controller.WorkSpaceEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkSpaceEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.lib.db.entities.WorkspaceTermsWithLanguage
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToEditEntity

interface WorkSpaceEditFragmentEventHandler {

    fun onClickEditWorkspaceTerms(workspaceTerms: WorkspaceTermsWithLanguage?)

    fun onClickNewWorkspaceTerms()

}

class WorkSpaceEditFragment: UstadEditFragment<WorkSpace>(), WorkSpaceEditView, WorkSpaceEditFragmentEventHandler {

    private var mBinding: FragmentWorkSpaceEditBinding? = null

    private var mPresenter: WorkSpaceEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, WorkSpace>?
        get() = mPresenter


    class WorkspaceTermsRecyclerAdapter(val activityEventHandler: WorkSpaceEditFragmentEventHandler,
            var presenter: WorkSpaceEditPresenter?): ListAdapter<WorkspaceTermsWithLanguage, WorkspaceTermsRecyclerAdapter.WorkspaceTermsViewHolder>(DIFF_CALLBACK_WORKSPACETERMS) {

            class WorkspaceTermsViewHolder(val binding: ItemWorkspaceTermsBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceTermsViewHolder {
                val viewHolder = WorkspaceTermsViewHolder(ItemWorkspaceTermsBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                viewHolder.binding.mEventHandler = activityEventHandler
                return viewHolder
            }

            override fun onBindViewHolder(holder: WorkspaceTermsViewHolder, position: Int) {
                holder.binding.workspaceTerms = getItem(position)
            }
        }

    override var workspaceTermsList: DoorLiveData<List<WorkspaceTermsWithLanguage>>? = null
        get() = field
        set(value) {
            field?.removeObserver(workspaceTermsObserver)
            field = value
            value?.observe(this, workspaceTermsObserver)
        }

    private var workspaceTermsRecyclerAdapter: WorkspaceTermsRecyclerAdapter? = null

    //private var workspaceTermsRecyclerView: RecyclerView? = null

    private val workspaceTermsObserver = Observer<List<WorkspaceTermsWithLanguage>?> {
        t -> workspaceTermsRecyclerAdapter?.submitList(t)
    }

    override fun onClickEditWorkspaceTerms(workspaceTerms: WorkspaceTermsWithLanguage?) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(workspaceTerms, R.id.workspace_terms_edit_dest, WorkspaceTermsWithLanguage::class.java)
    }

    override fun onClickNewWorkspaceTerms() = onClickEditWorkspaceTerms(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkSpaceEditBinding.inflate(inflater, container, false).also {
            rootView = it.root

            workspaceTermsRecyclerAdapter = WorkspaceTermsRecyclerAdapter(this, null)
            it.workspaceTermsRv.adapter = workspaceTermsRecyclerAdapter
            it.workspaceTermsRv.layoutManager = LinearLayoutManager(requireContext())
            it.activityEventHandler = this
        }

        mPresenter = WorkSpaceEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)

        workspaceTermsRecyclerAdapter?.presenter = mPresenter

        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.observeResult(this,
                WorkspaceTermsWithLanguage::class.java) {
            val workspaceTerms = it.firstOrNull() ?: return@observeResult

            mPresenter?.handleAddOrEditWorkspaceTerms(workspaceTerms)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
    }

    override var entity: WorkSpace? = null
        get() = field
        set(value) {
            field = value
            mBinding?.workSpace = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {

        val DIFF_CALLBACK_WORKSPACETERMS = object: DiffUtil.ItemCallback<WorkspaceTermsWithLanguage>() {
            override fun areItemsTheSame(oldItem: WorkspaceTermsWithLanguage, newItem: WorkspaceTermsWithLanguage): Boolean {
                return oldItem.wtUid == newItem.wtUid
            }

            override fun areContentsTheSame(oldItem: WorkspaceTermsWithLanguage, newItem: WorkspaceTermsWithLanguage): Boolean {
                return oldItem.wtLang == newItem.wtLang
                        && oldItem.termsHtml == newItem.termsHtml
            }
        }

    }

}