package com.ustadmobile.port.android.view

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
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
import kotlinx.android.synthetic.main.fragment_content_entry_edit2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickContentImportSourceSelection()

    fun handleClickLanguage()
}

class ContentEntryEdit2Fragment: UstadEditFragment<ContentEntryWithLanguage>(), ContentEntryEdit2View, ContentEntryEdit2FragmentEventHandler{

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    private var entryMetaData: ImportedContentEntryMetaData? = null

    override val mEditPresenter: UstadEditPresenter<*, ContentEntryWithLanguage>?
        get() = mPresenter


    override var entity: ContentEntryWithLanguage? = null
        get() = field
        set(value) {
            field = value
            mBinding?.viewVisibility = if(value != null && value.leaf) View.VISIBLE else View.GONE
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
    override var selectedFileUri: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.fileImportInfoVisibility = if(selectedFileUri != null)
                View.VISIBLE else View.GONE
            mBinding?.selectedFileUri = value
        }

    override var titleErrorEnabled: Boolean = false
        set(value) {
            entry_title.error = getString(R.string.field_required_prompt)
            mBinding?.titleErrorEnabled = value
            field = value
        }

    override var fileImportErrorVisible: Boolean = false
        set(value) {
            mBinding?.fileImportInfoVisibility = if(value) View.VISIBLE else View.GONE
            mBinding?.isImportError = value
            field = value
        }

    override fun formatStorageOptionLabel(storage: UMStorageDir): String {
        return String.format(UstadMobileSystemImpl.instance.getString(
                MessageID.download_storage_option_device, context as Any), storage.name,
                UMFileUtil.formatFileSize(File(storage.dirURI).usableSpace))
    }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            mBinding?.fieldsEnabled = value
            field = value
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
                        when (entryMetaData) {
                            null -> {
                                showSnackBar(getString(R.string.import_link_content_not_supported))
                            }
                            else -> {
                                selectedFileUri = uri.toString()
                            }
                        }
                        val entry = entryMetaData?.contentEntry
                        val entryUid = arguments?.get(ARG_ENTITY_UID)
                        if(entry != null){
                            if(entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                            fileImportErrorVisible = false
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
        mBinding?.storageOptions = storageOptions.map {
            String.format(UstadMobileSystemImpl.instance.getString(
                    MessageID.download_storage_option_device, context as Any), it.name,
                    UMFileUtil.formatFileSize(File(it.dirURI).usableSpace))
        }
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
            rootView = it.root
            it.fileImportInfoVisibility = View.GONE
            it.activityEventHandler = this
            it.viewVisibility = View.GONE
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        title = getString(R.string.content)

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

}