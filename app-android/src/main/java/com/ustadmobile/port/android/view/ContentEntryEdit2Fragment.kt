package com.ustadmobile.port.android.view

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryEdit2Binding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.createTempFileForDestination
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.sharedse.contentformats.*
import kotlinx.android.synthetic.main.fragment_content_entry_edit2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import org.kodein.di.on


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickContentImportSourceSelection()

    fun handleClickLanguage()
}

class ContentEntryEdit2Fragment(private val registry: ActivityResultRegistry? = null) : UstadEditFragment<ContentEntryWithLanguage>(), ContentEntryEdit2View, ContentEntryEdit2FragmentEventHandler {

    private var mBinding: FragmentContentEntryEdit2Binding? = null

    private var mPresenter: ContentEntryEdit2Presenter? = null

    override var entryMetaData: ImportedContentEntryMetaData? = null

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


    override var selectedFileUri: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.fileImportInfoVisibility = if (selectedFileUri != null)
                View.VISIBLE else View.GONE
            mBinding?.selectedFileUri = value
        }

    override var selectedUrl: String? = null

    override var titleErrorEnabled: Boolean = false
        set(value) {
            entry_title.error = getString(R.string.field_required_prompt)
            mBinding?.titleErrorEnabled = value
            field = value
        }

    override var fileImportErrorVisible: Boolean = false
        set(value) {
            val typedVal = TypedValue()
            requireActivity().theme.resolveAttribute(if (value) R.attr.colorError
            else R.attr.colorOnSurface, typedVal, true)
            mBinding?.fileImportInfoVisibility = if (value) View.VISIBLE else View.GONE
            mBinding?.importErrorColor = typedVal.data
            mBinding?.isImportError = value
            field = value
        }

    override var storageOptions: List<UMStorageDir>? = null
        set(value) {
            mBinding?.storageOptions = value
            field = value
        }


    override var fieldsEnabled: Boolean = false
        set(value) {
            mBinding?.fieldsEnabled = value
            field = value
        }

    override fun onClickContentImportSourceSelection() {
        onSaveStateToBackStackStateHandle()
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setItems(R.array.content_source_option) { dialog, which ->
            when (which) {
                0 -> handleFileSelection()
                1 -> handleLinkSelection()
            }
            dialog.dismiss()
        }
        builder.show()

    }



    internal fun handleFileSelection() {
        registerForActivityResult(ActivityResultContracts.GetContent(),
                registry ?: requireActivity().activityResultRegistry) { uri: Uri? ->
            if (uri != null) {
                try {
                    val input = requireContext().contentResolver.openInputStream(uri)
                    val tmpFile = findNavController().createTempFileForDestination(requireContext(),
                            "import-${System.currentTimeMillis()}.${UMFileUtil.getExtension(uri.toString())}")
                    val output = tmpFile.outputStream()
                    input?.copyTo(tmpFile.outputStream())
                    output.flush()
                    output.close()
                    input?.close()
                    GlobalScope.launch {
                        val accountManager: UstadAccountManager by instance()
                        val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_DB)
                        val metaData = extractContentEntryMetadataFromFile(tmpFile.toURI().toString(), db)
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
                        if (entry != null) {
                            if (entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                            fileImportErrorVisible = false
                            entity = entry
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }.launch("*/*")
    }

    override fun handleClickLanguage() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Language::class.java, R.id.language_list_dest)
    }


    private fun handleLinkSelection() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(ImportedContentEntryMetaData::class.java, R.id.import_link_view)
    }


    override suspend fun saveContainerOnExit(entryUid: Long, selectedBaseDir: String, db: UmAppDatabase, repo: UmAppDatabase): Container? {
        val file = entryMetaData?.uri
        val importMode = entryMetaData?.importMode
        val container = if (file != null && importMode != null) {
            withContext(Dispatchers.IO) {
                importContainerFromFile(entryUid, entryMetaData?.mimeType, selectedBaseDir, file, db, repo, importMode, requireContext())
            }
        } else null
        loading = true
        return container
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContentEntryEdit2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fileImportInfoVisibility = View.GONE
            it.activityEventHandler = this
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        ustadFragmentTitle = getString(R.string.content)

        mPresenter = ContentEntryEdit2Presenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
        navController.currentBackStackEntry?.savedStateHandle?.observeResult(viewLifecycleOwner,
                Language::class.java) {
            val language = it.firstOrNull() ?: return@observeResult
            entity?.language = language
            entity?.primaryLanguageUid = language.langUid
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ImportedContentEntryMetaData::class.java) {
            val metadata = it.firstOrNull() ?: return@observeResult
            // back from navigate import
            entryMetaData = metadata
            when (entryMetaData) {
                null -> {
                    showSnackBar(getString(R.string.import_link_content_not_supported))
                }
                else -> {
                    selectedUrl = metadata.uri
                }
            }
            val entry = entryMetaData?.contentEntry
            val entryUid = arguments?.get(ARG_ENTITY_UID)
            if (entry != null) {
                if (entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                entity = entry
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