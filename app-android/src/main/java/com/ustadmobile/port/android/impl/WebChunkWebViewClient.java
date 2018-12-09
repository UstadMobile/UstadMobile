package com.ustadmobile.port.android.impl;

import android.support.annotation.Nullable;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class WebChunkWebViewClient extends WebViewClient {


    private List<IndexLog> listIndex;
    private Map<String, IndexLog> indexMap = new HashMap<>();
    private ZipFile zipFile;
    private String url;

    public WebChunkWebViewClient(String pathToZip) {
        try {
            zipFile = new ZipFile(pathToZip);
            ZipEntry index = zipFile.getEntry("index.json");
            InputStream inputIndex = zipFile.getInputStream(index);
            Type indexListType = new TypeToken<ArrayList<IndexLog>>() {
            }.getType();

            listIndex = new Gson().fromJson(UMIOUtils.readStreamToString(inputIndex), indexListType);
            IndexLog firstUrlToOpen = listIndex.get(0);
            setUrl(firstUrlToOpen.url);
            for (IndexLog log : listIndex) {
                indexMap.put(log.url, log);
            }

        } catch (IOException e) {
            System.err.println("Error opening Zip File from path " + pathToZip);
        }
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String requestUrl = request.getUrl().toString();
        System.out.println("request url = " + requestUrl);
        IndexLog log = indexMap.get(requestUrl);
        if (log == null) {
            for (Map.Entry<String, IndexLog> e : indexMap.entrySet()) {

                if (e.getKey().contains("plixbrowse") && requestUrl.contains("plixbrowse")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("https://www.ck12.org/assessment/api/render/questionInstance?qID") &&
                        requestUrl.contains("https://www.ck12.org/assessment/api/render/questionInstance?qID")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if (e.getKey().contains("https://www.ck12.org/assessment/api/get/info/test/plix%20practice/plixID/") &&
                        requestUrl.contains("https://www.ck12.org/assessment/api/get/info/test/plix%20practice/plixID/")) {
                    log = indexMap.get(e.getKey());
                    break;
                }
                if(e.getKey().contains("https://www.ck12.org/assessment/api/start/tests/") &&
                        requestUrl.contains("https://www.ck12.org/assessment/api/start/tests/")){
                    log = indexMap.get(e.getKey());
                    break;
                }
            }
        }

        if (log == null) {
            System.err.println("did not find match for url in indexMap " + request.getUrl().toString());
            return super.shouldInterceptRequest(view, request);
        }
        try {
            ZipEntry entry = zipFile.getEntry(log.path);
            InputStream data = zipFile.getInputStream(entry);

            return new WebResourceResponse(log.mimeType, "utf-8", 200, "OK", log.headers, data);
        } catch (IOException e) {
            System.err.println("did not find entry in zip for url " + log.url);
            e.printStackTrace();
        }
        return super.shouldInterceptRequest(view, request);
    }



    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void close() {
        zipFile = null;
    }

    public class IndexLog {

        public String url;

        public String mimeType;

        public String path;

        public Map<String, String> headers;

    }

}
