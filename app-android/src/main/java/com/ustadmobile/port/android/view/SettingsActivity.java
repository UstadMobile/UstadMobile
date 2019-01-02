package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SettingsPresenter;
import com.ustadmobile.core.view.SettingsView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SettingsActivity extends UstadBaseActivity implements SettingsView {

    SettingsPresenter mPresenter;
    ConstraintLayout selLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_settings2);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.activity_settings2_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        selLayout = findViewById(R.id.activity_settings2_sel_question_set_cl);

        mPresenter = new SettingsPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        selLayout.setOnClickListener(view -> mPresenter.goToSELQuestionSets());

    }
}
