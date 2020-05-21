package com.ustadmobile.port.android.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryEdit2Binding
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryAddOptionsView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryAddOptionsView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.core.view.ContentEntryAddOptionsView.Companion.CONTENT_IMPORT_LINK
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil
import com.ustadmobile.port.sharedse.contentformats.ImportedContentEntryMetaData
import kotlinx.android.synthetic.main.fragment_content_entry_edit2.view.*
import java.io.File
import java.util.*

interface ContentEntryEdit2FragmentEventHandler {

    fun onEntryPublicStatusChanged(isChecked:Boolean)

    fun onClickFileSelection()

    fun handleClickLanguage()
}

class ContentEntryEdit2Fragment: UstadEditFragment<ContentEntryWithLanguage>(), ContentEntryEdit2View, ContentEntryEdit2FragmentEventHandler, DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    private var entryMetaData: ImportedContentEntryMetaData? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithLanguage>?
        get() = mPresenter

    lateinit var rootView: View

    private lateinit var selectedBaseDir: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentContentEntryEdit2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.licenceSelectionListener = this
            it.activityEventHandler = this
            it.isNewFolder = arguments?.get(ContentEntryEdit2View.CONTENT_TYPE).toString().toInt() == CONTENT_CREATE_FOLDER
            it.supportedFiles = HtmlCompat.fromHtml(getString(R.string.content_supported_files),HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
            it.showFileImport = arguments?.get(ContentEntryEdit2View.CONTENT_TYPE).toString().toInt() == CONTENT_IMPORT_FILE
            it.showLinkImport = arguments?.get(ContentEntryEdit2View.CONTENT_TYPE).toString().toInt() == CONTENT_IMPORT_LINK
            it.contentEntry?.lastModified = System.currentTimeMillis()
            it.contentEntry?.ceInactive = true
            it.isFileNotSupported = false
            it.contentEntry?.leaf = arguments?.get(ContentEntryEdit2View.CONTENT_ENTRY_LEAF).toString().toBoolean()
        }

        mPresenter = ContentEntryEdit2Presenter(requireContext(), arguments.toStringMap(), this,
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
        setEditFragmentTitle( R.string.content)
    }

    override var entity: ContentEntryWithLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
        }
    override var licenceOptions: List<ContentEntryEdit2Presenter.LicenceMessageIdOptions>? = null
        set(value) {
            field = value
            mBinding?.licenceOptions = value
        }

    override fun formatLabel(storage: UMStorageDir): String {
        return String.format(UstadMobileSystemImpl.instance.getString(
                MessageID.download_storage_option_device, context as Any), storage.name,
                UMFileUtil.formatFileSize(File(storage.dirURI as String).usableSpace))
    }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {}

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {}

    override fun onEntryPublicStatusChanged(isChecked: Boolean) {
        mBinding?.contentEntry?.publik = isChecked
    }

    override fun onClickFileSelection() {
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            mBinding?.selectedFileUri = uri.toString()
            if(uri != null){
               try{
                   val input = requireContext().contentResolver.openInputStream(uri)
                   val tmpFile = findNavController().createTempFileForDestination(requireContext(),
                           "import-${System.currentTimeMillis()}")
                   val output = tmpFile.outputStream()
                   input?.copyTo(tmpFile.outputStream())
                   output.flush()
                   output.close()
                   input?.close()
                   mBinding?.selectedFileUri = uri.toString()
                   entryMetaData = ContentTypeUtil.extractContentEntryMetadataFromFile(tmpFile.absoluteFile)
                   mBinding?.isFileNotSupported = entryMetaData == null
                   val entry = entryMetaData?.contentEntry
                   if(entry != null){
                       entity = ContentEntryWithLanguage(entry)
                       mBinding?.contentEntry = entity
                   }
               }catch(e: Exception) {
                   e.printStackTrace()
               }
            }

        }.launch("application/*")
    }

    override fun handleClickLanguage() {
        TODO("Not yet implemented")
    }


    override fun setUpStorageOptions(storageOptions: List<UMStorageDir>) {
        val options = ArrayList<String>()
        selectedBaseDir = storageOptions.first().dirURI
        storageOptions.forEach {
            val deviceStorageLabel = String.format(UstadMobileSystemImpl.instance.getString(
                    MessageID.download_storage_option_device, context as Any), it.name,
                    UMFileUtil.formatFileSize(File(it.dirURI).usableSpace))
            options.add(deviceStorageLabel)
        }

        val storageOptionAdapter = ArrayAdapter(Objects.requireNonNull<FragmentActivity>(activity),
                R.layout.licence_dropdown_selected_view, options)
        storageOptionAdapter.setDropDownViewResource(R.layout.licence_dropdown_view)
        rootView.container_storage_option.setAdapter(storageOptionAdapter)
        rootView.container_storage_option.setOnItemClickListener { adapterView, view, i, l ->
            run {
                selectedBaseDir = storageOptions[i].dirURI
            }
        }
    }

    override suspend fun saveContainerOnExit(entryUid: Long, db: UmAppDatabase, repo: UmAppDatabase) {
        ContentTypeUtil.importContainerFromZippedFile(entryUid,entryMetaData?.mimeType,selectedBaseDir,File(""),db,repo)
    }

}