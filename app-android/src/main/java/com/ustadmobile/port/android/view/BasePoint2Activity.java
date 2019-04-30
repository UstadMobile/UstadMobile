package com.ustadmobile.port.android.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.BasePoint2Presenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePoint2View;
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import androidx.work.WorkManager;

import static com.ustadmobile.port.android.util.ColorUtil.getContextCompatColorFromColor;


public class BasePoint2Activity extends UstadBaseActivity implements BasePoint2View {

    private Toolbar toolbar;

    private ViewPager mPager;
    private BasePointViewPagerAdapter mPagerAdapter;

    //Share app alert dialog
    private AlertDialog shareAppDialog;

    private BasePoint2Presenter mPresenter;

    private Menu mOptionsMenu;

    private SaleListFragment saleListFragment;
    private ComingSoonFragment comingSoonFragment;

    public static final int VIEW_POSITION_POSITION_CATALOG = 0;
    public static final int VIEW_POSITION_POSITION_INVENTORY = 1;
    public static final int VIEW_POSITION_POSITION_SALES = 2;
    public static final int VIEW_POSITION_POSITION_COURSES = 3;

    AHBottomNavigation bottomNavigation;
    private ActionBar ab;



    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_basepoint, menu);
        mOptionsMenu = menu;
        return true;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_basepoint2);

        //set up view pager.
        setUpViewPager();

        //Set up Toolbar:
        toolbar = findViewById(R.id.activity_basepoint2_toolbar);
        toolbar.setTitle("Ustad Mobile");
        setSupportActionBar(toolbar);

        ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_account_circle_white_36dp);
        ab.setDisplayHomeAsUpEnabled(true);



        //Call the Presenter
        mPresenter = new BasePoint2Presenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        //Get the bottom navigation component
        bottomNavigation = findViewById(R.id.activity_basepoint2_bottom_navigation);

        //Style it
        bottomNavigation.setDefaultBackgroundColor(getContextCompatColorFromColor(R.color.primary, getApplicationContext()));
        bottomNavigation.setAccentColor(getContextCompatColorFromColor(R.color.text_primary, getApplicationContext()));
        bottomNavigation.setInactiveColor(getContextCompatColorFromColor(R.color.bottom_navigation_unselected, getApplicationContext()));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setNotificationBackgroundColor(getContextCompatColorFromColor(R.color.text_primary, getApplicationContext()));
        bottomNavigation.setUseElevation(true, 2L);

        //Create the items to be added
        AHBottomNavigationItem catalog_item =
                new AHBottomNavigationItem(R.string.catalog,
                        R.drawable.ic_list_black_24dp, R.color.default_back_color);
        AHBottomNavigationItem inventory_item =
                new AHBottomNavigationItem(R.string.inventory,
                        R.drawable.ic_assignment_black_24dp, R.color.default_back_color);
        AHBottomNavigationItem sales_item =
                new AHBottomNavigationItem(R.string.sales,
                        R.drawable.ic_payment_note_cash_black_24dp, R.color.default_back_color);
        AHBottomNavigationItem courses_item =
                new AHBottomNavigationItem(R.string.courses,
                        R.drawable.ic_collections_bookmark_black_24dp, R.color.default_back_color);

        bottomNavigation.addItem(catalog_item);
        bottomNavigation.addItem(inventory_item);
        bottomNavigation.addItem(sales_item);
        bottomNavigation.addItem(courses_item);



        //Telling navigation to always show the text on the items. Unlike Google's
        // own implementation.
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        //Click listeners for the items.
        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {

            if (!wasSelected) {
                mPagerAdapter.getItem(position);
                mPager.setCurrentItem(position);
            }

            //Update title
            switch(position){
                case 0:
                    updateTitle(getText(R.string.catalog).toString());
                    break;
                case 1:
                    updateTitle(getText(R.string.inventory).toString());
                    break;
                case 2:
                    updateTitle(getText(R.string.sales).toString());
                    break;
                case 3:
                    updateTitle(getText(R.string.courses).toString());
                    break;

            }
            return true;
        });

        // Setting the very 1st item as default home screen.
        bottomNavigation.setCurrentItem(2);

        mPresenter.updateDueCountOnView();

    }



    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void updateImageOnView(String imagePath){
        File output = new File(imagePath);

        if (output.exists()) {
            Uri profileImage = Uri.fromFile(output);


            int iconDimen = dpToPx(36);
            runOnUiThread(() -> Picasso.get()
                    .load(profileImage)
                    .transform(new CircleTransform())
                    .resize(iconDimen, iconDimen)
                    .centerCrop()
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Drawable d = new BitmapDrawable(getResources(), bitmap);
                            ab.setHomeAsUpIndicator(d);
                            ab.setDisplayHomeAsUpEnabled(true);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    }));

        }

    }

    private void setUpViewPager(){
        mPager = findViewById(R.id.activity_basepoint2_viewpager);
        mPagerAdapter = new BasePointViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * Updates the toolbar's title
     * @param title The string of the title to be set to the toolbar
     */
    public void updateTitle(String title){
        toolbar.setTitle(title);
    }

    @Override
    public void showCatalog(boolean show) {

    }

    @Override
    public void showInventory(boolean show) {

    }

    @Override
    public void showSales(boolean show) {

    }

    @Override
    public void showCourses(boolean show) {

    }

    @Override
    public void shareAppSetupFile(String filePath) {

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
        int i = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                mPresenter.handleClickPersonIcon();
                return true;
        }
        if( i == R.id.menu_basepoint_about){
            mPresenter.handleClickAbout();
        }
        if(i == R.id.menu_basepoint_sync){
            forceSync();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void forceSync() {

        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG);
        UmAppDatabaseSyncWorker.queueSyncWorker(100, TimeUnit.MILLISECONDS);

        updateSyncing();
    }

    public void updateSyncing(){
        if(mOptionsMenu == null)
            return;

        MenuItem syncItem = mOptionsMenu.findItem(R.id.menu_basepoint_sync);
        if(syncItem==null)
            return;

        String syncingString = getString(R.string.syncing);
        syncItem.setTitle(syncingString);
        //syncItem.setActionView(R.color.enable_disable_text);
        //syncItem.getActionView().setBackgroundResource(R.color.enable_disable_text);
        SpannableString textWithColor = new SpannableString(syncingString);
        textWithColor.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_secondary)), 0,
                textWithColor.length(), 0);
        syncItem.setTitle(textWithColor);
        syncItem.setEnabled(false);
    }

    @Override
    public void sendToast(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String message = impl.getString(messageId, getContext());

        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        ).show());

    }

    @Override
    public void checkPermissions() {

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

    @Override
    public void updateNotificationForSales(int number) {
        //Sprint 2
        //Send notification to 2nd last item (sales)
        String nString = String.valueOf(number);
        if (number == 0) {
            nString = "";
        }
        bottomNavigation.setNotification(nString,
                bottomNavigation.getItemsCount() - 2);
    }

    /**
     * Feed view pager's Adapter
     */
    public class BasePointViewPagerAdapter extends FragmentStatePagerAdapter {

        //Map of position and fragment
        private WeakHashMap<Integer, UstadBaseFragment> positionMap;
        private static final int BASEPOINT_ITEM_COUNT = 4;

        private

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
                        comingSoonFragment = ComingSoonFragment.newInstance();
                        this.positionMap.put(position, comingSoonFragment);
                        return comingSoonFragment;
                    case 1:
                        comingSoonFragment = ComingSoonFragment.newInstance();
                        this.positionMap.put(position, comingSoonFragment);
                        return comingSoonFragment;
                    case 2:
                        saleListFragment = SaleListFragment.newInstance();
                        this.positionMap.put(position, saleListFragment);
                        return saleListFragment;
                    case 3:
                        comingSoonFragment = ComingSoonFragment.newInstance();
                        this.positionMap.put(position, comingSoonFragment);
                        return comingSoonFragment;
                    default:
                        return null;

                }
            }

        }

        @Override
        public int getCount() {

            return BASEPOINT_ITEM_COUNT;
        }
    }
}
