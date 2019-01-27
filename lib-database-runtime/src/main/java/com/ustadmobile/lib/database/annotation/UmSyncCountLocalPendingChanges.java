package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicates that this method should count the number of pending local changes that have
 * not yet been synced (used to determine if a task needs to be scheduled on the device to sync when
 * connectivity is regained).
 *
 * On a DAO: this should generate a count of the number of pending local changes made on this table.
 *
 * On a database: this should generate a count of the number of pending changes on all syncable DAOs
 * in this database.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface UmSyncCountLocalPendingChanges {
}
