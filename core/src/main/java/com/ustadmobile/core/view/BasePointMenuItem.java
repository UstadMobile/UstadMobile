package com.ustadmobile.core.view;

/**
 * Created by mike on 4/11/17.
 */

public class BasePointMenuItem {

    private int titleStringId;

    private String destination;

    private String iconName;

    public BasePointMenuItem(int titleStringId, String destination, String iconName) {
        this.titleStringId = titleStringId;
        this.destination = destination;
        this.iconName = iconName;
    }

    public int getTitleStringId() {
        return titleStringId;
    }

    public String getDestination() {
        return destination;
    }

    public String getIconName() {
        return iconName;
    }
}
