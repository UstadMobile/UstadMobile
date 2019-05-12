package com.ustadmobile.lib.contentscrapers;

import java.util.List;
import java.util.Map;

public class LogIndex {

    public String title;

    public List<IndexEntry> entries;

    public static class IndexEntry {

        public String url;

        public String mimeType;

        public String path;

        public Map<String, String> headers;

        public Map<String, String> requestHeaders;

    }

    public Map<String, String> links;

}
