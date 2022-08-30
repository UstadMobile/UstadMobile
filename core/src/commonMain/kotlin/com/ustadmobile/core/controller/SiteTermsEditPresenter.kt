package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.UmPlatformUtil
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.core.view.SiteTermsEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class SiteTermsEditPresenter(context: Any,
                             arguments: Map<String, String>, view: SiteTermsEditView,
                             lifecycleOwner: LifecycleOwner,
                             di: DI)
    : UstadEditPresenter<SiteTermsEditView, SiteTermsWithLanguage>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(
            SAVEDSTATE_KEY_LANGUAGE,
            ListSerializer(Language.serializer()), Language::class) {
            val selectedLang = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.stLanguage = selectedLang
            entity?.sTermsLang = selectedLang.iso_639_1_standard
            entity?.sTermsLangUid = selectedLang.langUid
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_LANGUAGE] = null
        }

    }

    override fun onLoadFromJson(bundle: Map<String, String>): SiteTermsWithLanguage? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SiteTermsWithLanguage? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, SiteTermsWithLanguage.serializer(), entityJsonStr)
        }else {
            editEntity = SiteTermsWithLanguage().apply {
                termsHtml = systemImpl.getString(MessageID.terms_and_policies_text, context)
            }

            presenterScope.launch {
                //set the language to be the default for a new policy
                val displayLocale = systemImpl.getDisplayedLocale(context)
                repo.onDbThenRepoWithTimeout(5000) { db, lastResult ->
                    val uiLanguage = db.languageDao.takeIf { lastResult == null }?.findByTwoCodeAsync(displayLocale)
                    if(uiLanguage != null) {
                        editEntity.stLanguage = uiLanguage
                        editEntity.sTermsLang = displayLocale
                        editEntity.sTermsLangUid = uiLanguage.langUid
                        view.entity = editEntity
                    }
                }
            }
        }

        return editEntity
    }


    fun handleClickLanguage() {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                LanguageListView.VIEW_NAME,
                Language::class,
                Language.serializer(), SAVEDSTATE_KEY_LANGUAGE)
        )
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, SiteTermsWithLanguage.serializer(),
                entityVal)
    }

    override fun handleClickSave(entity: SiteTermsWithLanguage) {
        //TODO: Any validation that is needed before accepting / saving this entity
        //TODO: Only save to the database when the persistence mode is PERSISTENCE_MODE.DB
        view.languageError = null

        if(entity.stLanguage == null) {
            view.languageError = systemImpl.getString(MessageID.field_required_prompt, context)
            return
        }

        presenterScope.launch {

            //TODO: Call commitToDatabase on any onetomany join helpers
            val serializedResult = safeStringify(di, ListSerializer(SiteTermsWithLanguage.serializer()),
                listOf(entity))
            finishWithResult(serializedResult)
        }
    }

    companion object {

        const val SAVEDSTATE_KEY_LANGUAGE = "Language"
        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}