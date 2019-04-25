package com.ustadmobile.port.android.view;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SalePaymentDetailPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.SalePaymentDetailView;
import com.ustadmobile.lib.db.entities.SalePayment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Calendar;
import java.util.Objects;

public class SalePaymentDetailActivity extends UstadBaseActivity implements SalePaymentDetailView {

    private Toolbar toolbar;
    private SalePaymentDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;

    private NumberPicker amountNP;
    private EditText paymentDateET;
    private EditText amountNPET;

    private Menu menu;

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
        setContentView(R.layout.activity_sale_payment_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_payment_detail_toolbar);
        toolbar.setTitle(getText(R.string.add_payment));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        amountNP =findViewById(R.id.activity_sale_payment_detail_amount_np);
        amountNP.setMinValue(1);
        amountNP.setMaxValue(9999999);
        amountNP.setValue(0);

        paymentDateET = findViewById(R.id.activity_sale_payment_detail_payment_date_et);

        Calendar myCalendar = Calendar.getInstance();

        //A Time picker listener that sets the from time.
        DatePickerDialog.OnDateSetListener dateListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.YEAR, year);

            String dateString =
                    UMCalendarUtil.getPrettyDateSuperSimpleFromLong(myCalendar.getTimeInMillis(),
                            null);
            mPresenter.handleDateUpdated(myCalendar.getTimeInMillis());
            paymentDateET.setText(dateString);
        };

        //Default view: not focusable.
        paymentDateET.setFocusable(false);

        //From time on click -> opens a timer picker.
        paymentDateET.setOnClickListener(v ->
                new DatePickerDialog(this, dateListener,
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        //Call the Presenter
        mPresenter = new SalePaymentDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        amountNPET = amountNP.findViewById(Resources.getSystem()
                .getIdentifier("numberpicker_input","id", "android"));

        amountNPET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int newVal = Integer.valueOf(s.toString());
                mPresenter.handleAmountUpdated(newVal);
            }
        });

    }

    @Override
    public void updateSalePaymentOnView(SalePayment payment) {
        amountNP.setValue((int) payment.getSalePaymentPaidAmount());

        paymentDateET.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                payment.getSalePaymentPaidDate(), null));
    }
}
