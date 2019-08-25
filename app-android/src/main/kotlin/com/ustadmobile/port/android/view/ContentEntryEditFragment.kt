package com.ustadmobile.port.android.view


import android.app.ProgressDialog
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.UMAndroidUtil.getDirectionality
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.lib.db.entities.ContentEntry
import java.io.File
import java.util.*
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.importContentEntryFromFile
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.getContent

/**
 * Fragment responsible for editing entry details
 */
class ContentEntryEditFragment : UstadDialogFragment(), ContentEntryEditView {

    override val viewContext: Any
        get() = activity as FragmentActivity

    private var actionListener: EntryCreationActionListener? = null

    private var presenter: ContentEntryEditPresenter? = null

    private var entryLicence: Spinner? = null

    private var entryTitle: TextInputEditText? = null

    private var entryDescription: TextInputEditText? = null

    private var entryThumbnail: ImageView? = null

    private var selectFileBtn: Button? = null

    private var toolbar: Toolbar? = null

    private var thumbnailUrl = ""

    private var selectedLicenceIndex = 0

    private var impl: UstadMobileSystemImpl? = null

    private var mStorageOptions: Spinner? = null

    private var rootView: View? = null

    private var selectedFile: File? = null

    private var supportedFilesList: TextView? = null

    private var isDoneBtnDisabled = false

    private var mProgress: ProgressDialog? = null

    private lateinit var umDatabase: UmAppDatabase

    private lateinit var umRepository: UmAppDatabase


    interface EntryCreationActionListener {

        fun browseFiles(callback: UmResultCallback<String>?, vararg mimeType: String)

        fun showSnackMessage(message: String)

        fun updateDocument(title: String, description: String)
    }


    fun setActionListener(actionListener: EntryCreationActionListener) {
        this.actionListener = actionListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_content_entry,
                container, false)

        val fragmentContext = context!!
        umDatabase = UmAppDatabase.getInstance(fragmentContext)
        umRepository = UmAccountManager.getRepositoryForActiveAccount(fragmentContext)

        toolbar = rootView!!.findViewById(R.id.toolbar)
        toolbar!!.setNavigationIcon(if (getDirectionality(activity?.applicationContext!!) == "ltr")
            R.drawable.ic_arrow_back_white_24dp
        else
            R.drawable.ic_arrow_forward_white_24dp)


        entryLicence = rootView!!.findViewById(R.id.entry_licence)
        entryDescription = rootView!!.findViewById(R.id.entry_description)
        entryTitle = rootView!!.findViewById(R.id.entry_title)
        entryThumbnail = rootView!!.findViewById(R.id.entry_thumbnail)
        selectFileBtn = rootView!!.findViewById(R.id.update_file)
        mStorageOptions = rootView!!.findViewById(R.id.storage_option)
        supportedFilesList = rootView!!.findViewById(R.id.supported_file_list)
        supportedFilesList!!.text = Html.fromHtml(getString(R.string.content_supported_files))

        val addThumbnail = rootView!!.findViewById<View>(R.id.add_folder_thumbnail)


        toolbar!!.setNavigationOnClickListener { dismiss() }

        toolbar!!.inflateMenu(R.menu.menu_content_entry_fragment_top)

        impl = UstadMobileSystemImpl.instance

        presenter = ContentEntryEditPresenter(activity!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        presenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        selectFileBtn!!.setOnClickListener { v ->
            if (actionListener != null) {
                actionListener!!.browseFiles(null, "application/*")
            }
        }


        toolbar!!.setOnMenuItemClickListener { item ->
            val menuId = item.itemId

            if (menuId == R.id.save_content) {
                if (!isDoneBtnDisabled) {
                    Thread {
                        presenter!!.handleSaveUpdateEntry(entryTitle!!.text.toString(),
                                entryDescription!!.text.toString(), thumbnailUrl,
                                selectedLicenceIndex)
                    }.start()
                }
            }
            true
        }

        addThumbnail.setOnClickListener { presenter!!.handleAddThumbnail() }

        mProgress = ProgressDialog(activity)
        mProgress!!.setMessage(getString(R.string.content_entry_importing))
        mProgress!!.setCancelable(false)

        return rootView
    }


