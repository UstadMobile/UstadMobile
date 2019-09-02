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
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


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
        ContentEntryListRecyclerViewAdapter.AdapterViewListener, LocalAvailabilityMonitor,
        ContentEntryListRecyclerViewAdapter.EmptyStateListener {

    override val viewContext: Any
        get() = context!!

    private var entryListPresenter: ContentEntryListFragmentPresenter? = null

    private var recyclerView: RecyclerView? = null

    private var contentEntryListener: ContentEntryListener? = null

    private lateinit var ustadBaseActivity: UstadBaseActivity

    private lateinit var managerAndroidBle: NetworkManagerBle

    private var recyclerAdapter: ContentEntryListRecyclerViewAdapter? = null

    private var savedInstanceState: Bundle? = null

    private var rootContainer: View? = null

    private var emptyViewHolder: RelativeLayout? = null

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
        val context = rootContainer!!.context
        recyclerView = rootContainer!!.findViewById(R.id.content_entry_list)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        emptyViewHolder = rootContainer!!.findViewById(R.id.emptyView)

        val emptyViewImage :ImageView = rootContainer!!.findViewById(R.id.emptyViewImage)
        val emptyViewText: TextView = rootContainer!!.findViewById(R.id.emptyViewText)

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
        recyclerView!!.addItemDecoration(dividerItemDecoration)
        checkReady()

        return rootContainer
    }


    override fun onAttach(context: Context?) {
        if (context is UstadBaseActivity) {
            this.ustadBaseActivity = context
            ustadBaseActivity.runAfterServiceConnection(Runnable{
                ustadBaseActivity.runOnUiThread {
                    managerAndroidBle = ustadBaseActivity.networkManagerBle!!
                    checkReady()
                }
            })
        }

        if (context is ContentEntryListener) {
            this.contentEntryListener = context
        }

        super.onAttach(context)
    }

    private fun checkReady() {
        if (entryListPresenter == null && rootContainer != null && ::managerAndroidBle.isInitialized) {
            //create entry adapter here to make sure bleManager is not null
            recyclerAdapter = ContentEntryListRecyclerViewAdapter(activity!!, this, this,
                    managerAndroidBle)
            recyclerAdapter!!.addListeners()
            recyclerAdapter!!.setEmptyStateListener(this)

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

        val data = LivePagedListBuilder(entryProvider, 20).build()
        data.observe(this, Observer<PagedList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>> { recyclerAdapter!!.submitList(it) })

        recyclerView!!.adapter = recyclerAdapter
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

    override fun startMonitoringAvailability(monitor: Any, entryUidsToMonitor: List<Long>) {
        GlobalScope.launch {
            if (::managerAndroidBle.isInitialized) {
                managerAndroidBle.startMonitoringAvailability(monitor, entryUidsToMonitor)
            }
        }
    }

    override fun stopMonitoringAvailability(monitor: Any) {
        if (::managerAndroidBle.isInitialized) {
            managerAndroidBle.stopMonitoringAvailability(monitor)
        }
    }

    override fun onStop() {
        stopMonitoringAvailability(this)
        super.onStop()
    }

    override fun onEntriesLoaded() {
        emptyViewHolder!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (recyclerAdapter != null)
            recyclerAdapter!!.removeListeners()
    }

    companion object {

        fun newInstance(args: Bundle): ContentEntryListFragment {
            val fragment = ContentEntryListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
