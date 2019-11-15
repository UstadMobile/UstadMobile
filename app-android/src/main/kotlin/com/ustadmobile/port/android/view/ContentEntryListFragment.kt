package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.ext.activeRange
import com.ustadmobile.port.android.view.ext.makeSnackbarIfRequired
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.Runnable
import java.util.concurrent.atomic.AtomicReference


/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the []
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class ContentEntryListFragment : UstadBaseFragment(), ContentEntryListFragmentView,
        ContentEntryListRecyclerViewAdapter.AdapterViewListener,
        ContentEntryListRecyclerViewAdapter.EmptyStateListener {


    override val viewContext: Any
        get() = context!!

    private var entryListPresenter: ContentEntryListFragmentPresenter? = null

    private lateinit var recyclerView: RecyclerView

    private var contentEntryListener: ContentEntryListener? = null

    private lateinit var ustadBaseActivity: UstadBaseActivity

    private lateinit var managerAndroidBle: NetworkManagerBle

    private var recyclerAdapter: ContentEntryListRecyclerViewAdapter? = null

    private var savedInstanceState: Bundle? = null

    private lateinit var rootContainer: View

    private var emptyViewHolder: RelativeLayout? = null

    internal class LocalAvailabilityPagedListCallback(val localAvailabilityManager: LocalAvailabilityManager,
                                                      var pagedList: PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?,
                                                      val onEntityAvailabilityChanged: (Map<Long, Boolean>) -> Unit) : PagedList.Callback() {

        val availabilityMonitorRequest = AtomicReference<AvailabilityMonitorRequest?>()

        val activeRange = AtomicReference(Pair(-1, -1))

        override fun onChanged(position: Int, count: Int) {
            handleActiveRangeChanged()
        }

        override fun onInserted(position: Int, count: Int) {
            handleActiveRangeChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            handleActiveRangeChanged()
        }

        private fun handleActiveRangeChanged() {
            val currentPagedList = pagedList
            val currentActiveRange = currentPagedList?.activeRange()
            if(currentPagedList != null && currentActiveRange != null
                    && !activeRange.compareAndSet(currentActiveRange, currentActiveRange)) {
                val containerUidsToMonitor = (currentActiveRange.first .. currentActiveRange.second-1)
                        .fold(mutableListOf<Long>(), {uidList, index ->
                            val contentEntry = currentPagedList.get(index)
                            if(contentEntry != null && contentEntry.leaf) {
                                val mostRecentContainerUid = contentEntry.mostRecentContainer?.containerUid ?: 0L
                                if(mostRecentContainerUid != 0L) {
                                    uidList += mostRecentContainerUid
                                }
                            }
                            uidList
                        })
                val newRequest = if(containerUidsToMonitor.isNotEmpty()) {
                    AvailabilityMonitorRequest(containerUidsToMonitor, onEntityAvailabilityChanged)
                }else {
                    null
                }
                val oldRequest = availabilityMonitorRequest.getAndSet(newRequest)
                if(oldRequest != null) {
                    localAvailabilityManager.removeMonitoringRequest(oldRequest)
                }

                if(newRequest != null) {
                    localAvailabilityManager.addMonitoringRequest(newRequest)
                }
            }
        }

        fun onDestroy() {
            val currentRequest = availabilityMonitorRequest.getAndSet(null)
            if(currentRequest != null){
                localAvailabilityManager.removeMonitoringRequest(currentRequest)
            }
        }
    }


    private var listSnapShot: List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?> = listOf()

    private var localAvailabilityPagedListCallback: LocalAvailabilityPagedListCallback? = null

    fun filterByLang(langUid: Long) {
        entryListPresenter!!.handleClickFilterByLanguage(langUid)
    }

    fun filterBySchemaCategory(contentCategoryUid: Long) {
        entryListPresenter!!.handleClickFilterByCategory(contentCategoryUid)
    }

    fun clickUpNavigation() {
        entryListPresenter!!.handleUpNavigation()
    }

    override fun setCategorySchemaSpinner(spinnerData: Map<Long, List<DistinctCategorySchema>>) {
        runOnUiThread(Runnable {
            if (contentEntryListener != null) {
                // TODO tell activiity to create the spinners
                contentEntryListener!!.setFilterSpinner(spinnerData)
            }
        })
    }

    override fun setLanguageOptions(result: List<Language>) {
        runOnUiThread(Runnable{
            if (contentEntryListener != null) {
                contentEntryListener!!.setLanguageFilterSpinner(result)
            }
        })
    }


    interface ContentEntryListener {
        fun setTitle(title: String)

        fun setFilterSpinner(idToValuesMap: Map<Long, List<DistinctCategorySchema>>)

        fun setLanguageFilterSpinner(result: List<Language>)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootContainer = inflater.inflate(R.layout.fragment_contententry_list, container, false)
        this.savedInstanceState = savedInstanceState

        // Set the adapter
        val context = rootContainer.context
        recyclerView = rootContainer.findViewById(R.id.content_entry_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        emptyViewHolder = rootContainer.findViewById(R.id.emptyView)

        val emptyViewImage :ImageView = rootContainer.findViewById(R.id.emptyViewImage)
        val emptyViewText: TextView = rootContainer.findViewById(R.id.emptyViewText)

        val isDownloadedSection = bundleToMap(arguments).containsKey(ARG_DOWNLOADED_CONTENT)

        val labelText = UstadMobileSystemImpl.instance.getString(
                if (isDownloadedSection) MessageID.empty_state_downloaded
                else MessageID.empty_state_libraries,
                context)

        val resource = if (isDownloadedSection)
            R.drawable.ic_file_download_black_24dp
        else
            R.drawable.ic_folder_black_24dp
        emptyViewImage.setImageResource(resource)
        emptyViewText.text = labelText

        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)
        checkReady()

        return rootContainer
    }


    private fun showSnackbarPromptsIfRequired() {
        val currentContext = context
        if(currentContext != null && ::managerAndroidBle.isInitialized
                && ::rootContainer.isInitialized){
            managerAndroidBle.enablePromptsSnackbarManager.makeSnackbarIfRequired(rootContainer,
                    currentContext)
        }
    }

    override fun onAttach(context: Context?) {
        if (context is UstadBaseActivity) {
            this.ustadBaseActivity = context
            ustadBaseActivity.runAfterServiceConnection(Runnable{
                ustadBaseActivity.runOnUiThread {
                    managerAndroidBle = ustadBaseActivity.networkManagerBle!!
                    checkReady()
                    showSnackbarPromptsIfRequired()
                }
            })
        }

        if (context is ContentEntryListener) {
            this.contentEntryListener = context
        }

        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        if(::managerAndroidBle.isInitialized) {
            showSnackbarPromptsIfRequired()
        }
    }

    private fun checkReady() {
        if (entryListPresenter == null &&  ::managerAndroidBle.isInitialized
                && ::rootContainer.isInitialized) {
            //create entry adapter here to make sure bleManager is not null
            recyclerAdapter = ContentEntryListRecyclerViewAdapter(activity!!, this,
                    managerAndroidBle, this)
            recyclerAdapter!!.addListeners()

            localAvailabilityPagedListCallback = LocalAvailabilityPagedListCallback(
                    managerAndroidBle.localAvailabilityManager, null) { availabilityMap ->
                runOnUiThread(Runnable { recyclerAdapter?.updateLocalAvailability(availabilityMap) })
            }

            val umRepoDb = UmAccountManager.getRepositoryForActiveAccount(activity!!)
            entryListPresenter = ContentEntryListFragmentPresenter(context as Context,
                    bundleToMap(arguments), this, umRepoDb.contentEntryDao)
            entryListPresenter!!.onCreate(bundleToMap(savedInstanceState))
        }
    }

    override fun onDetach() {
        super.onDetach()
        this.contentEntryListener = null
    }


    override fun setContentEntryProvider(entryProvider: DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>) {
        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(context!!)
                .contentEntryDaoBoundaryCallbacks.getChildrenByParentUidWithCategoryFilter(entryProvider)
        val data = LivePagedListBuilder(entryProvider, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()

        data.observe(this, Observer<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>> {
            recyclerAdapter!!.submitList(it)
            localAvailabilityPagedListCallback!!.pagedList = it
            it.addWeakCallback(listSnapShot, localAvailabilityPagedListCallback!!)
        })

        recyclerView.adapter = recyclerAdapter
    }

    override fun setToolbarTitle(title: String) {
        runOnUiThread(Runnable {
            if (contentEntryListener != null)
                contentEntryListener!!.setTitle(title)
        })
    }

    override fun showError() {
        Toast.makeText(context, R.string.content_entry_not_found, Toast.LENGTH_SHORT).show()
    }


    override fun contentEntryClicked(entry: ContentEntry?) {
        runOnUiThread(Runnable {
            if (entryListPresenter != null) {
                entryListPresenter!!.handleContentEntryClicked(entry!!)
            }
        })
    }

    override fun downloadStatusClicked(entry: ContentEntry?) {
        val impl = UstadMobileSystemImpl.instance
        if(::ustadBaseActivity.isInitialized){
            ustadBaseActivity.runAfterGrantingPermission(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Runnable { entryListPresenter!!.handleDownloadStatusButtonClicked(entry!!) },
                    impl.getString(MessageID.download_storage_permission_title, context!!),
                    impl.getString(MessageID.download_storage_permission_message, context!!))
        }
    }

    override fun onEntriesLoaded() {
        emptyViewHolder!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerAdapter?.removeListeners()
        localAvailabilityPagedListCallback?.onDestroy()
    }

    companion object {

        fun newInstance(args: Bundle): ContentEntryListFragment {
            val fragment = ContentEntryListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
