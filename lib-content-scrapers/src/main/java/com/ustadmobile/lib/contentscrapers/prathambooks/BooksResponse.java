package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BooksResponse {

    @Expose
    @SerializedName("data")
    public List<Data> data;
    @Expose
    @SerializedName("metadata")
    public Metadata metadata;

    @Expose
    @SerializedName("ok")
    public boolean ok;

    public static class Data {
        @Expose
        @SerializedName("isGif")
        public boolean isGif;
        @Expose
        @SerializedName("audioStatus")
        public String audioStatus;
        @Expose
        @SerializedName("isAudio")
        public boolean isAudio;
        @Expose
        @SerializedName("availableForOfflineMode")
        public boolean availableForOfflineMode;
        @Expose
        @SerializedName("publisher")
        public Publisher publisher;
        @Expose
        @SerializedName("description")
        public String description;
        @Expose
        @SerializedName("likesCount")
        public int likesCount;
        @Expose
        @SerializedName("readsCount")
        public int readsCount;
        @Expose
        @SerializedName("illustrators")
        public List<Illustrators> illustrators;
        @Expose
        @SerializedName("storyDownloaded")
        public boolean storyDownloaded;
        @Expose
        @SerializedName("authors")
        public List<Authors> authors;
        @Expose
        @SerializedName("coverImage")
        public CoverImage coverImage;
        @Expose
        @SerializedName("editorsPick")
        public boolean editorsPick;
        @Expose
        @SerializedName("recommended")
        public boolean recommended;
        @Expose
        @SerializedName("slug")
        public String slug;
        @Expose
        @SerializedName("level")
        public String level;
        @Expose
        @SerializedName("language")
        public String language;
        @Expose
        @SerializedName("title")
        public String title;
        @Expose
        @SerializedName("id")
        public int id;
    }

    public static class Publisher {
        @Expose
        @SerializedName("logo")
        public String logo;
        @Expose
        @SerializedName("slug")
        public String slug;
        @Expose
        @SerializedName("name")
        public String name;
    }

    public static class Illustrators {
        @Expose
        @SerializedName("slug")
        public String slug;
        @Expose
        @SerializedName("name")
        public String name;
    }

    public static class Authors {
        @Expose
        @SerializedName("name")
        public String name;
        @Expose
        @SerializedName("slug")
        public String slug;
    }

    public static class CoverImage {
        @Expose
        @SerializedName("sizes")
        public List<Sizes> sizes;

        @Expose
        @SerializedName("aspectRatio")
        public int aspectRatio;
    }

    public static class Sizes {
        @Expose
        @SerializedName("url")
        public String url;
        @Expose
        @SerializedName("width")
        public int width;
        @Expose
        @SerializedName("height")
        public int height;
    }


    public static class Metadata {
        @Expose
        @SerializedName("totalPages")
        public int totalPages;
        @Expose
        @SerializedName("page")
        public int page;
        @Expose
        @SerializedName("perPage")
        public int perPage;
        @Expose
        @SerializedName("hits")
        public int hits;
    }
}
