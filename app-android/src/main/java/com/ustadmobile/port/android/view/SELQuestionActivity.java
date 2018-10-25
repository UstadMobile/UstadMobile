package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionPresenter;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELQuestion activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements SELQuestionView
 */
public class SELQuestionActivity extends UstadBaseActivity implements SELQuestionView {

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private SELQuestionPresenter mPresenter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_question);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);



        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_question_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELQuestionPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_question_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }

    @Override
    public void updateQuestion(String questionText) {
        TextView question = findViewById(R.id.activity_sel_question_question);
        question.setText(questionText);

    }

    @Override
    public void updateQuestionNumber(String qNumber, String tNumber) {
        TextView qNum = findViewById(R.id.activity_sel_question_number_position);
        String qNumString = qNumber + "/" + tNumber;
        qNum.setText(qNumString);
        toolbar.setTitle(toolbar.getTitle().toString() + qNumber + "/" + tNumber);
    }


}
