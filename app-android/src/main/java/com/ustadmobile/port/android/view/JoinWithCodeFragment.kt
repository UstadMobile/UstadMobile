package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentJoinWithCodeBinding
import com.ustadmobile.core.controller.JoinWithCodePresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School

class JoinWithCodeFragment: UstadBaseFragment(), JoinWithCodeView {

    override var controlsEnabled: Boolean? = null
        get() = field
        set(value) {
            field = value
        }

    override var errorText: String? = null
        get() = field
        set(value) {
            mBinding?.errorText = value
            field = value
        }
    override var code: String? = null
        set(value) {
            mBinding?.joinCode = value
            field = value
        }

    private var mBinding: FragmentJoinWithCodeBinding? = null

    private var mPresenter: JoinWithCodePresenter? = null

    override var buttonLabel: String?
        get() = mBinding?.buttonLabel
        set(value) {
            mBinding?.buttonLabel = value
        }

    override var loading: Boolean
        get() = super.loading
        set(value) {
            super.loading = value
            mBinding?.buttonEnabled = !value
        }

    override fun finish() {
        findNavController().navigateUp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentJoinWithCodeBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tableId = arguments?.get(UstadView.ARG_CODE_TABLE).toString().toInt()
        ustadFragmentTitle = if(tableId == Clazz.TABLE_ID){
            mBinding?.entityType = requireContext().getString(R.string.clazz)
            requireContext().getString(R.string.join_existing_class)
        }else if (tableId == School.TABLE_ID){
            mBinding?.entityType = requireContext().getString(R.string.school)
            requireContext().getString(R.string.join_existing_school)
        }else{
            mBinding?.entityType = ""
            "ERR - Unknown entity type"
        }

        mPresenter = JoinWithCodePresenter(requireContext(), arguments.toStringMap(), this,
            di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mBinding = null
    }
}