package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.InputType
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.DashboardEntryListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.DashboardEntryListView
import com.ustadmobile.lib.db.entities.DashboardEntry
import com.ustadmobile.lib.db.entities.DashboardTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class DashboardEntryListFragment : UstadBaseFragment, IOnBackPressed, DashboardEntryListView {


    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    private var mPresenter: DashboardEntryListPresenter? = null

    private lateinit var entriesRV: RecyclerView
    private lateinit var floatingActionMenu: FloatingActionMenu
    private lateinit var tags: ChipGroup
    private val tagAll: Chip ?= null
    private lateinit var salesPerformance: FloatingActionButton
    private lateinit var salesLog: FloatingActionButton
    private lateinit var topLEs: FloatingActionButton

    private var recyclerAdapter: DashboardEntryListRecyclerAdapter? = null

    override fun onBackPressed(): Boolean {
        if(floatingActionMenu.isOpened()){
            floatingActionMenu.close(true)
            return false
        }else{
            return true
        }
    }

    @Nullable
    override fun onCreateView(inflater: LayoutInflater,
                              @Nullable container: ViewGroup?,
                              @Nullable savedInstanceState: Bundle?): View? {


        //Inflate view
        rootContainer = inflater.inflate(R.layout.fragment_dashboard_entry_list, container,
                false)
        setHasOptionsMenu(true)

        //Set recycler views
        //RecyclerView - Entries
        entriesRV = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_entries_rv)

        val display = activity!!.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = resources.displayMetrics.density
        val dpWidth = outMetrics.widthPixels / density
        val spanCount = Math.round(dpWidth / 450)


        val mRecyclerLayoutManager = GridLayoutManager(context, spanCount)
        //        RecyclerView.LayoutManager mRecyclerLayoutManager
        //                = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        entriesRV.setLayoutManager(mRecyclerLayoutManager)

        tags = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_tags_cg)
        //tagAll = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_chip_all);
        //TODO: Handle tag click

        floatingActionMenu = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_fab_menu)

        //Call the Presenter
        mPresenter = DashboardEntryListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Set listeners

        if(floatingActionMenu.isOpened){
            floatingActionMenu.close(false)
        }
        salesPerformance = rootContainer.findViewById<FloatingActionButton>(
                R.id.fragment_dashboard_entry_list_fab_menu_sales_performance)
        salesPerformance.setOnClickListener { v ->
                    floatingActionMenu.close(true)
                    mPresenter!!.handleClickNewSalePerformanceReport()
                }
        salesPerformance.setColorNormalResId(R.color.fab)
        salesPerformance.setColorPressedResId(R.color.fab_pressed)
        salesPerformance.setColorRippleResId(R.color.fab_ripple)

        salesLog = rootContainer.findViewById<FloatingActionButton>(R.id.fragment_dashboard_entry_list_fab_menu_sales_log)
        salesLog.setOnClickListener { v ->
                    floatingActionMenu.close(true)
                    mPresenter!!.handleClickNewSalesLogReport()
                }
        salesLog.setColorNormalResId(R.color.fab)
        salesLog.setColorPressedResId(R.color.fab_pressed)
        salesLog.setColorRippleResId(R.color.fab_ripple)

        topLEs = rootContainer.findViewById<FloatingActionButton>(R.id.fragment_dashboard_entry_list_fab_menu_top_les)
        topLEs.setOnClickListener { v ->
                    floatingActionMenu.close(true)
                    mPresenter!!.handleClickTopLEsReport()
                }
        topLEs.setColorNormalResId(R.color.fab)
        topLEs.setColorPressedResId(R.color.fab_pressed)
        topLEs.setColorRippleResId(R.color.fab_ripple)

        return rootContainer
    }

    override fun showSalesLogOption(show: Boolean) {
        if(show){
//            salesLog.visibility = View.VISIBLE
        }else{
            salesLog.visibility = View.GONE
        }
    }

    override fun showTopLEsOption(show: Boolean) {
        if(show){
//            topLEs.visibility = View.VISIBLE
        }else{
            topLEs.visibility = View.GONE
        }
    }

    override fun showSalesPerformanceOption(show: Boolean) {
        if(show){
//            salesPerformance.visibility = View.VISIBLE
        }else{
            salesPerformance.visibility = View.GONE
        }
    }


    override fun finish() {}

    override fun setDashboardEntryProvider(factory: DataSource.Factory<Int, DashboardEntry>) {

        recyclerAdapter = DashboardEntryListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                activity!!.applicationContext)

        // get the provider, set , observe, etc.

        val data =
                LivePagedListBuilder(factory, 20).build()

        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            //Observe the data:
            data.observe(thisP,
                    Observer<PagedList<DashboardEntry>> { recyclerAdapter!!.submitList(it) })
        }


        runOnUiThread(Runnable {
            //set the adapter
            entriesRV.setAdapter(recyclerAdapter)
        })

    }

    override fun setDashboardTagProvider(listProvider: DataSource.Factory<Int, DashboardTag>) {

        //TODO
    }

    override fun loadChips(chipNames: Array<String>) {
        for (chipName in chipNames) {
            val tag = Chip(Objects.requireNonNull(context))
            tag.text = chipName
            tags.addView(tag)
        }
    }

    override fun showSetTitle(existingTitle: String, entryUid: Long) {
        val edittext = EditText(context)
        edittext.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
        edittext.setText(existingTitle)

        val adb = AlertDialog.Builder(context!!)
                .setTitle("")
                .setMessage(getText(R.string.set_title))
                .setView(edittext)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    mPresenter!!.handleSetTitle(entryUid, edittext.text.toString())
                    dialog.dismiss()
                }

                .setNegativeButton(R.string.cancel, { dialog, which -> dialog.dismiss() })

        adb.create()
        adb.show()
    }


    constructor()  {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_insert_chart_black_24dp
        title = R.string.reports
    }

    constructor(args:Bundle) : this() {
        arguments = args
    }
    companion object {

        val icon = R.drawable.ic_insert_chart_black_24dp

        fun newInstance(): DashboardEntryListFragment {
            val fragment = DashboardEntryListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }


        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<DashboardEntry> = object
            : DiffUtil.ItemCallback<DashboardEntry>() {
            override fun areItemsTheSame(oldItem: DashboardEntry,
                                         newItem: DashboardEntry): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DashboardEntry,
                                            newItem: DashboardEntry): Boolean {
                return oldItem == newItem
            }
        }
    }

}
