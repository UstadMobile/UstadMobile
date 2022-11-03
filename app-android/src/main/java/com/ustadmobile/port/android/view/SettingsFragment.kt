package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView
import org.kodein.di.instance

interface SettingsFragmentEventListener {

    fun onClickAppLanguage()

}

class SettingsFragment : UstadBaseFragment(), SettingsView, SettingsFragmentEventListener{

    var mPresenter: SettingsPresenter? = null

    private var mBinding: FragmentSettingsBinding? = null

    override var displayLanguage: String?
        get() = mBinding?.displayLanguage
        set(value) {
            mBinding?.displayLanguage = value
        }

    override var workspaceSettingsVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.workspaceSettingsVisible = value
        }
    override var holidayCalendarVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.holidayCalendarVisible = value
        }

    override var reasonLeavingVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.reasonLeavingVisible = value
        }

    override var langListVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.langListVisible = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view:View
        mBinding = FragmentSettingsBinding.inflate(inflater, container, false).also {
            view = it.root
        }

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        mBinding?.presenter = mPresenter
        mBinding?.fragmentEventListener = this

        return view
    }

    override fun onClickAppLanguage() {
        val systemImpl: UstadMobileSystemImpl by instance()
        val langList = systemImpl.getAllUiLanguagesList(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_language)
            .setItems(langList.map { it.langDisplay }.toTypedArray()) { _, which ->
                val lang = langList[which].langCode
                systemImpl.setLocale(lang, requireContext())
                activity?.recreate()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.presenter = null
        mBinding?.fragmentEventListener = null
        mBinding = null
        mPresenter = null
    }

}
