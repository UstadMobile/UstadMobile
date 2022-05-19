package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.databinding.FragmentHtmlTextViewBinding
import com.ustadmobile.core.controller.HtmlTextViewDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HtmlTextViewDetailView
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class HtmlTextViewFragment: UstadDetailFragment<String>(), HtmlTextViewDetailView {

    private var mBinding: FragmentHtmlTextViewBinding? = null

    private var mPresenter: HtmlTextViewDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentHtmlTextViewBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter = HtmlTextViewDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var title: String? = null
        set(value) {
            field = value
            ustadFragmentTitle = value
        }

    override var entity: String? = null
        set(value) {
            field = value
            mBinding?.text = value
        }

}