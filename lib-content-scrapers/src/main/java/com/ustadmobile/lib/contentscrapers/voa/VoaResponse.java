package com.ustadmobile.lib.contentscrapers.voa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VoaResponse {

    @Expose
    @SerializedName("articleSection")
    public String articleSection;
    @Expose
    @SerializedName("description")
    public String description;
    @Expose
    @SerializedName("publisher")
    public Publisher publisher;
    @Expose
    @SerializedName("video")
    public List<Video> video;
    @Expose
    @SerializedName("keywords")
    public String keywords;
    @Expose
    @SerializedName("dateModified")
    public String dateModified;
    @Expose
    @SerializedName("datePublished")
    public String datePublished;
    @Expose
    @SerializedName("image")
    public Image image;
    @Expose
    @SerializedName("creator")
    public Creator creator;
    @Expose
    @SerializedName("author")
    public Author author;
    @Expose
    @SerializedName("name")
    public String name;
    @Expose
    @SerializedName("headline")
    public String headline;
    @Expose
    @SerializedName("url")
    public String url;
    @Expose
    @SerializedName("mainEntityOfPage")
    public String mainEntityOfPage;
    @Expose
    @SerializedName("inLanguage")
    public String inLanguage;

    public static class Publisher {
        @Expose
        @SerializedName("logo")
        public Logo logo;
        @Expose
        @SerializedName("name")
        public String name;
        @Expose
        @SerializedName("url")
        public String url;
    }

    public static class Logo {
        @Expose
        @SerializedName("height")
        public int height;
        @Expose
        @SerializedName("width")
        public int width;
        @Expose
        @SerializedName("url")
        public String url;
  
    }

    public static class Video {
        @Expose
        @SerializedName("uploadDate")
        public String uploadDate;
        @Expose
        @SerializedName("thumbnailUrl")
        public String thumbnailUrl;
        @Expose
        @SerializedName("name")
        public String name;
     
    }

    public static class Image {
        @Expose
        @SerializedName("height")
        public int height;
        @Expose
        @SerializedName("width")
        public int width;
        @Expose
        @SerializedName("url")
        public String url;
   
    }

    public static class Creator {
        @Expose
        @SerializedName("name")
        public String name;
     
    }

    public static class Author {
        @Expose
        @SerializedName("name")
        public String name;
    }
}
