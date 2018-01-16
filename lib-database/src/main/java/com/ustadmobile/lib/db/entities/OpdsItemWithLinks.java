package com.ustadmobile.lib.db.entities;

import java.util.List;

/**
 * Created by mike on 1/16/18.
 */

public interface OpdsItemWithLinks {

    List<OpdsLink> getLinks();

    void setLinks(List<OpdsLink> links);
}
