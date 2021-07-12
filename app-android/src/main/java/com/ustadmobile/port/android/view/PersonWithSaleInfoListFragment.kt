package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonWithSaleInfoListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class PersonWithSaleInfoListFragment(): UstadListViewFragment<PersonWithSaleInfo, PersonWithSaleInfo>(),
        PersonWithSaleInfoListView, MessageIdSpinner.OnMessageIdOptionSelectedListener,
        View.OnClickListener, BottomSheetOptionSelectedListener {

    private var mPresenter: PersonWithSaleInfoListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithSaleInfo>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = PersonWithSaleInfoListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonWithSaleInfoListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_we  )
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this, createNewText,
                onFilterOptionSelected = mPresenter)
        return view
    }

    override fun showAddLE(show: Boolean) {

        val optionList = if(show){
             listOf(BottomSheetOption(R.drawable.ic_add_black_24dp,
                requireContext().getString(R.string.new_le), NEW_LE),
                BottomSheetOption(R.drawable.ic_add_black_24dp, requireContext().getString(R.string.new_we), NEW_PRODUCER),
                BottomSheetOption(R.drawable.ic_add_black_24dp, requireContext().getString(R.string.new_customer), NEW_CUSTOMER))
        }else{
            listOf(BottomSheetOption(R.drawable.ic_add_black_24dp, requireContext().getString(R.string.new_we), NEW_PRODUCER),
                    BottomSheetOption(R.drawable.ic_add_black_24dp, requireContext().getString(R.string.new_customer), NEW_CUSTOMER))
        }

        fabManager?.onClickListener = {
            val sheet = OptionsBottomSheetFragment(optionList, this)
            sheet.show(childFragmentManager, sheet.tag)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.person)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.wes)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {

        }else{
            super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.saleDao

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        when(optionSelected.optionCode){
            NEW_LE -> mPresenter?.handleClickAddLE()
            NEW_PRODUCER -> mPresenter?.handleClickAddProducer()
            NEW_CUSTOMER -> mPresenter?.handleClickAddCustomer()
        }
    }

    companion object{
        const val NEW_LE = 2
        const val NEW_PRODUCER = 3
        const val NEW_CUSTOMER = 4
    }

}