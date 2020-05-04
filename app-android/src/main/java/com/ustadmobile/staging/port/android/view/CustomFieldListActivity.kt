package com.ustadmobile.staging.port.android.view


import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CustomFieldListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.CustomFieldListView
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton

class CustomFieldListActivity : UstadBaseActivity(), CustomFieldListView {

    private var toolbar: Toolbar? = null
    private var mPresenter: CustomFieldListPresenter? = null
    private var mRecyclerView: RecyclerView? = null
    private var entityTypeSpinner: Spinner? = null
    private val entityTypePresets: Array<String>? = null


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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_custom_field_list)

        //Toolbar:
        toolbar = findViewById(R.id.activity_custom_field_list_toolbar)
        toolbar!!.title = getText(R.string.custom_fields)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        entityTypeSpinner = findViewById(R.id.activity_custom_field_list_entity_type_spinner)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_custom_field_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = CustomFieldListPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        entityTypeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                mPresenter!!.handleEntityTypeChange(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_custom_field_list_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }


    }

    override fun setListProvider(factory: DataSource.Factory<Int, CustomField>) {
        val recyclerAdapter = CustomFieldListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<CustomField>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun setEntityTypePresets(entityTypePresets: Array<String>) {
        val adapter = ArrayAdapter(applicationContext,
                android.R.layout.simple_spinner_dropdown_item, entityTypePresets)
        entityTypeSpinner!!.adapter = adapter
        entityTypeSpinner!!.setSelection(CustomFieldListPresenter.ENTITY_TYPE_CLASS)
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<CustomField> = object : DiffUtil.ItemCallback<CustomField>() {
            override fun areItemsTheSame(oldItem: CustomField,
                                         newItem: CustomField): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CustomField,
                                            newItem: CustomField): Boolean {
                return oldItem.customFieldUid == newItem.customFieldUid
            }
        }
    }
}
