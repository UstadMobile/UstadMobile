package com.ustadmobile.core.domain.xapi.formatresponse

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * We want to be able to show the user (eg. a teacher) the response a student gave to a particular
 * question as recorded in an XAPI statement. When the student answers a multichoice question,
 * likert scale, etc. then Statement.result.response will use ids that are defined in the Activity
 * definition.
 *
 * This use case will use the StatementEntity, ActivityDefinition, and ActivityLangMapEntry to
 * format the human readable version of the response (e.g. the text as per the lang map entry
 * value, not the response id).
 */
class FormatStatementResponseUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) {

    data class FormattedStatementResponse(
        val string: String?
    )

    operator fun invoke(
        statement: StatementEntity,
        activityEntity: ActivityEntity?,
    ): Flow<FormattedStatementResponse> = flow {
        val interactionType = activityEntity?.actInteractionType
        val response = statement.resultResponse
        if(response == null || interactionType == null || interactionType == ActivityEntity.TYPE_UNSET) {
            emit(FormattedStatementResponse(string = null))
        }


        if(response != null && interactionType != null &&
            interactionType in INTERACTION_TYPES_WITH_IDS
        ) {
            listOfNotNull(db, repo).forEach { dataSource ->
                val langMapEntries = dataSource.activityLangMapEntryDao().findAllByActivityUid(
                    statement.statementObjectUid1
                )

                // https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#response-patterns
                val responseStr = when(interactionType) {
                    /*
                     * For choice and sequencing interactions the response is a comma separated list
                     * of ids, for a likert question, the response is simply a single id. In both
                     * cases we can map the ids to the ActivityLangMapEntry for the given id to get
                     * the string for the respons(es).
                     */
                    ActivityEntity.TYPE_CHOICE, ActivityEntity.TYPE_SEQUENCING,
                    ActivityEntity.TYPE_LIKERT -> {
                        val propPrefix = if(interactionType == ActivityEntity.TYPE_LIKERT) {
                            "scale"
                        }else {
                            "choices"
                        }

                        val responseIds = response.split("[,]")

                        responseIds.map { responseId ->
                            langMapEntries.firstOrNull {
                                it.almePropName == "$propPrefix-$responseId"
                            }?.almeValue ?: responseId
                        }.joinToString(separator = ", ")
                    }

                    /*
                     * For matching the response pattern is in the form of id1.id2,id3.id4 where id1 has
                     * been matched with id2, id3 with id4, etc.
                     */
                    ActivityEntity.TYPE_MATCHING -> {
                        response.split("[,]").map { matchPairStr ->
                            val (sourceId, targetId) = matchPairStr.split("[.]", limit = 2)
                            val sourceText = langMapEntries.firstOrNull {
                                it.almePropName == "source-$sourceId"
                            }?.almeValue ?: sourceId

                            val targetText = langMapEntries.firstOrNull {
                                it.almePropName == "target-$targetId"
                            }?.almeValue ?: targetId
                            "$sourceText - $targetText"
                        }.joinToString(", ")
                    }

                    ActivityEntity.TYPE_PERFORMANCE -> {
                        response.split("[,]").map { stepStr ->
                            val stepStrList = stepStr.split(".", limit = 2)
                            val stepId = stepStrList.first()
                            val stepResponse = stepStrList.getOrNull(1)

                            val stepText = langMapEntries.firstOrNull {
                                it.almePropName == "steps-$stepId"
                            }?.almeValue ?: stepId

                            //As per the spec the response might be plain text as per fill-in, or
                            //it might be numeric. A numeric response is probably a simple number,
                            //but could in theory be a range

                            "$stepText: ${stepResponse ?: ""}"
                        }.joinToString(", ")
                    }

                    else -> null
                }

                emit(FormattedStatementResponse(string = responseStr ?: response))
            }
        }else {
            statement.resultResponse?.let {
                FormattedStatementResponse(string = it)
            }
        }
    }

    companion object {

        val INTERACTION_TYPES_WITH_IDS = listOf(
            ActivityEntity.TYPE_CHOICE, ActivityEntity.TYPE_SEQUENCING, ActivityEntity.TYPE_LIKERT,
            ActivityEntity.TYPE_MATCHING, ActivityEntity.TYPE_PERFORMANCE,
        )

    }

}