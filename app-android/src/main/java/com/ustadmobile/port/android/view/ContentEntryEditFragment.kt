package com.ustadmobile.port.android.view


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_ENTRY
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin.Companion.CONTENT_MIMETYPE
import com.ustadmobile.core.controller.ContentEntryEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.UMAndroidUtil.getDirectionality
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryEditView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.getContent
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.importContentEntryFromFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Fragment responsible for editing entry details
 */
class ContentEntryEditFragment : UstadDialogFragment(), ContentEntryEditView {

    override val viewContext: Any
        get() = activity as FragmentActivity

    private var actionListener: EntryCreationActionListener? = null

    private var presenter: ContentEntryEditPresenter? = null

    private lateinit var entryLicence: Spinner

    private lateinit var entryTitle: TextInputEditText

    private lateinit var entryDescription: TextInputEditText

    private lateinit var entryThumbnail: ImageView

    private lateinit var selectFileBtn: Button

    private lateinit var toolbar: Toolbar

    private var thumbnailUrl: String? = null

    private var selectedLicenceIndex = 0

    private var impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    private var mStorageOptions: Spinner? = null

    private lateinit var rootView: View

    private lateinit var selectedFile: File

    private lateinit var supportedFilesList: TextView

    private var isDoneBtnDisabled = false

    private lateinit var umDatabase: UmAppDatabase

    private lateinit var umRepository: UmAppDatabase

    private lateinit var optionsActivity: UstadBaseWithContentOptionsActivity


    private lateinit var visibilitySwitch: SwitchCompat

    private lateinit var inActiveSwitch: SwitchCompat

    private lateinit var inActiveHolder: RelativeLayout

    private lateinit var visibilityHolder: RelativeLayout


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

    override fun onAttach(context: Context) {
        if(context is UstadBaseWithContentOptionsActivity){
            optionsActivity = context
        }
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(R.layout.fragment_content_entry,
                container, false)

        val fragmentContext = context!!
        umDatabase = UmAccountManager.getActiveDatabase(fragmentContext)
        umRepository = UmAccountManager.getRepositoryForActiveAccount(fragmentContext)

        toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(if (getDirectionality(activity?.applicationContext!!) == "ltr")
            R.drawable.ic_arrow_back_white_24dp
        else
            R.drawable.ic_arrow_forward_white_24dp)


        inActiveHolder = rootView.findViewById(R.id.entry_content_inactive_holder)
        visibilityHolder = rootView.findViewById(R.id.entry_content_visibility_holder)
        entryLicence = rootView.findViewById(R.id.entry_licence)
        visibilitySwitch = rootView.findViewById(R.id.entry_visibility_switch)
        inActiveSwitch = rootView.findViewById(R.id.entry_inactive_switch)
        entryDescription = rootView.findViewById(R.id.entry_description)
        entryTitle = rootView.findViewById(R.id.entry_title)
        entryThumbnail = rootView.findViewById(R.id.entry_thumbnail)
        selectFileBtn = rootView.findViewById(R.id.update_file)
        mStorageOptions = rootView.findViewById(R.id.storage_option)
        supportedFilesList = rootView.findViewById(R.id.supported_file_list)
        supportedFilesList.text = Html.fromHtml(getString(R.string.content_supported_files))

        val addThumbnail = rootView.findViewById<View>(R.id.add_folder_thumbnail)


        toolbar.setNavigationOnClickListener { dismiss() }

        toolbar.inflateMenu(R.menu.menu_content_entry_fragment_top)

        val umRepo = UmAccountManager.getRepositoryForActiveAccount(context!!)

        presenter = ContentEntryEditPresenter(activity!!,
                UMAndroidUtil.bundleToMap(arguments), this, umRepo.contentEntryDao,
                umRepo.contentEntryParentChildJoinDao, umRepo.contentEntryStatusDao,
                UmAccountManager.getActiveAccount(context!!)!!, UstadMobileSystemImpl.instance){
            dir: String, mimeType:String,entry: ContentEntry -> return@ContentEntryEditPresenter importFile(dir, mimeType, entry)
        }

        presenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        selectFileBtn.setOnClickListener {
            presenter!!.handleContentButton()
        }


        toolbar.setOnMenuItemClickListener { item ->
            val menuId = item.itemId

            if (menuId == R.id.save_content) {
                if (!isDoneBtnDisabled) {
                    GlobalScope.launch {
                        presenter!!.handleSaveUpdateEntry(entryTitle.text.toString(),
                                entryDescription.text.toString(),
                                if(thumbnailUrl != null) thumbnailUrl as String  else "",
                                selectedLicenceIndex, inActiveSwitch.isChecked, visibilitySwitch.isChecked)
                    }
                }
            }
            true
        }

        addThumbnail.setOnClickListener { presenter!!.handleAddThumbnail() }

        inActiveHolder.setOnClickListener{
            inActiveSwitch.isChecked = !inActiveSwitch.isChecked
        }

        visibilityHolder.setOnClickListener{
            visibilitySwitch.isChecked = ! visibilitySwitch.isChecked
        }

        return  rootView
    }



