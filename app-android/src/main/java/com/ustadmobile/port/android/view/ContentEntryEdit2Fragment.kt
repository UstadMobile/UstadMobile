package com.ustadmobile.port.android.view

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat
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
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryAddOptionsView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.sharedse.contentformats.ImportedContentEntryMetaData
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.importContainerFromZippedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


interface ContentEntryEdit2FragmentEventHandler {

    fun onEntryPublicStatusChanged(isChecked:Boolean)

    fun onClickContentImportSourceSelection()

    fun handleClickLanguage()
}

class ContentEntryEdit2Fragment: UstadEditFragment<ContentEntryWithLanguage>(), ContentEntryEdit2View, ContentEntryEdit2FragmentEventHandler, DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    private var entryMetaData: ImportedContentEntryMetaData? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithLanguage>?
        get() = mPresenter


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

    override var selectedStorageIndex: Int = 0
        get() = field
        set(value) {
            field = value
            mBinding?.selectedStorageIndex = value
        }
    override var jobTimeStamp: Long
        get() = System.currentTimeMillis()
        set(value) {}

    override fun formatLabel(storage: UMStorageDir): String {
        return String.format(UstadMobileSystemImpl.instance.getString(
                MessageID.download_storage_option_device, context as Any), storage.name,
                UMFileUtil.formatFileSize(File(storage.dirURI as String).usableSpace))
    }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            mBinding?.fieldsEnabled = value
            field = value
        }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {}

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {}

    override fun onEntryPublicStatusChanged(isChecked: Boolean) {
        mBinding?.contentEntry?.publik = isChecked
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        (activity as MainActivity).showSnackBar(message, action, actionMessageId)
    }


    override fun onClickContentImportSourceSelection() {
        onSaveStateToBackStackStateHandle()
        val builder:AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setItems(R.array.content_source_option) { dialog, which ->
            when (which) {
                0 -> handleFileSelection()
                1 -> {
                    //Handle link import here
                }
            }
            dialog.dismiss()
        }
        builder.show()

    }

    private fun handleFileSelection(){
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
                    GlobalScope.launch {
                        val metaData = extractContentEntryMetadataFromFile(tmpFile.absoluteFile,
                                UmAccountManager.getActiveDatabase(requireContext()))
                        entryMetaData = metaData
                        if(entryMetaData == null){
                            showSnackBar(getString(R.string.import_link_content_not_supported))
                        }
                        val entry = entryMetaData?.contentEntry
                        val entryUid = arguments?.get(ARG_ENTITY_UID)
                        if(entry != null){
                            if(entryUid != null){
                                entry.contentEntryUid = entryUid.toString().toLong()
                            }
                            entity = entry
                        }
                    }
                }catch(e: Exception) {
                    e.printStackTrace()
                }
            }

        }.launch("*/*")
    }

    override fun handleClickLanguage() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Language::class.java, R.id.language_list_dest)
    }


    override fun setUpStorageOptions(storageOptions: List<UMStorageDir>) {
        val options = ArrayList<String>()
        storageOptions.forEach {
            val deviceStorageLabel = String.format(UstadMobileSystemImpl.instance.getString(
                    MessageID.download_storage_option_device, context as Any), it.name,
                    UMFileUtil.formatFileSize(File(it.dirURI).usableSpace))
            options.add(deviceStorageLabel)
        }

        mBinding?.storageOptions = options
    }

    override suspend fun saveContainerOnExit(entryUid: Long, selectedBaseDir: String,db: UmAppDatabase, repo: UmAppDatabase): Container ?{
        val file = entryMetaData?.file
        return if(file != null){
            importContainerFromZippedFile(entryUid,entryMetaData?.mimeType,selectedBaseDir,file,db,repo)
        }else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContentEntryEdit2Binding.inflate(inflater, container, false).also {
            val contentType = arguments?.get(ContentEntryEdit2View.CONTENT_TYPE)
            rootView = it.root
            it.licenceSelectionListener = this
            it.activityEventHandler = this
            it.isNewFolder = if(contentType != null) contentType.toString().toInt() == CONTENT_CREATE_FOLDER else false
            it.supportedFiles = HtmlCompat.fromHtml(getString(R.string.content_supported_files),HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
            it.contentEntry?.lastModified = System.currentTimeMillis()
            it.contentEntry?.ceInactive = true
            it.contentEntry?.leaf = !it.isNewFolder
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        GlobalScope.launch {
            val thisFrag = this@ContentEntryEdit2Fragment
            val networkManagerBle = (activity as MainActivity).networkManagerBle.await()
            withContext(Dispatchers.Main){
                mPresenter = ContentEntryEdit2Presenter(requireContext(), arguments.toStringMap(), thisFrag,
                        thisFrag, UstadMobileSystemImpl.instance,
                        UmAccountManager.getActiveDatabase(requireContext()),
                        UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                        networkManagerBle.containerDownloadManager,
                        UmAccountManager.activeAccountLiveData)

                mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

                navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                        Language::class.java) {
                    val language = it.firstOrNull() ?: return@observeResult
                    entity?.language = language
                    entity?.primaryLanguageUid = language.langUid
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        entryMetaData = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle( R.string.content)
    }

}