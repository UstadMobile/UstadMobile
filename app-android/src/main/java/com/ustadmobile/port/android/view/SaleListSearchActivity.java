package com.ustadmobile.port.android.view;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleListSearchPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SaleListSearchView;
import com.ustadmobile.lib.db.entities.SaleListDetail;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class SaleListSearchActivity extends UstadBaseActivity implements SaleListSearchView {

    private Toolbar toolbar;
    private SaleListSearchPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private SearchView searchView;
    private Spinner locationSpinner;

    private float apl = 0.0f;
    private float aph = 1.0f;

    private CrystalRangeSeekbar valueSeekbar;
    private String currentValue = "";

    EditText dateRangeET;
    TextView valueRangeTV;

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_search){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));

        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentValue = query;
                // filter recycler view when query submitted
                mPresenter.updateFilter(apl, aph, currentValue);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                currentValue = query;
                // filter recycler view when text is changed
                mPresenter.updateFilter(apl, aph, currentValue);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            currentValue = "";
            mPresenter.updateFilter(apl, aph, currentValue);
            return false;
        });


        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sale_list_search);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_list_search_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sale_list_search_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        locationSpinner = findViewById(R.id.activity_sale_list_search_location_spinner);
        dateRangeET = findViewById(R.id.activity_sale_list_search_date_range_edittext);
        dateRangeET.setFocusable(false);
        valueSeekbar = findViewById(R.id.activity_sale_list_search_value_seekbar);
        valueRangeTV = findViewById(R.id.activity_sale_list_search_value_range_textview);

        //Call the Presenter
        mPresenter = new SaleListSearchPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleLocationSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        valueSeekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            updateValueRangeOnView(minValue.intValue(), maxValue.intValue());
            if(minValue.floatValue() > 0)
                apl = (minValue.intValue()/100f);
            if(maxValue.floatValue() > 0)
                aph = (maxValue.intValue()/100f);
            mPresenter.updateFilter(apl, aph, currentValue);
        });

        updateValueRangeOnView(0,100000);
    }

    private void updateValueRangeOnView(int from, int to){
        String rangeText = getText(R.string.from) + " " + from + "% - " + to + "%";
        valueRangeTV.setText(rangeText);
    }



    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleListDetail> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SaleListDetail>() {
                @Override
                public boolean areItemsTheSame(SaleListDetail oldItem,
                                               SaleListDetail newItem) {
                    return oldItem.getSaleUid() == newItem.getSaleUid();
                }

                @Override
                public boolean areContentsTheSame(SaleListDetail oldItem,
                                                  SaleListDetail newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<SaleListDetail> listProvider) {

        SaleListRecyclerAdapter recyclerAdapter =
                new SaleListRecyclerAdapter(DIFF_CALLBACK, mPresenter, false, false,
                        this, getApplicationContext());
        //A warning is expected
        DataSource.Factory<Integer, SaleListDetail> factory =
                (DataSource.Factory<Integer, SaleListDetail>)listProvider.getProvider();
        LiveData<PagedList<SaleListDetail>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void updateLocationSpinner(String[] locations) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
    }
}
