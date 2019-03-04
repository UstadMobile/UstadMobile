package com.ustadmobile.port.android.impl;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.ustadmobile.core.controller.WebChunkPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.port.sharedse.container.ContainerManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;


public class WebChunkWebViewClient extends WebViewClient {


    private ContainerManager containerManager;
    private WebChunkPresenter presenter;
    private Map<String, IndexLog.IndexEntry> indexMap = new HashMap<>();
    private Map<Pattern, String> linkPatterns = new HashMap<>();
    private String url;

    public WebChunkWebViewClient(Container pathToZip, WebChunkPresenter mPresenter, Object context) {
        try {
            this.presenter = mPresenter;
            UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context);
            UmAppDatabase appDatabase = UmAppDatabase.getInstance(context);

            containerManager = new ContainerManager(pathToZip, appDatabase, repoAppDatabase);

            ContainerEntryWithContainerEntryFile index = containerManager.getEntry("index.json");

            IndexLog indexLog = new Gson().fromJson(UMIOUtils.readStreamToString(containerManager.getInputStream(index)), IndexLog.class);
            List<IndexLog.IndexEntry> indexList = indexLog.entries;
            IndexLog.IndexEntry firstUrlToOpen = indexList.get(0);
            setUrl(firstUrlToOpen.url);


            for (IndexLog.IndexEntry log : indexList) {
                indexMap.put(log.url, log);
            }
            Map<String, String> linksMap = indexLog.links;
            if (linksMap != null && !linksMap.isEmpty()) {
                for (String link : linksMap.keySet()) {
                    linkPatterns.put(Pattern.compile(link), linksMap.get(link));
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening Zip File from path " + pathToZip);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String requestUrl = checkWithPattern(request.getUrl().toString());
        if (requestUrl != null) {
            presenter.handleUrlLinkToContentEntry(requestUrl);
            return true;
        }
        return super.shouldOverrideUrlLoading(view, request);
    }


    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        StringBuilder requestUrl = new StringBuilder(request.getUrl().toString());
        String sourceUrl = checkWithPattern(requestUrl.toString());
        if (sourceUrl != null) {
            presenter.handleUrlLinkToContentEntry(sourceUrl);
            new Handler(Looper.getMainLooper()).post(() -> view.loadUrl(getUrl()));
            return new WebResourceResponse("text/html", "utf-8", null);
        }

        if (requestUrl.toString().contains("/Take-a-hint")) {
            return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("true".getBytes(StandardCharsets.UTF_8)));
        }

        IndexLog.IndexEntry log = indexMap.get(requestUrl.toString());
        if (log == null) {
            for (Map.Entry<String, IndexLog.IndexEntry> e : indexMap.entrySet()) {

                if (e.getKey().contains("plixbrowse") && requestUrl.toString().contains("plixbrowse")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("https://www.ck12.org/assessment/api/render/questionInstance?qID") &&
                        requestUrl.toString().contains("https://www.ck12.org/assessment/api/render/questionInstance?qID")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("https://www.ck12.org/assessment/api/get/info/test/plix%20practice/plixID/") &&
                        requestUrl.toString().contains("https://www.ck12.org/assessment/api/get/info/test/plix%20practice/plixID/")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("https://www.ck12.org/assessment/api/start/tests/") &&
                        requestUrl.toString().contains("https://www.ck12.org/assessment/api/start/tests/")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("hint") && requestUrl.toString().contains("hint")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("attempt") && requestUrl.toString().contains("attempt")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("/api/internal/user/task/practice/") &&
                        requestUrl.toString().contains("/api/internal/user/task/practice/")) {
                    view.post(() ->
                            view.loadUrl(getUrl()));
                    return super.shouldInterceptRequest(view, request);
                }
                if (e.getKey().contains("/assessment_item") && requestUrl.toString().contains("/assessment_item")) {
                    int langIndex = requestUrl.indexOf("?lang");

                    String newRequestUrl = requestUrl.substring(0, langIndex);
                    log = indexMap.get(newRequestUrl);
                    if (log != null) {
                        break;
                    }
                }
                if (e.getKey().contains("/Quiz/Answer") && requestUrl.toString().contains("/Quiz/Answer")) {

                    Map<String, String> headers = request.getRequestHeaders();
                    String pageIndex = headers.get("PageIndex");
                    String answerId = headers.get("AnswerId");

                    requestUrl.append("?page=").append(pageIndex);
                    if (answerId != null && answerId.isEmpty()) {
                        requestUrl.append("&answer=").append(answerId);
                    }

                    log = indexMap.get(requestUrl.toString());
                    break;

                }
            }
        }


        if (log == null) {
            System.err.println("did not find match for url in indexMap " + request.getUrl().toString());
            return new WebResourceResponse("", "utf-8", 200, "OK", null, null);
        }
        try {
            InputStream data = containerManager.getInputStream(containerManager.getEntry(log.path));

            return new WebResourceResponse(log.mimeType, "utf-8", 200, "OK", log.headers, data);
        } catch (IOException e) {
            System.err.println("did not find entry in zip for url " + log.url);
            e.printStackTrace();
        }
        return super.shouldInterceptRequest(view, request);
    }

    private String checkWithPattern(String requestUrl) {
        for (Pattern linkPattern : linkPatterns.keySet()) {
            if (linkPattern.matcher(requestUrl).lookingAt()) {
                return linkPatterns.get(linkPattern);
            }
        }
        return null;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public class IndexLog {

        public String title;

        public List<IndexEntry> entries;

        public class IndexEntry {

            public String url;

            public String mimeType;

            public String path;

            public Map<String, String> headers;

            public Map<String, String> requestHeaders;

        }

        public Map<String, String> links;

    }

}
