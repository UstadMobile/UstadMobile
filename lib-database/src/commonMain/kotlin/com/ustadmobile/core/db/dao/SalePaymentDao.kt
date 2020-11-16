package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.dao.SalePaymentDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SalePayment

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SalePaymentDao : BaseDao<SalePayment>, OneToManyJoinDao<SalePayment> {

    @Update
    abstract suspend fun updateAsync(entity: SalePayment): Int


    @Query(QUERY_ALL_ACTIVE_SALE_PAYMENT_LIST)
    abstract fun findAllBySale(saleUid: Long): DataSource.Factory<Int,SalePayment>


    @Query("""UPDATE SalePayment SET salePaymentSaleUid = 0,
        WHERE salePaymentUid = :salePaymentUid
    """)
    abstract suspend fun deactivateSaleFromSalePayment(salePaymentUid : Long ): Int

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            deactivateSaleFromSalePayment(it)
        }
    }



    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"


        const val QUERY_ALL_ACTIVE_SALE_PAYMENT_LIST =
                """ 
                 SELECT * FROM SalePayment WHERE salePaymentSaleUid = :saleUid AND 
                 CAST(salePaymentActive AS INTEGER) = 1 ORDER BY salePaymentPaidDate DESC
                    
                """
    }
}
