package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionDetail2Presenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.view.SELQuestionDetail2View;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionOption;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SELQuestionDetail2Activity extends UstadBaseActivity implements SELQuestionDetail2View {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private SELQuestionDetail2Presenter mPresenter;
    private long currentQuestionUid;
    private EditText questionText;
    private Spinner questionType;
    ConstraintLayout addOptionCL;
    ConstraintLayout optionsCL;

    public static final DiffUtil.ItemCallback<SocialNominationQuestionOption> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SocialNominationQuestionOption>(){

            @Override
            public boolean areItemsTheSame(SocialNominationQuestionOption oldItem,
                                           SocialNominationQuestionOption newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(SocialNominationQuestionOption oldItem,
                                              SocialNominationQuestionOption newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_question_detail2);

        //Toolbar
        toolbar = findViewById(R.id.activity_sel_question_detail2_toolbar);
        toolbar.setTitle(R.string.edit_question);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Question text
        questionText = findViewById(R.id.activity_sel_question_detail2_question_name);

        //Quetion type
        questionType = findViewById(R.id.activity_sel_question_detail2_question_type_spinner);

        //Options CL
        optionsCL = findViewById(R.id.activity_sel_question_detail2_options_cl);

        //Add Option button
        addOptionCL = findViewById(R.id.activity_sel_question_detail2_add_question_cl);

        addOptionCL.setOnClickListener(view -> handleClickAddOption());

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_question_detail2_type_multi_choice_options_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Presenter
        mPresenter = new SELQuestionDetail2Presenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        questionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                handleQuestionTypeChange(i+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    @Override
    public void setQuestionTypePresets(String[] presets, int position) {
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_spinner_item, presets);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            questionType.setAdapter(adapter);
            questionType.setSelection(position);
        });

    }

    @Override
    public void setQuestionOptionsProvider(UmProvider<SocialNominationQuestionOption> listProvider) {

    }

    @Override
    public void setQuestionText(String questionTextString) {
        runOnUiThread(() -> questionText.setText(questionTextString));

    }

    @Override
    public void setQuestionType(int type) {
        switch(type){
            case SocialNominationQuestionDao
                    .SEL_QUESTION_TYPE_NOMINATION:
                showQuestionOptions(false);
                questionType.setSelection(type);
                break;
            case SocialNominationQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE:
                showQuestionOptions(true);
                questionType.setSelection(type);
                break;
            case SocialNominationQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT:
                showQuestionOptions(false);
                questionType.setSelection(type);
                break;
            default:
                break;

        }
    }

    @Override
    public void handleClickDone() {
        mPresenter.handleClickDone();
    }

    @Override
    public void showQuestionOptions(boolean show) {
        addOptionCL.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        addOptionCL.setEnabled(show);
    }

    @Override
    public void handleQuestionTypeChange(int type) {
        mPresenter.handleQuestionTypeChange(type);
    }

    @Override
    public void handleClickAddOption() {

        //Do something
        mPresenter.handleClickAddOption();
        //Do something
    }
}
