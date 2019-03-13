package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;

/**
 * A simple fragment that uses a webview to show content one page of an EPUB.
 */
public class EpubContentPageFragment extends Fragment {

    /**
     * Argument with the entire, absolute url for this page
     */
    public static final String ARG_PAGE_URL = "pg_url";

    /**
     * Page index argument: used to tag the webview so it can be specified during testing
     */
    public static final String ARG_PAGE_INDEX = "pg_index";

    private String mUrl;

    private int mPageIndex;

    /**
     * The webView for the given URL
     */
    private WebView webView;

    /**
     * Main root view here
     */
    private ViewGroup viewGroup;

    private TapToHideToolbarHandler mTapToHideToolbarHandler;

    public static final int HANDLER_CLICK_ON_LINK = 1;

    public static final int HANDLER_CLICK_ON_VIEW = 2;

    private Handler webViewTouchHandler;

    private MotionEvent touchDownEvent;

    private long touchDownTime;

    private GestureDetectorCompat gestureDetector;

    interface TapToHideToolbarHandler {

        void onTap(int pageIndex);

    }

    private Handler.Callback webViewClickCallback = (msg) -> {
        if(msg.what == HANDLER_CLICK_ON_VIEW){
            mTapToHideToolbarHandler.onTap(mPageIndex);
        }

        return true;
    };

    public static EpubContentPageFragment newInstance(String url, int pageIndex) {
        EpubContentPageFragment fragment = new EpubContentPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE_URL, url);
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        fragment.setArguments(args);
        return fragment;
    }


    public EpubContentPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getArguments() != null ?
                getArguments().getString(ARG_PAGE_URL, "about:blank") : "about:blank";
        mPageIndex = getArguments() != null ? getArguments().getInt(ARG_PAGE_INDEX) : 0;
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

        webView.setTag(mPageIndex);

        //Android after Version 17 (4.4) by default requires a gesture before any media playback happens
        if(Build.VERSION.SDK_INT >= 17) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(mUrl);
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    UMFileUtil.getFilename(url));
            DownloadManager downloadManager = (DownloadManager)getContext().getSystemService(
                    Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        });

        webViewTouchHandler = new Handler(webViewClickCallback);

        gestureDetector = new GestureDetectorCompat(webView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                webViewTouchHandler.sendEmptyMessageDelayed(HANDLER_CLICK_ON_VIEW, 200);
                return super.onSingleTapUp(e);
            }
        });

        webView.setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        return viewGroup;
    }

    @Override
    public void onPause() {
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof TapToHideToolbarHandler)
            mTapToHideToolbarHandler = (TapToHideToolbarHandler)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTapToHideToolbarHandler = null;
    }
}
