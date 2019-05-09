package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Contacts;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CustomFieldDetailPresenter;
import com.ustadmobile.core.controller.CustomFieldListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.CustomFieldDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class CustomFieldDetailActivity extends UstadBaseActivity implements CustomFieldDetailView {

    private Toolbar toolbar;
    private CustomFieldDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;

    private Spinner entityTypeSpinner;
    private Spinner fieldTypeSpinner;

    private EditText title;
    private EditText titleAlt;
    private EditText defaultET;
    ConstraintLayout optionsCL;
    ConstraintLayout addOptionsCL;


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
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_catalog_entry_presenter_share) {
            mPresenter.handleClickDone();

            return super.onOptionsItemSelected(item);
        }

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
        setContentView(R.layout.activity_custom_field_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_custom_field_detail_toolbar);
        toolbar.setTitle(getText(R.string.custom_field));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.activity_custom_field_detail_name);
        titleAlt = findViewById(R.id.activity_custom_field_detail_name_arabic);
        defaultET = findViewById(R.id.activity_custom_field_detail_default_et);

        entityTypeSpinner = findViewById(R.id.activity_custom_field_detail_entity_spinner);
        fieldTypeSpinner = findViewById(R.id.activity_custom_field_detail_field_type_spinner);

        optionsCL = findViewById(R.id.activity_custom_field_detail_options_cl);
        optionsCL.setVisibility(View.GONE);

        addOptionsCL = findViewById(R.id.activity_custom_field_detail_add_cl);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_custom_field_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new CustomFieldDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        addOptionsCL.setOnClickListener(v -> mPresenter.handleClickAddOption());

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleFieldNameChanged(s.toString());
            }
        });

        titleAlt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleFieldNameAltChanged(s.toString());
            }
        });

        entityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                mPresenter.handleEntityEntityChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fieldTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                mPresenter.handleFieldTypeChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        defaultET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleDefaultValueChanged(s.toString());
            }
        });



    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<CustomFieldValueOption> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CustomFieldValueOption>() {
                @Override
                public boolean areItemsTheSame(CustomFieldValueOption oldItem,
                                               CustomFieldValueOption newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(CustomFieldValueOption oldItem,
                                                  CustomFieldValueOption newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<CustomFieldValueOption> listProvider) {
        CustomFieldDetailRecyclerAdapter recyclerAdapter =
                new CustomFieldDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, CustomFieldValueOption> factory =
                (DataSource.Factory<Integer, CustomFieldValueOption>)
                        listProvider.getProvider();
        LiveData<PagedList<CustomFieldValueOption>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setDropdownPresetsOnView(String[] dropdownPresets) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, dropdownPresets);
        fieldTypeSpinner.setAdapter(adapter);

    }

    @Override
    public void setEntityTypePresetsOnView(String[] entityTypePresets) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, entityTypePresets);
        entityTypeSpinner.setAdapter(adapter);
    }

    @Override
    public void setCustomFieldOnView(CustomField customField) {
        title.setText(customField.getCustomFieldName());
        titleAlt.setText(customField.getCustomFieldNameAlt());
        defaultET.setText(customField.getCustomFieldDefaultValue());
        switch (customField.getCustomFieldType()){
            case CustomField.FIELD_TYPE_TEXT:
                fieldTypeSpinner.setSelection(CustomFieldDetailPresenter.FIELD_TYPE_TEXT);
                showOptions(false);
                break;
            case CustomField.FIELD_TYPE_DROPDOWN:
                fieldTypeSpinner.setSelection(CustomFieldDetailPresenter.FIELD_TYPE_DROPDOWN);
                showOptions(true);
                break;
            default:break;
        }
        switch (customField.getCustomFieldEntityType()){
            case Clazz.TABLE_ID:
                entityTypeSpinner.setSelection(CustomFieldListPresenter.ENTITY_TYPE_CLASS);
                break;
            case Person.TABLE_ID:
                entityTypeSpinner.setSelection(CustomFieldListPresenter.ENTITY_TYPE_PERSON);
                break;
            default:break;
        }
    }

    @Override
    public void showOptions(boolean show) {
        optionsCL.setVisibility(show?View.VISIBLE:View.GONE);
    }
}
