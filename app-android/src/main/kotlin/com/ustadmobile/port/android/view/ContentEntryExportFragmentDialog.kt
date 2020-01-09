package com.ustadmobile.port.android.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryExportPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryExportView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ContentEntryExportFragmentDialog :  UstadDialogFragment() , ContentEntryExportView,
        AdapterView.OnItemSelectedListener{

    override val viewContext: Any
        get() = activity!!

    private lateinit var  mDialog: AlertDialog

    lateinit var presenter: ContentEntryExportPresenter

    private lateinit var baseActivity: UstadBaseActivity

    private lateinit var dialogTitle: TextView

    private lateinit var dialogMessage: TextView

    lateinit var exportProgress: ProgressBar

    private lateinit var mStorageOptions: Spinner

    private lateinit var storageOptionHolder: RelativeLayout

    private lateinit var storageDirs: List<UMStorageDir>

    private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    override fun onAttach(context: Context?) {
        if(context is UstadBaseActivity){
            presenter = ContentEntryExportPresenter(activity!!, UMAndroidUtil.bundleToMap(arguments),
                    this,UmAccountManager.getRepositoryForActiveAccount(activity!!),
                    UmAccountManager.getActiveDatabase(context),impl)
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
        mStorageOptions = rootView.findViewById(R.id.storage_option)
        storageOptionHolder = rootView.findViewById(R.id.use_sdcard_option_holder)

        exportProgress.max = 100
        exportProgress.progress = 20

        val builder = AlertDialog.Builder(context!!)
        builder.setPositiveButton(R.string.download_continue_btn_label, null)
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
           GlobalScope.launch {
               presenter.handleClickPositive()
           }
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

    override fun setUpStorageOptions(storageOptions: List<UMStorageDir>) {
        val options = ArrayList<String>()
        this.storageDirs = storageOptions
        for (umStorageDir in storageOptions) {
            val deviceStorageLabel = String.format(impl.getString(
                    MessageID.download_storage_option_device, context!!), umStorageDir.name,
                    UMFileUtil.formatFileSize(File(umStorageDir.dirURI!!).usableSpace))
            options.add(deviceStorageLabel)
        }

        val storageOptionAdapter = ArrayAdapter(
                Objects.requireNonNull<Context>(context),
                android.R.layout.simple_spinner_item, options)
        storageOptionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)

        mStorageOptions.adapter = storageOptionAdapter
        mStorageOptions.onItemSelectedListener = this
    }

    override fun setDialogMessage(title: String) {
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
        storageOptionHolder.visibility = View.GONE
        exportProgress.isIndeterminate = false
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        presenter.handleStorageOptionSelection(storageDirs[position].dirURI!!)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        presenter.handleStorageOptionSelection(storageDirs[0].dirURI!!)
    }

    override fun updateExportProgress(progress: Int) {
        exportProgress.progress = progress
    }


}
