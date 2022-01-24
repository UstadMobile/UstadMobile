package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary

interface AttemptListListener {

    fun onClickPersonWithStatementDisplay(personWithAttemptsSummary: PersonWithAttemptsSummary)

}