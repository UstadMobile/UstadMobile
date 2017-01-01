package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.core.view.UstadView;

/**
 * Created by mike on 20/11/16.
 */

public interface EntityListView  extends UstadView {

    void addItem(ListableEntity item);

    void invalidateItem(ListableEntity item);

    void removeAllItems();

}
