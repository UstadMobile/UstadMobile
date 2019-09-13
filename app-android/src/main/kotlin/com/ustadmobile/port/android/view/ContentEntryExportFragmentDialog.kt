package com.ustadmobile.port.android.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryExportPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryExportView
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ContentEntryExportFragmentDialog :  UstadDialogFragment() , ContentEntryExportView{

    override val viewContext: Any
        get() = activity!!

    lateinit var  mDialog: AlertDialog

    lateinit var presenter: ContentEntryExportPresenter

    lateinit var baseActivity: UstadBaseActivity

    lateinit var dialogTitle: TextView

    lateinit var dialogMessage: TextView

    lateinit var exportProgress: ProgressBar

    override fun onAttach(context: Context?) {
        if(context is UstadBaseActivity){
            presenter = ContentEntryExportPresenter(activity!!, UMAndroidUtil.bundleToMap(arguments),
                    this,UmAccountManager.getRepositoryForActiveAccount(activity!!),
                    UmAppDatabase.getInstance(context), UstadMobileSystemImpl.instance)
            baseActivity = context
        }
        super.onAttach(context)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the layout for this fragment

        val umInflater = Objects.requireNonNull<Context>(context)
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = umInflater.inflate(R.layout.fragment_content_entry_export_fragment_dialog,null)

        dialogTitle = rootView.findViewById(R.id.dialog_title)
        dialogMessage = rootView.findViewById(R.id.dialog_message)
        exportProgress = rootView.findViewById(R.id.export_progress)

        exportProgress.max = 100
        exportProgress.progress = 20

        val builder = AlertDialog.Builder(context!!)
        builder.setPositiveButton(R.string.ok, null)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setView(rootView)
        mDialog = builder.create()

        isCancelable = false
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
        return mDialog
    }

    override fun onResume() {
        super.onResume()
        mDialog = dialog as AlertDialog
        val okButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val cancelButton = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        okButton.setOnClickListener {
            presenter.handleClickPositive()
        }

        cancelButton.setOnClickListener {
            presenter.handleClickNegative()
        }
    }

    override fun checkFilePermissions() {
        baseActivity.runAfterGrantingPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Runnable {
                    runOnUiThread(Runnable {
                        mDialog.show()
                    })
                },
                getString(R.string.download_storage_permission_title),
                getString(R.string.download_storage_permission_message))
    }

    override fun setMessageText(title: String) {
        dialogMessage.text = Html.fromHtml(String.format(getString(
                R.string.content_entry_export_message, "<b>$title</b>")))
    }

    override fun dismissDialog() {
        mDialog.dismiss()
    }

    override fun prepareProgressView(show: Boolean) {
        dialogTitle.setText(R.string.content_entry_export_inprogress)
        dialogMessage.visibility = View.GONE
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.GONE
        exportProgress.visibility = if(show) View.VISIBLE else View.GONE
        exportProgress.isIndeterminate = false
    }

    override fun updateExportProgress(progress: Int) {
        exportProgress.progress = progress
    }


}
