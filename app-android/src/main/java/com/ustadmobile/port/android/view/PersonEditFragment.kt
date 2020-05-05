package com.ustadmobile.port.android.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonEditBinding
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PICTURE
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.PresenterFieldRow
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import com.ustadmobile.port.android.view.util.ListSubmitObserver
import java.io.File

interface PersonEditFragmentEventHandler {

}

class PersonEditFragment: UstadEditFragment<Person>(), PersonEditView, PersonEditFragmentEventHandler {

    private var mBinding: FragmentPersonEditBinding? = null

    private var mPresenter: PersonEditPresenter? = null

    private var mPresenterFieldRowRecyclerAdapter: PresenterFieldRowEditRecyclerViewAdapter? = null

    override val mEditPresenter: UstadEditPresenter<*, Person>?
        get() = mPresenter

    private var mPresenterFieldRowObserver: ListSubmitObserver<PresenterFieldRow>? = null


    override var presenterFieldRows: DoorMutableLiveData<List<PresenterFieldRow>>? = null
        get() = field
        set(value) {
            val observer = mPresenterFieldRowObserver ?: return
            field?.removeObserver(observer)
            field = value
            field?.observe(this, observer)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mPresenterFieldRowRecyclerAdapter = PresenterFieldRowEditRecyclerViewAdapter().also {
            mPresenterFieldRowObserver = ListSubmitObserver(it)
        }

        mBinding = FragmentPersonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentPersonEditRecyclerview.adapter = mPresenterFieldRowRecyclerAdapter
            it.fragmentPersonEditRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = PersonEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding?.fragmentPersonEditRecyclerview?.adapter = null
        mPresenterFieldRowRecyclerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null

    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.person)
    }

    override var entity: Person? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {
        const val REQUEST_CODE_TAKE_PICTURE = 40

        const val KEY_PICTURE_FILE_DEST = "picUri"
    }

}