package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AddSaleProductToSaleCategoryPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView
import com.ustadmobile.lib.db.entities.SaleProduct

class AddSaleProductToSaleCategoryActivity
    : UstadBaseActivity(), AddSaleProductToSaleCategoryView {

    private var toolbar: Toolbar? = null
    private var mPresenter: AddSaleProductToSaleCategoryPresenter? = null
    private var mRecyclerView: RecyclerView? = null
    private var addItemCL: ConstraintLayout? = null
    private var addTextView: TextView? = null

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
        setContentView(R.layout.activity_add_sale_product_to_sale_category)

        //Toolbar:
        toolbar = findViewById(R.id.activity_add_sale_product_to_sale_category_toolbar)
        toolbar!!.title = getText(R.string.add_item)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_add_sale_product_to_sale_category_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        addItemCL = findViewById(R.id.activity_add_sale_product_to_sale_category_add_cl)
        addTextView = findViewById(R.id.activity_add_sale_product_to_sale_category_add_text)

        //Call the Presenter
        mPresenter = AddSaleProductToSaleCategoryPresenter(this,
                bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))


        //Listener
        addItemCL!!.setOnClickListener { v -> mPresenter!!.handleAddNewItem() }

    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val recyclerAdapter = SelectSaleProductToSaleCategoryRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                false, applicationContext)

        // get the provider, set , observe, etc.

        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this,
                Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun setAddtitle(title: String) {
        addTextView!!.text = title
    }

    override fun setToolbarTitle(title: String) {
        toolbar!!.title = title
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
