package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

    private Menu menu;

    private Spinner locationSpinner;
    private EditText discountET,orderNotesET;
    private CheckBox deliveredCB;
    private TextView orderTotal, totalAfterDiscount;
    private ConstraintLayout addItemCL;

    private long saleUid;

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);

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

        locationSpinner = findViewById(R.id.activity_sale_detail_location_spinner);
        discountET = findViewById(R.id.activity_sale_detail_discount);
        discountET.setText("0");
        deliveredCB = findViewById(R.id.activity_sale_detail_delivered);
        orderTotal = findViewById(R.id.activity_sale_detail_order_total);
        orderTotal.setText("0");
        totalAfterDiscount = findViewById(R.id.activity_sale_detail_order_after_discount_tota);
        totalAfterDiscount.setText("0");
        orderNotesET = findViewById(R.id.activity_sale_detail_order_notes);
        addItemCL = findViewById(R.id.activity_sale_detail_add_cl);


        //Call the Presenter
        mPresenter = new SaleDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        discountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                long discount = 0;
                if(s != null && s.length() > 0 ) {
                    discount = Long.valueOf(s.toString());
                }
                mPresenter.handleDiscountChanged(discount);


            }
        });

        orderNotesET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleOrderNotesChanged(s.toString());
            }
        });

        deliveredCB.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.handleSetDelivered(isChecked));

        addItemCL.setOnClickListener(v -> mPresenter.handleClickAddSaleItem());


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

        Observer customObserver = o -> {
            recyclerAdapter.submitList((PagedList<SaleItemListDetail>) o);
            mPresenter.getTotalSaleOrderAndDiscountAndUpdateView(saleUid);

        };



        //Observe the data:
        //data.observe(this, recyclerAdapter::submitList);
        data.observe(this, customObserver);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setLocationPresets(String[] locationPresets, int selectedPosition) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, locationPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setSelection(selectedPosition);

    }

    @Override
    public void updateOrderTotal(long orderTotalValue) {
        runOnUiThread(() -> {
            orderTotal.setText(String.valueOf(orderTotalValue));
            updateOrderTotalAfterDiscountTotalChanged(orderTotalValue);
        });

    }

    @Override
    public void updateOrderTotalAfterDiscount(long discountValue) {
        if(orderTotal.getText() != "") {
            long orderTotalValue = Long.parseLong(orderTotal.getText().toString());
            long totalAfterDiscountVal = orderTotalValue - discountValue;
            totalAfterDiscount.setText(String.valueOf(totalAfterDiscountVal));
        }else{
            totalAfterDiscount.setText("0");
        }
    }

    @Override
    public void updateOrderTotalAfterDiscountTotalChanged(long total) {
        if(total > 0){
            orderTotal.setText(String.valueOf(total));
        }
        long discount = 0;
        if(discountET.getText() != null && !discountET.getText().toString().equals("")){
            discount = Long.parseLong(discountET.getText().toString());
        }
        updateOrderTotalAfterDiscount(discount);

    }

    @Override
    public void updateSaleOnView(Sale sale) {
        runOnUiThread(() -> {
            if(sale != null){
                saleUid = sale.getSaleUid();
                deliveredCB.setChecked(sale.isSaleDone());
                orderNotesET.setText(sale.getSaleNotes());
                String discountValue = "0";
                if(sale.getSaleDiscount() > 0){
                    discountValue = String.valueOf(String.valueOf(sale.getSaleDiscount()));
                }
                discountET.setText(discountValue);
            }
        });

    }

    @Override
    public void setPaymentProvider(UmProvider<SalePayment> paymentProvider) {
        //Next Sprint
    }

    @Override
    public void updatePaymentTotal(long paymentTotal) {
        //Next Sprint
    }

}
