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
            mBinding?.code = value
            field = value
        }

    private var mBinding: FragmentJoinWithCodeBinding? = null

    private var mPresenter: JoinWithCodePresenter? = null

    override fun finish() {
        findNavController().navigateUp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentJoinWithCodeBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tableId = arguments?.get(UstadView.ARG_CODE_TABLE)?:"0".toString().toInt()
        ustadFragmentTitle = if(tableId == Clazz.TABLE_ID){
            mBinding?.entityType = requireContext().getString(R.string.clazz)
            requireContext().getString(R.string.join_existing,
                    requireContext().getString(R.string.clazz))
        }else if (tableId == School.TABLE_ID){
            mBinding?.entityType = requireContext().getString(R.string.school)
            requireContext().getString(R.string.join_existing,
                    requireContext().getString(R.string.school))
        }else{
            mBinding?.entityType = ""
            requireContext().getString(R.string.join_existing)
        }

        mPresenter = JoinWithCodePresenter(requireContext(), arguments.toStringMap(), this,
            di)
        mPresenter?.onCreate(null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_done) {
            val code = mBinding?.code ?: return super.onOptionsItemSelected(item)
            mPresenter?.handleClickDone(code)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mBinding = null
    }
}