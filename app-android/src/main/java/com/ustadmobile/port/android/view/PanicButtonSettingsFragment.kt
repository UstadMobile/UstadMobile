package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPanicButtonSettingsBinding
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.port.android.presenter.PanicButtonSettingsPresenter
import com.ustadmobile.port.android.presenter.PanicTriggerApp
import org.kodein.di.instance

interface PanicButtonSettingsFragmentEventListener {
    fun onClickSelectPanicTriggerApp()

    fun onClickExplanation()
}



class PanicButtonSettingsFragment: UstadBaseFragment(), PanicButtonSettingsView,
    PanicButtonSettingsFragmentEventListener
{

    private var mBinding: FragmentPanicButtonSettingsBinding? = null

    private var mPresenter: PanicButtonSettingsPresenter? = null

    override var panicTriggerAppList: List<PanicTriggerApp> = emptyList()
        set(value) {
            field = value
        }

    override var selectedTriggerApp: PanicTriggerApp? = null
        set(value) {
            field = value
            mBinding?.panicButtonAppName = value?.simpleName ?: ""
            mBinding?.itemPanicbuttonAppicon?.setImageDrawable(value?.appIcon)
        }

    override var unhideCode: String?
        get() = mBinding?.unlockCode
        set(value) {
            mBinding?.unlockCode = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View

        val systemImpl: UstadMobileSystemImpl by instance()

        mBinding = FragmentPanicButtonSettingsBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.eventListener = this

            it.panicButtonSettingsExitAppCheckbox.isChecked = systemImpl.getAppPref(
                PanicButtonSettingsPresenter.PREF_LOCK_AND_EXIT)?.toBoolean() ?: false
            it.panicButtonSettingsClearAppDataCheckbox.isChecked = systemImpl.getAppPref(
                PanicButtonSettingsPresenter.PREF_CLEAR_APP_DATA)?.toBoolean() ?: false
            it.panicButtonSettingsUninstallCheckbox.isChecked = systemImpl.getAppPref(
                PanicButtonSettingsPresenter.PREF_UNINSTALL_THIS_APP)?.toBoolean() ?: false
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter = PanicButtonSettingsPresenter(requireContext(),
            arguments.toStringMap(), this, di)


        mBinding?.panicButtonSettingsExitAppCheckbox?.setOnCheckedChangeListener { _, checked ->
            mPresenter?.onChangeLockAndExit(checked)
        }
        mBinding?.panicButtonSettingsClearAppDataCheckbox?.setOnCheckedChangeListener { _, checked ->
            mPresenter?.onChangeClearAppData(checked)
        }
        mBinding?.panicButtonSettingsUninstallCheckbox?.setOnCheckedChangeListener { _, checked ->
            mPresenter?.onChangeUninstallThisApp(checked)
        }

        mPresenter?.onCreate(savedInstanceState?.toStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onClickSelectPanicTriggerApp() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.panic_button_app)
            .setItems(panicTriggerAppList.map { it.simpleName }.toTypedArray()) {_, which ->
                mPresenter?.onSelectTriggerApp(panicTriggerAppList[which])
            }
            .show()
    }

    override fun onClickExplanation() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=info.guardianproject.ripple"))
        startActivity(intent)
    }

    companion object {



    }

}