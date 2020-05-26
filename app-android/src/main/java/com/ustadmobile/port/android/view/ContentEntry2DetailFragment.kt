package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntry2DetailBinding
import com.ustadmobile.core.controller.ContentEntry2DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import kotlinx.android.synthetic.main.fragment_content_entry2_detail.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


interface ContentEntryDetailFragmentEventHandler {
    fun handleDescription(description:String?): Spanned?

    fun handleFileSize(fileSize: Long): String

    fun handleOnClickOpenDownloadButton()
}

class ContentEntry2DetailFragment: UstadDetailFragment<ContentEntryWithMostRecentContainer>(), ContentEntry2DetailView, ContentEntryDetailFragmentEventHandler, ContentEntryDetailLanguageAdapter.AdapterViewListener {

    private var mBinding: FragmentContentEntry2DetailBinding? = null

    private var mPresenter: ContentEntry2DetailPresenter? = null

    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
        }

    override fun showFeedbackMessage(message: String, actionMessageId: Int, action: () -> Unit) {
        (activity as MainActivity).showFeedbackMessage(message, actionMessageId, action)
    }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }
    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun handleDescription(description: String?): Spanned? {
        return if(description == null) null else HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun handleFileSize(fileSize: Long): String {
        return UMFileUtil.formatFileSize(fileSize)
    }

    override fun handleOnClickOpenDownloadButton() {
        mPresenter?.handleOnClickOpenDownloadButton()
    }

    override fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>?) {
        if(result != null){
            val adapter = ContentEntryDetailLanguageAdapter(result, this@ContentEntry2DetailFragment)
            mBinding?.translationAdapter = adapter
        }
        mBinding?.showTranslation = result!= null && result.isNotEmpty()
    }

    override fun navigateToTranslation(entryUid: Long) {
        arguments?.putString(ARG_ENTITY_UID,entryUid.toString())
        findNavController().navigate(R.id.content_entry_details_dest, arguments)
    }

    override fun showDownloadOptionsDialog(map: Map<String, String>) {
        //TODO("Not yet implemented")
    }

    override fun selectContentEntryOfLanguage(contentEntryUid: Long) {
       mPresenter?.handleOnTranslationClicked(contentEntryUid)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContentEntry2DetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentEventHandler = this
        }

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        rootView.entry_detail_flex.layoutManager = flexboxLayoutManager
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch {
            val networkManagerBle = (activity as MainActivity).networkManagerBle.await()
            val thisFrag = this@ContentEntry2DetailFragment
            withContext(Dispatchers.Main){
                mPresenter = ContentEntry2DetailPresenter(requireContext(), arguments.toStringMap(), thisFrag,
                        thisFrag, UstadMobileSystemImpl.instance, true,
                        UmAccountManager.getActiveDatabase(requireContext()),
                        UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                        networkManagerBle.containerDownloadManager,
                        UmAccountManager.activeAccountLiveData, ::goToContentEntry)
                mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }
}