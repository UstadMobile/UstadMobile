package com.ustadmobile.port.android.view;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.BasePointActivity2Presenter;
import com.ustadmobile.core.view.BasePointView2;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.util.WeakHashMap;


/**
 * The new Base Point screen. This Activity has a bottom Navigation bar with buttons. We are using
 * an external component to achieve this vs Google's own (some differences when you start adding
 * more than 3).
 *
 * This Activity extends UstadBaseActivity and implements BasePointView
 */
public class BasePointActivity2 extends UstadBaseActivity implements BasePointView2 {

    private ViewPager mPager;
    private BasePointViewPagerAdapter mPagerAdapter;

    private Toolbar toolbar;

    private AlertDialog shareAppDialog;

    private BasePointActivity2Presenter mPresenter;

    private Menu mOptionsMenu;

    private ClazzListFragment classesFragment;
    private PeopleListFragment peopleListFragment;
    private FeedListFragment newFrag;

    public static final int VIEW_POSITION_POSITION_FEED = 0;
    public static final int VIEW_POSITION_POSITION_CLASSES = 1;
    public static final int VIEW_POSITION_POSITION_PEOPLE = 2;
    public static final int VIEW_POSITION_POSITION_REPORTS = 3;

    /**
     * ViewPager set up in its own method for clarity.
     */
    private void setupViewPager() {
        mPager = findViewById(R.id.container_feedlist);
        mPagerAdapter = new BasePointViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * The overridden onCreate method does the following:
     *
     * 1. Creates, names, styles and sets the Bottom Navigation
     * 2. Sets the default location (Feed)
     * 3. Sets the toolbar title upon navigation
     *
     * @param savedInstanceState        The application's bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_base_point2);

        //set up view pager
        setupViewPager();

        //Toolbar
        toolbar = findViewById(R.id.base_point_2_toolbar);
        toolbar.setTitle("Ustad Mobile");
        setSupportActionBar(toolbar);

        mPresenter = new BasePointActivity2Presenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Get the bottom navigation component.
        AHBottomNavigation bottomNavigation = findViewById(R.id.bottom_navigation);

        //Style
        bottomNavigation.setDefaultBackgroundColor(fetchColor(R.color.primary));
        bottomNavigation.setAccentColor(fetchColor(R.color.just_black));
        bottomNavigation.setInactiveColor(fetchColor(R.color.bottom_nav_yourInactiveColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setUseElevation(true, 2L);

        //Create the items to be added
        AHBottomNavigationItem feed_item =
                new AHBottomNavigationItem(R.string.feed,
                        R.drawable.ic_today_black_48dp, R.color.default_back_color);
        AHBottomNavigationItem classes_item =
                new AHBottomNavigationItem(R.string.classes,
                        R.drawable.ic_people_black_48dp, R.color.default_back_color);
        AHBottomNavigationItem people_item =
                new AHBottomNavigationItem(R.string.people,
                        R.drawable.ic_person_black_24dp, R.color.default_back_color);

        //Add the items
        bottomNavigation.addItem(feed_item);
        bottomNavigation.addItem(classes_item);
        bottomNavigation.addItem(people_item);

        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {

            if (!wasSelected) {
                //mPagerAdapter.notifyDataSetChanged();
                mPagerAdapter.getItem(position);
                //mPagerAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(position);
            }

            //Update title
            switch(position){
                case 0:
                    updateTitle(getText(R.string.feed).toString());
                    break;
                case 1:
                    updateTitle(getText(R.string.my_classes).toString());
                    break;
                case 2:
                    updateTitle(getText(R.string.people).toString());
                    break;

            }
            return true;
        });

        // Setting the very 1st item as default home screen.
        bottomNavigation.setCurrentItem(0);

    }


    @Override
    public void shareAppSetupFile(String filePath) {
        String applicationId = getPackageName();
        Uri sharedUri = FileProvider.getUriForFile(this,
                applicationId+".fileprovider",
                new File(filePath));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }

        dismissShareAppDialog();
    }

    @Override
    public void showBulkUploadForAdmin(boolean show) {
        MenuItem bulkUploadMenuItem = mOptionsMenu.findItem(R.id.menu_basepoint_bulk_upload_master);
        if(bulkUploadMenuItem != null){
            bulkUploadMenuItem.setVisible(show);
        }
    }

    @Override
    public void showSettings(boolean show) {

        MenuItem allClazzSettingsMenuItem = mOptionsMenu.findItem(R.id.menu_settings_gear);
        if(allClazzSettingsMenuItem != null){
            allClazzSettingsMenuItem.setVisible(show);
        }
    }

