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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonListSearchPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PersonListSearchView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class PersonListSearchActivity extends UstadBaseActivity implements PersonListSearchView {


    private RecyclerView mRecyclerView;
    private PersonListSearchPresenter mPresenter;
    private SearchView searchView;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_person_list_search);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.activity_person_list_search_toolbar);
        toolbar.setTitle(R.string.people);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_person_list_search_rv);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Presenter
        mPresenter = new PersonListSearchPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

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
                // filter recycler view when query submitted
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
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
    public void setPeopleListProvider(UmProvider<PersonWithEnrollment> listProvider) {

        PersonWithEnrollmentRecyclerAdapter recyclerAdapter =
                new PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, getApplicationContext(),
                        this, mPresenter, false, false);
        //A warning is expected
        DataSource.Factory<Integer, PersonWithEnrollment> factory =
                (DataSource.Factory<Integer, PersonWithEnrollment>)listProvider.getProvider();
        LiveData<PagedList<PersonWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<PersonWithEnrollment> DIFF_CALLBACK2 =
        new DiffUtil.ItemCallback<PersonWithEnrollment>() {
            @Override
            public boolean areItemsTheSame(PersonWithEnrollment oldItem,
                                           PersonWithEnrollment newItem) {
                return oldItem.getPersonUid() == newItem.getPersonUid();
            }

            @Override
            public boolean areContentsTheSame(PersonWithEnrollment oldItem,
                                              PersonWithEnrollment newItem) {
                return oldItem.equals(newItem);
            }
        };
}
