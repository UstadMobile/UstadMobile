package com.ustadmobile.core.networkmanager;

import java.util.Set;

public interface LocalAvailabilityListener {

    void onLocalAvailabilityChanged(Set<Long> locallyAvailableEntries);

}
