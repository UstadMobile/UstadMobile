package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.OpdsEndpoint;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AddFeedDialogView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Handles adding a feed to the user's feed list. Those feeds are stored as OPDS text in the
 * preferences keys, using OpdsEndpoint.
 */

public class AddFeedDialogPresenter extends UstadBaseController implements UstadJSOPDSItem.OpdsItemLoadCallback {

    private AddFeedDialogView addFeedDialogView;

    private UstadJSOPDSFeed presetFeeds;

    private int dropDownlSelectedIndex = 0;

    private String prefkey;

    public static final String ARG_PREFKEY = "pk";

    private UstadJSOPDSFeed loadingFeed;

    private String opdsUrlError = null;

    public AddFeedDialogPresenter(Object context, AddFeedDialogView addFeedDialogView) {
        super(context);
        this.addFeedDialogView = addFeedDialogView;
    }


    public void onCreate(Hashtable args, Hashtable savedState) {
        InputStream in = null;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        prefkey = (String)args.get(ARG_PREFKEY);

        try {
            in = impl.openResourceInputStream("/com/ustadmobile/core/libraries.opds", getContext());
            presetFeeds = new UstadJSOPDSFeed();
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(in);
            presetFeeds.loadFromXpp(xpp, null);

            String[] presetNames = new String[presetFeeds.size() + 2];
            presetNames[0] = "Select a feed";
            presetNames[1] = "Add by URL";
            for(int i = 0; i < presetFeeds.size(); i++) {
                presetNames[i + 2] = presetFeeds.getEntry(i).title;
            }


            addFeedDialogView.setDropdownPresets(presetNames);
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 682, null, e);
        }catch(XmlPullParserException x) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 682, null, x);
        }finally {
            UMIOUtils.closeInputStream(in);
        }
    }

    public void handlePresetSelected(int index) {
        addFeedDialogView.setUrlFieldVisible(index == 1);
        dropDownlSelectedIndex = index;
    }

    public void handleClickAdd() {
        if(dropDownlSelectedIndex > 1) {
            //take it from the libraries.opds preset that was selected
            UstadJSOPDSEntry entry = presetFeeds.getEntry(dropDownlSelectedIndex - 2);
            addFeed(entry, entry.getFirstLink(UstadJSOPDSItem.LINK_REL_SUBSECTION, null));
        }else if(dropDownlSelectedIndex == 1) {
            addFeedDialogView.setUiEnabled(false);
            addFeedDialogView.setProgressVisible(true);
            String feedUrl = addFeedDialogView.getOpdsUrl();
            loadingFeed = new UstadJSOPDSFeed(feedUrl);
            loadingFeed.loadFromUrlAsync(feedUrl, null, getContext(), this);
        }
    }

    public void handleClickCancel() {

    }

    public void handleOpdsUrlChanged(String opdsUrl) {
        if(opdsUrlError != null) {
            opdsUrlError = null;
            addFeedDialogView.setError(null);
        }
    }

    @Override
    public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entryLoaded) {

    }

    @Override
    public void onDone(UstadJSOPDSItem item) {
        if(item == loadingFeed) {
            String[] link = new String[UstadJSOPDSItem.LINK_ATTRS_END];
            link[UstadJSOPDSItem.ATTR_REL] = UstadJSOPDSItem.LINK_REL_SUBSECTION;
            link[UstadJSOPDSItem.ATTR_MIMETYPE] = UstadJSOPDSItem.TYPE_NAVIGATIONFEED;
            link[UstadJSOPDSItem.ATTR_HREF] = item.getHref();
            addFeed(item, link);
        }
    }

    @Override
    public void onError(UstadJSOPDSItem item, final Throwable cause) {
        if(item == loadingFeed) {
            addFeedDialogView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addFeedDialogView.setProgressVisible(false);
                    addFeedDialogView.setUiEnabled(true);
                    opdsUrlError = "Error: " + cause != null ? cause.getMessage() : "";
                    addFeedDialogView.setError(opdsUrlError);
                }
            });
        }
    }

    public void addFeed(UstadJSOPDSItem item, String[] link) {
        final boolean[] completedOk = new boolean[1];
        try {
            UstadJSOPDSFeed userFeedList = (UstadJSOPDSFeed)OpdsEndpoint.getInstance().loadItem(
                    UMFileUtil.joinPaths(new String[]{OpdsEndpoint.OPDS_PROTO_PREFKEY_FEEDS, prefkey}),
                    null, getContext(), null);
            UstadJSOPDSEntry feedEntry = new UstadJSOPDSEntry(userFeedList);
            feedEntry.title = item.title;
            feedEntry.id = item.id;
            feedEntry.addLink(link);
            String[] thumbnailLinks = item.getThumbnailLink(false);
            if(thumbnailLinks != null) {
                String[] thumbnailLinksMod = new String[thumbnailLinks.length];
                System.arraycopy(thumbnailLinks, 0, thumbnailLinksMod, 0, thumbnailLinksMod.length);
                thumbnailLinksMod[UstadJSOPDSItem.ATTR_HREF] = UMFileUtil.resolveLink(item.getHref(),
                        thumbnailLinksMod[UstadJSOPDSItem.ATTR_HREF]);
                feedEntry.addLink(thumbnailLinksMod);
            }
            userFeedList.addEntry(feedEntry);
            OpdsEndpoint.getInstance().saveFeedToPreferences(userFeedList, prefkey, getContext());
            completedOk[0] = true;
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 683, prefkey, e);
        }

        addFeedDialogView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(completedOk[0]) {
                    addFeedDialogView.dismiss();
                }else {
                    addFeedDialogView.setProgressVisible(false);
                    addFeedDialogView.setUiEnabled(true);
                }
            }
        });

    }

    @Override
    public void setUIStrings() {

    }


}
