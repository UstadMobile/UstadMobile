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
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RedirectView
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
        mPresenter = RedirectPresenter(requireContext(),UMAndroidUtil.bundleToMap(arguments),this, di)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override var showGetStarted: Boolean? = null
        set(value) {
            field = value
            val canSelectServer = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION,
                    requireContext())
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.redirect_dest, true).build()
            val destination = if(value != null && value){
                if(canSelectServer) R.id.account_get_started_dest else R.id.login_dest }
            else R.id.home_content_dest
            findNavController().navigate(destination,null, navOptions)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}