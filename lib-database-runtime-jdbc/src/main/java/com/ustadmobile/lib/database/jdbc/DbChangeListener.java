package com.ustadmobile.lib.database.jdbc;

import java.util.List;

/**
 * Listener interface used to listen for changes to tables.
 */
public interface DbChangeListener {

    /**
     * Called when tables have been changed.
     *
     * @param tablesChanged list of tables that have been changed
     */
    void onTablesChanged(List<String> tablesChanged);

}
