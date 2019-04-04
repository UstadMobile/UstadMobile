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
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.lib.db.entities.SalePayment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class SaleDetailActivity extends UstadBaseActivity implements SaleDetailView {

    private Toolbar toolbar;
    private SaleDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;


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
        setContentView(R.layout.activity_sale_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_detail_toolbar);
        toolbar.setTitle(getText(R.string.sale_detail));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sale_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SaleDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sale_detail_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleItemListDetail> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SaleItemListDetail>() {
                @Override
                public boolean areItemsTheSame(SaleItemListDetail oldItem,
                                               SaleItemListDetail newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SaleItemListDetail oldItem,
                                                  SaleItemListDetail newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<SaleItemListDetail> listProvider) {
        SaleItemRecyclerAdapter recyclerAdapter =
                new SaleItemRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleItemListDetail> factory =
                (DataSource.Factory<Integer, SaleItemListDetail>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleItemListDetail>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setLocationPresets(String[] locationPresets, int selectedPosition) {

    }

    @Override
    public void setPaymentProvider(UmProvider<SalePayment> paymentProvider) {

    }

    @Override
    public void updateOrderTotal(long orderTotal) {

    }

    @Override
    public void updateOrderDiscountTotal(long discountTotal) {

    }

    @Override
    public void updateSaleOnView(Sale sale) {

    }

    @Override
    public void updatePaymentTotal(long paymentTotal) {

    }
}
