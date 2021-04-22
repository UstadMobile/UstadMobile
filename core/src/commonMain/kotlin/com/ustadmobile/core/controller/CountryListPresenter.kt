package com.ustadmobile.core.controller

import com.ustadmobile.core.view.CountryListView
import com.ustadmobile.lib.db.entities.Country
import org.kodein.di.DI

class CountryListPresenter(context: Any, args: Map<String, String>, view: CountryListView, di: DI)
    : UstadBaseController<CountryListView>(context, args, view, di) {


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }


    fun handleClickCountry(country: Country) {
        view.finishWithResult(country)
    }

}