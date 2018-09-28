package com.ustadmobile.lib.database.jdbc;

import java.util.List;

public interface DbChangeListener {

    void onTablesChanged(List<String> tablesChanged);

}
