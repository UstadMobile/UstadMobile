package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.toughra.ustadmobile.databinding.FragmentPersonDetailBinding
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailFragmentEventHandler {
    fun handleClickDialNumber(number: String)

    fun handleClickEmail(emailAddr: String)

    fun handleClickSms(number: String)
}

class PersonDetailFragment: UstadDetailFragment<PersonWithDisplayDetails>(), PersonDetailView, PersonDetailFragmentEventHandler {

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentEventHandler = this
        }

        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
    }

    override fun handleClickDialNumber(number: String) {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            setData(Uri.parse("tel:$number"))
        }

        if(callIntent.resolveActivity(requireContext().packageManager) != null)
            requireContext().startActivity(callIntent)
    }

    override fun handleClickEmail(emailAddr: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddr))
            data = Uri.parse("mailto:$emailAddr")
        }
        if(emailIntent.resolveActivity(requireContext().packageManager) != null) {
            requireContext().startActivity(emailIntent)
        }
    }

    override fun handleClickSms(number: String) {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
        }
        if(smsIntent.resolveActivity(requireContext().packageManager) != null) {
            requireContext().startActivity(smsIntent)
        }
    }

    override var entity: PersonWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            if(viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title = value?.firstNames + " " + value?.lastName
        }



}