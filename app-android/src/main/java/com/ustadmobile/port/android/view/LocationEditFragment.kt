package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLocationEditBinding
import com.ustadmobile.core.controller.LocationEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.LocationEditView
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.port.android.util.ext.*


interface LocationEditFragmentEventHandler {

}

class LocationEditFragment: UstadEditFragment<Location>(), LocationEditView, LocationEditFragmentEventHandler {

    private var mBinding: FragmentLocationEditBinding? = null

    private var mPresenter: LocationEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Location>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentLocationEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = LocationEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.new_location, R.string.edit_location)
    }

    override var entity: Location? = null
        get() = field
        set(value) {
            field = value
            mBinding?.location = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}