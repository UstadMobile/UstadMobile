package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELSelectConsentPresenter;
import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELSelectConsent activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements SELSelectConsentView
 */
public class SELSelectConsentActivity extends UstadBaseActivity implements SELSelectConsentView {

    private Toolbar toolbar;

    private SELSelectConsentPresenter mPresenter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
        setContentView(R.layout.activity_selselect_consent);

        //Toolbar:
        toolbar = findViewById(R.id.activity_selselect_consent_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Call the Presenter
        mPresenter = new SELSelectConsentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        CheckBox consentCheckBox = findViewById(R.id.activity_selselect_consent_checkbox);

        //FAB:
        FloatingTextButton fab = findViewById(R.id.activity_selselect_consent_fab);
        fab.setOnClickListener(v ->
                mPresenter.handleClickPrimaryActionButton(consentCheckBox.isChecked()));

    }


}
