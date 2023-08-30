package com.ustadmobile.port.android.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter
import com.ustadmobile.sharedse.controller.DownloadDialogPresenter.Companion.STACKED_BUTTON_CANCEL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.instance
import org.kodein.di.on
import java.util.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.R as CR

//It would be nice to move this to proguard-rules.pro and allow obfuscation of the contents of the class
@Keep
class DownloadDialogFragment : UstadDialogFragment(), DownloadDialogView,
        DialogInterface.OnClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener {

    private lateinit var rootView: View

    private lateinit var mDialog: AlertDialog

    private var mPresenter: DownloadDialogPresenter? = null

    private lateinit var stackedOptionHolderView: RelativeLayout

    private lateinit var wifiOnlyHolder: RelativeLayout

    private lateinit var statusTextView: TextView

    private lateinit var wifiOnlyView: CheckBox

    private lateinit var calculateHolder: RelativeLayout

    private lateinit var mStorageOptions: Spinner

    private var savedInstanceState : Bundle? = null

    private lateinit var storageDirs: List<ContainerStorageDir>

    internal var viewIdMap = HashMap<Int, Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.fragment_download_layout_view, null)

        this.savedInstanceState = savedInstanceState
        stackedOptionHolderView = rootView.findViewById(R.id.stacked_option_holder)
        statusTextView = rootView.findViewById(R.id.download_option_status_text)
        wifiOnlyView = rootView.findViewById(R.id.wifi_only_option)
        mStorageOptions = rootView.findViewById(R.id.storage_option)
        calculateHolder = rootView.findViewById(R.id.download_calculate_holder)
        wifiOnlyHolder = rootView.findViewById(R.id.wifi_only_option_holder)


        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton(CR.string.ok, this)
        builder.setNegativeButton(CR.string.cancel, this)
        builder.setView(rootView)

        mDialog = builder.create()

        wifiOnlyView.setOnCheckedChangeListener(this)
        wifiOnlyHolder.setOnClickListener(this)

        //mapping presenter constants to view ids
//        viewIdMap[DownloadDialogPresenter.STACKED_BUTTON_PAUSE] = R.id.action_btn_pause_download
        viewIdMap[DownloadDialogPresenter.STACKED_BUTTON_CANCEL] = R.id.action_btn_cancel_download
//        viewIdMap[DownloadDialogPresenter.STACKED_BUTTON_CONTINUE] = R.id.action_btn_continue_download


        mPresenter = DownloadDialogPresenter(context as Context, bundleToMap(arguments),
            this@DownloadDialogFragment, di, this).also {
            it.onCreate(null)
            GlobalScope.launch(Dispatchers.Main) {
                showStorageOptions()
            }
        }

        return mDialog
    }

    private suspend fun showStorageOptions() {
        val impl: UstadMobileSystemImpl by di.instance()
        val accountManager: UstadAccountManager by di.instance()
        val containerStorageManager: ContainerStorageManager by on(accountManager.activeAccount).instance()
        val storageOptions = containerStorageManager.storageList
        this.storageDirs = storageOptions
        val optionLabels = storageOptions.map { umStorageDir ->
            String.format(impl.getString(MR.strings.download_storage_option_device),
                    umStorageDir.name,
                    UMFileUtil.formatFileSize(umStorageDir.usableSpace))
        }
        val storageOptionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
                optionLabels)
        storageOptionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)

        mStorageOptions.adapter = storageOptionAdapter
        mStorageOptions.onItemSelectedListener = this
    }

    override fun setBottomButtonsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        val buttonNegative = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val buttonPositive = mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        buttonNegative.visibility = visibility
        buttonPositive.visibility = visibility
    }

    override fun setBottomButtonPositiveText(text: String) {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).text = text
    }

    override fun setBottomButtonNegativeText(text: String) {
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).text = text
    }

    override fun setDownloadOverWifiOnly(wifiOnly: Boolean) {
        wifiOnlyView.isChecked = wifiOnly
    }

    override fun setStatusText(statusText: String, totalItems: Int, sizeInfo: String) {
        statusTextView.visibility = View.VISIBLE
        statusTextView.text = Html.fromHtml(String.format(statusText, totalItems, sizeInfo))
    }


    override fun setStackedOptions(optionIds: IntArray, optionTexts: Array<String>) {
        for (i in optionIds.indices) {
            val viewId = STACKED_BUTTON_ANDROID_ID_TO_PRESENTER_ID_MAP.entries.find { it.value == i }?.key
                ?: throw IllegalArgumentException("setStackOptions: cant find view id $i")
            val mStackedButton = rootView.findViewById<Button>(viewId)
            mStackedButton.text = optionTexts[i]
            mStackedButton.setOnClickListener(this)
        }
    }

    override fun setStackOptionsVisible(visible: Boolean) {
        stackedOptionHolderView.visibility = if (visible) View.VISIBLE else View.GONE
    }


    override fun dismissDialog() {
        mDialog.dismiss()
    }


    override fun setWifiOnlyOptionVisible(visible: Boolean) {
        wifiOnlyHolder.visibility = if (visible) View.VISIBLE else View.GONE
    }


    override fun setCalculatingViewVisible(visible: Boolean) {
        calculateHolder.visibility = if (visible) View.VISIBLE else View.GONE
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
        if (viewId in STACKED_BUTTON_ANDROID_ID_TO_PRESENTER_ID_MAP.keys) {
            STACKED_BUTTON_ANDROID_ID_TO_PRESENTER_ID_MAP.get(viewId)?.let { presenterButtonId ->
                mPresenter?.handleClickStackedButton(presenterButtonId)
            }
        } else if (viewId == R.id.wifi_only_option_holder) {
            val checkboxState = !wifiOnlyView.isChecked
            wifiOnlyView.isChecked = checkboxState
            mPresenter?.handleClickWiFiOnlyOption(checkboxState)
        }
    }

    override fun setBottomPositiveButtonEnabled(enabled: Boolean) {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = enabled
    }

    override fun setWarningText(text: String) {
        rootView.findViewById<TextView>(R.id.download_warning_text).text = text
    }

    override fun setWarningTextVisible(visible: Boolean) {
        rootView.findViewById<TextView>(R.id.download_warning_text).visibility = if(visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        mPresenter?.handleClickWiFiOnlyOption(isChecked)
    }


    override fun onCancel(dialog: DialogInterface) {
        mPresenter?.handleClickNegative(false)
        super.onCancel(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        mPresenter?.handleStorageOptionSelection(storageDirs[position])
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        mPresenter?.handleStorageOptionSelection(storageDirs[0])
    }

    companion object {
        val STACKED_BUTTON_ANDROID_ID_TO_PRESENTER_ID_MAP = mapOf(
            R.id.action_btn_cancel_download to STACKED_BUTTON_CANCEL
        )
    }
}
