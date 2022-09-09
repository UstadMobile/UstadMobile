package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemLearnerGroupMemberListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.LearnerGroupMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class LearnerGroupMemberListFragment : UstadListViewFragment<LearnerGroupMember, LearnerGroupMemberWithPerson>(), LearnerGroupMemberListView {

    private var mPresenter: LearnerGroupMemberListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in LearnerGroupMemberWithPerson>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.learnerGroupMemberDao

    class LearnerGroupMemberListViewHolder(val itemBinding: ItemLearnerGroupMemberListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class LearnerGroupMemberListRecyclerAdapter :
            SelectablePagedListAdapter<LearnerGroupMemberWithPerson, LearnerGroupMemberListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnerGroupMemberListViewHolder {
            val mBinding = ItemLearnerGroupMemberListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
            }
            return LearnerGroupMemberListViewHolder(mBinding)
        }

        override fun onBindViewHolder(holder: LearnerGroupMemberListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.learnerGroupMember = item
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        mPresenter = LearnerGroupMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter()
        mDataRecyclerViewAdapter = LearnerGroupMemberListRecyclerAdapter()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ustadFragmentTitle = getString(R.string.select_group_members)
        fabManager?.text = requireContext().getText(R.string.member)

        fabManager?.onClickListener = {

            val list = mDataRecyclerViewAdapter?.currentList?.map { it.person?.personUid }
            navigateToPickEntityFromList(Person::class.java,
                    R.id.person_list_dest,
                    bundleOf(
                            PersonListView.ARG_EXCLUDE_PERSONUIDS_LIST to list?.joinToString()))

        }

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java) {
            val student = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleNewMemberToGroup(student)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_group_selection, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_selection_done -> {
                mPresenter?.handleClickGroupSelectionDone()
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<LearnerGroupMemberWithPerson> = object
            : DiffUtil.ItemCallback<LearnerGroupMemberWithPerson>() {
            override fun areItemsTheSame(oldItem: LearnerGroupMemberWithPerson,
                                         newItem: LearnerGroupMemberWithPerson): Boolean {
                return oldItem.learnerGroupMemberUid == newItem.learnerGroupMemberUid
            }

            override fun areContentsTheSame(oldItem: LearnerGroupMemberWithPerson,
                                            newItem: LearnerGroupMemberWithPerson): Boolean {
                return oldItem == newItem
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


}