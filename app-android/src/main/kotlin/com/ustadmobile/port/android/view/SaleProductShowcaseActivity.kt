package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductShowcasePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleProductShowcaseView
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductPicture

class SaleProductShowcaseActivity : UstadBaseActivity(), SaleProductShowcaseView {

    private var toolbar: Toolbar? = null
    private lateinit var mPresenter: SaleProductShowcasePresenter

    // If you have a recycler view 
    private var mRecyclerView: RecyclerView? = null
    private lateinit var descText : TextView
    private lateinit var chip: Chip

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
            R.id.menu_save_edit -> {
                mPresenter.handleClickEdit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_saleproduct_showcase)

        //Toolbar:
        toolbar = findViewById(R.id.activity_saleproduct_showcase_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_saleproduct_showcase_pictures_rv)
        val cRecyclerLayoutManager = LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView!!.layoutManager = cRecyclerLayoutManager

        descText = findViewById(R.id.textView32)
        chip = findViewById(R.id.activity_saleproduct_showcase_chip)

        //Call the Presenter
        mPresenter = SaleProductShowcasePresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

    override fun updateSaleProductOnView(saleProduct: SaleProduct) {
        val impl = UstadMobileSystemImpl.instance
        toolbar!!.title = saleProduct.getNameLocale(impl.getLocale(this))
        descText.text = saleProduct.getDescLocale(impl.getLocale(this))
    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleProductPicture>) {
        val recyclerAdapter = SaleProductImageScrollRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProductPicture>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateCreatorOnView(creator: String) {
        chip.text = creator
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)
        menu.findItem(R.id.menu_save_edit).setVisible(true)
        return true
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleProductPicture> = object
            : DiffUtil.ItemCallback<SaleProductPicture>() {
            override fun areItemsTheSame(oldItem: SaleProductPicture,
                                         newItem: SaleProductPicture): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SaleProductPicture,
                                            newItem: SaleProductPicture): Boolean {
                return oldItem == newItem
            }
        }

    }
}
