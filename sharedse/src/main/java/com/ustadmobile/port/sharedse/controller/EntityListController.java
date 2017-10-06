package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.AsyncLoadableController;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.model.ListableEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public abstract class EntityListController extends UstadBaseController implements AsyncLoadableController {

    protected List<ListableEntity> entityList;

    public EntityListController(Object context) {
        super(context);
        entityList = new ArrayList<>();
    }

    public List<ListableEntity> getList() {
        return entityList;
    }

    @Override
    public void setUIStrings() {

    }


}
