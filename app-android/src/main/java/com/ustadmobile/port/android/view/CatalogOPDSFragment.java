/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */


package com.ustadmobile.port.android.view;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CatalogModel;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * An Android Fragment that implements the CatalogView to show an OPDS Catalog
 *
 * Use newInstance to create a new Fragment and use the FragmentManager in the normal way
 *
 */
public class CatalogOPDSFragment extends UstadBaseFragment implements View.OnClickListener, View.OnLongClickListener, CatalogView, ControllerReadyListener, SwipeRefreshLayout.OnRefreshListener, DialogInterface.OnClickListener, AdapterView.OnItemSelectedListener{

    private View rootContainer;

    private Map<String, OPDSEntryCard> idToCardMap;

    protected CatalogController mCatalogController;

    private UstadJSOPDSEntry[] mSelectedEntries;

    private int mFetchFlags;

    private boolean hasDisplayed = false;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    //Trackers whether or not there is a loading operation (e.g. refresh) going on
    private boolean isLoading = false;

    protected Dialog addFeedDialog;

    private boolean mDeleteOptionAvailable;

    private boolean mAddOptionAvailable;

    private static final int MENUCMDID_ADD = 1200;

    private static final int MENUCMDID_DELETE = 1201;

    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mRecyclerAdapter;

    private RecyclerView.LayoutManager mRecyclerLayoutManager;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * One can also put in the bundle frag-menuid to set the menuid to be used : otherwise menus
     * will be set according to if the feed is acquisition or navigation type
     *
     * @return A new instance of fragment CatalogOPDSFragment.
     */
    public static CatalogOPDSFragment newInstance(Bundle args) {
        CatalogOPDSFragment fragment = new CatalogOPDSFragment();
        Bundle bundle = new Bundle();
        bundle.putAll(args);
        fragment.setArguments(bundle);

        return fragment;
    }

    public CatalogOPDSFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootContainer = inflater.inflate(R.layout.fragment_catalog_opds, container, false);
        mSelectedEntries = new UstadJSOPDSEntry[0];
        setHasOptionsMenu(true);

        idToCardMap = new WeakHashMap<String, OPDSEntryCard>();

        mFetchFlags = getArguments().getInt(CatalogController.KEY_FLAGS,
                CatalogController.CACHE_ENABLED);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootContainer.findViewById(
                R.id.fragment_catalog_swiperefreshview);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        rootContainer.findViewById(R.id.fragment_catalog_browsebutton).setOnClickListener(this);

        mRecyclerView = (RecyclerView)rootContainer.findViewById(R.id.fragment_catalog_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        rootContainer.findViewById(R.id.fragment_catalog_addbutton).setOnClickListener(this);

        loadCatalog();

        return rootContainer;
    }

