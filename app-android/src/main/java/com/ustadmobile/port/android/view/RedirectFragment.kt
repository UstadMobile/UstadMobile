package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentRedirectBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.GrantAppPermissionView
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView
import org.kodein.di.instance

/**
 * The redirect fragment is the root of the nav controller graph. It is kept in the back stack
 * so that it is possible for the back stack to be cleared (e.g. by using popUpTo=redirect_dest)
 *
 * The redirect is only ever done once. If the user comes back to here thereafter, that means we
 * need to finish the activity itself.
 */
class RedirectFragment : UstadBaseFragment(), RedirectView {

    private var mPresenter: RedirectPresenter? = null

    private var mBinding: FragmentRedirectBinding? = null

    val impl: UstadMobileSystemImpl by instance()

    //This needs to be done in onStart, not onCreate. The activity's views are not ready on onCreate,
    // and this causes systemImpl.go to fail at finding the nav controller
    private val viewLifecycleObserver = object: DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)

            val intent = requireActivity().intent
            val intentData = intent.data?.toString()

            /**
             * Young student no password single sign-on Android is an exception: does not use
             * accountmanager etc.
             */
            if(intent.action == UstadAccountManager.ACTION_STUDENT_NO_PASSWORD_SINGLE_SIGN_ON) {
                findNavController().navigate(
                    resId = R.id.student_no_password_sign_on_course_list,
                    args = bundleOf(),
                    navOptions = navOptions {
                        popUpTo(R.id.redirect_dest) {
                            inclusive = true
                        }
                    }
                )

                return@onStart
            }


            val intentMap = mutableMapOf<String, String>()

            if(!intentData.isNullOrEmpty()) {
                intentMap[UstadView.ARG_OPEN_LINK] = intentData
            }

            intent.getStringExtra(UstadView.ARG_OPEN_LINK)?.also {
                intentMap[UstadView.ARG_OPEN_LINK] = it
            }

            /*
             * If being used by the AuthenticatorActivity to respond to an intent, map this to
             * the viewname
             */
            if(intent.action == UstadAccountManager.ACTION_GET_AUTH_TOKEN) {
                intentMap[UstadView.ARG_OPEN_LINK] = "${GrantAppPermissionView.VIEW_NAME}?" +
                    "${GrantAppPermissionView.ARG_PERMISSION_UID}=0" +
                    "&${GrantAppPermissionView.ARG_RETURN_NAME}=true"
            }

            intent.getStringExtra(UstadView.ARG_ACCOUNT_NAME)?.also {
                intentMap[UstadView.ARG_ACCOUNT_NAME] = it
            }

            val argMap =arguments.toStringMap() + requireActivity().intent.extras.toStringMap() +
                intentMap
            mPresenter = RedirectPresenter(requireContext(),
                argMap,this@RedirectFragment, di).withViewLifecycle()
            mPresenter?.onCreate(null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView : View
        mBinding = FragmentRedirectBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedState = findNavController().currentBackStackEntry?.savedStateHandle
        val alreadyRedirected: String = savedState?.get(KEY_REDIRECTED) ?: false.toString()
        if(alreadyRedirected.toBoolean()) {
            requireActivity().finish()
        }else {
            viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)
            findNavController().currentBackStackEntry?.savedStateHandle?.set(KEY_REDIRECTED,
                true.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }

    companion object {

        const val KEY_REDIRECTED = "redirected"

    }
}