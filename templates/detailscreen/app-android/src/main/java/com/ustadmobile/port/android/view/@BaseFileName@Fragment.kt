package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.Fragment@Entity@DetailBinding
import com.ustadmobile.core.controller.@Entity@DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.@Entity@DetailView
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.port.android.util.ext.*
@DisplayEntity_Import@

interface @BaseFileName@FragmentEventHandler {

}

class @BaseFileName@Fragment: UstadDetailFragment<@DisplayEntity@>(), @Entity@DetailView, @Entity@DetailFragmentEventHandler {

    private var mBinding: Fragment@Entity@DetailBinding? = null

    private var mPresenter: @Entity@DetailPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = Fragment@Entity@DetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = @Entity@DetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        //TODO: Set title here
    }

    override var entity: @DisplayEntity@? = null
        get() = field
        set(value) {
            field = value
            mBinding?.@Entity_VariableName@ = value
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }

}