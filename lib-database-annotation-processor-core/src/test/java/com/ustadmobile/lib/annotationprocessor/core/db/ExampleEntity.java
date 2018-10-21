package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ExampleEntity {

    @UmPrimaryKey(autoIncrement = true)
    private int uid;

    private String name;

    private int locationPk;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLocationPk() {
        return locationPk;
    }

    public void setLocationPk(int locationPk) {
        this.locationPk = locationPk;
    }
}
