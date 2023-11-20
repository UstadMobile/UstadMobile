package com.ustadmobile.port.android.view

import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.VerbEntity


class StatementListViewFragment(): UstadBaseMvvmFragment() {

    companion object {

        @JvmField
        val VERB_ICON_MAP = mapOf(
                VerbEntity.VERB_COMPLETED_UID.toInt() to R.drawable.verb_complete,
                VerbEntity.VERB_PROGRESSED_UID.toInt() to R.drawable.verb_progress,
                VerbEntity.VERB_ATTEMPTED_UID.toInt() to R.drawable.verb_attempt,
                VerbEntity.VERB_INTERACTED_UID.toInt() to R.drawable.verb_interactive,
                VerbEntity.VERB_ANSWERED_UID.toInt() to R.drawable.verb_answered,
                VerbEntity.VERB_SATISFIED_UID.toInt() to R.drawable.verb_passed,
                VerbEntity.VERB_PASSED_UID.toInt() to R.drawable.verb_passed,
                VerbEntity.VERB_FAILED_UID.toInt() to R.drawable.verb_failed)

    }

}