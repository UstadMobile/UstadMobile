package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleProductCategoryListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleProductCategoryListView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SaleProductCategoryListActivity extends UstadBaseActivity implements SaleProductCategoryListView {

    private Toolbar toolbar;
    private SaleProductCategoryListPresenter mPresenter;
    private RecyclerView mRecyclerView, cRecyclerView;

    private Menu menu;

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        menu.findItem(R.id.action_search).setVisible(true);
        return true;
    }
    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (i == R.id.action_search) {
            //TODO: Handle search
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sale_product_category_list);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_product_category_list_toolbar);
        toolbar.setTitle(getText(R.string.category));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_sale_product_category_list_items_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //categories RecyclerView
        cRecyclerView = findViewById(R.id.activity_sale_product_category_list_categories_recyclerview);
        cRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SaleProductCategoryListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleNameWithImage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SaleNameWithImage>() {
                @Override
                public boolean areItemsTheSame(SaleNameWithImage oldItem,
                                               SaleNameWithImage newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SaleNameWithImage oldItem,
                                                  SaleNameWithImage newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductWithDescRecyclerAdapter recyclerAdapter =
                new SelectSaleProductWithDescRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        this, getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleNameWithImage> factory =
                (DataSource.Factory<Integer, SaleNameWithImage>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleNameWithImage>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setCategoriesListProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductWithDescRecyclerAdapter recyclerAdapter =
                new SelectSaleProductWithDescRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        this, getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleNameWithImage> factory =
                (DataSource.Factory<Integer, SaleNameWithImage>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleNameWithImage>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        cRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setMessageOnView(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String message = impl.getString(messageId, getContext());

        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        ).show());

    }
}
