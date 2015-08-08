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

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CatalogOPDSFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CatalogOPDSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CatalogOPDSFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private View rootContainer;

    private CatalogView catalogView;

    private ListView catalogListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param viewId Parameter 1.
     * @return A new instance of fragment CatalogOPDSFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CatalogOPDSFragment newInstance(int viewId) {
        CatalogOPDSFragment fragment = new CatalogOPDSFragment();
        Bundle args = new Bundle();
        args.putInt(UstadMobileSystemImplAndroid.EXTRA_VIEWID, viewId);
        fragment.setArguments(args);
        return fragment;
    }

    public CatalogOPDSFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int viewId = getArguments().getInt(UstadMobileSystemImplAndroid.EXTRA_VIEWID);
            this.catalogView = CatalogViewAndroid.getViewById(viewId);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootContainer = inflater.inflate(R.layout.fragment_catalog_opds, container, false);
        UstadJSOPDSFeed feed = catalogView.getController().getModel().opdsFeed;
        LinearLayout linearLayout = (LinearLayout)this.rootContainer.findViewById(
                R.id.fragment_catalog_container);
        for(int i = 0; i < feed.entries.length; i++) {
            OPDSEntryCard cardView  = (OPDSEntryCard) inflater.inflate(
                    R.layout.fragment_opds_item, null);
            cardView.setOPDSEntry(feed.entries[i]);
            cardView.setOnClickListener(this);
            ((TextView)cardView.findViewById(R.id.opdsitem_title_text)).setText(
                    feed.entries[i].title);
            linearLayout.addView(cardView);
        }

        setHasOptionsMenu(true);

        return rootContainer;
    }

    public CatalogView getCatalogView() {
        return catalogView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        boolean isAcquisitionFeed = this.catalogView.getController().getModel().opdsFeed.isAcquisitionFeed();
        if(isAcquisitionFeed) {
            inflater.inflate(R.menu.menu_opds_acquireopts, menu);
        }else {
            inflater.inflate(R.menu.menu_opds_navopts, menu);
        }
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
    public void onClick(View view) {
        if(view instanceof OPDSEntryCard) {
            UstadJSOPDSEntry entry = ((OPDSEntryCard)view).getEntry();
            catalogView.getController().handleClickEntry(entry);
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

}
