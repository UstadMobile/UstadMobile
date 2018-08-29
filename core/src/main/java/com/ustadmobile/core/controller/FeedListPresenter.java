package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.FeedListView;
import com.ustadmobile.lib.db.entities.FeedEntry;

import java.util.Hashtable;
import java.util.List;

public class FeedListPresenter extends UstadBaseController<FeedListView>{

    private long personUid = -1L;

    public FeedListPresenter(Object context, Hashtable arguments, FeedListView view) {
        super(context, arguments, view);
    }

    @Override
    public void setUIStrings() {

    }

    private UmProvider<FeedEntry> feedEntryUmProvider;

    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        //Testing: TODO: Remove
        personUid = 1L;
        new Thread(() -> {
            List<FeedEntry> all2 =
                    UmAppDatabase.getInstance(view.getContext()).getFeedEntryDao().findByPersonUidList(personUid);
            int size2 = all2.size();
        }).start();

        feedEntryUmProvider = UmAppDatabase.getInstance(view.getContext()).getFeedEntryDao()
                .findByPersonUid(personUid);
        view.setFeedEntryProvider(feedEntryUmProvider);

    }

    public static Hashtable splitQuery(String query) {
        Hashtable query_pairs = new Hashtable();

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return query_pairs;
    }


    /**
     * Takes action on a feed.
     */
    public void handleClickFeedEntry(FeedEntry feedEntry){
        //TODO: Check this
        String feedLink = feedEntry.getLink();

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String linkViewName = feedLink.split("\\?")[0];
        Hashtable args = splitQuery(feedLink);

        impl.go(linkViewName, args, view.getContext());

    }


}
