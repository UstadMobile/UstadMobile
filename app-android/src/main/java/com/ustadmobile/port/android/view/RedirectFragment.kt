package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.databinding.FragmentRedirectBinding
import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
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
    val viewLifecycleObserver = object: DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)

            val intentData = requireActivity().intent.data
            val intentMap: Map<String, String> = if (intentData == null || intentData.toString().isEmpty()) {
                mapOf()
            } else {
                mapOf(UstadView.ARG_DEEPLINK to intentData.toString())
            }
            mPresenter = RedirectPresenter(requireContext(),
                arguments.toStringMap() + requireActivity().intent.extras.toStringMap() + intentMap,
                this@RedirectFragment, di)
            mPresenter?.onCreate(null)
        }
    }

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
    }

    companion object {

        const val KEY_REDIRECTED = "redirected"

    }
}