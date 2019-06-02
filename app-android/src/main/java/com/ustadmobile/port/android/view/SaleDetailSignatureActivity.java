package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleDetailSignaturePresenter;
import com.ustadmobile.core.view.SaleDetailSignatureView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class SaleDetailSignatureActivity extends UstadBaseActivity implements SaleDetailSignatureView {

    private Toolbar toolbar;
    private SaleDetailSignaturePresenter mPresenter;
    SignaturePad mSignaturePad;


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
        setContentView(R.layout.activity_record_signature);

        //Toolbar:
        toolbar = findViewById(R.id.activity_record_signature_toolbar);
        toolbar.setTitle(getText(R.string.add_signature));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Call the Presenter
        mPresenter = new SaleDetailSignaturePresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        mSignaturePad = findViewById(R.id.activity_record_signature_signaturepad);

        //FAB and its listener
        FloatingTextButton accept = findViewById(R.id.activity_record_signature_fab_accept);
        accept.setOnClickListener(v -> mPresenter.handleClickAccept());

        FloatingTextButton cancel = findViewById(R.id.activity_record_signature_fab_clear);
        cancel.setOnClickListener(v -> mSignaturePad.clear());

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {}

            @Override
            public void onSigned() {
                String signSvg = mSignaturePad.getSignatureSvg();
                mPresenter.updateSignatureSvg(signSvg);
            }

            @Override
            public void onClear() {
                mPresenter.updateSignatureSvg("");
            }
        });

    }


}
