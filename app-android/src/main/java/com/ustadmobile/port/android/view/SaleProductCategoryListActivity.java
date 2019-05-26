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

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleProductCategoryListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleProductCategoryListView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SaleProductCategoryListActivity extends UstadBaseActivity implements SaleProductCategoryListView {

    private Toolbar toolbar;
    private SaleProductCategoryListPresenter mPresenter;
    private RecyclerView mRecyclerView, cRecyclerView;

    private FloatingActionButton itemActionButton, subCategoryActionButton;
    private FloatingActionMenu floatingActionMenu;

    private Menu menu;
    private boolean hideEdit = false;

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_edit, menu);

        menu.findItem(R.id.action_search).setVisible(true);

        menu.findItem(R.id.action_edit).setVisible(!hideEdit);
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
        }else if(i == R.id.action_edit){
            mPresenter.handleClickEditThisCategory();
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

        floatingActionMenu = findViewById(R.id.activity_sale_product_category_list_fab_menu);
        itemActionButton = findViewById(R.id.activity_sale_product_category_list_fab_item);
        subCategoryActionButton = findViewById(R.id.activity_sale_product_category_list_fab_subcategory);

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_sale_product_category_list_items_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //categories RecyclerView
        cRecyclerView = findViewById(R.id.activity_sale_product_category_list_categories_recyclerview);
        LinearLayoutManager cRecyclerLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        cRecyclerView.setLayoutManager(cRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SaleProductCategoryListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Listeners
        itemActionButton.setOnClickListener(v -> {
            floatingActionMenu.close(true);
            mPresenter.handleClickAddItem();
        });

        subCategoryActionButton.setOnClickListener(v -> {
            floatingActionMenu.close(true);
            mPresenter.handleClickAddSubCategory();
        });
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
                        this, false, getApplicationContext());

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

        SelectSaleCategoryRecyclerAdapter recyclerAdapter =
                new SelectSaleCategoryRecyclerAdapter(DIFF_CALLBACK, mPresenter, this, false,
                        true, getApplicationContext());

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

    @Override
    public void initFromSaleCategory(SaleProduct saleProductCategory) {
        if(saleProductCategory != null){
            runOnUiThread(() -> toolbar.setTitle(saleProductCategory.getSaleProductName()));
        }
    }

    @Override
    public void updateSortPresets(String[] presets) {
        //TODO:
    }

    @Override
    public void hideFAB(boolean hide) {
        runOnUiThread(() -> floatingActionMenu.setVisibility(hide?View.GONE:View.VISIBLE));
    }

    @Override
    public void hideEditMenu(boolean hide) {
        hideEdit = hide;
        //runOnUiThread(() -> menu.findItem(R.id.action_edit).setVisible(!hide));

    }
}
