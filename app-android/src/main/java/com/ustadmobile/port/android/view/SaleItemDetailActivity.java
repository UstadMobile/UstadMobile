package com.ustadmobile.port.android.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleItemDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleItemReminder;
import com.ustadmobile.lib.db.entities.SalePayment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class SaleItemDetailActivity extends UstadBaseActivity implements SaleItemDetailView {

    private Toolbar toolbar;
    private SaleItemDetailPresenter mPresenter;
    private Menu menu;

    private TextView totalTV;
    private RadioButton saleRB, preOrderRB;
    NumberPicker quantityNP, pppNP;
    EditText pppNPET, quantityNPET;
    int quantityDefaultValue = 1;
    int pppDefaultValue =0;
    int minValue = 0;
    int maxValue = 99990;

    private View preOrderHline;
    private TextView orderDueDateTV;
    private EditText orderDueDateET;

    private View reminderHline;
    private TextView addReminderTV;
    private RecyclerView remindersRV;

    private boolean preOrderSelected = false;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout
        setContentView(R.layout.activity_sale_item_detail);

        //Toolbar
        toolbar = findViewById(R.id.activity_sale_item_detail_toolbar);
        toolbar.setTitle(getText(R.string.sale_detail));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        totalTV = findViewById(R.id.activity_sale_item_detail_total_amount);
        saleRB = findViewById(R.id.activity_sale_item_detail_radiobutton_sold);
        preOrderRB = findViewById(R.id.activity_sale_item_detail_radiobutton_preorder);
        quantityNP = findViewById(R.id.activity_sale_item_detail_quantity_numberpicker);
        quantityNPET = quantityNP.findViewById(Resources.getSystem().getIdentifier
                ("numberpicker_input", "id", "android"));
        quantityNPET.setFocusable(false);

        pppNP = findViewById(R.id.activity_sale_payment_detail_amount_np);
        pppNPET = pppNP.findViewById(Resources.getSystem().getIdentifier("numberpicker_input",
                "id", "android"));
        pppNPET.setFocusable(false);

        preOrderHline = findViewById(R.id.activity_sale_item_detail_preorder_hline);
        orderDueDateTV = findViewById(R.id.activity_sale_item_detail_preorder_due_date_tv);
        orderDueDateET = findViewById(R.id.activity_sale_item_detail_order_due_date_date_edittext);

        reminderHline = findViewById(R.id.activity_sale_item_detil_notification_hline);
        addReminderTV = findViewById(R.id.activity_sale_item_detail_add_reminder_tv);
        remindersRV = findViewById(R.id.activity_sale_item_detail_reminder_rv);
        RecyclerView.LayoutManager pRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        remindersRV.setLayoutManager(pRecyclerLayoutManager);

        //Date
        Calendar myCalendar = Calendar.getInstance();

        //A Time picker listener that sets the from time.
        DatePickerDialog.OnDateSetListener dateListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.YEAR, year);

            String dateString =
                    UMCalendarUtil.getPrettyDateSuperSimpleFromLong(myCalendar.getTimeInMillis(),
                            null);
            mPresenter.handleChangeOrderDueDate(myCalendar.getTimeInMillis());
            orderDueDateET.setText(dateString);
        };

        //Default view: not focusable.
        orderDueDateET.setFocusable(false);

        //From time on click -> opens a timer picker.
        orderDueDateET.setOnClickListener(v ->
                new DatePickerDialog(this, dateListener,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        quantityNP.setMinValue(1);
        quantityNP.setValue(quantityDefaultValue);
        quantityNP.setMaxValue(99999);

        pppNP.setMinValue(minValue);
        pppNP.setMaxValue(maxValue);
        pppNP.setValue(pppDefaultValue);

        //Presenter
        mPresenter = new SaleItemDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        quantityNP.setOnValueChangedListener((picker, oldVal, newVal) ->
        {
            int ppp = pppNP.getValue();
            mPresenter.handleChangeQuantity(newVal);
            mPresenter.updateTotal(newVal, ppp);
        });
        
        pppNP.setOnValueChangedListener((picker, oldVal, newVal) ->
        {
            int q = quantityNP.getValue();
            mPresenter.handleChangePPP(newVal);
            mPresenter.updateTotal(q, newVal);
        });

        pppNPET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int newVal = Integer.valueOf(s.toString());
                int q = quantityNP.getValue();
                mPresenter.handleChangePPP(newVal);
                mPresenter.updateTotal(q, newVal);
            }
        });

        preOrderRB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.setPreOrder(isChecked);
            showPreOrder(isChecked);
            preOrderSelected = isChecked;
        });

        saleRB.setOnCheckedChangeListener((buttonView, isChecked) -> mPresenter.setSold(isChecked));

        addReminderTV.setOnClickListener(v -> mPresenter.handleClickAddReminder());
    }

    @Override
    public void updateSaleItemOnView(SaleItem saleItem, String productName) {
        runOnUiThread(() -> {
            if(saleItem != null) {
                int q = saleItem.getSaleItemQuantity();
                float ppp = saleItem.getSaleItemPricePerPiece();
                long total = (long) (q * ppp);

                if(productName != null && productName != ""){
                    toolbar.setTitle(productName);
                }
                if(q != 0) {
                    quantityNP.setValue(q);
                }
                if(ppp > 0) {
                    pppNP.setValue((int) ppp);
                }
                totalTV.setText(String.valueOf(total));
                saleRB.setChecked(saleItem.isSaleItemSold());

                if(preOrderSelected){
                    preOrderRB.setChecked(preOrderSelected);
                }else {
                    preOrderRB.setChecked(saleItem.isSaleItemPreorder());
                }


                long dueDate = saleItem.getSaleItemDueDate();
                if(dueDate >0){
                    orderDueDateET.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                            dueDate, null));
                }
            }
        });

    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleItemReminder> DIFF_CALLBACK_REMINDER =
            new DiffUtil.ItemCallback<SaleItemReminder>() {
                @Override
                public boolean areItemsTheSame(SaleItemReminder oldItem,
                                               SaleItemReminder newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SaleItemReminder oldItem,
                                                  SaleItemReminder newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setReminderProvider(UmProvider<SaleItemReminder> paymentProvider) {
        SaleItemReminderRecyclerAdapter recyclerAdapter =
                new SaleItemReminderRecyclerAdapter(DIFF_CALLBACK_REMINDER, mPresenter, this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleItemReminder> factory =
                (DataSource.Factory<Integer, SaleItemReminder>)
                        paymentProvider.getProvider();
        LiveData<PagedList<SaleItemReminder>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        runOnUiThread(() -> remindersRV.setAdapter(recyclerAdapter));

    }

    @Override
    public void updateTotal(long total) {
        totalTV.setText(String.valueOf(total));
    }

    @Override
    public void updatePPP(long ppp) {
        pppNP.setValue((int) ppp);
    }

    @Override
    public void showPreOrder(boolean show) {
        preOrderHline.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        orderDueDateTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        orderDueDateET.setVisibility(show?View.VISIBLE:View.INVISIBLE);

        remindersRV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        reminderHline.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        addReminderTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
    }
}
