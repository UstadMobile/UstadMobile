package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.UstadView;

import java.util.Hashtable;

/**
 * So that we can add click listener for different use cases.
 * @param <V>   The view
 */
public abstract class CommonHandlerPresenter<V extends UstadView>
        extends UstadBaseController<V>  {

    public CommonHandlerPresenter(Object context) {
        super(context);
    }

    //The constructor will throw an uncast check warning. That is expected.
    public CommonHandlerPresenter(Object context, Hashtable arguments, UstadView view) {
        super(context, arguments, (V) view);
    }

    /**
     * Primary action on item.
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    public abstract void handleCommonPressed(Object arg);

    /**
     * Secondary action on item.
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    public abstract void handleSecondaryPressed(Object arg);
}