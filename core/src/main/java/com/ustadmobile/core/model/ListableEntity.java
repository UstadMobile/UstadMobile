package com.ustadmobile.core.model;

/**
 * Created by mike on 14/11/16.
 */

public interface ListableEntity {

    public static final int STATUSICON_ATTENTION = 0;

    public static final int STATUSICON_SENDING = 1;

    public static final int STATUSICON_SENT = 2;

    String getTitle();

    String getDetail();

    String getId();

    String getStatusText();

    int getStatusIconCode();


}
