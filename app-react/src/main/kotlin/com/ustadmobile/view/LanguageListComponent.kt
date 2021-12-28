package com.ustadmobile.view

import com.ustadmobile.core.controller.LanguageListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.createListItemWithLeftIconTitleAndDescription
import react.RBuilder

class LanguageListComponent(mProps: UmProps): UstadListComponent<Language, Language>(mProps) ,
    LanguageListView{

    private var mPresenter: LanguageListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.languageDao

    override val listPresenter: UstadListPresenter<*, in Language>?
        get() = mPresenter

    override val viewName: String
        get() = LanguageListView.VIEW_NAME

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.languages)
        showCreateNewItem = true
        createNewText = getString(MessageID.add_a_new_language)
        fabManager?.text = getString(MessageID.language)
        mPresenter = LanguageListPresenter(this, arguments,
            this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: Language) {
        createListItemWithLeftIconTitleAndDescription("language",
            item.name,
            "${item.iso_639_2_standard}/${item.iso_639_3_standard}",
            onMainList = true)
    }

    override fun handleClickEntry(entry: Language) {
        mPresenter?.handleClickEntry(entry)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}