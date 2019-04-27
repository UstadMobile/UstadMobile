package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SelectLanguageDialogView;


/**
 * Presenter for SelectLanguageDialog view
 **/
public class SelectLanguageDialogPresenter extends UstadBaseController<SelectLanguageDialogView> {

    UmAppDatabase repository;


    public SelectLanguageDialogPresenter(Object context, Hashtable arguments, SelectLanguageDialogView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }


}