    override fun startBrowseFiles() {
        if (actionListener != null) {
            actionListener!!.browseFiles(null, "application/*")
        }
    }

    override fun showUpdateContentDialog(title: String, options: List<String>){

        val builder = AlertDialog.Builder(viewContext as Context)
        builder.setTitle(title)
        builder.setItems(options.toTypedArray()) { _, which ->
            if(which == 0){
                startBrowseFiles()
            }else if(which == 1){
                presenter!!.handleUpdateLink()
            }
        }
        builder.show()

    }


    override fun showFileSelector(show: Boolean) {
        selectFileBtn.visibility = if (show) View.VISIBLE else View.GONE
        supportedFilesList.visibility = if (show) View.VISIBLE else View.GONE
    }



    override fun updateFileBtnLabel(label: String) {
        selectFileBtn.text = label
    }


    override fun onDestroy() {
        presenter!!.onDestroy()
        super.onDestroy()
    }

    override fun setUpLicence(licence: List<String>, index: Int) {
        val licenceAdapter = ArrayAdapter(Objects.requireNonNull<FragmentActivity>(activity),
                R.layout.licence_dropdown_selected_view, licence)
        licenceAdapter.setDropDownViewResource(R.layout.licence_dropdown_view)
        entryLicence.adapter = licenceAdapter

        entryLicence.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedLicenceIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedLicenceIndex = 0
            }
        }
        entryLicence.setSelection(index, true)
    }


    override fun setContentEntry(contentEntry: ContentEntry) {
        entryTitle.setText(contentEntry.title)
        toolbar.title = contentEntry.title

        entryDescription.setText(contentEntry.description)

        this.thumbnailUrl = contentEntry.thumbnailUrl

        if (this.thumbnailUrl != null && (this.thumbnailUrl as String).isNotEmpty()) {
            Picasso.get().load(thumbnailUrl).into(entryThumbnail)
        }

        inActiveSwitch.isChecked = !contentEntry.ceInactive
        visibilitySwitch.isChecked = contentEntry.publik
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::selectedFile.isInitialized){
            outState.putSerializable(SELECTED_FILE, selectedFile)
        }
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
        rootView.findViewById<View>(R.id.storage_option_label).visibility = visibility

    }


    override fun showImageSelector(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        rootView.findViewById<View>(R.id.image_selector_holder).visibility = visibility
    }

    override fun setUpStorageOption(storageDirs: List<UMStorageDir>) {

        val storageOptions = ArrayList<String>()
        for (umStorageDir in storageDirs) {
            val deviceStorageLabel = String.format(impl.getString(
                    MessageID.download_storage_option_device, context as Any), umStorageDir.name,
                    UMFileUtil.formatFileSize(File(umStorageDir.dirURI as String).usableSpace))
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
        val errorTxt = rootView.findViewById<TextView>(R.id.file_not_supported)
        errorTxt.visibility = visibility
        errorTxt.text = message
    }


    override fun showMessageAndDismissDialog(message: String?, document: Boolean) {
        dismissDialog()
        if (actionListener != null) {

            if (document)
                actionListener!!.updateDocument(entryTitle.text.toString(),
                        entryDescription.text.toString())

            if (message != null)
                actionListener!!.showSnackMessage(message)
        }
    }

    fun checkIfIsSupportedFile(file: File) {
        this.selectedFile = file
        val content = getContent(selectedFile)
        if (::optionsActivity.isInitialized && optionsActivity.importDialog.isShowing) {
            isDoneBtnDisabled = false
            optionsActivity.importDialog.dismiss()
        }
        presenter!!.handleSelectedFile(selectedFile.absolutePath, selectedFile.length(),
                if(content.containsKey(CONTENT_MIMETYPE)) content[CONTENT_MIMETYPE] as String else null,
                if(content.containsKey(CONTENT_ENTRY)) content[CONTENT_ENTRY] as ContentEntry else null)
    }

    override fun dismissDialog() {
        dismiss()
    }


    private suspend fun importFile(baseDir:String, mimeType: String, contentEntry: ContentEntry): ContentEntry{
        return importContentEntryFromFile(activity!!, contentEntry,mimeType,baseDir,selectedFile)
    }

    override fun showAddThumbnailMessage() {
        Toast.makeText(activity, getString(R.string.content_folder_image_message),
                Toast.LENGTH_LONG).show()
    }

    override fun showProgressDialog() {
        isDoneBtnDisabled = true
    }

    companion object {

        private const val SELECTED_FILE = "selected_file"
    }

}
