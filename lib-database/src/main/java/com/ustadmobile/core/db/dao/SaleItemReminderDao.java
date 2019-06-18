package com.ustadmobile.core.db.dao;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleItemReminder;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN )
@UmRepository
public abstract class SaleItemReminderDao implements
        SyncableDao<SaleItemReminder, SaleItemReminderDao> {

    @UmQuery("SELECT * FROM SaleItemReminder WHERE SaleItemReminderSaleItemUid = :uid AND " +
            " saleItemReminderActive =1 AND saleItemReminderActive = 1")
    public abstract UmProvider<SaleItemReminder> findBySaleItemUid(long uid);

    @UmQuery("SELECT * FROM SaleItemReminder WHERE SaleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderActive = 1")
    public abstract List<SaleItemReminder> findBySaleItemUidList(long uid);

    @UmQuery("UPDATE SaleItemReminder SET SaleItemReminderActive = 0 WHERE SaleItemReminderUid = :uid " +
            "AND saleItemReminderActive = 1")
    public abstract void invalidateReminder(long uid, UmCallback<Integer> resultCallback);

    @UmQuery("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderActive = 1")
    public abstract void findBySaleItemUidAsync(long uid, UmCallback<List<SaleItemReminder>> resultCallback);

    @UmQuery("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderDays = :days AND saleItemReminderActive = 1")
    public abstract void findBySaleItemUidAndDaysAsync(long uid, int days, UmCallback<List<SaleItemReminder>> resultCallback);
}
