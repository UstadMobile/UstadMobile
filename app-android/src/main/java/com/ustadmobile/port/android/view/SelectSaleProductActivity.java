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
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectSaleProductPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SelectSaleProductActivity extends UstadBaseActivity implements SelectSaleProductView {

    private Toolbar toolbar;
    private SelectSaleProductPresenter mPresenter;
    private RecyclerView recentRV;
    private RecyclerView categoryRV;
    private RecyclerView collectionRV;

    private FloatingActionMenu fam;

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_select_sale_product);

        //Toolbar:
        toolbar = findViewById(R.id.activity_select_sale_product_toolbar);
        toolbar.setTitle(getText(R.string.add_item));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView - Recent
        recentRV = findViewById(
                R.id.activity_select_sale_product_recent_rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recentRV.setLayoutManager(layoutManager);

        //RecyclerView - Category
        categoryRV = findViewById(
                R.id.activity_select_sale_product_category_rv);
        LinearLayoutManager layoutManager2
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoryRV.setLayoutManager(layoutManager2);

        //Recyclerview - Collection
        collectionRV = findViewById(
                R.id.activity_select_sale_product_collection_rv);
        LinearLayoutManager layoutManager3
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        collectionRV.setLayoutManager(layoutManager3);

        //Call the Presenter
        mPresenter = new SelectSaleProductPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this, false);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        fam = findViewById(R.id.activity_select_sale_product_fab_menu);
        fam.setVisibility(View.GONE);
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
    public void setRecentProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductRecyclerAdapter recyclerAdapter =
                new SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        false,false,
                        getApplicationContext());

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
        recentRV.setAdapter(recyclerAdapter);
    }

    @Override
    public void setCategoryProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductRecyclerAdapter recyclerAdapter =
                new SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        true,false,
                        getApplicationContext());

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
        categoryRV.setAdapter(recyclerAdapter);
    }

    @Override
    public void setCollectionProvider(UmProvider<SaleNameWithImage> collectionProvider) {
        SelectSaleProductRecyclerAdapter recyclerAdapter =
                new SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        true,false,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleNameWithImage> factory =
                (DataSource.Factory<Integer, SaleNameWithImage>)
                        collectionProvider.getProvider();
        LiveData<PagedList<SaleNameWithImage>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        collectionRV.setAdapter(recyclerAdapter);
    }

    @Override
    public void showMessage(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String toast = impl.getString(messageId, this);
        runOnUiThread(() -> Toast.makeText(
                this,
                toast,
                Toast.LENGTH_SHORT
        ).show());
    }
}
