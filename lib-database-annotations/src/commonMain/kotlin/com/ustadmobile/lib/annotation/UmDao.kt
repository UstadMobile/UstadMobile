package com.ustadmobile.lib.database.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class UmDao(val selectPermissionCondition: String = "(:accountPersonUid = :accountPersonUid)", val insertPermissionCondition: String = "(:accountPersonUid = :accountPersonUid)", val updatePermissionCondition: String = "(:accountPersonUid = :accountPersonUid)",
                       /**
                        * If the entity of this DAO is effectively joined to one other entity, then it makes sense that
                        * the permissions for this entity should be linked to the other. This is done by adding an SQL
                        * join, and then taking the permission conditions from that other Dao.
                        *
                        * @return the class representing the DAO from which we should inherit permission, if applicable
                        */
                       val inheritPermissionFrom: KClass<*> = Unit::class,
                       /**
                        * If we are inheriting permissions from another entity, this should be the foreign key on this
                        * entity.
                        *
                        * @return The name of the foreign key column on this entity
                        */
                       val inheritPermissionForeignKey: String = "",
                       /**
                        * If we are inheriting permissions from another entity, this should be the primary key of the
                        * foreign entity
                        *
                        * @return The name of the primary key column on the foreign entity
                        */
                       val inheritPermissionJoinedPrimaryKey: String = "",
                       /**
                        * Sometimes permission clauses may require multiple joins to another table, including when
                        * permission is not actually inherited from any other entity.
                        *
                        * @return
                        */
                       val permissionJoin: String = "",
                       /**
                        * If true, this DAO will have a binary (input stream) attachment. This is stored in a directory
                        * that must be supplied
                        *
                        * @return
                        */
                       val hasAttachment: Boolean = false)
