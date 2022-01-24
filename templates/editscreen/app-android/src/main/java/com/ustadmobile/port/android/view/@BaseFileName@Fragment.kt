package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.Fragment@BaseFileName@Binding
import com.ustadmobile.core.controller.@BaseFileName@Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.@BaseFileName@View
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.port.android.util.ext.*
@EditEntity_Import@

interface @BaseFileName@FragmentEventHandler {

}

class @BaseFileName@Fragment: UstadEditFragment<@Entity@>(), @BaseFileName@View, @BaseFileName@FragmentEventHandler {

    private var mBinding: Fragment@BaseFileName@Binding? = null

    private var mPresenter: @BaseFileName@Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, @Entity@>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = Fragment@BaseFileName@Binding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = @BaseFileName@Presenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: @EditEntity@? = null
        get() = field
        set(value) {
            field = value
            mBinding?.@Entity_VariableName@ = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}