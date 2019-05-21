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
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleProductDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductSelected;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SaleProductDetailActivity extends UstadBaseActivity implements SaleProductDetailView {

    private Toolbar toolbar;
    private SaleProductDetailPresenter mPresenter;
    private RecyclerView cRecyclerView;

    private Menu menu;

    EditText titleEng, descEng, titleDari, descDari, titlePashto, descPastho;

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);

        menu.findItem(R.id.menu_save).setVisible(true);
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

        } else if (i == R.id.menu_save) {
            mPresenter.handleClickSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sale_product_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_product_detail_toolbar);
        toolbar.setTitle(getText(R.string.create_new_subcategory));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Categories RecyclerView
        cRecyclerView = findViewById(R.id.activity_sale_product_detail_categories_rv);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        cRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        titleEng = findViewById(R.id.activity_sale_product_detail_title_english);
        titleDari = findViewById(R.id.activity_sale_product_detail_title_dari);
        titlePashto = findViewById(R.id.activity_sale_product_detail_title_pashto);

        descEng = findViewById(R.id.activity_sale_product_detail_desc_english);
        descDari = findViewById(R.id.activity_sale_product_detail_desc_dari);
        descPastho = findViewById(R.id.activity_sale_product_detail_desc_pashto);

        //Call the Presenter
        mPresenter = new SaleProductDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));



    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleProductSelected> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SaleProductSelected>() {
                @Override
                public boolean areItemsTheSame(SaleProductSelected oldItem,
                                               SaleProductSelected newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SaleProductSelected oldItem,
                                                  SaleProductSelected newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<SaleProductSelected> listProvider) {
        SaleProductCategorySelectorRecyclerAdapter recyclerAdapter =
                new SaleProductCategorySelectorRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleProductSelected> factory =
                (DataSource.Factory<Integer, SaleProductSelected>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleProductSelected>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        cRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void updateToolbarTitle(String titleName) {
        runOnUiThread(() -> toolbar.setTitle(titleName));
    }

    @Override
    public void updateImageOnView(String imagePath) {
        //TODO
    }

    @Override
    public void addImageFromCamera() {
        //TODO
    }

    @Override
    public void initFromSaleProduct(SaleProduct saleProduct) {
        if(saleProduct != null){
            if(saleProduct.getSaleProductName() != null && !saleProduct.getSaleProductName().isEmpty()){
                titleEng.setText(saleProduct.getSaleProductName());
            }
            if(saleProduct.getSaleProductNameDari() != null && !saleProduct.getSaleProductNameDari().isEmpty()){
                titleDari.setText(saleProduct.getSaleProductNameDari());
            }
            if(saleProduct.getSaleProductNamePashto() != null && !saleProduct.getSaleProductNamePashto().isEmpty()){
                titlePashto.setText(saleProduct.getSaleProductNamePashto());
            }
            if(saleProduct.getSaleProductDesc() != null && !saleProduct.getSaleProductDesc().isEmpty()){
                descEng.setText(saleProduct.getSaleProductDesc());
            }
            if(saleProduct.getSaleProductDescDari() != null && !saleProduct.getSaleProductDescDari().isEmpty()){
                descDari.setText(saleProduct.getSaleProductDescDari());
            }
            if(saleProduct.getSaleProductDescPashto() != null && !saleProduct.getSaleProductDescPashto().isEmpty()){
                descPastho.setText(saleProduct.getSaleProductDescPashto());
            }


        }
    }

}
