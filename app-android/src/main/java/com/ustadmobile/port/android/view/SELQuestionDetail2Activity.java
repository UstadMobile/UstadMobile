package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
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

        questionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                mPresenter.updateQuestionTitle(editable.toString());
            }
        });

    }


    @Override
    public void setQuestionTypePresets(String[] presets) {
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_spinner_item, presets);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            questionType.setAdapter(adapter);

            //Set listener
            setQuestionTypeListener();

        });

    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_done, menu);
        return true;
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }


        // Handle item selection
        int i = item.getItemId();
        //If this activity started from other activity
        if (i == R.id.menu_catalog_entry_presenter_share) {
            mPresenter.handleClickDone();

            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setQuestionOptionsProvider(UmProvider<SocialNominationQuestionOption> listProvider) {
        SELQuestionOptionRecyclerAdapter recyclerAdapter =
                new SELQuestionOptionRecyclerAdapter(
                        DIFF_CALLBACK,
                        getApplicationContext(),
                        this,
                        mPresenter);

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SocialNominationQuestionOption> factory =
                (DataSource.Factory<Integer, SocialNominationQuestionOption>)
                        listProvider.getProvider();
        LiveData<PagedList<SocialNominationQuestionOption>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
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
        optionsCL.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        optionsCL.setEnabled(show);
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

    @Override
    public void setQuestionTypeListener() {
        runOnUiThread(() ->
                questionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                handleQuestionTypeChange(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }));

    }

    @Override
    public void setQuestionOnView(SocialNominationQuestion selQuestion) {
        if(selQuestion.getQuestionText() != null)
            setQuestionText(selQuestion.getQuestionText());
        if(selQuestion.getQuestionType() > 0)
            setQuestionType(selQuestion.getQuestionType());

    }
}