    /**
     * Load the catalog from the arguments given
     */
    public void loadCatalog(Hashtable args) {
        CatalogController.makeControllerForView(this, args, this);
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "createView: " +
                args.get(CatalogController.KEY_URL) +
                args.get(CatalogController.KEY_RESMOD));
    }

    /**
     * Load the catalog with the default arguments that we got when the fragment was created
     */
    public void loadCatalog() {
        loadCatalog(UMAndroidUtil.bundleToHashtable(getArguments()));
    }


    @Override
    public void controllerReady(final UstadController controller, int flags) {
        final SwipeRefreshLayout refreshLayout = mSwipeRefreshLayout;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        isLoading = false;
        mCatalogController = (CatalogController)controller;
        setBaseController(mCatalogController);

        mRecyclerAdapter = new OPDSRecyclerAdapter(mCatalogController);

        //in case user left activity when loading was going on
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    refreshLayout.setRefreshing(false);

                }
            });
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (controller == null) {
                    String errMsg = LocaleUtil.formatMessage(
                            impl.getString(MessageIDConstants.course_catalog_load_error), "Catalog controller");
                    impl.getAppView(getActivity()).showAlertDialog(impl.getString(MessageIDConstants.error),
                            errMsg);
                } else {
                    getActivity().setTitle(mCatalogController.getModel().opdsFeed.title);
                    mCatalogController.setUIStrings();
                    setSelectedEntries(new UstadJSOPDSEntry[0]);
                    mRecyclerView.setAdapter(mRecyclerAdapter);
                    getActivity().supportInvalidateOptionsMenu();
                    mCatalogController.loadThumbnails();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!hasDisplayed) {
            hasDisplayed = true;
        }else {
            loadCatalog();
        }

    }


    /**
     * Get the OPDSEntryCard for the given OPDS Entry ID
     *
     * @param id OPDS Entry id
     * @return OPDSEntryCard representing this item
     */
    public OPDSEntryCard getEntryCardByOPDSID(String id) {
        return idToCardMap.get(id);
    }


    @Override
    public void setController(CatalogController controller) {
        this.mCatalogController = controller;
    }

    @Override
    public CatalogController getController() {
        return mCatalogController;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(mDeleteOptionAvailable && mSelectedEntries.length > 0) {
            MenuItem item = menu.add(Menu.NONE, MENUCMDID_DELETE, 1, "");
            item.setIcon(R.drawable.ic_delete_white_24dp);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENUCMDID_ADD:
                mCatalogController.handleClickAdd();
                return true;
            case R.id.action_opds_acquire:
                if(getSelectedEntries().length > 0) {
                    UstadJSOPDSFeed feed = mCatalogController.getModel().opdsFeed;
                    UstadJSOPDSFeed acquisitionFeed = new UstadJSOPDSFeed(feed.href, feed.title,
                            feed.id+"-acquire");
                    UstadJSOPDSEntry[] selectedEntries = getSelectedEntries();
                    for(int i = 0; i  <selectedEntries.length; i++) {
                        acquisitionFeed.addEntry(new UstadJSOPDSEntry(acquisitionFeed, selectedEntries[i]));
                    }

                    mCatalogController.handleClickDownload(acquisitionFeed);
                }else {
                    mCatalogController.handleClickDownloadAll();
                }

                return true;
            case MENUCMDID_DELETE:
                if(getSelectedEntries().length > 0) {
                    mCatalogController.handleClickDeleteEntries(getSelectedEntries());
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCatalogController != null) {
            mCatalogController.handleViewPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCatalogController != null) {
            mCatalogController.handleViewDestroy();
        }
    }

    public void toggleEntrySelected(OPDSEntryCard card) {
        boolean nowSelected = !card.isSelected();
        card.setSelected(nowSelected);

        //TODO: Fix this
        UstadJSOPDSEntry[] currentSelection = getSelectedEntries();
        int newArraySize = nowSelected ? currentSelection.length + 1 : currentSelection.length -1;
        UstadJSOPDSEntry[] newSelection = new UstadJSOPDSEntry[newArraySize];

        if(nowSelected) {
            //just make the new selection one larger
            System.arraycopy(currentSelection, 0, newSelection, 0, currentSelection.length);
            newSelection[newSelection.length-1] = card.getEntry();
        }else {
            for(int i = 0, elCount = 0; i < currentSelection.length; i++) {
                if(!currentSelection[i].id.equals(card.getEntry().id)) {
                    newSelection[elCount] = currentSelection[i];
                    elCount++;
                }
            }
        }

        setSelectedEntries(newSelection);
    }



    @Override
    public void onClick(View view) {
        if(view instanceof OPDSEntryCard) {
            OPDSEntryCard card = ((OPDSEntryCard)view);
            if(getSelectedEntries().length > 0) {
                toggleEntrySelected(card);
            }else {
                mCatalogController.handleClickEntry(card.getEntry());
            }
            return;
        }

        switch(view.getId()) {
            case R.id.fragment_catalog_browsebutton:
                mCatalogController.handleClickBrowseButton();
                break;
            case R.id.fragment_catalog_addbutton:
                mCatalogController.handleClickAdd();
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if(view instanceof OPDSEntryCard) {
            OPDSEntryCard card = (OPDSEntryCard)view;
            toggleEntrySelected(card);
            return true;
        }
        return false;
    }

    @Override
    public void showConfirmDialog(String title, String message, String positiveChoice, String negativeChoice, final int commandId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton(positiveChoice, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                mCatalogController.handleConfirmDialogClick(true, commandId);
            }
        });
        builder.setNegativeButton(negativeChoice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mCatalogController.handleConfirmDialogClick(false, commandId);
            }
        });
        builder.create().show();
    }


    @Override
    public void setEntryStatus(final String entryId, final int status) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if(idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setOPDSEntryOverlay(status);
                }
            }
        });
    }

    @Override
    public void setEntrythumbnail(final String entryId, String iconFileURI) {
        iconFileURI = UMFileUtil.stripPrefixIfPresent("file://", iconFileURI);

        final Bitmap bitmap = BitmapFactory.decodeFile(iconFileURI);
        final String errURI = iconFileURI;

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //TODO: idToCardMap should not be null when this is called... this should only be called after cards are made...
                if(idToCardMap != null && idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setThumbnail(bitmap);
                }
            }
        });
    }

    @Override
    public void setEntryBackground(String entryId, String backgroundFileURI) {
        //TODO: Implement setting entry background on Android
    }

    @Override
    public void setCatalogBackground(String backgroundFileURI) {
        //TODO: implement setting catalog background on Android
    }

    @Override
    public void updateDownloadAllProgress(int loaded, int total) {

    }

    @Override
    public void setDownloadEntryProgressVisible(final String entryId, final boolean visible) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if(idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setProgressBarVisible(visible);
                }
            }
        });
    }

    @Override
    public void updateDownloadEntryProgress(final String entryId, final int loaded, final int total) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                int progressPercent = Math.round(((float)loaded/(float)total) * OPDSEntryCard.PROGRESS_ENTRY_MAX);
                if(idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setDownloadProgressBarProgress(progressPercent);
                }
            }
        });
    }

    @Override
    public UstadJSOPDSEntry[] getSelectedEntries() {
        return mSelectedEntries;
    }

    @Override
    public void setSelectedEntries(UstadJSOPDSEntry[] entries) {
        if((entries.length == 0 && mSelectedEntries.length > 0) || (entries.length > 0 && mSelectedEntries.length ==0)) {
            getActivity().supportInvalidateOptionsMenu();
        }

        this.mSelectedEntries = entries;

        UstadJSOPDSFeed thisFeed = mCatalogController.getModel().opdsFeed;
        for(int i = 0; i < thisFeed.entries.length; i++) {
            boolean isSelected = false;
            for(int j = 0; j < entries.length; j++) {
                if(thisFeed.entries[i].id.equals(entries[j].id)) {
                    isSelected = true;
                    break;
                }
            }

            if(idToCardMap.containsKey(thisFeed.entries[i].id)) {
                idToCardMap.get(thisFeed.entries[i].id).setSelected(isSelected);
            }
        }

    }

    @Override
    public void refresh() {
        this.onRefresh();
    }

    /**
     * Handle when the user selects to refresh
     */
    @Override
    public void onRefresh() {
        if(!isLoading) {
            Hashtable args = UMAndroidUtil.bundleToHashtable(getArguments());
            int flagArg = args.containsKey(CatalogController.KEY_FLAGS) ? (Integer)args.get(CatalogController.KEY_FLAGS) : 0;
            flagArg = flagArg | CatalogController.CACHE_DISABLED;
            args.put(CatalogController.KEY_FLAGS, flagArg);
            loadCatalog(args);
        }
    }

    @Override
    public void setBrowseButtonVisible(boolean buttonVisible) {
        this.rootContainer.findViewById(R.id.fragment_catalog_browsebutton).setVisibility(
                buttonVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBrowseButtonLabel(String browseButtonLabel) {
        ((Button)this.rootContainer.findViewById(R.id.fragment_catalog_browsebutton)).setText(browseButtonLabel);
    }

    @Override
    public void setDeleteOptionAvailable(boolean deleteOptionAvailable) {
        this.mDeleteOptionAvailable = deleteOptionAvailable;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setAddOptionAvailable(final boolean addOptionAvailable) {
        this.mAddOptionAvailable = addOptionAvailable;
        getActivity().runOnUiThread( new Runnable() {
            public void run() {
                rootContainer.findViewById(R.id.fragment_catalog_addbutton).setVisibility(
                        addOptionAvailable ? View.VISIBLE : View.GONE);
            }
        });

        //getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showAddFeedDialog() {
        AddFeedDialogFragment dialogFragment = new AddFeedDialogFragment();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "AddFeedDialog");
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

    @Override
    public void setAddFeedDialogTextFieldsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        addFeedDialog.findViewById(R.id.basepoint_addfeed_title).setVisibility(visibility);
        addFeedDialog.findViewById(R.id.basepoint_addfeed_url).setVisibility(visibility);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
        mCatalogController.handleFeedPresetSelected(index);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //do nothing
    }

    /**
     * Handle when the user has clicked a button on the dialog: to either add the feed or
     * to cancel
     *
     * @param dialogInterface
     * @param id
     */
    //@Override
    public void onClick(DialogInterface dialogInterface, int id) {
        if(id == DialogInterface.BUTTON_POSITIVE) {
            mCatalogController.handleAddFeed(getAddFeedDialogURL(), getAddFeedDialogTitle());
        }else if(id == DialogInterface.BUTTON_NEGATIVE) {
            //cancel it

        }
        dialogInterface.dismiss();
        addFeedDialog = null;
    }


    public class OPDSRecyclerAdapter extends RecyclerView.Adapter<OPDSRecyclerAdapter.ViewHolder> {

        private CatalogController controller;

        private UstadJSOPDSFeed feed;

        public OPDSRecyclerAdapter(CatalogController controller) {
            this.controller = controller;
            this.feed = controller.getModel().opdsFeed;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public OPDSEntryCard mEntryCard;

            public ViewHolder(OPDSEntryCard entryCard) {
                super(entryCard);
                mEntryCard = entryCard;
            }
        }

        @Override
        public OPDSRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            OPDSEntryCard cardView  = (OPDSEntryCard) LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fragment_opds_item, null);
            return new ViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(OPDSRecyclerAdapter.ViewHolder holder, int position) {
            holder.mEntryCard.setOPDSEntry(feed.entries[position]);
            holder.mEntryCard.setOnClickListener(CatalogOPDSFragment.this);
            holder.mEntryCard.setOnLongClickListener(CatalogOPDSFragment.this);

            //check the acquisition status
            int entryStatus = controller.getEntryAcquisitionStatus(feed.entries[position].id);
            if(entryStatus != -1) {
                holder.mEntryCard.setOPDSEntryOverlay(entryStatus);
            }

            if(entryStatus == CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS) {
                holder.mEntryCard.setProgressBarVisible(true);
            }

            //Make sure if this entry is being recycled the idToCardMap won't get confused
            if(idToCardMap.containsValue(holder.mEntryCard)) {
                Iterator<Map.Entry<String, OPDSEntryCard>> iterator = idToCardMap.entrySet().iterator();
                Map.Entry<String, OPDSEntryCard> entry;
                while(iterator.hasNext()) {
                    entry = iterator.next();
                    if(entry.getValue().equals(holder.mEntryCard)) {
                        iterator.remove();
                    }
                }
            }

            idToCardMap.put(feed.entries[position].id, holder.mEntryCard);
        }

        public int getItemCount() {
            return feed != null ? feed.entries.length : 0;
        }
    }


    public static class AddFeedDialogFragment extends DialogFragment {

        private Dialog dialog;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setTitle(impl.getString(MessageIDConstants.add_library));
            View dialogView =inflater.inflate(R.layout.dialog_basepoint_addfeed, null);
            builder.setView(dialogView);
            Fragment frag = getActivity().getSupportFragmentManager().findFragmentByTag(CatalogActivity.FRAGMENT_CATALOG_TAG);
            if(frag != null && frag instanceof CatalogOPDSFragment) {
                CatalogOPDSFragment catFrag = (CatalogOPDSFragment)frag;
                builder.setNegativeButton(impl.getString(MessageIDConstants.cancel), catFrag);
                builder.setPositiveButton(impl.getString(MessageIDConstants.add), catFrag);
                ((EditText)dialogView.findViewById(R.id.basepoint_addfeed_title)).setHint(impl.getString(MessageIDConstants.library_title));
                ((EditText)dialogView.findViewById(R.id.basepoint_addfeed_url)).setHint(impl.getString(MessageIDConstants.library_url));
                //TODO: Fix me
                String[] presetTitles = catFrag.mCatalogController.getFeedList(
                        CatalogController.OPDS_FEEDS_INDEX_TITLE);
                ArrayAdapter<String> adapter= new ArrayAdapter<>(getContext(),
                        R.layout.item_basepoint_dialog_spinneritem, presetTitles);
                Spinner presetsSpinner = ((Spinner)dialogView.findViewById(
                        R.id.basepoint_addfeed_src_spinner));
                presetsSpinner.setAdapter(adapter);
                dialog = builder.create();
                catFrag.addFeedDialog = dialog;
                presetsSpinner.setOnItemSelectedListener(catFrag);
            }else {
                throw new IllegalArgumentException("");
            }

            return dialog;
        }


    }

}
