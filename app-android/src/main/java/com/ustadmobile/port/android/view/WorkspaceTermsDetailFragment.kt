package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkspaceTermsDetailBinding
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.controller.WorkspaceTermsDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceTermsDetailView
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class WorkspaceTermsDetailFragment: UstadDetailFragment<WorkspaceTerms>(), WorkspaceTermsDetailView {

    private var mBinding: FragmentWorkspaceTermsDetailBinding? = null

    private var mPresenter: WorkspaceTermsDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var acceptButtonVisible: Boolean = false
        set(value) {
            field = value
            activity?.invalidateOptionsMenu()
        }

    override var entity: WorkspaceTerms? = null
        get() = field
        set(value) {
            field = value
            mBinding?.workspaceTerms = value
            mBinding?.termsWebview?.loadData(value?.termsHtml ?: "", "text/html",
                    "UTF-8")
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.takeIf { acceptButtonVisible }?.inflate(R.menu.menu_accept, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkspaceTermsDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = WorkspaceTermsDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        return rootView
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
        entity = null
    }



}