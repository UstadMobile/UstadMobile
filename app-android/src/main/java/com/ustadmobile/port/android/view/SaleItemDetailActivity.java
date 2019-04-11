package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleItemDetailPresenter;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SaleItemDetailActivity extends UstadBaseActivity implements SaleItemDetailView {

    private Toolbar toolbar;
    private SaleItemDetailPresenter mPresenter;
    private Menu menu;

    private TextView totalTV;
    private RadioButton saleRB, preOrderRB;
    NumberPicker quantityNP, pppNP;

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
        pppNP = findViewById(R.id.activity_sale_item_detail_price_per_piece_number_picker);

        quantityNP.setMinValue(1);
        quantityNP.setValue(2);
        quantityNP.setMaxValue(99999);

        pppNP.setMinValue(0);
        pppNP.setValue(0);
        pppNP.setMaxValue(999999);

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
                quantityNP.setValue(q);
                pppNP.setValue((int) ppp);
                totalTV.setText(String.valueOf(total));
                saleRB.setActivated(saleItem.isSaleItemSold());
                preOrderRB.setActivated(saleItem.isSaleItemPreorder());
            }
        });

    }

    @Override
    public void updateTotal(long total) {
        totalTV.setText(String.valueOf(total));
    }

    @Override
    public void updatePPP(long ppp) {
        pppNP.setValue((int) ppp);
    }
}
