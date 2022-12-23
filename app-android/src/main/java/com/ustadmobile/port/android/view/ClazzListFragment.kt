package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.core.os.bundleOf
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ClazzListFragment(): UstadListViewFragment<Clazz, ClazzWithListDisplayDetails>(),
        ClazzList2View, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener,
        BottomSheetOptionSelectedListener{

    private var mPresenter: ClazzListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWithListDisplayDetails>?
        get() = mPresenter

    override var newClazzListOptionVisible: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        mPresenter = ClazzListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_a_new_class),  onClickSort = this,
            sortOrderOption = mPresenter?.sortOptions?.get(0),
            filterOptions = ClazzListPresenter.FILTER_OPTIONS.toListFilterOptions(requireContext(), di),
            onFilterOptionSelected = mPresenter)
        mDataRecyclerViewAdapter = ClazzListRecyclerAdapter(mPresenter, di)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.course)

        //override this to show our own bottom sheet
        fabManager?.onClickListener = {
            val optionList = if(newClazzListOptionVisible) {
                listOf(BottomSheetOption(R.drawable.ic_add_black_24dp,
                        requireContext().getString(R.string.add_a_new_course), NEW_CLAZZ))
            }else {
                listOf()
            } + listOf(BottomSheetOption(R.drawable.ic_login_24px,
                requireContext().getString(R.string.join_existing_course), JOIN_CLAZZ))

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

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        when(optionSelected.optionCode) {
            NEW_CLAZZ -> mPresenter?.handleClickCreateNewFab()
            JOIN_CLAZZ -> mPresenter?.handleClickJoinClazz()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onClick(v: View?) {
        if(v?.id == R.id.item_createnew_layout) {
            var args = bundleOf()
            val filterExcludeMembersOfSchool =
                    arguments?.get(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL)?.toString()?.toLong()?:0L
            if(filterExcludeMembersOfSchool != 0L){
                args = bundleOf(UstadView.ARG_SCHOOL_UID to filterExcludeMembersOfSchool.toString())
            }
            args.putAll(arguments)
            mPresenter?.handleClickAddNewItem(args.toStringMap())
        } else {
            super.onClick(v)
        }
    }

    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: IdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao


    companion object {

        const val NEW_CLAZZ = 2

        const val JOIN_CLAZZ = 3


        @JvmStatic
        val FOREIGNKEYADAPTER_COURSE = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.coursePictureDao.findByClazzUidAsync(foreignKey)?.coursePictureUri
            }
        }

    }


}