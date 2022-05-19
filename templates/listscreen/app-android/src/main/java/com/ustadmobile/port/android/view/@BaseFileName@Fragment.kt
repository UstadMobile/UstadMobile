package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ustadmobile.core.controller.@BaseFileName@Presenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.@BaseFileName@View
import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class @BaseFileName@Fragment(): UstadListViewFragment<@Entity@, @DisplayEntity@>(),
    @BaseFileName@View, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener
{

    private var mPresenter: @BaseFileName@Presenter? = null

    override val listPresenter: UstadListPresenter<*, in @DisplayEntity@>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = @BaseFileName@Presenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = @BaseFileName@RecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_a_new_@Entity_LowerCase@))
        return view
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.@Entity_LowerCase@_edit_dest, @Entity@::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = TODO("Provide repo e.g. dbRepo.@Entity@Dao")

}