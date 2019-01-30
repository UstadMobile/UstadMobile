package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UmDao {

    int syncType() default UmSyncType.SYNC_NONE;

    String selectPermissionCondition() default "(:accountPersonUid = :accountPersonUid)";

    String insertPermissionCondition() default "(:accountPersonUid = :accountPersonUid)";

    String updatePermissionCondition() default "(:accountPersonUid = :accountPersonUid)";

    /**
     * If the entity of this DAO is effectively joined to one other entity, then it makes sense that
     * the permissions for this entity should be linked to the other. This is done by adding an SQL
     * join, and then taking the permission conditions from that other Dao.
     *
     * @return the class representing the DAO from which we should inherit permission, if applicable
     */
    Class inheritPermissionFrom() default Void.class;

    /**
     * If we are inheriting permissions from another entity, this should be the foreign key on this
     * entity.
     *
     * @return The name of the foreign key column on this entity
     */
    String inheritPermissionForeignKey() default "";

    /**
     * If we are inheriting permissions from another entity, this should be the primary key of the
     * foreign entity
     *
     * @return The name of the primary key column on the foreign entity
     */
    String inheritPermissionJoinedPrimaryKey() default "";

    /**
     * Sometimes permission clauses may require multiple joins to another table, including when
     * permission is not actually inherited from any other entity.
     *
     * @return
     */
    String permissionJoin() default "";

    /**
     * If true, this DAO will have a binary (input stream) attachment. This is stored in a directory
     * that must be supplied
     *
     * @return
     */
    boolean hasAttachment() default false;

}
