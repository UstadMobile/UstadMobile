package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.Fragment@Entity@DetailBinding
import androidx.navigation.fragment.findNavController

import com.ustadmobile.core.controller.@Entity@DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
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

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = Fragment@Entity@DetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(view, savedInstanceState)
        mPresenter = @Entity@DetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: @DisplayEntity@? = null
        get() = field
        set(value) {
            field = value
            mBinding?.@Entity_VariableName@ = value
        }

}