package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzLog;

/**
 * ClassLogList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClassLogListView extends UstadView {

    String VIEW_NAME = "ClassLogList";

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param clazzLogListProvider The provider data
     */
    void setClazzLogListProvider(UmProvider<ClazzLog> clazzLogListProvider);

}
