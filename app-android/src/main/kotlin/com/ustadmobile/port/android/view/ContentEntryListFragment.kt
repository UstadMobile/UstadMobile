package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle
import kotlinx.coroutines.Runnable


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
class ContentEntryListFragment : UstadBaseFragment(), ContentEntryListFragmentView, ContentEntryListRecyclerViewAdapter.AdapterViewListener, LocalAvailabilityMonitor {
    override val viewContext: Any
        get() = context!!

    private var entryListPresenter: ContentEntryListFragmentPresenter? = null

    private var recyclerView: RecyclerView? = null

    private var contentEntryListener: ContentEntryListener? = null

    private var ustadBaseActivity: UstadBaseActivity? = null

    private var managerAndroidBle: NetworkManagerAndroidBle? = null

    private var recyclerAdapter: ContentEntryListRecyclerViewAdapter? = null

    private var savedInstanceState: Bundle? = null

    private var rootContainer: View? = null

    fun filterByLang(langUid: Long) {
        entryListPresenter!!.handleClickFilterByLanguage(langUid)
    }

    fun filterBySchemaCategory(contentCategoryUid: Long, contentCategorySchemaUid: Long) {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootContainer = inflater.inflate(R.layout.fragment_contententry_list, container, false)
        this.savedInstanceState = savedInstanceState

        // Set the adapter
        val context = rootContainer!!.context
        recyclerView = rootContainer!!.findViewById(R.id.content_entry_list)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager.VERTICAL)
        recyclerView!!.addItemDecoration(dividerItemDecoration)
        checkReady()

        return rootContainer
    }


    override fun onAttach(context: Context?) {
        if (context is UstadBaseActivity) {
            this.ustadBaseActivity = context
            ustadBaseActivity!!.runAfterServiceConnection(Runnable{
                ustadBaseActivity!!.runOnUiThread {
                    managerAndroidBle = ustadBaseActivity!!
                            .networkManagerBle as NetworkManagerAndroidBle
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
        if (entryListPresenter == null && managerAndroidBle != null && rootContainer != null) {
            entryListPresenter = ContentEntryListFragmentPresenter(context as Context,
                    UMAndroidUtil.bundleToMap(arguments), this)
            entryListPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
        }
    }

    override fun onDetach() {
        super.onDetach()
        this.contentEntryListener = null
        this.ustadBaseActivity = null
    }

    override fun setContentEntryProvider(entryProvider: DataSource.Factory<Int, ContentEntryWithStatusAndMostRecentContainerUid>) {
        if (recyclerAdapter != null)
            recyclerAdapter!!.removeListeners()

        recyclerAdapter = ContentEntryListRecyclerViewAdapter(activity!!, this, this,
                managerAndroidBle)
        recyclerAdapter!!.addListeners()
        val data = LivePagedListBuilder(entryProvider, 20).build()
        data.observe(this, Observer<PagedList<ContentEntryWithStatusAndMostRecentContainerUid>> { recyclerAdapter!!.submitList(it) })

        recyclerView!!.adapter = recyclerAdapter
    }

    override fun setToolbarTitle(title: String) {
        runOnUiThread(Runnable {
            if (contentEntryListener != null)
                contentEntryListener!!.setTitle(title)
        })
    }

    override fun showError() {
        Toast.makeText(getContext(), R.string.content_entry_not_found, Toast.LENGTH_SHORT).show()
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
        ustadBaseActivity!!.runAfterGrantingPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Runnable { entryListPresenter!!.handleDownloadStatusButtonClicked(entry!!) },
                impl.getString(MessageID.download_storage_permission_title, getContext()!!),
                impl.getString(MessageID.download_storage_permission_message, getContext()!!))
    }

    override fun startMonitoringAvailability(monitor: Any, containerUidsToMonitor: List<Long>) {
        Thread {
            if (managerAndroidBle != null) {
                managerAndroidBle!!.startMonitoringAvailability(monitor, containerUidsToMonitor)
            }
        }.start()
    }

    override fun stopMonitoringAvailability(monitor: Any) {
        if (managerAndroidBle != null) {
            managerAndroidBle!!.stopMonitoringAvailability(monitor)
        }
    }

    override fun onStop() {
        stopMonitoringAvailability(this)
        super.onStop()
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
