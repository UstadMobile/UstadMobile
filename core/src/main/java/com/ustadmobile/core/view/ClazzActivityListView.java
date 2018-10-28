package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.core.db.UmProvider;

/**
 * ClazzActivityList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzActivityListView extends UstadView {

    String VIEW_NAME = "ClazzActivityList";


    /**
     * Sets Current provider
     * <p>
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<ClazzActivity> listProvider);


    /**
     * For Android: closes the activity.
     */
    void finish();

}
