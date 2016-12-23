package com.ustadmobile.core.view;

import com.ustadmobile.core.model.ListableEntity;

/**
 * Created by mike on 20/11/16.
 */

public interface EntityListView  extends UstadView{

    void addItem(ListableEntity item);

    void invalidateItem(ListableEntity item);

    void removeAllItems();

}
