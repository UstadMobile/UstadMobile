package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;

/**
 * So that we can add click listener for different use cases.
 * @param <V>
 */
public abstract class CommonHandlerPresenter<V extends UstadView>
        extends UstadBaseController<V>  {

    public CommonHandlerPresenter(Object context) {
        super(context);
    }

    public CommonHandlerPresenter(Object context, Hashtable arguments, UstadView view) {
        super(context, arguments, (V) view);
    }

    /**
     * Primary action on item.
     * @param arg
     */
    public abstract void handleCommonPressed(Object arg);

    /**
     * Secondary action on item.
     * @param arg
     */
    public abstract void handleSecondaryPressed(Object arg);
}