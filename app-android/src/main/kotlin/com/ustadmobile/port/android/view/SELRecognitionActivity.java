package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELRecognitionPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELRecognitionView;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELRecognition activity. This Activity extends UstadBaseActivity and implements
 * SELRecognitionView. This activity is responsible for showing and handling recognition which is
 * merely a toggle on people blob lists and it will only allow to proceed if recognition check box
 * is explicitly checked.
 */
public class SELRecognitionActivity extends UstadBaseActivity implements SELRecognitionView {

    private RecyclerView mRecyclerView;
    private SELRecognitionPresenter mPresenter;

    /**
     * The DIFF callback
     */
    public static final DiffUtil.ItemCallback<PersonWithPersonPicture> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PersonWithPersonPicture>() {
                @Override
                public boolean areItemsTheSame(PersonWithPersonPicture oldItem,
                                               PersonWithPersonPicture newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(PersonWithPersonPicture oldItem,
                                                  PersonWithPersonPicture newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is Handling
     *  back button pressed.
     *
     * @param item  The menu item that was selected / clicked
     * @return      true if accounted for.
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

    /**
     * Sets the people blob list recycler adapter to the recycler view and observes it.
     *
     * @param listProvider The provider data
     */
    @Override
    public void setListProvider(UmProvider<PersonWithPersonPicture> listProvider) {

        // Specify the mAdapter
        PeopleBlobListRecyclerAdapter recyclerAdapter =
                new PeopleBlobListRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        mPresenter, true);

        // get the provider, set , observe, etc.
        // A warning is expected.
        DataSource.Factory<Integer, PersonWithPersonPicture> factory =
                (DataSource.Factory<Integer, PersonWithPersonPicture>)
                        listProvider.getProvider();
        LiveData<PagedList<PersonWithPersonPicture>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * In Order:
     *      1. Sets layout
     *      2. Sets toolbar
     *      3. Sets Recycler View
     *      4. Sets layout of Recycler View with Grid (for people blobs)
     *      5. Instantiates the presenter and calls it's onCreate()
     *      6. Sets the Floating action button (that starts the SEL) to presenter's method along
     *      with the value of recognized checkbox.
     *
     * @param savedInstanceState    The application bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_recognition);

        //Toolbar:
        Toolbar toolbar = findViewById(R.id.activity_sel_recognition_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_recognition_recyclerview);
        //View people blobs as a grid
        RecyclerView.LayoutManager mRecyclerLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELRecognitionPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        CheckBox recognizedCheckBox = findViewById(R.id.activity_sel_recognition_checkbox);

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_recognition_fab);
        fab.setOnClickListener(v ->
                mPresenter.handleClickPrimaryActionButton(recognizedCheckBox.isChecked()));

    }

}
