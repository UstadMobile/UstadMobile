package com.ustadmobile.lib.contentscrapers.voa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoaResponse {

    @Expose
    @SerializedName("articleSection")
    public String articleSection;
    @Expose
    @SerializedName("description")
    public String description;

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

}
