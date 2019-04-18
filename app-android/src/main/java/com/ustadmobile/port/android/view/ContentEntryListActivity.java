package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.lib.db.entities.DistinctCategorySchema;
import com.ustadmobile.lib.db.entities.Language;

import java.util.List;
import java.util.Map;

public class ContentEntryListActivity extends UstadBaseActivity implements ContentEntryListFragment.ContentEntryListener, AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        setUMToolbar(R.id.content_entry_list_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ContentEntryListFragment currentFrag = ContentEntryListFragment.newInstance(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.entry_content, currentFrag)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                clickUpNavigation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clickUpNavigation() {
        runOnUiThread(() ->{
            ContentEntryListFragment fragment = (ContentEntryListFragment) getSupportFragmentManager().findFragmentById(R.id.entry_content);
            fragment.clickUpNavigation();
        });

    }


    @Override
    public void setTitle(String title) {
        runOnUiThread(() -> {
            Toolbar toolBarTitle = findViewById(R.id.content_entry_list_toolbar);
            toolBarTitle.setTitle(title);
        });
    }

    @Override
    public void setFilterSpinner(Map<Long, List<DistinctCategorySchema>> idToValuesMap) {
        runOnUiThread(() -> {
            LinearLayout spinnerLayout = findViewById(R.id.content_entry_list_spinner_layout);
            spinnerLayout.setVisibility(View.VISIBLE);
            for (Long id : idToValuesMap.keySet()) {

                Spinner spinner = new Spinner(this);
                ArrayAdapter<DistinctCategorySchema> dataAdapter = new ArrayAdapter<>(this,
                        R.layout.content_entry_list_spinner_layout, idToValuesMap.get(id));
                spinner.setAdapter(dataAdapter);
                spinner.setOnItemSelectedListener(this);
                spinner.getBackground().setColorFilter(ContextCompat.getColor(
                        this, android.R.color.white),
                        PorterDuff.Mode.SRC_ATOP);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                spinnerLayout.addView(spinner, params);
            }
        });

    }

    @Override
    public void setLanguageFilterSpinner(List<Language> result) {
        runOnUiThread(() -> {
            LinearLayout spinnerLayout = findViewById(R.id.content_entry_list_spinner_layout);
            spinnerLayout.setVisibility(View.VISIBLE);
            Spinner spinner = new Spinner(this);
            ArrayAdapter<Language> dataAdapter = new ArrayAdapter<>(this,
                    R.layout.content_entry_list_spinner_layout, result);
            spinner.setAdapter(dataAdapter);
            spinner.setOnItemSelectedListener(this);
            spinner.getBackground().setColorFilter(ContextCompat.getColor(
                    this, android.R.color.white),
                    PorterDuff.Mode.SRC_ATOP);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            spinnerLayout.addView(spinner, 0, params);
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        runOnUiThread(() -> {
            Object item = adapterView.getItemAtPosition(pos);

            if (adapterView.getChildAt(0) != null) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);
            }

            ContentEntryListFragment fragment = (ContentEntryListFragment) getSupportFragmentManager().findFragmentById(R.id.entry_content);
            if (item instanceof Language) {
                // language
                Language lang = (Language) item;
                fragment.filterByLang(lang.getLangUid());

            } else if (item instanceof DistinctCategorySchema) {
                DistinctCategorySchema categorySchema = (DistinctCategorySchema) item;
                fragment.filterBySchemaCategory(categorySchema.getContentCategoryUid(), categorySchema.getContentCategorySchemaUid());
            }
        });

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
