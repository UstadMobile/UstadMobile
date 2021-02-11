package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentRedirectBinding
import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView
import org.kodein.di.direct
import org.kodein.di.instance


class RedirectFragment : UstadBaseFragment(), RedirectView {

    private var mPresenter: RedirectPresenter? = null

    private var mBinding: FragmentRedirectBinding? = null

    val impl: UstadMobileSystemImpl by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView : View
        mBinding = FragmentRedirectBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intentData = requireActivity().intent.data
        val intentMap: Map<String, String> = if (intentData == null || intentData.toString().isEmpty()) {
            mapOf()
        } else {
            mapOf(UstadView.ARG_INTENT to intentData.toString())
        }
        mPresenter = RedirectPresenter(requireContext(),
                arguments.toStringMap() + requireActivity().intent.extras.toStringMap() + intentMap,
                this, di)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override fun showNextScreen(viewName: String, args: Map<String, String>) {
        val destinationProvider: DestinationProvider = direct.instance()
        val navDestinationId = destinationProvider.lookupDestinationName(viewName)?.destinationId
                ?: throw IllegalArgumentException("No destination for viewname: $viewName")
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.redirect_dest, true).build()
        findNavController().navigate(navDestinationId, args.toBundle(), navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}