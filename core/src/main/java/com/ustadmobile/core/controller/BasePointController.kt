/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.BasePointMenuItem
import com.ustadmobile.core.view.BasePointView
import com.ustadmobile.core.view.UstadView

import java.util.HashMap
import java.util.Vector

/**
 *
 * The base point is right now basically a wrapper containing two catalogs
 *
 *
 * @author mike
 */
class BasePointController(context: Any, private var basePointView: BasePointView?) : UstadBaseController<UstadView>(context) {

    private var args: Map<String, String>? = null

    var isWelcomeScreenDisplayed = false

    private val keepTmpVariables = false

    fun onCreate(args: Map<String, String>?, savedState: Map<String, String>?) {
        this.args = args
        basePointView!!.setClassListVisible(false)
        val impl = UstadMobileSystemImpl.instance

        if (savedState != null && savedState.containsKey(ARG_WELCOME_SCREEN_DISPLAYED)) {
            isWelcomeScreenDisplayed = savedState[ARG_WELCOME_SCREEN_DISPLAYED] == "true"
        }

        var catalogTabs: Vector<*>? = null
        if (args != null) {
            catalogTabs = UMFileUtil.splitCombinedViewArguments(args, "catalog", '-')
        }

        if (catalogTabs == null || catalogTabs.isEmpty()) {
            val defaultArgs = UstadMobileSystemImpl.instance.getAppConfigString(AppConfig.KEY_FIRST_DEST, null, getContext())
            catalogTabs = UMFileUtil.splitCombinedViewArguments(UMFileUtil.parseURLQueryString(defaultArgs!!) as Map<String, String>,
                    "catalog", '-')
        }

        for (i in catalogTabs.indices) {
            basePointView!!.addTab(catalogTabs.elementAt(i) as Map<String, String>)
        }
    }

    /**
     * For use by the related view: generate the required arguments
     *
     * @param position
     *
     * @return
     */
    fun getCatalogOPDSArguments(position: Int): MutableMap<String, String?> {
        val keys = args!!.keys.iterator()
        val result = mutableMapOf<String,String?>()
        var keyVal: String
        val prefix = position.toString() + OPDS_ARGS_PREFIX
        val prefixLen = prefix.length
        while (keys.hasNext()) {
            keyVal = keys.next()
            if (keyVal.startsWith(prefix)) {
                result[keyVal.substring(prefixLen)] = args!![keyVal]
            }
        }

        return result
    }

    override fun setView(view: UstadView) {
        if (view is BasePointView) {
            super.setView(view)
            this.basePointView = view
        } else {
            throw IllegalArgumentException("Must be basepointview")
        }
    }


    /**
     * Handle when the user clicks one of the base point menu items.
     *
     * This is configured via the buildconfig system : see buildconfig.default.properties for
     * details on configuring this.
     *
     * @param item
     */
    fun handleClickBasePointMenuItem(item: BasePointMenuItem) {
        UstadMobileSystemImpl.instance.go(item.destination, getContext())
    }

    override fun onResume() {

    }

    override fun onDestroy() {
        if (!keepTmpVariables) {
            UstadMobileSystemImpl.instance.setAppPref("tmp$ARG_WELCOME_SCREEN_DISPLAYED",
                    null!!, getContext())
        }
    }

    fun handleClickShareApp() {
        basePointView!!.showShareAppDialog()
    }

    fun handleClickReceive() {
        UstadMobileSystemImpl.instance.go("ReceiveCourse", null, getContext())
    }

    fun handleClickConfirmShareApp(zip: Boolean) {
        //        final UstadMobileSystemImpl impl =UstadMobileSystemImpl.getInstance();
        //        basePointView.setShareAppDialogProgressVisible(true);
        //        impl.getAppSetupFile(getContext(), zip, new UmCallback() {
        //
        //            @Override
        //            public void onSuccess(Object result) {
        //                impl.getNetworkManager().shareAppSetupFile((String)result,
        //                        impl.getString(MessageID.share, getContext()));
        //                basePointView.dismissShareAppDialog();
        //            }
        //
        //            @Override
        //            public void onFailure(Throwable exception) {
        //
        //            }
        //        });
    }

    companion object {

        /**
         * The arguments given to this class are passed down to create two
         * catalog views: e.g. 0-opds-url -> http://server.com/file.opds
         *
         * Will get passed down as url -> http://server.com/file.opds
         */
        val OPDS_ARGS_PREFIX = "-opds"

        /**
         * Indicates the tab for items already downloaded
         */
        val INDEX_DOWNLOADEDENTRIES = 0

        /**
         * Indicates the tab for browsing OPDS feeds
         */
        val INDEX_BROWSEFEEDS = 1

        /**
         * Indicates the tab for class management
         */
        val INDEX_CLASSES = 1

        val NUM_CATALOG_TABS = 1

        val ARG_WELCOME_SCREEN_DISPLAYED = "wsd"

        val CMD_SHARE_APP = 1005

        val CMD_RECEIVE_ENTRY = 1006
    }


}
