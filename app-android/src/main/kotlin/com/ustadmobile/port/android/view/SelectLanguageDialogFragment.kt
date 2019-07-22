package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectLanguageDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.core.view.SelectLanguageDialogView

import java.util.Objects


class SelectLanguageDialogFragment : UstadDialogFragment(),
        SelectLanguageDialogView, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View

    private var mPresenter: SelectLanguageDialogPresenter? = null
    //Context (Activity calling this)
    private var mAttachedContext: Context? = null
    internal var toolbar: Toolbar? = null
    internal lateinit var english: TextView
    internal lateinit var dari: TextView
    internal lateinit var pashto: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = context!!.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = inflater.inflate(R.layout.fragment_select_language_dialog, null)

        english = rootView.findViewById(R.id.fragment_select_language_dialog_english)
        dari = rootView.findViewById(R.id.fragment_select_language_dialog_dari)
        pashto = rootView.findViewById(R.id.fragment_select_language_dialog_pashto)

        english.setOnClickListener { v -> handleClickEnglish() }
        dari.setOnClickListener { v -> handleClickDari() }
        pashto.setOnClickListener { v -> handleClickPashto() }

        mPresenter = SelectLanguageDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Set any view components and its listener (post presenter work)

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.choose_language)
        builder.setView(rootView)
        dialog = builder.create()

        return dialog
    }

    private fun handleClickEnglish() {
        //Set locale
        val impl = UstadMobileSystemImpl.instance
        impl.setLocale("en", context!!)
        finish()

    }

    private fun handleClickDari() {
        val impl = UstadMobileSystemImpl.instance
        impl.setLocale("fa", context!!)
        finish()
    }

    private fun handleClickPashto() {
        val impl = UstadMobileSystemImpl.instance
        impl.setLocale("ps", context!!)
        finish()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mAttachedContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mAttachedContext = null
    }

    override fun finish() {
        dialog.dismiss()
        activity!!.finish()
        startActivity(activity!!.intent)
    }

    companion object {


        fun newInstance(): SelectLanguageDialogFragment {
            val fragment = SelectLanguageDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