    @Override
    public void updatePermissionCheck() {
        if(classesFragment != null) {
            classesFragment.forceCheckPermissions();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        //If this activity started from other activity
        if (i == R.id.menu_basepoint_share) {
            mPresenter.handleClickShareIcon();
        } else if (i == R.id.menu_basepoint_bulk_upload_master){
            mPresenter.handleClickBulkUpload();
        }
        else if(i==R.id.menu_basepoint_logout){
            finishAffinity();
            mPresenter.handleLogOut();
        }
        else if ( i == R.id.menu_settings_gear){
            mPresenter.handleClickSettingsIcon();
        }
        else if( i == R.id.menu_basepoint_search){
            mPresenter.handleClickSearchIcon();
        }
        else if( i == R.id.menu_basepoint_about){
            mPresenter.handleClickAbout();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share, menu);

        //tint
        MenuItem shareMenuItem = menu.findItem(R.id.menu_basepoint_share);
        MenuItem bulkUploadMenuItem = menu.findItem(R.id.menu_basepoint_bulk_upload_master);
        MenuItem settingsMenuItem = menu.findItem(R.id.menu_settings_gear);
        MenuItem logoutMenuItem = menu.findItem(R.id.menu_basepoint_logout);

        Drawable shareMenuIcon = AppCompatResources.getDrawable(getApplicationContext(),
                R.drawable.ic_share_white_24dp);
        Drawable bulkUploadMenuIcon =
                AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_file_upload_white_24dp);
        Drawable settingsMenuIcon =
                AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_settings_white_24dp);
        Drawable logoutMenuIcon =
                AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_dropout_bcd4_24dp);

        assert shareMenuIcon != null;
        shareMenuIcon.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_IN);
        assert bulkUploadMenuIcon != null;
        bulkUploadMenuIcon.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_IN);
        assert settingsMenuIcon != null;
        settingsMenuIcon.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_IN);
        assert logoutMenuIcon != null;
        logoutMenuIcon.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_IN);

        shareMenuItem.setIcon(shareMenuIcon);
        bulkUploadMenuItem.setIcon(bulkUploadMenuIcon);
        settingsMenuItem.setIcon(settingsMenuIcon);
        logoutMenuItem.setIcon(logoutMenuIcon);

        mOptionsMenu = menu;
        mPresenter.getLoggedInPerson();

        //Search stuff
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_basepoint_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));

        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                switch (mPager.getCurrentItem()){
                    case VIEW_POSITION_POSITION_FEED:
                        break;
                    case VIEW_POSITION_POSITION_CLASSES:
                        classesFragment.searchClasses(query);
                        break;
                    case VIEW_POSITION_POSITION_PEOPLE:
                        peopleListFragment.searchPeople(query);
                        break;
                    case VIEW_POSITION_POSITION_REPORTS:
                        break;
                    default:
                        break;
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed

                // filter recycler view when query submitted
                switch (mPager.getCurrentItem()){
                    case VIEW_POSITION_POSITION_FEED:
                        break;
                    case VIEW_POSITION_POSITION_CLASSES:
                        classesFragment.searchClasses(query);
                        break;
                    case VIEW_POSITION_POSITION_PEOPLE:
                        peopleListFragment.searchPeople(query);
                        break;
                    case VIEW_POSITION_POSITION_REPORTS:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });


        searchView.setOnCloseListener(() -> {

            // filter recycler view when query submitted
            switch (mPager.getCurrentItem()){
                case VIEW_POSITION_POSITION_FEED:
                    break;
                case VIEW_POSITION_POSITION_CLASSES:
                    classesFragment.searchClasses("");
                    break;
                case VIEW_POSITION_POSITION_PEOPLE:
                    peopleListFragment.searchPeople("");
                    break;
                case VIEW_POSITION_POSITION_REPORTS:
                    break;
                default:
                    break;
            }
            return false;
        });
        return true;
    }

    /**
     * Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    public void updateTitle(String title){
        toolbar.setTitle(title);
    }

    @Override
    public void showShareAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.share_application);
        builder.setView(R.layout.fragment_share_app_dialog);
        builder.setPositiveButton(R.string.share, null);
        builder.setNegativeButton(R.string.cancel, null);
        shareAppDialog = builder.create();
        shareAppDialog.setOnShowListener(dialogInterface -> {
            Button okButton = shareAppDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(v -> mPresenter.handleConfirmShareApp());
        });
        shareAppDialog.show();
    }

    @Override
    public void dismissShareAppDialog() {
        shareAppDialog.dismiss();
        shareAppDialog=null;
    }

    /**
     * Feed view pager's Adapter
     */
    private class BasePointViewPagerAdapter extends FragmentStatePagerAdapter{

        //Map of position and fragment
        WeakHashMap<Integer, UstadBaseFragment> positionMap;

        //Constructor creates the adapter
        BasePointViewPagerAdapter(FragmentManager fm) {
            super(fm);
            positionMap = new WeakHashMap<>();
        }

        /**
         * Generate fragment for that page/position
         *
         * @param position  position of item
         * @return  the fragment
         */
        @Override
        public Fragment getItem(int position) {
            UstadBaseFragment thisFragment = positionMap.get(position);
            if(thisFragment != null){
                return thisFragment;
            }else{
                switch(position){
                    case 0:
                        newFrag =
                                FeedListFragment.newInstance();
                        this.positionMap.put(position, newFrag);
                        return newFrag;
                    case 1:
                        classesFragment =
                                ClazzListFragment.newInstance();
                        this.positionMap.put(position, classesFragment);
                        return classesFragment;

                    case 2:
                        peopleListFragment =
                                PeopleListFragment.newInstance();
                        this.positionMap.put(position, peopleListFragment);
                        return peopleListFragment;

                    default:
                        return null;
                }
            }
        }

        @Override
        public int getCount() {
            //return positionMap.size();
            return 3;
        }
    }

    /**
     * Get color from ContextCompat
     *
     * @param color The color code
     * @return  the color
     */
    public int fetchColor(int color){
        return ContextCompat.getColor(this, color);
    }


}
