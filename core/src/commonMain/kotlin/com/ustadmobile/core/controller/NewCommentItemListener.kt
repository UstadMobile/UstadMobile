package com.ustadmobile.core.controller

interface NewCommentItemListener {

    fun addComment(entityType: Int, entityUid: Long, comment: String, public: Boolean, to: Long)

}