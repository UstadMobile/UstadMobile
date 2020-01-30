package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R

import android.view.Menu
import android.widget.Spinner
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.controller.NewInventoryItemPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.NewInventoryItemView
import com.ustadmobile.lib.db.entities.SaleProduct


class NewInventoryItemActivity : UstadBaseActivity(), NewInventoryItemView {

    private var toolbar: Toolbar? = null
    private lateinit var mPresenter: NewInventoryItemPresenter

    // If you have a recycler view 
    private var mRecyclerView: RecyclerView? = null

    private lateinit var newProductCL : ConstraintLayout

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        return true
    }

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
        setContentView(R.layout.activity_new_inventoryitem)

        //Toolbar:
        toolbar = findViewById(R.id.activity_new_inventoryitem_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sortSpinner = findViewById(R.id.activity_new_inventoryitem_sort_by_spinner2)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_new_inventoryitem_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        newProductCL = findViewById(R.id.activity_new_inventoryitem_add_cl)

        //Call the Presenter
        mPresenter = NewInventoryItemPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Sort spinner handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleSortChanged(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        newProductCL.setOnClickListener{mPresenter.handleClickNewProduct()}

    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val recyclerAdapter = SelectSaleProductToSaleCategoryRecyclerAdapter(DIFF_CALLBACK,
                mPresenter!!, this, false, applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleProduct> = object : DiffUtil.ItemCallback<SaleProduct>() {
            override fun areItemsTheSame(oldItem: SaleProduct,
                                         newItem: SaleProduct): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SaleProduct,
                                            newItem: SaleProduct): Boolean {
                return oldItem == newItem
            }
        }

    }
}
