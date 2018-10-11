package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.v7.util.DiffUtil;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import com.ustadmobile.core.controller.SELSelectConsentPresenter;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;

import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.toughra.ustadmobile.R;


import com.ustadmobile.core.view.SELSelectConsentView;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_selselect_consent);

        //Toolbar:
        toolbar = findViewById(R.id.activity_selselect_consent_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
