package com.ustadmobile.lib.database.jdbc;

import org.junit.Test;

public class TestUmDbBuilder {

    @Test
    public void givenNoExistingDb_whenBuilt_thenShouldRunCreateAllTables() {

    }

    @Test
    public void givenPreviousDbVersion_whenBuilt_thenShouldRunMigrations() {

    }

    @Test
    public void givenCurrentDbVersion_whenBuilt_thenShouldNotRunMigrationsOrCreateAllTables() {

    }

    @Test
    public void givenFutureDbVersion_whenBuilt_shouldThrowIllegalStateException() {

    }

}
