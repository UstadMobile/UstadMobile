package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemSchoolListItemBinding
import com.ustadmobile.core.controller.SchoolListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class SchoolListFragment : UstadListViewFragment<School, SchoolWithMemberCountAndLocation>(),
        SchoolListView, View.OnClickListener,
        BottomSheetOptionSelectedListener{

    private var mPresenter: SchoolListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in SchoolWithMemberCountAndLocation>?
        get() = mPresenter

    override var newSchoolListOptionVisible: Boolean = false

    class SchoolListViewHolder(val itemBinding: ItemSchoolListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class SchoolListRecyclerAdapter(var presenter: SchoolListPresenter?)
        : SelectablePagedListAdapter<SchoolWithMemberCountAndLocation,
            SchoolListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolListViewHolder {
            val itemBinding = ItemSchoolListItemBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            itemBinding.presenter = presenter
            return SchoolListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: SchoolListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.school = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SchoolListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = SchoolListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_a_new_school),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.school)
        //override this to show our own bottom sheet
        fabManager?.onClickListener = {
            val optionList = if(newSchoolListOptionVisible) {
                listOf(BottomSheetOption(R.drawable.ic_add_black_24dp,
                        requireContext().getString(R.string.add_a_new_school), NEW_SCHOOL))
            }else {
                listOf()
            } + listOf(BottomSheetOption(R.drawable.ic_login_24px,
                    requireContext().getString(R.string.join_existing_school), JOIN_SCHOOL))

            val sheet = OptionsBottomSheetFragment(optionList, this)
            sheet.show(childFragmentManager, sheet.tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.school)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(v: View?) {
        if(v?.id == R.id.item_createnew_layout) {
           mPresenter?.handleClickAddNewItem()
        }else{
            super.onClick(v)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        mDataRecyclerViewAdapter = null
        mDataBinding = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SchoolWithMemberCountAndLocation> = object
            : DiffUtil.ItemCallback<SchoolWithMemberCountAndLocation>() {
            override fun areItemsTheSame(oldItem: SchoolWithMemberCountAndLocation,
                                         newItem: SchoolWithMemberCountAndLocation): Boolean {

                return oldItem.schoolUid == newItem.schoolUid
            }

            override fun areContentsTheSame(oldItem: SchoolWithMemberCountAndLocation,
                                            newItem: SchoolWithMemberCountAndLocation): Boolean {
                return oldItem == newItem
            }
        }


        const val NEW_SCHOOL = 2

        const val JOIN_SCHOOL = 3
    }

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        when(optionSelected.optionCode) {
            NEW_SCHOOL -> mPresenter?.handleClickCreateNewFab()
            JOIN_SCHOOL -> mPresenter?.handleClickJoinSchool()
        }
    }
}