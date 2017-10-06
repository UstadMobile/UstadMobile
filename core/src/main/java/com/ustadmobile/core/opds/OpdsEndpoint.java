package com.ustadmobile.core.opds;

import com.ustadmobile.core.catalog.DirectoryScanner;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;
import java.util.Hashtable;

import static com.ustadmobile.core.controller.CatalogPresenter.USER_RESOURCE;

/**
 * Created by mike on 10/4/17.
 */

public class OpdsEndpoint {

    private static OpdsEndpoint instance = new OpdsEndpoint();

    public static final String OPDS_PROTOCOL = "opds:///";

    /**
     * A url that provides a list of the of the contents that have been downloaded
     * to the device
     */
    public static final String OPDS_PROTO_DEVICE = "opds:///com.ustadmobile.app.devicefeed";

    public static final String ARG_BASE_HREF = "basehref";

    public static final String ARG_LINK_HREF_MODE = "hrefmode";

    public static final String ARG_DEVICEFEED_RESOURCE_MODE = "resmode";

    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * with file URLs
     */
    public static final int LINK_HREF_MODE_FILE = 0;

    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * ids e.g. baseHref/containerId
     */
    public static final int LINK_HREF_MODE_ID = 1;



    public static OpdsEndpoint getInstance() {
        return instance;
    }


    /**
     *
     * @param opdsUri
     * @param item An UstadJSOPDSFeed or UstadJSOPDSEntry object that the opdsUri will be loaded into.
     *             (Optional). Can be null, if null a new object will be created.
     * @param context
     * @return
     * @throws IOException
     */
    public UstadJSOPDSItem loadItem(String opdsUri, UstadJSOPDSItem item, Object context, UstadJSOPDSItem.OpdsItemLoadCallback callback) throws IOException {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UstadMobileSystemImpl.l(UMLog.DEBUG, 678, "OpdsEndpoint: load " + opdsUri);
        if(opdsUri.startsWith(OPDS_PROTO_DEVICE)) {
            Hashtable args = UMFileUtil.parseURLQueryString(opdsUri);
            int resourceMode = args.containsKey(ARG_DEVICEFEED_RESOURCE_MODE) ?
                    ((Integer)args.get(ARG_DEVICEFEED_RESOURCE_MODE)).intValue() : CatalogPresenter.SHARED_RESOURCE;

            String baseHref;
            if(args.containsKey(ARG_BASE_HREF)) {
                baseHref = (String)args.get(ARG_BASE_HREF);
            }else {
                if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
                    baseHref = impl.getUserContentDirectory(impl.getActiveUser(context));
                }else {
                    baseHref = impl.getSharedContentDir();
                }
            }

            int linkMode = args.containsKey(ARG_LINK_HREF_MODE) ?
                    ((Integer)args.get(ARG_LINK_HREF_MODE)).intValue() : LINK_HREF_MODE_FILE;


            UMStorageDir[] dirs = impl.getStorageDirs(resourceMode, context);
            UstadJSOPDSFeed deviceFeed = item != null ? (UstadJSOPDSFeed)item : null;
            return makeDeviceFeed(dirs, baseHref, linkMode, deviceFeed, context, callback);
        }else {
            return null;
        }
    }

    protected UstadJSOPDSFeed makeDeviceFeed(UMStorageDir[] dirs, String baseHREF, int linkHrefMode,
                 UstadJSOPDSFeed deviceFeed, Object context,
                 UstadJSOPDSItem.OpdsItemLoadCallback callback) throws IOException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(deviceFeed == null) {
            deviceFeed = new UstadJSOPDSFeed(OpdsEndpoint.OPDS_PROTO_DEVICE,
                    impl.getString(MessageID.my_resources, context), "com.ustadmobile.devicefeed");
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setHrefModeBaseHref(baseHREF);
        scanner.setLinkHrefMode(linkHrefMode);

        int dirMode;
        for(int i = 0; i < dirs.length; i++) {
            dirMode = dirs[i].isUserSpecific()
                    ? USER_RESOURCE
                    : CatalogPresenter.SHARED_RESOURCE;
            scanner.scanDirectory(dirs[i].getDirURI(), null, "scandir", "scandir",
                    dirMode, callback, deviceFeed, context);
        }

        return deviceFeed;
    }
}
