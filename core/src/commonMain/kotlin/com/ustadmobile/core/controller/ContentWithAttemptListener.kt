package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ContentWithAttemptSummary

interface ContentWithAttemptListener {

    fun onClickContentWithAttempt(contentWithAttemptSummary: ContentWithAttemptSummary)
}