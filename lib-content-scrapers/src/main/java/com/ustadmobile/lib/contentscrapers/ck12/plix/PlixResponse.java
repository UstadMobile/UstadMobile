package com.ustadmobile.lib.contentscrapers.ck12.plix;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlixResponse {

    @Expose
    @SerializedName("response")
    public Response response;

    public static class Response {
        @Expose
        @SerializedName("question")
        public Question question;
    }

    public static class Question {
        @Expose
        @SerializedName("updated")
        public String updated;
        @Expose
        @SerializedName("created")
        public String created;
        @Expose
        @SerializedName("_id")
        public String _id;
    }
}
