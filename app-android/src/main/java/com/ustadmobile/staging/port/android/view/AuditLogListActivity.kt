package com.ustadmobile.staging.port.android.view


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AuditLogListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AuditLogListView
import com.ustadmobile.lib.db.entities.AuditLogWithNames
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class AuditLogListActivity : UstadBaseActivity(), AuditLogListView, PopupMenu.OnMenuItemClickListener {

    private var toolbar: Toolbar? = null
    private var mPresenter: AuditLogListPresenter? = null
    private var mRecyclerView: RecyclerView? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_export, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { this.onMenuItemClick(it) })
        popup.menu.findItem(R.id.menu_export_xls).isVisible = false
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.menu_export_csv) {
            mPresenter!!.dataToCSV()
            return true
        }
        return false
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_audit_log_list)

        //Toolbar:
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.setTitle(getText(R.string.audit_log))
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_audit_log_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        //Call the Presenter
        mPresenter = AuditLogListPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.activity_audit_log_list_fab)

        fab.setOnClickListener { v -> showPopup(v) }


    }

    override fun setListProvider(factory: DataSource.Factory<Int, AuditLogWithNames>) {
        val recyclerAdapter = AuditLogListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<AuditLogWithNames>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun generateCSVReport(data: List<Array<String>>) {

        var csvReportFilePath = ""
        //Create the file.

        val dir = filesDir
        val output = File(dir, "classbook_audit_log_" +
                System.currentTimeMillis() + ".csv")
        csvReportFilePath = output.absolutePath

        try {
            val fileWriter = FileWriter(csvReportFilePath)
            val tableTextdataIterator = data.iterator()

            while (tableTextdataIterator.hasNext()) {
                var firstDone = false
                val lineArray = tableTextdataIterator.next()
                for (i in lineArray.indices) {
                    if (firstDone) {
                        fileWriter.append(",")
                    }
                    firstDone = true
                    fileWriter.append(lineArray[i])
                }
                fileWriter.append("\n")
            }
            fileWriter.close()


        } catch (e: IOException) {
            e.printStackTrace()
        }

        val applicationId = packageName
        val sharedUri = FileProvider.getUriForFile(this,
                "$applicationId.provider",
                File(csvReportFilePath))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<AuditLogWithNames>
                = object : DiffUtil.ItemCallback<AuditLogWithNames>() {
            override fun areItemsTheSame(oldItem: AuditLogWithNames,
                                newItem: AuditLogWithNames): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: AuditLogWithNames,
                                   newItem: AuditLogWithNames): Boolean {
                return oldItem == newItem
            }
        }
    }
}
