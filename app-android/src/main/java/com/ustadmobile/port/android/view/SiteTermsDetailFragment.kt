package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteTermsDetailBinding
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.controller.SiteTermsDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class SiteTermsDetailFragment: UstadDetailFragment<SiteTerms>(), SiteTermsDetailView {

    private var mBinding: FragmentSiteTermsDetailBinding? = null

    private var mPresenter: SiteTermsDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var acceptButtonVisible: Boolean = false
        set(value) {
            field = value
            activity?.invalidateOptionsMenu()
        }

    override var entity: SiteTerms? = null
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
        mBinding = FragmentSiteTermsDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteTermsDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
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