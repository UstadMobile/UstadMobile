package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.core.db.UmProvider;

/**
 * SELEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELEditView extends UstadView {

    String VIEW_NAME = "SELEdit";

    /**
     * Sets Current provider
     * <p>
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<Person> listProvider);

    void finish();


}
