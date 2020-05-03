package com.ustadmobile.port.android.view.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.networkmanager.defaultGson

/**
 * This base ActivityResultContract allows returning an entity result from user selections in another
 * activity. The other activity could be the edit activity (e.g. where the user creates or edits an
 * item) or the list activity (e.g. where the user picks one or many entities).
 *
 * The activity (Edit or List) that is being called must use EXTRA_RESULT_KEY and put a JSON there
 * with a list of the expected entity type.
 *
 * @param context context to use when creating the intent
 * @param entityClass the Class object for the entity that is going to be selected
 * @param <I> The Input type (to be determined by the subclass)
 * @param <T> The Entity type
 */
abstract class AbstractCrudActivityResultContract<I, T>(val context: Context, val entityClass: Class<T>)
    : ActivityResultContract<I, List<T>?>() {

    override fun parseResult(resultCode: Int, intent: Intent?): List<T>? {
        if(resultCode != Activity.RESULT_OK)
            return null

        val resultJsonStr = intent?.extras?.getString(EXTRA_RESULT_KEY) ?: return null
        return defaultGson().fromJson(resultJsonStr, TypeToken.getParameterized(
                List::class.java, entityClass).type)
    }

    companion object {

        const val EXTRA_RESULT_KEY = "entityResult"

    }

}