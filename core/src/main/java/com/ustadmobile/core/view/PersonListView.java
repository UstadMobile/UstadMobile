package com.ustadmobile.core.view;

import com.ustadmobile.core.model.ListableEntity;

import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public interface PersonListView extends UstadView {
    void setRefreshing(boolean refreshingActive);

    void setEntityList(List<? extends ListableEntity> list);
}
