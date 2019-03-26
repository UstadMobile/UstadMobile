package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CustomFieldListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.CustomFieldListView;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class CustomFieldListActivity extends UstadBaseActivity implements CustomFieldListView {

    private Toolbar toolbar;
    private CustomFieldListPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private Spinner entityTypeSpinner;
    private String[] entityTypePresets;


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
        setContentView(R.layout.activity_custom_field_list);

        //Toolbar:
        toolbar = findViewById(R.id.activity_custom_field_list_toolbar);
        toolbar.setTitle(getText(R.string.custom_fields));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        entityTypeSpinner = findViewById(R.id.activity_custom_field_list_entity_type_spinner);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_custom_field_list_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new CustomFieldListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        entityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleEntityTypeChange(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_custom_field_list_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<CustomField> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CustomField>() {
                @Override
                public boolean areItemsTheSame(CustomField oldItem,
                                               CustomField newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(CustomField oldItem,
                                                  CustomField newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<CustomField> listProvider) {
        CustomFieldListRecyclerAdapter recyclerAdapter =
                new CustomFieldListRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, CustomField> factory =
                (DataSource.Factory<Integer, CustomField>)
                        listProvider.getProvider();
        LiveData<PagedList<CustomField>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setEntityTypePresets(String[] entityTypePresets) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, entityTypePresets);
        entityTypeSpinner.setAdapter(adapter);
        entityTypeSpinner.setSelection(CustomFieldListPresenter.ENTITY_TYPE_CLASS);
    }
}
