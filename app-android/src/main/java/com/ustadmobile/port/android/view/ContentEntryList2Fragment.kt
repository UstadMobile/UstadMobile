package com.ustadmobile.port.android.view

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_TITLE
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.view.ext.runAfterRequestingPermissionIfNeeded
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.sharedse.view.DownloadDialogView

class ContentEntryList2Fragment : UstadListViewFragment<ContentEntry, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>(),
        ContentEntryList2View, View.OnClickListener, FragmentBackHandler{

    private var mPresenter: ContentEntryList2Presenter? = null

    override val listPresenter: UstadListPresenter<*, in ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
        get() = mPresenter




    override fun onHostBackPressed() = mPresenter?.handleOnBackPressed() ?: false

    override fun showDownloadDialog(args: Map<String, String>) {
        runAfterRequestingPermissionIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            UstadMobileSystemImpl.instance.go(DownloadDialogView.VIEW_NAME, args, requireContext())
        }
    }

    override var title: String? = null
        set(value) {
            ustadFragmentTitle = value
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val mTitle = arguments?.get(ARG_PARENT_ENTRY_TITLE)
        if(mTitle != null){
            ustadFragmentTitle = mTitle.toString()
        }
        mPresenter = ContentEntryList2Presenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ContentEntryListRecyclerAdapter(mPresenter,
                arguments?.get(UstadView.ARG_LISTMODE).toString())
        val createNewText = requireContext().getString(R.string.add_a_new,
                requireContext().getString(R.string.content_editor_create_new_title))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }



    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                getString(R.string.content_editor_create_new_title)
    }

    override fun showContentEntryAddOptions(parentEntryUid: Long) {
        val entryAddOption = ContentEntryAddOptionsBottomSheetFragment()
        entryAddOption.arguments = UMAndroidUtil.mapToBundle(mapOf(UstadView.ARG_PARENT_ENTRY_UID
                to parentEntryUid.toString()))
        entryAddOption.show(childFragmentManager, entryAddOption.tag)

    }

    /**
     * OnClick function that will handle
     * when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickCreateNewFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.contentEntryDao



    companion object {

        @JvmField
        val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
                ContentEntry.EBOOK_TYPE to R.drawable.ic_book_black_24dp,
                ContentEntry.VIDEO_TYPE to R.drawable.video_youtube,
                ContentEntry.DOCUMENT_TYPE to R.drawable.text_doc_24px,
                ContentEntry.ARTICLE_TYPE to R.drawable.article_24px,
                ContentEntry.COLLECTION_TYPE to R.drawable.collections_24px,
                ContentEntry.INTERACTIVE_EXERICSE_TYPE to 0,
                ContentEntry.AUDIO_TYPE to R.drawable.ic_audiotrack_24px
        )

        @JvmField
        val CONTENT_ENTRY_TYPE_LABEL_MAP = mapOf(
                ContentEntry.EBOOK_TYPE to MessageID.ebook,
                ContentEntry.VIDEO_TYPE to MessageID.video,
                ContentEntry.DOCUMENT_TYPE to MessageID.document,
                ContentEntry.ARTICLE_TYPE to MessageID.article,
                ContentEntry.COLLECTION_TYPE to MessageID.collection,
                ContentEntry.INTERACTIVE_EXERICSE_TYPE to MessageID.interactive,
                ContentEntry.AUDIO_TYPE to MessageID.audio
        )

        val DIFF_CALLBACK: DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = object
            : DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>() {
            override fun areItemsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                         newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem.contentEntryUid == newItem.contentEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                            newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem == newItem
            }
        }
    }
}