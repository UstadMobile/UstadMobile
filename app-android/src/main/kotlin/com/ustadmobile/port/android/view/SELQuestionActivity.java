package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionPresenter;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELQuestion activity - responsible for displaying the question in between SEL runs.
 */
public class SELQuestionActivity extends UstadBaseActivity implements SELQuestionView {

    private Toolbar toolbar;
    private SELQuestionPresenter mPresenter;

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item  The item selected
     * @return  true if accounted for
     */
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
        setContentView(R.layout.activity_sel_question);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        RecyclerView mRecyclerView = findViewById(
                R.id.activity_sel_question_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELQuestionPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_question_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

    }

    /**
     * Updates the question (usually called from the presenter) on the view
     *
     * @param questionText  The question string text
     */
    @Override
    public void updateQuestion(String questionText) {
        TextView question = findViewById(R.id.activity_sel_question_question);
        question.setText(questionText);

    }

    /**
     * Updates the question counter and totals (usually called from the presenter) on the view.
     *
     * @param qNumber   The question number from the set
     * @param tNumber   The total number of questions (usually no. of questions in a set)
     */
    @Override
    public void updateQuestionNumber(String qNumber, String tNumber) {
        TextView qNum = findViewById(R.id.activity_sel_question_number_position);
        String qNumString = qNumber + "/" + tNumber;
        qNum.setText(qNumString);
        toolbar.setTitle(toolbar.getTitle().toString() + qNumber + "/" + tNumber);
    }


}
