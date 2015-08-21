package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.toughra.ustadmobile.R;

/**
 * A simple Fragment that uses a WebView to show one part of a piece of content
 *
 */
public class ContainerPageFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGEURL = "page";

    /**
     * The url of the page to be loaded
     */
    private String mPageURL;

    /**
     * The webView for the given URL
     */
    private WebView webView;

    /**
     * Main root view here
     */
    private ViewGroup viewGroup;

    private OnFragmentInteractionListener mListener;

    /**
     * Generates a new Fragment for a page fragment
     *
     * @param mPageURL The URL
     *
     * @return A new instance of fragment ContainerPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContainerPageFragment newInstance(String mPageURL) {
        ContainerPageFragment fragment = new ContainerPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGEURL, mPageURL);
        fragment.setArguments(args);
        return fragment;
    }

    public ContainerPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.mPageURL = getArguments().getString(ARG_PAGEURL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(viewGroup == null) {
            viewGroup = (RelativeLayout)inflater.inflate(R.layout.fragment_container_page,
                    container, false);
            webView = (WebView)viewGroup.findViewById(R.id.fragment_container_page_webview);

            //Android after Version 17 (4.4) by default requires a gesture before any media playback happens
            if(Build.VERSION.SDK_INT >= 17) {
                webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }

            webView.loadUrl(mPageURL);
            webView.getSettings().setJavaScriptEnabled(true);
        }
        return viewGroup;
    }

    public void evaluateJavascript(String script) {
        webView.loadUrl(script);
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
    public void onDestroy() {
        super.onDestroy();
        this.webView = null;
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
