package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.FeedEntry;

/**
 * FeedList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface FeedListView extends UstadView {

    String VIEW_NAME = "FeedList";

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the Recycler view.
     *
     * @param feedEntryUmProvider The provider data
     */
    void setFeedEntryProvider(UmProvider<FeedEntry> feedEntryUmProvider);

}
