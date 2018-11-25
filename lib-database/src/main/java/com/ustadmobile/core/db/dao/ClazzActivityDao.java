package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.DailyActivityNumbers;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ClazzActivityDao implements SyncableDao<ClazzActivity, ClazzActivityDao> {

    @UmInsert
    public abstract long insert(ClazzActivity entity);

    @UmUpdate
    public abstract void update(ClazzActivity entity);

    @UmInsert
    public abstract void insertAsync(ClazzActivity entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzActivity")
    public abstract UmProvider<ClazzActivity> findAllClazzActivityChanges();

    @UmUpdate
    public abstract void updateAsync(ClazzActivity entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM ClazzActivity where clazzActivityClazzUid = :clazzUid")
    public abstract UmProvider<ClazzActivity> findByClazzUid(long clazzUid);

    @UmQuery("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    public abstract ClazzActivity findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<ClazzActivity> result);

    @UmQuery("SELECT * FROM ClazzActivity WHERE clazzActivityClazzUid = :clazzUid AND " +
            " clazzActivityLogDate = :logDate")
    public abstract ClazzActivity findByClazzAndDate(long clazzUid, long logDate);

    @UmQuery("SELECT * FROM ClazzActivity WHERE clazzActivityClazzUid = :clazzUid AND " +
            " clazzActivityLogDate = :logDate")
    public abstract void findByClazzAndDateAsync(long clazzUid, long logDate, UmCallback<ClazzActivity> result);


    @UmQuery("SELECT  " +
            " COUNT(CASE WHEN ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as good, " +
            " COUNT(CASE WHEN NOT ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as bad, " +
            " (:clazzUid) as clazzUid, " +
            " ClazzActivity.clazzActivityLogDate as dayDate " +
            " FROM ClazzActivity " +
            " WHERE ClazzActivity.clazzActivityClazzUid = :clazzUid " +
            " AND ClazzActivity.clazzActivityLogDate > :fromDate " +
            " AND ClazzActivity.clazzActivityLogDate < :toDate " +
            " GROUP BY ClazzActivity.clazzActivityLogDate ")
    public abstract void getDailyAggregateFeedback(long clazzUid, long fromDate, long toDate,
                                                   UmCallback<List<DailyActivityNumbers>> results);

    @UmQuery("SELECT  " +
            " COUNT(CASE WHEN ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as good, " +
            " COUNT(CASE WHEN NOT ClazzActivity.clazzActivityGoodFeedback THEN 1 END) as bad, " +
            " (:clazzUid) as clazzUid, " +
            " ClazzActivity.clazzActivityLogDate as dayDate " +
            " FROM ClazzActivity " +
            " WHERE ClazzActivity.clazzActivityClazzUid = :clazzUid " +
            " AND ClazzActivity.clazzActivityLogDate > :fromDate " +
            " AND ClazzActivity.clazzActivityLogDate < :toDate " +
            " AND ClazzActivity.clazzActivityClazzActivityChangeUid = :activityChangeUid " +
            " GROUP BY ClazzActivity.clazzActivityLogDate ")
    public abstract void getDailyAggregateFeedbackByActivityChange(
            long clazzUid, long fromDate, long toDate, long activityChangeUid,
                                                   UmCallback<List<DailyActivityNumbers>> results);

    public void createClazzActivityForDate(long currentClazzUid, long currentLogDate,
                                      UmCallback<Long> callback){

        findByClazzAndDateAsync(currentClazzUid, currentLogDate, new UmCallback<ClazzActivity>() {
            @Override
            public void onSuccess(ClazzActivity result) {
                if(result != null){
                    callback.onSuccess(result.getClazzActivityUid());
                }else{
                    //Create one
                    ClazzActivity newClazzActivity = new ClazzActivity();
                    newClazzActivity.setClazzActivityLogDate(currentLogDate);
                    newClazzActivity.setClazzActivityDone(false); //should be set to true with done
                    newClazzActivity.setClazzActivityClazzUid(currentClazzUid);

                    insertAsync(newClazzActivity, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


}
