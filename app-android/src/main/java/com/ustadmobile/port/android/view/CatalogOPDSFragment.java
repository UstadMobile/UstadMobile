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


import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.recyclerview.extensions.DiffCallback;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.opds.OpdsFilterOptions;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * An Android Fragment that implements the CatalogView to show an OPDS Catalog
 *
 * Use newInstance to create a new Fragment and use the FragmentManager in the normal way
 *
 */
public class CatalogOPDSFragment extends UstadBaseFragment implements View.OnClickListener,
        View.OnLongClickListener, CatalogView, SwipeRefreshLayout.OnRefreshListener{

    private View rootContainer;

    private Map<String, OPDSEntryCard> idToCardMap;

    private Map<String, String> idToThumbnailUrlMap;

    private Map<String, Integer> idToStatusMap = new Hashtable<>();

    private Map<String, Boolean> idToProgressVisibleMap = new Hashtable<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean mDeleteOptionAvailable;

    private boolean mAddOptionAvailable;

    private static final int MENUCMDID_ADD = 1200;

    private static final int MENUCMDID_DELETE = 1201;

    private static final int MENUCMD_SHARE = 1202;

    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mRecyclerAdapter;

    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private boolean isRequesting=false;

    private String[] alternativeTranslationLanguages;

    private int alterantiveTranslationLanguagesDisabledItemIndex = -1;

    private ArrayList<MenuItem> alternativeTranslationLanguageMenuItems;

    private CatalogPresenter mCatalogPresenter;

    private OpdsFilterOptions filterOptions;

    private CatalogOPDSFragmentListener catalogOpdsFragmentListener;

    private Set<String> selectedUuids;

    private Set<OpdsEntryRecyclerAdapter.OpdsEntryViewHolder> boundViewHolders;

    /**
     * This interface *should* be implemented by any activity that holds a catalog fragment. The
     * fragment will fire events that instruct the activity to update the filter bar and
     * the visibility of the floating action button.
     */
    public interface CatalogOPDSFragmentListener {

        /**
         * Notifies the hosting activity that the filter options have been updated (e.g. loaded).
         * The activity should call getFilterOptions to get the current filter options
         *
         * @see CatalogOPDSFragment#getFilterOptions()
         * @param catalogFragment
         */
        void filterOptionsUpdated(CatalogOPDSFragment catalogFragment);

        /**
         * Notifies the hosting activity to show/hide the floating action button for adding a feed.
         * The activity should call isAddOptionAvaialble to see if the floating action button
         * should be shown.
         *
         * @see CatalogOPDSFragment#isAddOptionAvailable()
         * @param catalogFragment
         */
        void updateAddFabVisibility(CatalogOPDSFragment catalogFragment);

    }

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

    private BroadcastReceiver broadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootContainer = inflater.inflate(R.layout.fragment_catalog_opds, container, false);
        setHasOptionsMenu(true);

        idToCardMap = new WeakHashMap<>();
        idToThumbnailUrlMap = new HashMap<>();

        mSwipeRefreshLayout = rootContainer.findViewById(R.id.fragment_catalog_swiperefreshview);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        rootContainer.findViewById(R.id.fragment_catalog_footer_button).setOnClickListener(this);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_catalog_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        mCatalogPresenter = new CatalogPresenter(getContext(), this);

        mCatalogPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()),
                UMAndroidUtil.bundleToHashtable(savedInstanceState));

        selectedUuids = new HashSet<>();
        boundViewHolders = new HashSet<>();

        return rootContainer;
    }

    @Override
    public void setEntryProvider(UmProvider<OpdsEntryWithRelations> entryProvider) {
        OpdsEntryRecyclerAdapter adapter = new OpdsEntryRecyclerAdapter();
        DataSource.Factory factory = (DataSource.Factory)entryProvider.getProvider();
        LiveData<PagedList<OpdsEntryWithRelations>> data =  new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, adapter::setList);
        mRecyclerView.setAdapter(adapter);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        mCatalogPresenter.onStop();
    }

    @Override
    public void onAttach(Context context) {
        if(context instanceof CatalogOPDSFragmentListener) {
            this.catalogOpdsFragmentListener = (CatalogOPDSFragmentListener)context;
        }

        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        catalogOpdsFragmentListener = null;
    }

    /**
     * Get the OPDSEntryCard for the given OPDS Entry ID
     *
     * @param id OPDS Entry uuid
     * @return OPDSEntryCard representing this item
     */
    public OPDSEntryCard getEntryCardByOPDSID(String id) {
        return idToCardMap.get(id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(mDeleteOptionAvailable && selectedUuids.size() > 0) {
            MenuItem item = menu.add(Menu.NONE, MENUCMDID_DELETE, 1, "");
            item.setIcon(R.drawable.ic_delete_white_24dp);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        MenuItem shareItem = menu.add(Menu.NONE, MENUCMD_SHARE, 2, "");
        shareItem.setIcon(R.drawable.ic_share_white_24dp);
        shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        if(alternativeTranslationLanguages != null) {
            SubMenu languagesSubmenu = menu.addSubMenu(Menu.NONE, 700, 3, "");
            languagesSubmenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            languagesSubmenu.getItem().setTitle(R.string.catalog_language);
            alternativeTranslationLanguageMenuItems = new ArrayList<>(alternativeTranslationLanguages.length);

            MenuItem langItem;
            for(int i = 0; i < alternativeTranslationLanguages.length; i++) {
                langItem = languagesSubmenu.add(alternativeTranslationLanguages[i]);
                if(i == alterantiveTranslationLanguagesDisabledItemIndex)
                    langItem.setEnabled(false);

                alternativeTranslationLanguageMenuItems.add(langItem);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setAlternativeTranslationLinks(String[] translationLinks, int disabledItem) {
        this.alternativeTranslationLanguages = translationLinks;
        alterantiveTranslationLanguagesDisabledItemIndex = disabledItem;
        if(getActivity() != null)
            getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(alternativeTranslationLanguageMenuItems != null
                && alternativeTranslationLanguageMenuItems.contains(item)) {
            int selectedIndex = alternativeTranslationLanguageMenuItems.indexOf(item);
            mCatalogPresenter.handleClickAlternativeLanguage(selectedIndex);
        }

        if(itemId == MENUCMDID_ADD) {
            mCatalogPresenter.handleClickAdd();
            return true;
        }else if(itemId == MENUCMDID_DELETE) {
            mCatalogPresenter.handleClickDelete();
            return true;
        }else if(itemId == MENUCMD_SHARE) {
            mCatalogPresenter.handleClickShare();
            return true;
        }



        if(alternativeTranslationLanguageMenuItems.contains(item)){
            //TODO: handle click alternative translation
//            mCatalogController.handleClickAlternativeTranslationLink(
//                    alternativeTranslationLanguageMenuItems.indexOf(item));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCatalogPresenter.onDestroy();
    }

    public void toggleEntrySelected(OPDSEntryCard card) {
        boolean cardSelected = selectedUuids.contains(card.getOpdsEntry().getUuid());
        cardSelected = !cardSelected;

        card.setSelected(cardSelected);
        if(cardSelected) {
            selectedUuids.add(card.getOpdsEntry().getUuid());
        }else {
            selectedUuids.remove(card.getOpdsEntry().getUuid());
        }

        if(cardSelected && selectedUuids.size() == 1 && getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }else if(!cardSelected && selectedUuids.isEmpty()){
            getActivity().invalidateOptionsMenu();
        }

        mCatalogPresenter.handleSelectedEntriesChanged(selectedUuids);
    }



    @Override
    public void onClick(View view) {
        if(view instanceof OPDSEntryCard) {
            OPDSEntryCard card = ((OPDSEntryCard)view);
            if(selectedUuids.size() > 0) {
                toggleEntrySelected(card);
            }else {
                mCatalogPresenter.handleClickEntry(card.getOpdsEntry());
            }
            return;
        }

        if(view.getId() == R.id.fragment_catalog_footer_button) {
            mCatalogPresenter.handleClickFooterButton();
        }else if(view.getId() == R.id.activity_basepoint_fab) {
            mCatalogPresenter.handleClickAdd();
        }
    }

    @Override
    public void setFilterOptions(OpdsFilterOptions filterOptions) {
        this.filterOptions = filterOptions;
        if(this.catalogOpdsFragmentListener != null)
            catalogOpdsFragmentListener.filterOptionsUpdated(this);
    }

    public OpdsFilterOptions getFilterOptions() {
        return filterOptions;
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
    public void setEntryStatus(final String entryId, final int status) {
        idToStatusMap.put(entryId, status);
        super.runOnUiThread(new Runnable() {
            public void run() {
                if(idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setOPDSEntryOverlay(status);
                }
            }
        });
    }

    @Override
    public void setEntrythumbnail(final String entryId, String iconUrl) {
        idToThumbnailUrlMap.put(entryId, iconUrl);
        OPDSEntryCard card = idToCardMap.get(entryId);
        if(card != null)
            card.setThumbnailUrl(iconUrl, mCatalogPresenter, this);
    }

    @Override
    public void setEntryProgress(final String entryId, final CourseProgress progress) {
        super.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(idToCardMap != null && idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setProgress(progress);
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
        this.idToProgressVisibleMap.put(entryId, visible);
        super.runOnUiThread(new Runnable() {
            public void run() {
                if(idToCardMap.containsKey(entryId)) {
                    idToCardMap.get(entryId).setProgressBarVisible(visible);
                }
            }
        });
    }

    @Override
    public void updateDownloadEntryProgress(final String entryId, final float progress, final String statusText) {
        super.runOnUiThread(new Runnable() {
            public void run() {
                OPDSEntryCard card = idToCardMap.get(entryId);
                if(card != null) {
                    card.setDownloadProgressBarProgress(progress);
                    card.setDownloadProgressStatusText(statusText);
                }
            }
        });
    }

    @Override
    public void setSelectedEntries(Set<String> selectedEntries) {
        if(((selectedEntries.isEmpty() && !this.selectedUuids.isEmpty())
            || (!selectedEntries.isEmpty() && this.selectedUuids.isEmpty()))
            && getActivity() != null) {

            getActivity().invalidateOptionsMenu();
        }

        for(OpdsEntryRecyclerAdapter.OpdsEntryViewHolder holder : boundViewHolders) {
            OpdsEntry entry = holder.mEntryCard.getOpdsEntry();
            if(entry == null)
                continue;

            holder.mEntryCard.setSelected(selectedEntries.contains(entry.getUuid()));
        }

        this.selectedUuids = selectedEntries;
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
        mCatalogPresenter.handleRefresh();
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        mSwipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void setFooterButtonVisible(boolean buttonVisible) {
        this.rootContainer.findViewById(R.id.fragment_catalog_footer_button).setVisibility(
                buttonVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setFooterButtonLabel(String browseButtonLabel) {
        ((Button)this.rootContainer.findViewById(R.id.fragment_catalog_footer_button)).setText(browseButtonLabel);
    }

    @Override
    public void setDeleteOptionAvailable(boolean deleteOptionAvailable) {
        this.mDeleteOptionAvailable = deleteOptionAvailable;
        if(getActivity() != null)
            getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setAddOptionAvailable(final boolean addOptionAvailable) {
        this.mAddOptionAvailable = addOptionAvailable;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(catalogOpdsFragmentListener != null)
                    catalogOpdsFragmentListener.updateAddFabVisibility(CatalogOPDSFragment.this);
            }
        });
    }

    public boolean isAddOptionAvailable() {
        return mAddOptionAvailable;
    }

    class OpdsEntryRecyclerAdapter extends PagedListAdapter<OpdsEntryWithRelations, OpdsEntryRecyclerAdapter.OpdsEntryViewHolder> {

        public class OpdsEntryViewHolder extends RecyclerView.ViewHolder {
            public OPDSEntryCard mEntryCard;

            public OpdsEntryViewHolder(OPDSEntryCard entryCard) {
                super(entryCard);
                mEntryCard = entryCard;
            }
        }

        protected OpdsEntryRecyclerAdapter() {
            super(DIFF_CALLBACK);
        }

        @Override
        public OpdsEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            OPDSEntryCard cardView  = (OPDSEntryCard) LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fragment_opds_item, null);
            cardView.setOnClickListener(CatalogOPDSFragment.this);
            cardView.setOnLongClickListener(CatalogOPDSFragment.this);
            return new OpdsEntryViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(OpdsEntryViewHolder holder, int position) {
            final OpdsEntryWithRelations entry = getItem(position);
            holder.mEntryCard.setOpdsEntry(entry);
            String imageUri = null;

            if(entry != null) {
                OpdsLink imgLink = entry.getThumbnailLink(true);
                if(imgLink != null)
                    imageUri = imgLink.getHref();

                holder.mEntryCard.setSelected(selectedUuids.contains(entry.getUuid()));
                boundViewHolders.add(holder);
            }

            if(imageUri != null) {
                holder.mEntryCard.setThumbnailUrl(mCatalogPresenter.resolveLink(imageUri),
                        mCatalogPresenter, CatalogOPDSFragment.this);
            }
        }

        @Override
        public void onViewRecycled(OpdsEntryViewHolder holder) {
            super.onViewRecycled(holder);
            boundViewHolders.remove(holder);
        }
    }

    public static final DiffCallback<OpdsEntryWithRelations> DIFF_CALLBACK = new DiffCallback<OpdsEntryWithRelations>() {
        @Override
        public boolean areItemsTheSame(@NonNull OpdsEntryWithRelations oldItem, @NonNull OpdsEntryWithRelations newItem) {
            return oldItem.getUuid() == newItem.getUuid();
        }

        @Override
        public boolean areContentsTheSame(@NonNull OpdsEntryWithRelations oldItem, @NonNull OpdsEntryWithRelations newItem) {
            return oldItem.getTitle() != null && newItem.getTitle().equals(newItem.getTitle());
        }
    };

}
