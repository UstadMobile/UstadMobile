package com.ustadmobile.lib.contentscrapers.util;

import com.google.gson.annotations.SerializedName;

public class VideoApi {

    public DownloadUrl download_urls;

    public static class DownloadUrl {

        public String mp4;

        public String png;

        @SerializedName("mp4-low")
        public String mp4Low;

    }

    public String youtube_id;

}
