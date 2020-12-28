package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkspaceTermsBinding
import com.ustadmobile.core.controller.WorkspaceTermsPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceTermsView

class WorkspaceTermsFragment : UstadBaseFragment(), WorkspaceTermsView {

    private var mBinding: FragmentWorkspaceTermsBinding? = null

    private var mPresenter: WorkspaceTermsPresenter? = null

    override var termsHtml: String? = null
        set(value) {
            mBinding?.termsWebview?.loadData(termsHtml, "text/html", "UTF-8")
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding =  FragmentWorkspaceTermsBinding.inflate(inflater, container, false)
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = WorkspaceTermsPresenter(requireContext(), arguments.toStringMap(),
                this, di)
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_accept, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.accept -> {
                mPresenter?.handleClickAccept()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mBinding = null
        mPresenter = null
    }
}