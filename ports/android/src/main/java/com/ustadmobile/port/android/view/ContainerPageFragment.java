package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

    private String autoplayRunJavascript;

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
        this.autoplayRunJavascript = ((ContainerActivity)getActivity()).getAutoplayRunJavascript();
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

            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(mPageURL);
            webView.setWebViewClient(new ContainerPageWebViewClient(webView));
        }
        return viewGroup;
    }

    public void evaluateJavascript(String script) {
        if(webView != null) {
            webView.loadUrl(script);
        }
    }

    /**
     * Shows a toast message with the page number / total number of pages
     *
     * @param index
     * @param numPages
     */
    public void showPagePosition(int index, int numPages) {
        Toast t = Toast.makeText(getContext(), index + "/" + numPages, Toast.LENGTH_SHORT);
        t.show();
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
        this.viewGroup = null;
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

    /**
     * The WebView Client for Android handles a few tweaks including:
     *  Run autoplay on pages that the user winds up on by clicking links
     *  Launch external websites in the system default web browser
     */
    public class ContainerPageWebViewClient extends WebViewClient {

        private WebView containerView;

        private boolean isFirstPage;

        public ContainerPageWebViewClient(WebView containerView) {
            this.containerView = containerView;
            this.isFirstPage = true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            this.isFirstPage = false;
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(!this.isFirstPage) {
                this.containerView.loadUrl(ContainerPageFragment.this.autoplayRunJavascript);
            }
        }
    }

    public class JsObject {
        @JavascriptInterface
        public void stateSaved(){

        }
    }

}
