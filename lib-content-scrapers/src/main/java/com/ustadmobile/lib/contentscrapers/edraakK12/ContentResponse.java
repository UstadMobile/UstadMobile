package com.ustadmobile.lib.contentscrapers.edraakK12;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/***
 * Generated from DTO Generator Plugin
 */
public class ContentResponse {

    @Expose
    @SerializedName("is_eligible")
    public boolean is_eligible;
    @Expose
    @SerializedName("component_type")
    public String component_type;
    @Expose
    @SerializedName("classification")
    public List<String> classification;
    @Expose
    @SerializedName("target_component")
    public ContentResponse target_component;
    @Expose
    @SerializedName("_cls")
    public String _cls;
    @Expose
    @SerializedName("quick_access_components")
    public List<String> quick_access_components;
    @Expose
    @SerializedName("gamification_points_on_completion")
    public int gamification_points_on_completion;
    @Expose
    @SerializedName("non_eligible_view")
    public String non_eligible_view;
    @Expose
    @SerializedName("eligibility_criteria")
    public List<String> eligibility_criteria;
    
    @Expose
    @SerializedName("license")
    public String license;
    @Expose
    @SerializedName("scaffolds")
    public List<String> scaffolds;
    @Expose
    @SerializedName("prerequisites")
    public List<String> prerequisites;
    @Expose
    @SerializedName("keywords")
    public List<Keywords> keywords;
    @Expose
    @SerializedName("updated")
    public String updated;
    @Expose
    @SerializedName("created")
    public String created;
    @Expose
    @SerializedName("children")
    public List<ContentResponse> children;
    @Expose
    @SerializedName("visibility")
    public String visibility;
    @Expose
    @SerializedName("deleted")
    public boolean deleted;
    @Expose
    @SerializedName("accept_activity_after_due_datetime")
    public boolean accept_activity_after_due_datetime;
    @Expose
    @SerializedName("title")
    public String title;
    @Expose
    @SerializedName("parent_id")
    public String parent_id;
    @Expose
    @SerializedName("program")
    public int program;
    @Expose
    @SerializedName("id")
    public String id;

    @SerializedName("is_preset")
    public boolean is_preset;

    @SerializedName("list_in_program_overview")
    public boolean list_in_program_overview;

    @Expose
    @SerializedName("child_index")
    public int child_index;

    @SerializedName("completed")
    public boolean completed;

    @SerializedName("listing_description")
    public String listing_description;

    @Expose
    @SerializedName("video_info")
    public Video_info video_info;

    @Expose
    @SerializedName("question_set")
    public ContentResponse question_set;

    @Expose
    @SerializedName("full_description")
    public String full_description;

    @Expose
    @SerializedName("explanation")
    public String explanation;

    @Expose
    @SerializedName("hints")
    public List<Hint> hints;

    @Expose
    @SerializedName("description")
    public String description;

    @Expose
    @SerializedName("video_download_allowed")
    public boolean video_download_allowed;
    @Expose
    @SerializedName("edraak_video_id")
    public String edraak_video_id;
    @Expose
    @SerializedName("source_type")
    public String source_type;

    @Expose
    @SerializedName("item_id")
    public String item_id;

    @Expose
    @SerializedName("hint_content")
    public String hintContent;

    @Expose
    @SerializedName("choices")
    public List<Choice> choices;

    @Expose
    @SerializedName("correct_answer_precise")
    public String answer;

    @Expose
    @SerializedName("correct_answer_range_from")
    public String answer_range_from;

    @Expose
    @SerializedName("correct_answer_range_to")
    public String answer_range_to;

    public static class Choice{

        @Expose
        @SerializedName("item_id")
        public String item_id;

        @Expose
        @SerializedName("title")
        public String title;

        @Expose
        @SerializedName("description")
        public String description;

        @Expose
        @SerializedName("is_correct")
        public Boolean isCorrect;

        @Expose
        @SerializedName("feedback")
        public String feedback;

        @Expose
        @SerializedName("_cls")
        public String _cls;

    }

    public static class Hint{

        @Expose
        @SerializedName("item_id")
        public String item_id;

        @Expose
        @SerializedName("title")
        public String title;

        @Expose
        @SerializedName("description")
        public String description;

        @Expose
        @SerializedName("hint_content")
        public String hintContent;

        @Expose
        @SerializedName("_cls")
        public String _cls;

    }



    public static class Video_info {
        @Expose
        @SerializedName("edraak_video_id")
        public String edraak_video_id;
        @Expose
        @SerializedName("status")
        public String status;
        @Expose
        @SerializedName("duration")
        public double duration;
        @Expose
        @SerializedName("client_video_id")
        public String client_video_id;
        @Expose
        @SerializedName("created")
        public String created;
        @Expose
        @SerializedName("url")
        public String url;
        @Expose
        @SerializedName("subtitles")
        public List<String> subtitles;
        @Expose
        @SerializedName("encoded_videos")
        public List<Encoded_videos> encoded_videos;
    }

    public static class Encoded_videos {
        @Expose
        @SerializedName("profile")
        public String profile;
        @Expose
        @SerializedName("bitrate")
        public int bitrate;
        @Expose
        @SerializedName("file_size")
        public int file_size;
        @Expose
        @SerializedName("url")
        public String url;
        @Expose
        @SerializedName("modified")
        public String modified;
        @Expose
        @SerializedName("created")
        public String created;
    }

    public static class Keywords {
        @Expose
        @SerializedName("_cls")
        public String _cls;
        @Expose
        @SerializedName("ar")
        public String ar;
        @Expose
        @SerializedName("en")
        public String en;
        @Expose
        @SerializedName("description")
        public String description;
        @Expose
        @SerializedName("title")
        public String title;
        @Expose
        @SerializedName("item_id")
        public String item_id;
    }

}
