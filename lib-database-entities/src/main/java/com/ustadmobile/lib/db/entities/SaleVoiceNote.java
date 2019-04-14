package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 75)
public class SaleVoiceNote {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long saleVoiceNoteUid;

    private long saleVoiceNoteSaleUid;

    private int saleVoiceNoteFileSize;

    private long saleVoiceNoteTimestamp;

    private String saleVoiceNoteMime;

    @UmSyncMasterChangeSeqNum
    private long saleVoiceNoteMCSN;

    @UmSyncLocalChangeSeqNum
    private long saleVoiceNoteCSN;

    @UmSyncLastChangedBy
    private int saleVoiceNoteLCB;


    public String getSaleVoiceNoteMime() {
        return saleVoiceNoteMime;
    }

    public void setSaleVoiceNoteMime(String saleVoiceNoteMime) {
        this.saleVoiceNoteMime = saleVoiceNoteMime;
    }

    public long getSaleVoiceNoteMCSN() {
        return saleVoiceNoteMCSN;
    }

    public void setSaleVoiceNoteMCSN(long saleVoiceNoteMCSN) {
        this.saleVoiceNoteMCSN = saleVoiceNoteMCSN;
    }

    public long getSaleVoiceNoteCSN() {
        return saleVoiceNoteCSN;
    }

    public void setSaleVoiceNoteCSN(long saleVoiceNoteCSN) {
        this.saleVoiceNoteCSN = saleVoiceNoteCSN;
    }

    public int getSaleVoiceNoteLCB() {
        return saleVoiceNoteLCB;
    }

    public void setSaleVoiceNoteLCB(int saleVoiceNoteLCB) {
        this.saleVoiceNoteLCB = saleVoiceNoteLCB;
    }

    public long getSaleVoiceNoteUid() {
        return saleVoiceNoteUid;
    }

    public void setSaleVoiceNoteUid(long saleVoiceNoteUid) {
        this.saleVoiceNoteUid = saleVoiceNoteUid;
    }

    public long getSaleVoiceNoteSaleUid() {
        return saleVoiceNoteSaleUid;
    }

    public void setSaleVoiceNoteSaleUid(long saleVoiceNoteSaleUid) {
        this.saleVoiceNoteSaleUid = saleVoiceNoteSaleUid;
    }

    public int getSaleVoiceNoteFileSize() {
        return saleVoiceNoteFileSize;
    }

    public void setSaleVoiceNoteFileSize(int saleVoiceNoteFileSize) {
        this.saleVoiceNoteFileSize = saleVoiceNoteFileSize;
    }

    public long getSaleVoiceNoteTimestamp() {
        return saleVoiceNoteTimestamp;
    }

    public void setSaleVoiceNoteTimestamp(long saleVoiceNoteTimestamp) {
        this.saleVoiceNoteTimestamp = saleVoiceNoteTimestamp;
    }


}
