package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleNameWithImage
import com.ustadmobile.lib.db.entities.SaleProductGroup

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleProductGroupDao : SyncableDao<SaleProductGroup, SaleProductGroupDao> {

    //INSERT

    @Insert
    abstract fun insertAsync(entity: SaleProductGroup, insertCallback: UmCallback<Long>)

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): UmLiveData<List<SaleProductGroup>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<SaleProductGroup>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveAsync(allActiveCallback: UmCallback<List<SaleProductGroup>>)

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): UmProvider<SaleProductGroup>

    @Query(ALL_ACTIVE_TYPED_QUERY)
    abstract fun findAllTypedActiveLive(type: Int): UmLiveData<List<SaleProductGroup>>

    @Query(ALL_ACTIVE_TYPED_QUERY)
    abstract fun findAllTypedActiveList(type: Int): List<SaleProductGroup>

    @Query(ALL_ACTIVE_TYPED_QUERY)
    abstract fun findAllTypedActiveAsync(type: Int, allActiveCallback: UmCallback<List<SaleProductGroup>>)

    @Query(ALL_ACTIVE_TYPED_QUERY)
    abstract fun findAllTypedActiveProvider(type: Int): UmProvider<SaleProductGroup>

    @Query(ALL_ACTIVE_TYPED_SNWI_QUERY)
    abstract fun findAllTypedActiveSNWILive(type: Int): UmLiveData<List<SaleNameWithImage>>

    @Query(ALL_ACTIVE_TYPED_SNWI_QUERY)
    abstract fun findAllTypedActiveSNWIList(type: Int): List<SaleNameWithImage>

    @Query(ALL_ACTIVE_TYPED_SNWI_QUERY)
    abstract fun findAllTypedActiveSNWIAsync(type: Int, allActiveCallback: UmCallback<List<SaleNameWithImage>>)

    @Query(ALL_ACTIVE_TYPED_SNWI_QUERY)
    abstract fun findAllTypedActiveSNWIProvider(type: Int): UmProvider<SaleNameWithImage>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(uid: Long): SaleProductGroup

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidAsync(uid: Long, findByUidCallback: UmCallback<SaleProductGroup>)

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): UmLiveData<SaleProductGroup>

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntity(uid: Long)

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntityAsync(uid: Long, inactivateCallback: UmCallback<Int>)

    //UPDATE:

    @Update
    abstract fun updateAsync(entity: SaleProductGroup, updateCallback: UmCallback<Int>)

    companion object {


        //FIND ALL ACTIVE

        const val ALL_ACTIVE_QUERY = "SELECT * FROM SaleProductGroup WHERE saleProductGroupActive = 1"

        const val ALL_ACTIVE_TYPED_QUERY = "SELECT * FROM SaleProductGroup WHERE saleProductGroupActive = 1 AND " + " saleProductGroupType = :type"

        const val ALL_ACTIVE_TYPED_SNWI_QUERY = "SELECT SaleProductGroup.saleProductGroupName as name, SaleProductGroup.saleProductGroupDesc as description, " +
                " (SELECT SaleProductPicture.saleProductPictureUid FROM SaleProductPicture " +
                "  WHERE saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
                "  ORDER BY saleProductPictureTimestamp DESC LIMIT 1) as pictureUid, " +
                "  SaleProductGroup.saleProductGroupUid as productGroupUid, " +
                "  SaleProduct.saleProductUid as productUid, " +
                "  :type AS type " +
                " FROM SaleProductGroup  " +
                "   LEFT JOIN SaleProduct ON " +
                "  SaleProduct.saleProductUid = " +
                "  (SELECT SaleProductGroupJoin.saleProductGroupJoinProductUid " +
                "   FROM SaleProductGroupJoin " +
                "   WHERE SaleProductGroupJoin.saleProductGroupJoinGroupUid = " +
                "   SaleProductGroup.saleProductGroupUid " +
                "    ORDER BY SaleProductGroupJoin.saleProductGroupJoinDateCreated DESC LIMIT 1) " +
                " WHERE SaleProductGroup.saleProductGroupActive = 1 AND saleProductGroupType = :type "

        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProductGroup WHERE saleProductGroupUid = :uid"

        //INACTIVATE:

        const val INACTIVATE_QUERY = "UPDATE SaleProductGroup SET saleProductGroupActive = 0 WHERE saleProductGroupUid = :uid"
    }
}
