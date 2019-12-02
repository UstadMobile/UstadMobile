package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter
import com.ustadmobile.sharedse.network.NetworkManagerBle
import java.io.File
import java.util.*


class DownloadDialogFragment : UstadDialogFragment(), DownloadDialogView,
        DialogInterface.OnClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener {

    override val viewContext: Any
        get() = context!!

    private var rootView: View? = null

    private var mDialog: AlertDialog? = null

    private var mPresenter: DownloadDialogPresenter? = null

    private var stackedOptionHolderView: RelativeLayout? = null

    private var wifiOnlyHolder: RelativeLayout? = null

    private var statusTextView: TextView? = null

    private var wifiOnlyView: CheckBox? = null

    private var calculateHolder: RelativeLayout? = null

    private var mStorageOptions: Spinner? = null

    private var impl: UstadMobileSystemImpl? = null

    private var savedInstanceState : Bundle? = null

    private lateinit var managerBle: NetworkManagerBle

    private lateinit var storageDirs: List<UMStorageDir>

    internal var viewIdMap = HashMap<Int, Int>()

    private lateinit var activity: UstadBaseActivity

    override fun onAttach(context: Context?) {
        if (context is UstadBaseActivity) {
            activity = context
            context.runAfterServiceConnection(Runnable{
                context.runOnUiThread {
                    managerBle = context.networkManagerBle!!
                    checkReady()
                }
            })
        }

        super.onAttach(context)
    }

    private fun checkReady(){
        if(::managerBle.isInitialized){
            mPresenter = DownloadDialogPresenter(context as Context, managerBle,
                    bundleToMap(arguments), this, UmAppDatabase.getInstance(context as Context),
                    UmAccountManager.getRepositoryForActiveAccount(context as Context))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = Objects.requireNonNull<Context>(context)
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.fragment_download_layout_view, null)

        this.savedInstanceState = savedInstanceState
        stackedOptionHolderView = rootView!!.findViewById(R.id.stacked_option_holder)
        statusTextView = rootView!!.findViewById(R.id.download_option_status_text)
        wifiOnlyView = rootView!!.findViewById(R.id.wifi_only_option)
        mStorageOptions = rootView!!.findViewById(R.id.storage_option)
        calculateHolder = rootView!!.findViewById(R.id.download_calculate_holder)
        wifiOnlyHolder = rootView!!.findViewById(R.id.wifi_only_option_holder)

        impl = UstadMobileSystemImpl.instance


        val builder = AlertDialog.Builder(context!!)
        builder.setPositiveButton(R.string.ok, this)
        builder.setNegativeButton(R.string.cancel, this)
        builder.setView(rootView)

        mDialog = builder.create()

        wifiOnlyView!!.setOnCheckedChangeListener(this)
        wifiOnlyHolder!!.setOnClickListener(this)

        //mapping presenter constants to view ids
        viewIdMap[DownloadDialogPresenter.STACKED_BUTTON_PAUSE] = R.id.action_btn_pause_download
        viewIdMap[DownloadDialogPresenter.STACKED_BUTTON_CANCEL] = R.id.action_btn_cancel_download
        viewIdMap[DownloadDialogPresenter.STACKED_BUTTON_CONTINUE] = R.id.action_btn_continue_download

        if(mPresenter != null){
            mPresenter!!.onCreate(bundleToMap(savedInstanceState))
        }

        return mDialog as AlertDialog
    }


    override fun showStorageOptions(storageOptions: List<UMStorageDir>) {
        val options = ArrayList<String>()
        this.storageDirs = storageOptions
        for (umStorageDir in storageOptions) {
            val deviceStorageLabel = String.format(impl!!.getString(
                    MessageID.download_storage_option_device, context!!), umStorageDir.name,
                    UMFileUtil.formatFileSize(File(umStorageDir.dirURI!!).usableSpace))
            options.add(deviceStorageLabel)
        }

        val storageOptionAdapter = ArrayAdapter(
                Objects.requireNonNull<Context>(context),
                android.R.layout.simple_spinner_item, options)
        storageOptionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)

        mStorageOptions!!.adapter = storageOptionAdapter
        mStorageOptions!!.onItemSelectedListener = this
    }

    override fun setBottomButtonsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        val buttonNegative = mDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE)
        val buttonPositive = mDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
        buttonNegative.visibility = visibility
        buttonPositive.visibility = visibility
    }

    override fun setBottomButtonPositiveText(text: String) {
        mDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).text = text
    }

    override fun setBottomButtonNegativeText(text: String) {
        mDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE).text = text
    }

    override fun setDownloadOverWifiOnly(wifiOnly: Boolean) {
        wifiOnlyView!!.isChecked = wifiOnly
    }

    override fun setStatusText(statusText: String, totalItems: Int, sizeInfo: String) {
        statusTextView!!.visibility = View.VISIBLE
        statusTextView!!.text = Html.fromHtml(String.format(statusText, totalItems, sizeInfo))
    }


    override fun setStackedOptions(optionIds: IntArray, optionTexts: Array<String>) {
        for (i in optionIds.indices) {
            val mStackedButton = rootView!!.findViewById<Button>(viewIdMap[optionIds[i]]!!)
            mStackedButton.text = optionTexts[i]
            mStackedButton.setOnClickListener(this)
        }
    }

    override fun setStackOptionsVisible(visible: Boolean) {
        stackedOptionHolderView!!.visibility = if (visible) View.VISIBLE else View.GONE
    }


    override fun dismissDialog() {
        mDialog!!.dismiss()
    }


    override fun setWifiOnlyOptionVisible(visible: Boolean) {
        wifiOnlyHolder!!.visibility = if (visible) View.VISIBLE else View.GONE
    }


    override fun setCalculatingViewVisible(visible: Boolean) {
        calculateHolder!!.visibility = if (visible) View.VISIBLE else View.GONE
    }


    override fun onClick(dialog: DialogInterface, which: Int) {
       if(mPresenter != null){
           when (which) {
               DialogInterface.BUTTON_POSITIVE -> mPresenter!!.handleClickPositive()

               DialogInterface.BUTTON_NEGATIVE -> mPresenter!!.handleClickNegative()
           }
       }
    }

    override fun onClick(stackedButton: View) {
        val viewId = stackedButton.id
        if (viewId != R.id.wifi_only_option_holder && viewId != R.id.use_sdcard_option_holder) {
            mPresenter!!.handleClickStackedButton(viewId)
        } else if (viewId == R.id.wifi_only_option_holder) {
            val checkboxState = !wifiOnlyView!!.isChecked
            wifiOnlyView!!.isChecked = checkboxState
            mPresenter!!.handleClickWiFiOnlyOption(checkboxState)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        mPresenter!!.handleClickWiFiOnlyOption(isChecked)
    }


    override fun onCancel(dialog: DialogInterface?) {
        mPresenter!!.handleClickNegative(false)
        super.onCancel(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        mPresenter!!.handleStorageOptionSelection(storageDirs[position].dirURI!!)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        mPresenter!!.handleStorageOptionSelection(storageDirs[0].dirURI!!)
    }

    override fun cancelOrPauseDownload(jobId: Long, cancel: Boolean) {
        //TODO: this should be handled using the downloadjobitemmanager, not sent back to the view
    }
}
