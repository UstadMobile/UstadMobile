package com.ustadmobile.lib.contentscrapers.africanbooks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AfricanBooksResponse {

    @Expose
    @SerializedName("other")
    public String other;
    @Expose
    @SerializedName("approved")
    public String approved;
    @Expose
    @SerializedName("app")
    public boolean app;
    @Expose
    @SerializedName("dual")
    public boolean dual;
    @Expose
    @SerializedName("level")
    public String level;
    @Expose
    @SerializedName("lang")
    public String lang;
    @Expose
    @SerializedName("people")
    public String people;
    @Expose
    @SerializedName("author")
    public String author;
    @Expose
    @SerializedName("summary")
    public String summary;
    @Expose
    @SerializedName("date")
    public String date;
    @Expose
    @SerializedName("title")
    public String title;
    @Expose
    @SerializedName("id")
    public String id;
}
