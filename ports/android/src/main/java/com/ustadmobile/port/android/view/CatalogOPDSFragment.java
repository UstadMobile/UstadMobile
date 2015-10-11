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

import android.app.Activity;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.U;
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
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CatalogOPDSFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CatalogOPDSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CatalogOPDSFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, CatalogView, ControllerReadyListener {

    private OnFragmentInteractionListener mListener;

    private View rootContainer;

    private ListView catalogListView;

    private Map<String, OPDSEntryCard> idToCardMap;

    protected CatalogController mCatalogController;

    private UstadJSOPDSEntry[] mSelectedEntries;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CatalogOPDSFragment.
     */
    // TODO: Rename and change types and number of parameters
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


    public void loadCatalog(final String url, int resourceMode) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        final int fetchFlags = CatalogController.CACHE_ENABLED;

        CatalogController.makeControllerForView(this, url, resourceMode, fetchFlags, this);
    }

    @Override
    public void controllerReady(final UstadController controller, int flags) {
        if(controller == null) {
            //there was an error loading the controller
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String errMsg = LocaleUtil.formatMessage(impl.getString(U.id.course_catalog_load_error),
                    "Catalog controller");
            impl.getAppView(getActivity()).showAlertDialog(impl.getString(U.id.error), errMsg);
        }else {
            //TODO: check that the activity has not been destroyed etc.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupFromCatalogController((CatalogController) controller);
                }
            });
        }
    }

    public void setupFromCatalogController(CatalogController controller) {
        mCatalogController = controller;
        CatalogModel model = controller.getModel();
        UstadJSOPDSFeed feed = model.opdsFeed;
        getActivity().setTitle(feed.title);
        controller.setUIStrings(this);

        LayoutInflater inflater = getLayoutInflater(null);
        LinearLayout linearLayout = (LinearLayout)this.rootContainer.findViewById(
                R.id.fragment_catalog_container);

        int entryStatus = -1;
        for(int i = 0; i < feed.entries.length; i++) {

            OPDSEntryCard cardView  = (OPDSEntryCard) inflater.inflate(
                    R.layout.fragment_opds_item, null);
            cardView.setOPDSEntry(feed.entries[i]);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            ((TextView)cardView.findViewById(R.id.opdsitem_title_text)).setText(
                    feed.entries[i].title);
            linearLayout.addView(cardView);

            //check the acquisition status
            entryStatus = controller.getEntryAcquisitionStatus(feed.entries[i].id);
            if(entryStatus != -1) {
                cardView.setOPDSEntryOverlay(entryStatus);
            }

            if(entryStatus == CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS) {
                cardView.setProgressBarVisible(true);
            }

            idToCardMap.put(feed.entries[i].id, cardView);
        }

        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootContainer = inflater.inflate(R.layout.fragment_catalog_opds, container, false);
        mSelectedEntries = new UstadJSOPDSEntry[0];
        setHasOptionsMenu(true);

        idToCardMap = new WeakHashMap<String, OPDSEntryCard>();

        String catalogURL = getArguments().getString(CatalogController.KEY_URL);
        int resourceMode = getArguments().getInt(CatalogController.KEY_RESMOD, -1);
        UstadMobileSystemImpl.l(UMLog.INFO, 371, "createView: " + catalogURL + resourceMode);
        loadCatalog(catalogURL, resourceMode);

        return rootContainer;
    }

    public void onStart() {
        super.onStart();
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
        if(mCatalogController != null && mCatalogController.getModel().opdsFeed != null) {
            boolean isAcquisitionFeed = mCatalogController.getModel().opdsFeed.isAcquisitionFeed();
            if(isAcquisitionFeed) {
                inflater.inflate(R.menu.menu_opds_acquireopts, menu);
            }else {
                inflater.inflate(R.menu.menu_opds_navopts, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_opds_acquire:
                if(getSelectedEntries().length > 0) {
                    mCatalogController.handleClickDownloadEntries(getSelectedEntries());
                }else {
                    mCatalogController.handleClickDownloadAll();
                }

                return true;

            case R.id.action_opds_deleteitem:
                if(getSelectedEntries().length > 0) {
                    mCatalogController.handleClickDeleteEntries(getSelectedEntries());
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public void setMenuOptions(String[] menuOptions) {
        ((CatalogActivity)getActivity()).setMenuOptions(menuOptions);
    }

    @Override
    public void setEntryStatus(final String entryId, final int status) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                idToCardMap.get(entryId).setOPDSEntryOverlay(status);
            }
        });
    }

    @Override
    public void updateDownloadAllProgress(int loaded, int total) {

    }

    @Override
    public void setDownloadEntryProgressVisible(final String entryId, final boolean visible) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                idToCardMap.get(entryId).setProgressBarVisible(visible);
            }
        });
    }

    @Override
    public void updateDownloadEntryProgress(final String entryId, final int loaded, final int total) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                int progressPercent = Math.round(((float)loaded/(float)total) * OPDSEntryCard.PROGRESS_ENTRY_MAX);
                idToCardMap.get(entryId).setDownloadProgressBarProgress(progressPercent);
            }
        });
    }

    @Override
    public UstadJSOPDSEntry[] getSelectedEntries() {
        return mSelectedEntries;
    }

    @Override
    public void setSelectedEntries(UstadJSOPDSEntry[] entries) {
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

            idToCardMap.get(thisFeed.entries[i].id).setSelected(isSelected);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void handleClickMenuItem(int index) {
        if(mCatalogController != null) {
            mCatalogController.handleClickMenuItem(index);
        }
    }

}
