package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
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
public class CatalogOPDSFragment extends Fragment implements AdapterView.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private View rootContainer;

    private CatalogView catalogView;

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
        return rootContainer;
    }

    @Override
    public void onStart() {
        super.onStart();
        UstadJSOPDSFeed feed = catalogView.getController().getModel().opdsFeed;
        ListView catalogList =
            (ListView)this.rootContainer.findViewById(R.id.fragment_catalog_listview);
        OPDSFeedAdapter feedAdapter = new OPDSFeedAdapter(feed, this.getActivity());
        catalogList.setAdapter(feedAdapter);
        catalogList.setOnItemClickListener(this);

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UstadJSOPDSEntry entry= this.catalogView.getController().getModel().opdsFeed.entries[position];
        catalogView.getController().handleClickEntry(entry);
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

    public class OPDSFeedAdapter extends BaseAdapter {

        private UstadJSOPDSFeed feed;
        private Context context;

        public OPDSFeedAdapter(UstadJSOPDSFeed feed, Context context) {
            this.feed = feed;
            this.context = context;
        }

        @Override
        public int getCount() {
            return feed.entries.length;
        }

        @Override
        public Object getItem(int position) {
            return feed.entries[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView returnVal = null;
            if(convertView != null && convertView instanceof TextView) {
                returnVal = (TextView)convertView;
            }else {
                returnVal = new TextView(context);
            }

            returnVal.setText(feed.entries[position].title);
            return returnVal;
        }
    }

}
