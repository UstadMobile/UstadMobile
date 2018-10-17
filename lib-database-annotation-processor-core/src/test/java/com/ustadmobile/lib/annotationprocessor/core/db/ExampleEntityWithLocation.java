package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class ExampleEntityWithLocation extends ExampleEntity{

    @UmEmbedded
    private ExampleLocation location;

    public ExampleLocation getLocation() {
        return location;
    }

    public void setLocation(ExampleLocation location) {
        this.location = location;
    }
}
