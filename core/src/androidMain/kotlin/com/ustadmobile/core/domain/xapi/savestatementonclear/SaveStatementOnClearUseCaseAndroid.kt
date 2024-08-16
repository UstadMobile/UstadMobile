package com.ustadmobile.core.domain.xapi.savestatementonclear

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SaveStatementOnClearUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
    private val json: Json,
): SaveStatementOnClearUseCase {

    override fun invoke(statements: List<XapiStatement>, xapiSession: XapiSessionEntity) {
        val jobData = Data.Builder()
            .putString(SaveStatementOnClearUseCase.KEY_ENDPOINT, endpoint.url)
            .putString(
                SaveStatementOnClearUseCase.KEY_STATEMENTS,
                json.encodeToString(ListSerializer(XapiStatement.serializer()), statements)
            )
            .putString(
                SaveStatementOnClearUseCase.KEY_XAPI_SESSION,
                json.encodeToString(XapiSessionEntity.serializer(), xapiSession)
            ).build()

        val workRequest = OneTimeWorkRequestBuilder<SaveStatementOnClearWorker>()
            .setInputData(jobData)
            .build()

        WorkManager.getInstance(appContext).enqueue(workRequest)
    }
}