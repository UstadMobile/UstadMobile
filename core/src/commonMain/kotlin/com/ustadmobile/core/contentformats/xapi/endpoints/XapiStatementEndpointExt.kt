package com.ustadmobile.core.contentformats.xapi.endpoints

import com.ustadmobile.core.contentformats.xapi.*
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMTinCanUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.UmAccount

fun XapiStatementEndpoint.storeProgressStatement(account: UmAccount, entry: ContentEntry,
                                                 progress: Int, duration: Long, contextRegistration: String,
                                                 clazzUid: Long) {
    val statement = Statement().apply {
        this.actor = Actor().apply {
            this.account = Account().apply {
                this.homePage = account.endpointUrl
                this.name = account.username ?: "guest"
            }
        }
        this.verb = Verb().apply {
            this.id = if (progress == 100) "https://w3id.org/xapi/adl/verbs/satisfied" else "http://adlnet.gov/expapi/verbs/progressed"
            this.display = mapOf("en-US" to if (progress == 100) "satisfied" else "progressed")
        }
        this.context = XContext().apply {
            registration = contextRegistration
        }
        this.result = Result().apply {
            this.completion = progress == 100
            this.duration = UMTinCanUtil.format8601Duration(duration)
            this.extensions = mapOf("https://w3id.org/xapi/cmi5/result/extensions/progress" to progress)
        }
        this.`object` = XObject().apply {
            this.id = entry.entryId ?: UMFileUtil.joinPaths(account.endpointUrl,
                    "/contentEntryUid/${entry.contentEntryUid}")
            this.objectType = "Activity"
            this.definition = Definition().apply {
                this.name = mapOf("en-US" to (entry.title ?: ""))
                this.description = mapOf("en-US" to (entry.description ?: ""))
            }
        }
    }

    storeStatements(listOf(statement), "", entry.contentEntryUid, clazzUid)
}


fun XapiStatementEndpoint.storeCompletedStatement(account: UmAccount, entry: ContentEntry,
                                                  contextRegistration: String,
                                                  scoreProgress: ContentEntryStatementScoreProgress?,
                                                  clazzUid: Long){


    val statement = Statement().apply {
        this.actor = Actor().apply {
            this.account = Account().apply {
                this.homePage = account.endpointUrl
                this.name = account.username
            }
        }
        this.verb = Verb().apply {
            this.id = "http://adlnet.gov/expapi/verbs/completed"
            this.display = mapOf("en-US" to "completed")
        }
        this.context = XContext().apply {
            registration = contextRegistration
        }

        this.result = Result().apply {
            this.completion = true
            this.extensions = mapOf("https://w3id.org/xapi/cmi5/result/extensions/progress" to 100)
            if(scoreProgress != null){
                success = true
                this.score = Score().apply {
                    raw = scoreProgress.resultScore.toLong()
                    max = scoreProgress.resultMax.toLong()
                    scaled = scoreProgress.resultScaled
                }
            }
        }

        this.`object` = XObject().apply {
            this.id = entry.entryId ?: UMFileUtil.joinPaths(account.endpointUrl,
                    "/contentEntryUid/${entry.contentEntryUid}")
            this.objectType = "Activity"
            this.definition = Definition().apply {
                this.name = mapOf("en-US" to (entry.title ?: ""))
                this.description = mapOf("en-US" to (entry.description ?: ""))
            }
        }
    }

    storeStatements(listOf(statement), "", entry.contentEntryUid, clazzUid)
}