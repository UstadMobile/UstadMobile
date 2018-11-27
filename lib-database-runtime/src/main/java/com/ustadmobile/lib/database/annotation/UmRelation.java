package com.ustadmobile.lib.database.annotation;

/**
 * Created by mike on 1/21/18.
 */

public @interface UmRelation {

    //Column on this entity which is used for the join (normally the primary key)
    String parentColumn();

    //Column on the related entity that connects with the parent column.
    String entityColumn();
}
