package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.SELQuestionSetsView;

import java.util.Hashtable;

public class SELQuestionSetsPresenter extends UstadBaseController<SELQuestionSetsView> {

    public SELQuestionSetsPresenter(Object context, Hashtable arguments, SELQuestionSetsView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

    }

    public void handleClickPrimaryActionButton(){
        //TODO: Go to adding a new SEL Question set
    }

    @Override
    public void setUIStrings() {

    }
}
