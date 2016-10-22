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
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;

/**
 * A simple Fragment that uses a WebView to show one part of a piece of content
 *
 */
public class ContainerPageFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE_BASE_URI = "baseuri";

    public static final String ARG_PAGE_HREF = "href";

    public static final String ARG_PAGE_QUERY = "query";

    public static final String ARG_PAGE_INDEX = "index";

    private String mBaseURI;

    private String mHref;

    private String mQuery;

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

    private String currentPageTitle;

    private int pageSpineIndex;


    /**
     * Generates a new Fragment for a page fragment
     *
     * @param baseURI The base URI of the page (normally where the EPUB OPF itself is) - e.g. http://localhost:1234/mount/EPUB.
     *                 This must be a directory e.g. full url to load is baseURI/href
     * @param href The HREF of the page itself as per it's entry in the OPF manifest
     * @param query Query string to append to the URL (including the '?')
     *
     * @return A new instance of fragment ContainerPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContainerPageFragment newInstance(String baseURI, String href, String query, int pageSpineIndex) {
        ContainerPageFragment fragment = new ContainerPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE_BASE_URI, baseURI);
        args.putString(ARG_PAGE_HREF, href);
        args.putString(ARG_PAGE_QUERY, query);
        args.putInt(ARG_PAGE_INDEX, pageSpineIndex);
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
            mBaseURI = getArguments().getString(ARG_PAGE_BASE_URI);
            mHref = getArguments().getString(ARG_PAGE_HREF);
            mQuery = getArguments().getString(ARG_PAGE_QUERY);
            pageSpineIndex = getArguments().getInt(ARG_PAGE_INDEX);
        }
        this.autoplayRunJavascript = ((ContainerActivity)getActivity()).getAutoplayRunJavascript();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(viewGroup == null) {
            viewGroup = (RelativeLayout) inflater.inflate(R.layout.fragment_container_page,
                    container, false);
            webView = (WebView) viewGroup.findViewById(R.id.fragment_container_page_webview);
        }else {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 517, "Containerpage: recycled onCreateView");
        }

        //Android after Version 17 (4.4) by default requires a gesture before any media playback happens
        if(Build.VERSION.SDK_INT >= 17) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.loadUrl(getPageURL());
        webView.setWebViewClient(new ContainerPageWebViewClient(webView));
        webView.setWebChromeClient(new ContainerPageViewWebChromeClient());


        return viewGroup;
    }

    public void evaluateJavascript(String script) {
        if (webView != null) {
            webView.loadUrl(script);
        }
    }

    private void loadURL() {
        if(webView != null) {
            webView.loadUrl(getPageURL());
        }
    }

    public void setPageHref(String href, boolean reload) {
        mHref = href;
        if(reload)
            loadURL();
    }

    public String getPageHref() {
        return mHref;
    }

    public void setBaseURI(String baseURI, boolean reload) {
        mBaseURI = baseURI;
        if(reload)
            loadURL();
    }

    public String getBaseURI() {
        return mBaseURI;
    }

    public void setQuery(String query, boolean reload) {
        mQuery = query;
        if(reload)
            loadURL();
    }


    public String getPageURL() {
        return UMFileUtil.joinPaths(new String[] {mBaseURI, mHref}) + mQuery;
    }

    public String getPageTitle() {
        return currentPageTitle;
    }

    private void updatePageTitle(String pageTitle) {
        this.currentPageTitle = pageTitle;
        if(getActivity() != null && getActivity() instanceof ContainerActivity) {
            ((ContainerActivity)getActivity()).handlePageTitleUpdated(pageSpineIndex, pageTitle);
        }
    }


    /**
     * Shows a toast message with the page number / total number of pages
     *
     * @param index
     * @param numPages
     */
    public void showPagePosition(int index, int numPages) {
        //Toast t = Toast.makeText(getContext(), index + "/" + numPages, Toast.LENGTH_SHORT);
        //t.show();
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

            if(ContainerPageFragment.this.getUserVisibleHint()) {
                this.containerView.loadUrl(ContainerPageFragment.this.autoplayRunJavascript);
            }

            /*
            if(!this.isFirstPage) {

            }
            */
        }


    }

    public class ContainerPageViewWebChromeClient extends WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            ContainerPageFragment.this.updatePageTitle(title);

            super.onReceivedTitle(view, title);
        }
    }

}
