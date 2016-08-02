package com.ustadmobile.port.android.view;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Hashtable;
import java.util.WeakHashMap;

public class BasePointActivity extends UstadBaseActivity implements BasePointView, DialogInterface.OnClickListener, Spinner.OnItemSelectedListener {

    protected BasePointController mBasePointController;

    protected BasePointPagerAdapter mPagerAdapter;

    private int[] tabIconsIds = new int[]{R.drawable.ic_sd_card_black_24dp,
            R.drawable.ic_public_black_24dp, R.drawable.ic_group_black_24dp};

    protected Dialog addFeedDialog;

    protected boolean classListVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_point);
        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        //make OPDS fragments and set them here
        mBasePointController = BasePointController.makeControllerForView(this, args);
        setBaseController(mBasePointController);
        setUMToolbar();

        mPagerAdapter = new BasePointPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)findViewById(R.id.basepoint_pager);
        viewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.basepoint_tabs);
        tabLayout.setupWithViewPager(viewPager);
        for(int i = 0; i < mPagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setIcon(tabIconsIds[i]);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_basepoint_addfeed:
                mBasePointController.handleClickAddFeed();
                return true;
            case R.id.action_basepoint_removefeed:
                CatalogOPDSFragment opdsFragment = (CatalogOPDSFragment)mPagerAdapter.getItem(
                        BasePointController.INDEX_BROWSEFEEDS);
                mBasePointController.handleRemoveItemsFromUserFeed(
                        opdsFragment.getSelectedEntries());
                opdsFragment.setSelectedEntries(new UstadJSOPDSEntry[0]);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showAddFeedDialog() {
        AddFeedDialogFragment dialogFragment = new AddFeedDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "AddFeedDialog");
    }

    @Override
    public void setAddFeedDialogURL(String url) {
        ((EditText)addFeedDialog.findViewById(R.id.basepoint_addfeed_url)).setText(url);
    }

    @Override
    public String getAddFeedDialogURL() {
        return ((EditText)addFeedDialog.findViewById(R.id.basepoint_addfeed_url)).getText().toString();
    }

    @Override
    public String getAddFeedDialogTitle() {
        return ((EditText)addFeedDialog.findViewById(R.id.basepoint_addfeed_title)).getText().toString();
    }

    @Override
    public void setAddFeedDialogTitle(String title) {
        ((EditText)addFeedDialog.findViewById(R.id.basepoint_addfeed_title)).setText(title);
    }

    /**
     * Handle when the user has clicked a button on the dialog: to either add the feed or
     * to cancel
     *
     * @param dialogInterface
     * @param id
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int id) {
        if(id == DialogInterface.BUTTON_POSITIVE) {
            mBasePointController.handleAddFeed(getAddFeedDialogURL(), getAddFeedDialogTitle());
        }else if(id == DialogInterface.BUTTON_NEGATIVE) {
            //cancel it

        }
        dialogInterface.dismiss();
        addFeedDialog = null;
    }

    @Override
    public void refreshCatalog(int column) {
        ((CatalogOPDSFragment) mPagerAdapter.getItem(column)).loadCatalog();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
        mBasePointController.handleFeedPresetSelected(index);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void setClassListVisible(boolean visible) {
        this.classListVisible= visible;
    }


    public static class AddFeedDialogFragment extends DialogFragment {

        private Dialog dialog;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setTitle("Add Feed");
            View dialogView =inflater.inflate(R.layout.dialog_basepoint_addfeed, null);
            builder.setView(dialogView);
            BasePointActivity activity = (BasePointActivity) getActivity();
            builder.setNegativeButton("Cancel", activity);
            builder.setPositiveButton("Add", activity);
            String[] presetTitles = activity.mBasePointController.getFeedList(
                BasePointController.OPDS_FEEDS_INDEX_TITLE);
            ArrayAdapter<String> adapter= new ArrayAdapter<>(activity,
                    R.layout.item_basepoint_dialog_spinneritem, presetTitles);
            Spinner presetsSpinner = ((Spinner)dialogView.findViewById(
                    R.id.basepoint_addfeed_src_spinner));
            presetsSpinner.setAdapter(adapter);
            presetsSpinner.setOnItemSelectedListener(activity);
            dialog = builder.create();
            activity.addFeedDialog = dialog;
            return dialog;
        }


    }


    public class BasePointPagerAdapter extends FragmentStatePagerAdapter {

        private int[] tabTitles = new int[]{MessageIDConstants.downloaded_items, MessageIDConstants.browse_feeds, MessageIDConstants.classes};

        private WeakHashMap<Integer, Fragment> fragmentMap;

        public BasePointPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentMap = new WeakHashMap<>();
        }


        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragmentMap.get(position);
            if(fragment == null) {
                Bundle bundle = null;
                switch(position) {
                    case BasePointController.INDEX_BROWSEFEEDS:
                    case BasePointController.INDEX_DOWNLOADEDENTRIES:
                        Hashtable posArgs =
                                BasePointActivity.this.mBasePointController.getCatalogOPDSArguments(position);
                        bundle = UMAndroidUtil.hashtableToBundle(posArgs);
                        if(position == BasePointController.INDEX_BROWSEFEEDS) {
                            bundle.putInt(CatalogOPDSFragment.ARG_MENUID, R.menu.menu_basepoint_remotefeeds);
                        }
                        fragment = CatalogOPDSFragment.newInstance(bundle);
                        break;
                    case BasePointController.INDEX_CLASSES:
                        bundle = new Bundle();
                        fragment = ClassListFragment.newInstance(bundle);
                        break;
                }
                fragmentMap.put(position, fragment);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return BasePointActivity.this.classListVisible ? 3 : 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return UstadMobileSystemImpl.getInstance().getString(tabTitles[position]);
        }


    }
}
