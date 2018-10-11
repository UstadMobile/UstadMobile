package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;

public abstract class CommonHandlerPresenter<V extends UstadView>
        extends UstadBaseController<V>  {

    public CommonHandlerPresenter(Object context) {
        super(context);
    }

    public CommonHandlerPresenter(Object context, Hashtable arguments, UstadView view) {
        super(context, arguments, (V) view);
    }

    abstract void handleCommonPressed(Object arg);
}
