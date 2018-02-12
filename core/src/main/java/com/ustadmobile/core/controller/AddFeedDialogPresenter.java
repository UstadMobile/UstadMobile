package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddFeedDialogView;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Handles adding a feed to the user's feed list. Those feeds are stored as OPDS text in the
 * preferences keys, using OpdsEndpoint.
 */

public class AddFeedDialogPresenter extends UstadBaseController implements OpdsEntry.OpdsItemLoadCallback {

    private AddFeedDialogView addFeedDialogView;

    private List<OpdsEntryWithRelations> presetFeedsList;

    private int dropDownlSelectedIndex = 0;

    public static final String ARG_UUID = "uuid";

    private String opdsUrlError = null;

    private UmLiveData<OpdsEntryWithRelations> entry;

    private UmLiveData<List<OpdsEntryWithRelations>> presetListLiveData;

    private UmObserver<List<OpdsEntryWithRelations>> presetListObserver;

    String loadedUuid;

    private String uuidToAddTo;

    public AddFeedDialogPresenter(Object context, AddFeedDialogView addFeedDialogView) {
        super(context);
        this.addFeedDialogView = addFeedDialogView;
    }

    public void onCreate(Hashtable args, Hashtable savedState) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        uuidToAddTo = (String)args.get(ARG_UUID);
        presetFeedsList = new ArrayList<>();
        entry = DbManager.getInstance(getContext()).getOpdsEntryWithRelationsRepository().getEntryByUrl(
                "asset:///com/ustadmobile/core/libraries.opds", "preset_libraries_opds");
        entry.observe(this, this::handlePresetParentFeedUpdated);
    }

    private void handlePresetParentFeedUpdated(OpdsEntryWithRelations presetParent) {
        if(loadedUuid == null && presetParent != null) {
            loadedUuid = presetParent.getUuid();
            presetListLiveData = DbManager.getInstance(getContext()).getOpdsEntryWithRelationsDao()
                    .getEntriesByParentAsList(loadedUuid);
            presetListObserver = this::handlePresetFeedListUpdated;
            presetListLiveData.observe(this, presetListObserver);
        }
    }

    private void handlePresetFeedListUpdated(List<OpdsEntryWithRelations> presetFeedsList) {
        if(presetFeedsList != null) {
            final String[] presetNames = new String[presetFeedsList.size() + 2];
            presetNames[0] = "Select a feed";
            presetNames[1] = "Add by URL";
            for(int i = 0; i < presetFeedsList.size(); i++) {
                presetNames[i + 2] = presetFeedsList.get(i).getTitle();
            }
            this.presetFeedsList = presetFeedsList;

            addFeedDialogView.runOnUiThread( () -> addFeedDialogView.setDropdownPresets(presetNames));
        }
    }

    public void handlePresetSelected(int index) {
        addFeedDialogView.setUrlFieldVisible(index == 1);
        dropDownlSelectedIndex = index;
    }

    public void handleClickAdd() {
        if(dropDownlSelectedIndex > 1) {
            //take it from the libraries.opds preset that was selected
            OpdsEntry addedEntry = presetFeedsList.get(dropDownlSelectedIndex-2);
            OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(uuidToAddTo,
                addedEntry.getUuid(), 0);
            final DbManager dbManager = DbManager.getInstance(getContext());

            OpdsEntryParentToChildJoinDao dao = dbManager.getOpdsEntryParentToChildJoinDao();

            dao.insertAsLastEntryForParentAsync(join, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    addFeedDialogView.runOnUiThread(() -> addFeedDialogView.dismiss());
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }else if(dropDownlSelectedIndex == 1) {
            addFeedDialogView.setUiEnabled(false);
            addFeedDialogView.setProgressVisible(true);
            String feedUrl = addFeedDialogView.getOpdsUrl();
            DbManager.getInstance(getContext()).getOpdsEntryWithRelationsRepository()
                    .getEntryByUrl(feedUrl, null, this);
        }
    }

    @Override
    public void onDone(OpdsEntry entry) {
        if(entry.getEntryId() != null && entry.getTitle() != null) {
            OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(uuidToAddTo,
                    entry.getUuid(), 0);
            OpdsEntryParentToChildJoinDao dao = DbManager.getInstance(getContext())
                    .getOpdsEntryParentToChildJoinDao();
            join.setChildIndex(dao.getNumEntriesByParent(uuidToAddTo)+1);
            dao.insertAsync(join, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    addFeedDialogView.runOnUiThread(() -> addFeedDialogView.dismiss());
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }else {
            onError(entry,
                    new IllegalArgumentException("No entry uuid or title - probably not an OPDS feed"));
        }

    }

    @Override
    public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {

    }

    @Override
    public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {

    }

    @Override
    public void onError(OpdsEntry item, Throwable cause) {
        addFeedDialogView.runOnUiThread(() -> {
            addFeedDialogView.setProgressVisible(false);
            addFeedDialogView.setUiEnabled(true);
            opdsUrlError = "Error: " + cause != null ? cause.getMessage() : "";
            addFeedDialogView.setError(opdsUrlError);
        });
    }


    @Override
    public void setUIStrings() {

    }

    public void handleOpdsUrlChanged(String opdsUrl) {
        if(opdsUrlError != null) {
            opdsUrlError = null;
            addFeedDialogView.setError(null);
        }
    }


}
