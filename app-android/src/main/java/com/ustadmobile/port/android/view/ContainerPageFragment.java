package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * A simple fragment that uses a webview to show content one page of an EPUB.
 */
public class ContainerPageFragment extends Fragment {

    /**
     * Argument with the entire, absolute url for this page
     */
    public static final String ARG_PAGE_URL = "pg_url";

    private String mUrl;

    /**
     * The webView for the given URL
     */
    private WebView webView;

    /**
     * Main root view here
     */
    private ViewGroup viewGroup;

    public static final String PAUSE_ALL_MEDIA_SCRIPT_ASSET_NAME = "http/epub/ustadmobile-pause-all.js";

    public static ContainerPageFragment newInstance(String url) {
        ContainerPageFragment fragment = new ContainerPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE_URL, url);
        fragment.setArguments(args);
        return fragment;
    }


    public ContainerPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getArguments() != null ?
                getArguments().getString(ARG_PAGE_URL, "about:blank") : "about:blank";
    }

    @SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(viewGroup == null) {
            viewGroup = (RelativeLayout) inflater.inflate(R.layout.fragment_container_page,
                    container, false);
            webView = viewGroup.findViewById(R.id.fragment_container_page_webview);
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
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(mUrl);
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    UMFileUtil.getFilename(url));
            DownloadManager downloadManager = (DownloadManager)getContext().getSystemService(
                    Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        });

        return viewGroup;
    }

    @Override
    public void onPause() {
        InputStream assetIn = null;
        String pauseOnCloseJs;
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
}
