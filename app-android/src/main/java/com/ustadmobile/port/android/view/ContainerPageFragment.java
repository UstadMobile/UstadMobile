package com.ustadmobile.port.android.view;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple Fragment that uses a WebView to show one part of a piece of content. This fragment MUST
 * be attached to ContainerActivity in order to work. When the fragment is restored from a saved
 * state the internal server URL may have changed since hwen it was created. It therefor relies on
 * attaching to the activity to get these values.
 */
public class ContainerPageFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE_HREF = "href";

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

    public static final String PAUSE_ALL_MEDIA_SCRIPT_ASSET_NAME = "http/ustadmobile-pause-all.js";


    /**
     * Generates a new Fragment for a page fragment
     *
     * @param href The HREF of the page itself as per it's entry in the OPF manifest
     *
     * @return A new instance of fragment ContainerPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContainerPageFragment newInstance(String href, int pageSpineIndex) {
        ContainerPageFragment fragment = new ContainerPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE_HREF, href);
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
            mHref = getArguments().getString(ARG_PAGE_HREF);
            pageSpineIndex = getArguments().getInt(ARG_PAGE_INDEX);
        }
//        TODO: check this - what if it hasn't attached yet?
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

        webView.setWebViewClient(new ContainerPageWebViewClient(webView));
        webView.setWebChromeClient(new ContainerPageViewWebChromeClient());
        loadURL();

        return viewGroup;
    }

    public void evaluateJavascript(String script) {
        if (webView != null) {
            webView.loadUrl(script);
        }
    }

    private void loadURL() {
        if(webView != null && mBaseURI != null && (webView.getUrl() == null || !webView.getUrl().equals(getPageURL()))) {
            webView.loadUrl(getPageURL());
        }
    }

    @Override
    public void onPause() {
        InputStream assetIn = null;
        String pauseOnCloseJs = null;
        /*
         * On Android 4.4 and below the web view does not automatically pause media.
         */
        try {
            assetIn = getContext().getAssets().open(PAUSE_ALL_MEDIA_SCRIPT_ASSET_NAME);
            pauseOnCloseJs = UMIOUtils.readToString(assetIn, "UTF-8");
            if(webView != null)
                webView.loadUrl("javascript:" + pauseOnCloseJs);
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeInputStream(assetIn);
        }

        if(webView != null) {
            webView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(webView != null) {
            webView.onResume();
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
        //Toast t = Toast.makeText(getTargetContext(), index + "/" + numPages, Toast.LENGTH_SHORT);
        //t.show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
            if(context instanceof ContainerActivity) {
                final ContainerActivity activity = (ContainerActivity)context;
                activity.runWhenMounted(new Runnable() {
                    @Override
                    public void run() {
                        ContainerPageFragment.this.mQuery = activity.getXapiQuery();
                        ContainerPageFragment.this.mBaseURI = activity.getBaseURL();
                        loadURL();
                    }
                });
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
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
            if(url.endsWith("?action=download")) {
                final String downloadUrl = url.substring(0, url.indexOf('?'));

                final long[] downloadId = new long[]{-1};

                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        long completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -2);
                        if(completedDownloadId == downloadId[0]) {
                            context.unregisterReceiver(this);
                            startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                        }
                    }
                };


                IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                getContext().registerReceiver(receiver, intentFilter);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        UMFileUtil.getFilename(downloadUrl));
                DownloadManager downloadManager = (DownloadManager)ContainerPageFragment.this.getContext().getSystemService(
                        Context.DOWNLOAD_SERVICE);
                downloadId[0] = downloadManager.enqueue(request);

                return true;
            }else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            boolean isBlankUrl = url != null && url.equalsIgnoreCase("about");
            //TODO: Fix autoplay javascript. This causes an infinite reload of about:blank when the activity is recreated from it's saved state
//            if(ContainerPageFragment.this.getUserVisibleHint()) {
//                this.containerView.loadUrl(ContainerPageFragment.this.autoplayRunJavascript);
//            }
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