    override fun showFileSelector(show: Boolean) {
        selectFileBtn!!.visibility = if (show) View.VISIBLE else View.GONE
        supportedFilesList!!.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun setEntryTitle(title: String) {
        entryTitle!!.setText(title)
        toolbar!!.title = title
    }


    override fun updateFileBtnLabel(label: String) {
        selectFileBtn!!.text = label
    }

    override fun setDescription(description: String) {
        entryDescription!!.setText(description)
    }


    override fun onDestroy() {
        presenter!!.onDestroy()
        super.onDestroy()
    }

    override fun setUpLicence(licence: List<String>, index: Int) {
        val licenceAdapter = ArrayAdapter(Objects.requireNonNull<FragmentActivity>(activity),
                R.layout.licence_dropdown_selected_view, licence)
        licenceAdapter.setDropDownViewResource(R.layout.licence_dropdown_view)
        entryLicence!!.adapter = licenceAdapter

        entryLicence!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedLicenceIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedLicenceIndex = 0
            }
        }
        entryLicence!!.setSelection(index, true)
    }

    override fun setThumbnail(thumbnailUrl: String?) {
        if (thumbnailUrl != null && thumbnailUrl.isNotEmpty()) {
            this.thumbnailUrl = thumbnailUrl
            Picasso.get().load(thumbnailUrl).into(entryThumbnail)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SELECTED_FILE, selectedFile)
    }

    override fun onViewStateRestored(@Nullable savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        try {
            selectedFile = savedInstanceState!!.getSerializable(SELECTED_FILE) as File
        } catch (e: NullPointerException) {
            /*Should not happen*/
        }

    }

    override fun showStorageOptions(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        mStorageOptions!!.visibility = visibility
        rootView!!.findViewById<View>(R.id.storage_option_label).visibility = visibility

    }


    override fun showImageSelector(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        rootView!!.findViewById<View>(R.id.image_selector_holder).visibility = visibility
    }

    override fun setUpStorageOption(storageDirs: List<UMStorageDir>) {

        val storageOptions = ArrayList<String>()
        for (umStorageDir in storageDirs) {
            val deviceStorageLabel = String.format(impl!!.getString(
                    MessageID.download_storage_option_device, context!!), umStorageDir.name,
                    UMFileUtil.formatFileSize(
                            File(umStorageDir.dirURI).usableSpace))
            storageOptions.add(deviceStorageLabel)
        }

        val storageOptionAdapter = ArrayAdapter(Objects.requireNonNull<FragmentActivity>(activity),
                R.layout.licence_dropdown_selected_view, storageOptions)
        storageOptionAdapter.setDropDownViewResource(R.layout.licence_dropdown_view)

        mStorageOptions!!.adapter = storageOptionAdapter
        mStorageOptions!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                presenter!!.handleStorageOptionChange(storageDirs[position].dirURI!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }


    override fun showErrorMessage(message: String?, visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        val errorTxt = rootView!!.findViewById<TextView>(R.id.file_not_supported)
        errorTxt.visibility = visibility
        errorTxt.text = message
    }


    override fun showMessageAndDismissDialog(message: String?, document: Boolean) {
        dismissDialog()
        if (actionListener != null) {

            if (document)
                actionListener!!.updateDocument(entryTitle!!.text.toString(),
                        entryDescription!!.text.toString())

            if (message != null)
                actionListener!!.showSnackMessage(message)
        }
    }

    fun checkIfIsSupportedFile(file: File) {
        this.selectedFile = file
        val content = getContent(file)
        presenter!!.handleSelectedFilePath(selectedFile!!.absolutePath)
        presenter!!.handleSelectedFileToImport(content)
    }

    override fun dismissDialog() {
        if (mProgress!!.isShowing) {
            isDoneBtnDisabled = false
            mProgress!!.dismiss()
        }
        dismiss()
    }

    override fun importContent(content: HashMap<String, Any?>) {
        importContentEntryFromFile(activity!!, content, presenter!!.getSelectedStorageOption(),
                selectedFile!!, object : UmCallback<ContentEntry> {
            override fun onSuccess(result: ContentEntry?) {
                presenter!!.handleImportedFile(result, selectedFile!!.length())
            }

            override fun onFailure(exception: Throwable?) {}
        })
    }

    override fun showAddThumbnailMessage() {
        Toast.makeText(activity, getString(R.string.content_folder_image_message),
                Toast.LENGTH_LONG).show()
    }

    override fun showProgressDialog() {
        isDoneBtnDisabled = true
        mProgress!!.show()
    }

    companion object {

        private const val SELECTED_FILE = "selected_file"
    }

}
