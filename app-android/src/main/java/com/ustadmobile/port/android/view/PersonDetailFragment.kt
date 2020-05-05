package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.toughra.ustadmobile.databinding.FragmentPersonDetailBinding
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.PresenterFieldRow
import com.ustadmobile.port.android.view.util.ListSubmitObserver

interface PersonDetailFragmentEventHandler {

}

class PersonDetailFragment: UstadDetailFragment<PersonWithDisplayDetails>(), PersonDetailView, PersonDetailFragmentEventHandler {

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var presenterFieldRows: DoorMutableLiveData<List<PresenterFieldRow>>? = null
        get() = field
        set(value) {
            val observer = mPresenterFieldRowObserver ?: return
            field?.removeObserver(observer)
            field = value
            field?.observe(this, observer)
        }

    private var mPresenterFieldRowRecyclerAdapter: PresenterFieldRowViewRecyclerViewAdapter? = null

    private var mPresenterFieldRowObserver: ListSubmitObserver<PresenterFieldRow>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mPresenterFieldRowRecyclerAdapter = PresenterFieldRowViewRecyclerViewAdapter().also {
            mPresenterFieldRowObserver = ListSubmitObserver(it)
        }

        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentPersonDetailRecyclerview.adapter = mPresenterFieldRowRecyclerAdapter
            it.fragmentPersonDetailRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentPersonDetailRecyclerview?.adapter = null
        mPresenterFieldRowObserver = null
        mPresenterFieldRowRecyclerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        //TODO: Set title here


    }

    override var entity: PersonWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
        }



}