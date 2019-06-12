package com.ustadmobile.lib.contentscrapers.edraakK12

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/***
 * Generated from DTO Generator Plugin
 */
class ContentResponse {

    @Expose
    @SerializedName("is_eligible")
    var is_eligible: Boolean = false
    @Expose
    @SerializedName("component_type")
    var component_type: String? = null
    @Expose
    @SerializedName("classification")
    var classification: List<String>? = null
    @Expose
    @SerializedName("target_component")
    var target_component: ContentResponse? = null
    @Expose
    @SerializedName("_cls")
    var _cls: String? = null
    @Expose
    @SerializedName("quick_access_components")
    var quick_access_components: List<String>? = null
    @Expose
    @SerializedName("gamification_points_on_completion")
    var gamification_points_on_completion: Int = 0
    @Expose
    @SerializedName("non_eligible_view")
    var non_eligible_view: String? = null
    @Expose
    @SerializedName("eligibility_criteria")
    var eligibility_criteria: List<String>? = null

    @Expose
    @SerializedName("license")
    var license: String? = null
    @Expose
    @SerializedName("scaffolds")
    var scaffolds: List<String>? = null
    @Expose
    @SerializedName("prerequisites")
    var prerequisites: List<String>? = null
    @Expose
    @SerializedName("keywords")
    var keywords: List<Keywords>? = null
    @Expose
    @SerializedName("updated")
    var updated: String? = null
    @Expose
    @SerializedName("created")
    var created: String? = null
    @Expose
    @SerializedName("children")
    var children: List<ContentResponse>? = null
    @Expose
    @SerializedName("visibility")
    var visibility: String? = null
    @Expose
    @SerializedName("deleted")
    var deleted: Boolean = false
    @Expose
    @SerializedName("accept_activity_after_due_datetime")
    var accept_activity_after_due_datetime: Boolean = false
    @Expose
    @SerializedName("title")
    var title: String? = null
    @Expose
    @SerializedName("parent_id")
    var parent_id: String? = null
    @Expose
    @SerializedName("program")
    var program: Int = 0
    @Expose
    @SerializedName("id")
    var id: String? = null

    @SerializedName("is_preset")
    var is_preset: Boolean = false

    @SerializedName("list_in_program_overview")
    var list_in_program_overview: Boolean = false

    @Expose
    @SerializedName("child_index")
    var child_index: Int = 0

    @SerializedName("completed")
    var completed: Boolean = false

    @SerializedName("listing_description")
    var listing_description: String? = null

    @Expose
    @SerializedName("video_info")
    var video_info: Video_info? = null

    @Expose
    @SerializedName("question_set")
    var question_set: ContentResponse? = null

    @Expose
    @SerializedName("full_description")
    var full_description: String? = null

    @Expose
    @SerializedName("explanation")
    var explanation: String? = null

    @Expose
    @SerializedName("hints")
    var hints: List<Hint>? = null

    @Expose
    @SerializedName("description")
    var description: String? = null

    @Expose
    @SerializedName("video_download_allowed")
    var video_download_allowed: Boolean = false
    @Expose
    @SerializedName("edraak_video_id")
    var edraak_video_id: String? = null
    @Expose
    @SerializedName("source_type")
    var source_type: String? = null

    @Expose
    @SerializedName("item_id")
    var item_id: String? = null

    @Expose
    @SerializedName("hint_content")
    var hintContent: String? = null

    @Expose
    @SerializedName("choices")
    var choices: List<Choice>? = null

    @Expose
    @SerializedName("correct_answer_precise")
    var answer: String? = null

    @Expose
    @SerializedName("correct_answer_range_from")
    var answer_range_from: String? = null

    @Expose
    @SerializedName("correct_answer_range_to")
    var answer_range_to: String? = null

    class Choice {

        @Expose
        @SerializedName("item_id")
        var item_id: String? = null

        @Expose
        @SerializedName("title")
        var title: String? = null

        @Expose
        @SerializedName("description")
        var description: String? = null

        @Expose
        @SerializedName("is_correct")
        var isCorrect: Boolean? = null

        @Expose
        @SerializedName("feedback")
        var feedback: String? = null

        @Expose
        @SerializedName("_cls")
        var _cls: String? = null

    }

    class Hint {

        @Expose
        @SerializedName("item_id")
        var item_id: String? = null

        @Expose
        @SerializedName("title")
        var title: String? = null

        @Expose
        @SerializedName("description")
        var description: String? = null

        @Expose
        @SerializedName("hint_content")
        var hintContent: String? = null

        @Expose
        @SerializedName("_cls")
        var _cls: String? = null

    }


    class Video_info {
        @Expose
        @SerializedName("edraak_video_id")
        var edraak_video_id: String? = null
        @Expose
        @SerializedName("status")
        var status: String? = null
        @Expose
        @SerializedName("duration")
        var duration: Double = 0.toDouble()
        @Expose
        @SerializedName("client_video_id")
        var client_video_id: String? = null
        @Expose
        @SerializedName("created")
        var created: String? = null
        @Expose
        @SerializedName("url")
        var url: String? = null
        @Expose
        @SerializedName("subtitles")
        var subtitles: List<String>? = null
        @Expose
        @SerializedName("encoded_videos")
        var encoded_videos: List<Encoded_videos>? = null
    }

    class Encoded_videos {
        @Expose
        @SerializedName("profile")
        var profile: String? = null
        @Expose
        @SerializedName("bitrate")
        var bitrate: Int = 0
        @Expose
        @SerializedName("file_size")
        var file_size: Int = 0
        @Expose
        @SerializedName("url")
        var url: String? = null
        @Expose
        @SerializedName("modified")
        var modified: String? = null
        @Expose
        @SerializedName("created")
        var created: String? = null
    }

    class Keywords {
        @Expose
        @SerializedName("_cls")
        var _cls: String? = null
        @Expose
        @SerializedName("ar")
        var ar: String? = null
        @Expose
        @SerializedName("en")
        var en: String? = null
        @Expose
        @SerializedName("description")
        var description: String? = null
        @Expose
        @SerializedName("title")
        var title: String? = null
        @Expose
        @SerializedName("item_id")
        var item_id: String? = null
    }

}
