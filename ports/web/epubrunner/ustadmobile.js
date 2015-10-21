/* 
<!--This file is part of Ustad Mobile.  
    
    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:
    
    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ 
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new 
    functionality whose purpose is to diminish or remove the Ustad Mobile 
    Logo.  You must leave the Ustad Mobile logo as the logo for the 
    application to be used with any launcher (e.g. the mobile app launcher).  
    
    If you want a commercial license to remove the above restriction you must
    contact us.  
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.




This program is free software.  It is licensed under the GNU GENERAL PUBLIC LICENSE ( http://www.gnu.org/copyleft/gpl.html ) with the following 

GPL License Additional Terms

All names, links, and logos of Ustad Mobile and Toughra Technologies FZ LLC must be kept as they are in the original distribution.  If any new screens are added you must include the Ustad Mobile logo as it has been used in the original distribution.  You may not create any new functionality whose purpose is to diminish or remove the Ustad Mobile Logo.  You must leave the Ustad Mobile logo as the logo for the application to be used with any launcher (e.g. the mobile app launcher).  

If you need a commercial license to remove these restrictions please contact us by emailing info@ustadmobile.com 

-->

*/

/* 

This javascript creates the header and footer of ustad mobile content in packages and does a lot of global actions via the functions (esp Menu Links).

*/

//require('nw.gui').Window.get().showDevTools();
//alert("loaded tools");

var UstadMobile;

var ustadMobileInstance = null;

/**
 * Creates the main UstadMobile Object
 * 
 * @class UstadMobile
 * @constructor
 */
UstadMobile = function() {
    /** 
     * Main app controller for title, menu items etc.
     * 
     * @type {UstadMobileAppController} 
     */
    this.appController = null;
};

/**
 * Get the Instance of UstadMobile class
 * @returns {UstadMobile} UstadMobile instance
 */
UstadMobile.getInstance = function() {
    if(ustadMobileInstance === null) {
        ustadMobileInstance = new UstadMobile();
    }
    
    return ustadMobileInstance;
};

/**
 * Constant: the base directory name where content is put - in the global or
 * app specific persistent storage area
 * 
 * @type String
 */
UstadMobile.CONTENT_DIRECTORY = "ustadmobileContent";

/**
 * Constant: the prefix to add to force an attachment to download for save-as
 * on the HTTP server
 * 
 * @type String
 */
UstadMobile.HTTP_ATTACHMENT_POSTFIX = "ustad_attachment_download";

/**
 * Constant: The subdirectory, under CONTENT_DIRECTORY where in progress
 * downloads are carried out until complete
 * 
 * @type String
 */
UstadMobile.DOWNLOAD_SUBDIR = "inprogress";

/**
 * Constant representing Linux OS
 * @type Number
 */
UstadMobile.OS_LINUX = 0;

/**
 * Constant representing the Windows OS
 * @type type
 */
UstadMobile.OS_WINDOWS = 1;

/**
 * Indicates page goes to the left - fixed 0 value
 * 
 * @type Number
 */
UstadMobile.LEFT = 0;

/**
 * Indicates spage goes to the right - fixed 1 value
 * 
 * @type Number
 */
UstadMobile.RIGHT = 1;

/**
 * 
 * @type Number
 */
UstadMobile.MIDDLE = 2;

/**
 * Indicates that we are in the app context
 * 
 * @type Number
 */
UstadMobile.ZONE_APP = 0;

/**
 * Indicates that we are in the content context
 * 
 * @type Number
 */
UstadMobile.ZONE_CONTENT = 1;

/**
 * To use with standardized naming for files related to the app or content zone
 * e.g. 
 *  UstadMobile.ZONENAMES[UstadMobile.ZONE_APP] = "app"
 *  UstadMobile.ZONENAMES[UstadMobile.ZONE_CONTENT] = "content"
 *  
 * @type Array
 */
UstadMobile.ZONENAMES = ["app", "content"];

/**
 * Constant telling UstadMobile to get the in content menu contents from the
 * content directory itself - use for NodeWebKit
 * 
 * @type {Number} 
 */
UstadMobile.MENUMODE_USECONTENTDIR = 0;

/**
 * Constant representing the runtime config key for the menu
 * 
 * @type {String}
 */
UstadMobile.RUNTIME_MENUMODE = "ustad_menumode";

/**
 * Constant: URL for Internal HTTP server to use to close content iframe
 * 
 * @type String
 */
UstadMobile.URL_CLOSEIFRAME = "/closeiframe";

UstadMobile.URL_TINCAN_QUEUE = "/tincan-queue"

/**
 * Constant: URL to request page cleanup procedure (e.g. ThreadTimer issue)
 * To run when a page is removed from memory.
 * 
 * @type String
 */
UstadMobile.URL_PAGECLEANUP = "/pagecleanup";

/**
 * Constant - Go page for content...
 * @type string 
 */
UstadMobile.PAGE_BOOKLIST = "ustadmobile_booklist.html";

/**
 * Constant - Go page for course download
 * 
 * @type string
 */
UstadMobile.PAGE_DOWNLOAD = "ustadmobile_getPackages.html";

/**
 * Constant - page for settings
 * @type string
 */
UstadMobile.PAGE_SETTINGS = "ustadmobile_setLanguage.html";

/** 
 * Constant - page for about menu
 * 
 * @type String
 */
UstadMobile.PAGE_ABOUT = "ustadmobile_aboutus.html";


/**
 * Constant - page for the table of contents
 * @type String
 */
UstadMobile.PAGE_TOC = "exetoc.html";

/**
 * Constant - page for showing dialog for sending feedback
 * 
 * @type String
 */
UstadMobile.PAGE_FEEDBACK = "feedback_dialog.html";

/**
 * Constant - page for login
 * 
 * @type string
 */
UstadMobile.PAGE_LOGIN = "index.html";

/**
 * 
 * @type type string
 */
UstadMobile.PAGE_CONTENT_MENU = "ustadmobile_menupage_content.html";


/**
 * Default TinCan Prefix for activities that have no tincan.xml file
 * @type String
 */
UstadMobile.TINCAN_DEFAULT_PREFIX = 
        "http://www.ustadmobile.com/um-tincan/activities/";

UstadMobile.prototype = {
    
    /**
     * If base paths (content directory etc) are ready
     * 
     * @type Boolean
     */
    pathsReady: false,
    
    /**
     * Array of functions to run once paths are created
     * @type {Array}
     */
    pendingPathEventListeners: [],
    
    /**
     * If the internal http server is ready
     * 
     * @type Boolean
     */
    httpServerReady: false,
    
    /**
     * Array of functions to run once the http server is ready
     * 
     * @type {Array}
     */
    pendingHttpListeners: [],
    
    
    /**
     * Panel HTML to be used
     * @type {String}
     */
    panelHTML : null,
    
    
    /**
     * The directory that course assets are downloaded to whilst download
     * is in process.
     * 
     * @type {String}
     */
    downloadDestDirURI: null,
    
    /**
     * The directory where (complete) courses are saved to
     * 
     * @type {String}
     */
    contentDirURI: null,
    
    /**
     * Information needed for the running of the app - can be set by a file
     * that gets lazy loaded.
     * 
     * @returns {Object}
     */
    runtimeInfo: {},
    
    /**
     * Whether or not the runtime info has loaded
     * 
     * @type Boolean
     */
    runtimeInfoLoaded : false,
    
    /**
     * List of functions that need to run once we have loaded runtime info
     * @returns {undefined}
     */
    pendingRuntimeInfoLoadedListeners: [],
    
    
    /**
     * Used to control startup init - load files specific to this implementation
     * (e.g. Cordova only, NodeWebKit only, Content/App Zone only etc)
     * 
     * @type Array
     */
    initScriptsToLoad: [],
    
    /**
     * Used to check if init scripts have loaded - array of booleans
     * @type Array
     */
    initScriptsLoaded: [],
    
    /**
     * Whether or not all init scripts have loaded
     * 
     * @type Boolean
     */
    initScriptsAllLoaded: false,
    
    /**
     * User interface language - lang code as in en-US
     * 
     * @type string
     */
    _uiLang: null,
    
    /**
     * System implementation layer 
     * 
     * @type UstadMobileAppImplementation
     */
    systemImpl: null,
    
    /**
     * Functions to run after init has taken place
     * @type Array
     */
    pendingRunAfterInitListeners: [],
    
    /**
     * Listeners and callbacks to run 
     * @type Array
     */
    implementationReadyListeners: [],
    
    
    
    
    /**
     * Primary startup method - To happen on mobileinit
     * 
     */
    init: function() {
        $.mobile.allowCrossDomainPages = true;
        $.support.cors = true;
        console.log("Mobileinit changes set for jQuery mobile for PhoneGap");
        
        //Load the scripts appropriate to the implementation and context
        this.loadInitScripts(function() {        
            console.log("main ustad mobile init running - scripts loaded");
            if(UstadMobile.getInstance().getZone() === UstadMobile.ZONE_APP) {
                UstadMobile.getInstance().checkPaths();
            }
            
            $(document).on( "pagecontainershow", function( event, ui ) {
                UstadMobile.getInstance().pageInit(event, ui);
            });
            
            console.log("Zone detect: " + UstadMobile.getInstance().getZone());

            if(UstadMobile.getInstance().getZone() === UstadMobile.ZONE_CONTENT) {
                UstadMobileContentZone.getInstance().init();
            }else {
                UstadMobileAppZone.getInstance().init();
            }
            
            UstadMobile.getInstance().initScriptsAllLoaded = true;
            
            UstadMobileUtils.runAllFunctions(
                    UstadMobile.getInstance().pendingRunAfterInitListeners,
                    [true], UstadMobile.getInstance());
            
        });
    },
    
    /**
     * Run the given function when the given implementation is ready.  If 
     * implementation is ready run it immediately, otherwise 
     * 
     * @method
     * @param function fn function to run
     */
    runWhenImplementationReady: function(fn) {
        var isReady = this.systemImpl !== null
                && this.systemImpl.implementationReady;
        UstadMobileUtils.runOrWait(isReady, fn, [true], this, 
            this.implementationReadyListeners);
    },
    
    /**
     * Fire the implementation readyd event
     * @method
     */
    fireImplementationReady: function() {
        this.systemImpl.implementationReady = true;
        UstadMobileUtils.runAllFunctions(this.implementationReadyListeners, [true],
            this);
    },
    
    
    /**
     * Run the given function once all init scripts have loaded
     * this will refere to UstadMobile.getInstance() .  If this has already
     * happened - run now.  Otherwise run when it's all done.
     * 
     * @param {type} fn
     */
    runWhenInitDone: function(fn) {
        UstadMobileUtils.runOrWait(this.initScriptsAllLoaded, fn, [true], this,
            this.pendingRunAfterInitListeners);
    },
    
    /**
     * Programmatically load a list of scripts in order sequentially
     * 
     * @param scriptList Array array of scripts to load sequentially
     * @param completionCallback function Callback to run when done attempting to load all
     * 
     */
    loadScriptsInOrder: function(scriptList, completionCallback) { 
        var currentScriptIndex = 0;
        var totalLoaded = 0;
        
        var goNextScript = function() {
            if(currentScriptIndex < (scriptList.length-1)) {
                currentScriptIndex++;
                loadScriptFn();
            }else {
                UstadMobileUtils.runCallback(completionCallback,
                            [totalLoaded], this);
            }
        };
        
        var loadScriptFn = function() {
            UstadMobile.getInstance().loadUMScript(
                scriptList[currentScriptIndex], 
                function() {
                    //success callback
                    console.log("Loaded script: " + scriptList[currentScriptIndex]);
                    totalLoaded++;
                    goNextScript();
                }, function() {
                    console.log("Failed to load: " + scriptList[currentScriptIndex]);
                    goNextScript();
                });
        };
        
        loadScriptFn();
    },
    
    
    /**
     * Function to load all init scripts required depending on the environment
     * we are running in
     * 
     * @method
     * @param function successCallback : call when all have successfully loaded
     * @param function failCallback : call if there is a failure
     */
    loadInitScripts: function(successCallback, failCallback) {
        var umObj = UstadMobile.getInstance();
        
        if(umObj.getZone() === UstadMobile.ZONE_CONTENT) {
            umObj.initScriptsToLoad.push("ustadmobile-localization.js");
            umObj.initScriptsToLoad.push("ustadmobile-contentzone.js");
            umObj.initScriptsToLoad.push("ustadjs.js");
        }else {
            umObj.initScriptsToLoad.push("js/ustadmobile-controllers.js");
            umObj.initScriptsToLoad.push("js/ustadmobile-views.js");
            umObj.initScriptsToLoad.push("js/ustadmobile-models.js");
            
            umObj.initScriptsToLoad.push("lib/ustadjs.js");
            umObj.initScriptsToLoad.push("lib/tincan.js");
            umObj.initScriptsToLoad.push("lib/tincan_queue.js");
            umObj.initScriptsToLoad.push("js/ustadmobile-getpackages.js");
            if(UstadMobile.getInstance().isNodeWebkit()) {
                umObj.initScriptsToLoad.push("js/ustadmobile-http-server.js");
            }
            
            umObj.initScriptsToLoad.push("js/ustadmobile-localization.js");
            umObj.initScriptsToLoad.push("js/ustadmobile-appzone.js");
            var implName = umObj.isNodeWebkit() ? "nodewebkit" : (window.cordova ?
                "cordova" : null);
            if(implName !== null) {
                umObj.initScriptsToLoad.push("js/ustadmobile-appimpl-" 
                        + implName + ".js");
                umObj.initScriptsToLoad.push("js/ustadmobile-views-" 
                        + implName + ".js");
            }
        }
        
        var numScripts = umObj.initScriptsToLoad.length;
        
        umObj.initScriptsLoaded = umObj.makeArray(false, numScripts);
        
        for(var i = 0; i < numScripts; i++) {
            umObj.loadUMScript(umObj.initScriptsToLoad[i], function(evt) {
                var scriptEl = evt.target || evt.srcElement;
                var scriptIndex = umObj.initScriptsToLoad.indexOf(
                        scriptEl.getAttribute("src"));
                umObj.initScriptsLoaded[scriptIndex] = true;
                if(umObj.countVal(umObj.initScriptsLoaded, true) === numScripts) {
                    successCallback();
                }
            });
        }
    },
    
    /**
     * Find the hint that we can use for the actual viewport
     * Taken from 
     * http://stackoverflow.com/questions/22103323/jquery-mobile-viewport-height
     * 
     * @returns {Number} viewport height minus toolbars in pixels
     */
    getJQMViewportHeight: function() {
        var screen = $.mobile.getScreenHeight();
        var header = $(".ui-page-active .ui-header").hasClass("ui-header-fixed") ? $(".ui-page-active .ui-header").outerHeight() - 1 : $(".ui-page-active .ui-header").outerHeight();
        var footer = $(".ui-page-active .ui-footer").hasClass("ui-footer-fixed") ? $(".ui-page-active .ui-footer").outerHeight() - 1 : $(".ui-page-active .ui-footer").outerHeight();

        /* content div has padding of 1em = 16px (32px top+bottom). This step
        can be skipped by subtracting 32px from content var directly. */
        var contentCurrent = $(".ui-page-active .ui-content").outerHeight() - $(".ui-page-active .ui-content").height();

        var contentHeight = screen - header - footer - contentCurrent;
        return contentHeight;
    },
    
    /**
     * Load the localization Strings for the given language
     * 
     * @param string localeCode
     * @param function completeCallback - optional run when initLocale is done
     * 
     * @returns {undefined}
     */
    initLocale: function(localeCode, completeCallback) {
        if(UstadMobileLocalization.SUPPORTED_LANGS.indexOf(localeCode) === -1) {
            throw "Exception: Language " + localeCode + " is not supported";
        }
        
        this.loadUMScript("locale/" + localeCode + ".js", function() {
            UstadMobileUtils.runCallback(completeCallback, [true], 
                UstadMobile.getInstance());
        });
    },
    
    /**
     * Utility function to count the number of occurences of a value in an array
     * 
     * @param Array arr
     * @param number valToCount
     * 
     * @returns number The number of occurences of valToCount in arr
     */
    countVal : function(arr, valToCount) {
        var instanceCount = 0;
        for(var i = 0; i < arr.length; i++) {
            if(arr[i] === valToCount) {
                instanceCount++;
            }
        }
        
        return instanceCount;
    },
    
    /**
     * Utility function to make an array of a given length and set all values
     * to defaultval
     * 
     * @method
     * @param defaultVal mixed Default value to give to each element
     * @param count number number of elements to make
     * @returns Array with all values set to defaultVal
     */
    makeArray: function(defaultVal, count) {
        var retVal = [];
        for(var i = 0; i < count; i++) {
            retVal.push(defaultVal);
        }
        
        return retVal;
    },
    
    
    /**
     * Pre init - happens on documentready
     * 
     */
    preInit: function() {
        //required to make sure exe created pages show correctly
        console.log("UstadMobile: Running Pre-Init");
        $("body").addClass("js");
        //this.loadPanel();
        
        if(UstadMobile.getInstance().getZone() === UstadMobile.ZONE_CONTENT) {
            this.loadRuntimeInfo();
        }
    },
    
    /**
     * Runs when the page event is triggered
     * 
     * @param evt {Object} from jQueryMobile
     * @param ui {Object} UI param from jQueryMobile event
     */
    pageInit: function(evt, ui) {
        
    },
    
    /**
     * Detect if we run nodewebkit
     * @returns {Boolean} true/false if we are running in node webkit
     */
    isNodeWebkit: function() {
        if(typeof require !== "undefined") {
            return true;
        }else {
            return false;
        }
    },
    
    /**
     * Detect if we are running cordova
     * @return boolean true if running in cordova, false otherwise
     */
    isCordova: function() {
        if(window.cordova) {
            return true;
        }else {
            return false;
        }
    },
    
    /**
     * Detect what Operating System NodeWebKit is running
     * on - really just using process.platform (for now)
     *
     * @return Number Numerical flag representing the OS: 
     * UstadMobile.OS_WINDOWS or OS_LINUX etc, -1 if not nodewebkit
     * @method getNodeWebKitOS
     */
    getNodeWebKitOS: function() {
        if(UstadMobile.getInstance().isNodeWebkit()) {
            if(process.platform === "win32") {
                return UstadMobile.OS_WINDOWS;
            }else if(process.platform === "linux") {
                return UstadMobile.OS_LINUX;
            }
        }else {
            return -1;
        }
    },
    
    /**
     * Loads a script by dynamically inserting a script tag in the head element.
     * If the script is already in the head - it will do nothing and run the success
     * callback with just one parameter (true).
     * 
     * @param scriptURL string script to load
     * @param successCallback function Function to run on successful completion (optional)
     * @param failCallback function Function to run on failure (optional)
     */
    loadUMScript: function(scriptURL, successCallback, failCallback) {
        var scriptEls = document.getElementsByTagName("script");
        for(var i = 0; i < scriptEls.length; i++) {
            if(scriptEls[i].getAttribute("src") === scriptURL) {
                //this script already loaded; return
                UstadMobileUtils.runCallback(successCallback, [true], scriptEls[i]);
                return;
            }
        }
        
        var fileref=document.createElement('script');
        fileref.setAttribute("type","text/javascript");
        fileref.setAttribute("src", scriptURL);
        if(typeof successCallback !== "undefined" && successCallback !== null) {
            fileref.onload = successCallback;
        }
        if(typeof failCallback !== "undefined" && failCallback !== null) {
            fileref.onerror = failCallback;
        }
        
        document.getElementsByTagName("head")[0].appendChild(fileref);
    },
   
    /**
     * Load ustad_runtime.json if it exists to acquire hints (e.g. path back
     * to the app directory etc.
     * 
     * @param runtimeCallback {function} callback to run on fail/success passes data, textStatus, jqXHR from $.ajax
     */
    loadRuntimeInfo: function(runtimeCallback) {
        var queryVars = {};
        if(window.location.search.length > 2) {
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i=0;i<vars.length;i++) {
                var pair = vars[i].split("=");
                queryVars[pair[0]] = decodeURIComponent(pair[1]);
            }
        }
        
        if(queryVars['ustad_runtime']) {
            UstadMobile.getInstance().runtimeInfo = JSON.parse(
                    queryVars['ustad_runtime']);
            var runtimeData = UstadMobile.getInstance().runtimeInfo;
            UstadMobile.getInstance().runtimeInfoLoaded = true;
            if(runtimeData['baseURL']) {
                localStorage.setItem("baseURL", runtimeData['baseURL']);
            }
            if(typeof runtimeCallback !== "undefined" && runtimeCallback !== null) {
                runtimeCallback(runtimeData);
            }
            UstadMobile.getInstance().fireRuntimeInfoLoadedEvent();
        }else {
            UstadMobile.getInstance().runtimeInfoLoaded = true;
            console.log("page does not have runtime arguments provided");
            if(typeof runtimeCallback !== "undefined" && runtimeCallback !== null) {
                runtimeCallback(null);
            }
            UstadMobile.getInstance().fireRuntimeInfoLoadedEvent();
        }
    },
    
    /**
     * Run this once the runtime info has loaded (or failed to load)
     * @param {function} callback
     */
    runAfterRuntimeInfoLoaded: function(callback) {
        if(this.runtimeInfoLoaded) {
            callback();
        }else {
            this.pendingRuntimeInfoLoadedListeners.push(callback);
        }
    },
    
    
    /**
     * Run all pending listeners
     */
    fireRuntimeInfoLoadedEvent: function() {
        for(var i = 0; i < this.pendingRuntimeInfoLoadedListeners.length; i++) {
            var fn = this.pendingRuntimeInfoLoadedListeners.pop();
            fn();
        }
    },
    
    /**
     *  Get a runtime value, or return null if this value is not set
     *  
     *  @param key variable keyname 
     *  @return the value if set, null otherwise
     */
    getRuntimeInfoVal: function(key) {
        if(typeof this.runtimeInfo[key] !== "undefined") {
            return this.runtimeInfo[key];
        }else {
            return null;
        }
    },
    
    /**
     * Get the current default server to use
     * 
     * @method getDefaultServer
     * @returns {UstadMobileServerSettings}
     */
    getDefaultServer: function() {
        var umServer = new UstadMobileServerSettings("UstadMobile",
            "http://umcloud1.ustadmobile.com/umlrs/",
            "http://umcloud1.ustadmobile.com/getcourse/?id=");
        return umServer;
    },
    
    /**
     * Checks to make sure that the required directories are made....
     * 
     * @returns {undefined}
     */
    checkPaths: function() {
        UstadMobile.getInstance().systemImpl.checkPaths();
    },
    
    /**
     * Remove any file:/ from the start of a path leaving at most one / at the
     * start of it.  E.g. to be used to change from file:/// used by a browser
     * to something to be used by filesystem libs
     * 
     * @param filePath {String} Path from which we will remove file:/// from
     */
    removeFileProtoFromURL: function(filePath) {
        var filePrefix = "file:";
        var um = UstadMobile.getInstance();
        if(filePath.substring(0, filePrefix.length) === filePrefix) {
            //check how many / slashes we need rid of
            var endPos = filePrefix.length;
            
            //in Unix file:/// should change to just / , in Windoze 
            //there should not be a leading slash
            var numSlashesAllowed = 1;
            if(um.getNodeWebKitOS() === UstadMobile.OS_WINDOWS) {
                numSlashesAllowed = 0;
            }
            
            for(; filePath.charAt(endPos+numSlashesAllowed) === '/'; endPos++) {
                //do nothing
            }
            
            var pathFixed = filePath.substring(endPos);
            return pathFixed;
        }else {
            return filePath;
        }
    },
    
    /**
     * Make sure a subdirectory exists
     * 
     * @param {String} subdirName subdirectory name to be created
     * @param {DirectoryEntry} parentDirEntry Parent persistent storage dir to create under
     * @param {function} successCallback called when successfully done
     * @param {function} failCallback when directory creation fails
     */
    checkAndMakeUstadSubDir: function(subdirName, parentDirEntry, successCallback, failCallback) {
        parentDirEntry.getDirectory(subdirName, 
            {create: true, exclusive: false},
            function(subDirEntry) {
                successCallback(subDirEntry);
            }, function(err){
                failCallback(err);
            });
    },
    
    firePathCreationEvent: function(isSuccessful, errInfo) {
        if(isSuccessful) {
            this.pathsReady = true;
        
            while(this.pendingPathEventListeners.length > 0) {
                var fn = this.pendingPathEventListeners.pop();
                fn();
            }
        }else {
            console.log("MAJOR ERROR CREATING PATHS:");
            console.log(errInfo);
        }
    },
    
    
    /**
     * Run once the http server is ready
     * 
     * @param {type} callback
     * @returns {undefined}
     */
    runAfterHTTPReady: function(callback) {
        if(this.httpReady) {
            callback();
        }else {
            this.pendingHttpListeners.push(callback);
        }
    },
    
    /**
     * Run any callbacks that are waiting for the HTTP Server to be ready
     * 
     */
    fireHTTPReady: function() {
        this.httpReady = true;
        console.log("UstadMobile.js: Informing pending listeners http is ready");
        while(this.pendingHttpListeners.length > 0) {
            var fn = this.pendingHttpListeners.pop();
            fn();
        }
    },
    
    /**
     * Run a callback when paths are ready (content and download directory).
     * 
     * @param callback function to run when paths are created 
     */
    runAfterPathsCreated: function(callback) {
        if(this.pathsReady) {
            callback();
        }else {
            this.pendingPathEventListeners.push(callback);
        }
    },
    
    /**
     * Loads the panel for the context (e.g. ustadmobile_panel_ZONENAME.html)
     * 
     * Will then setup the menu button on the top left to access the panel
     * 
     * @returns {undefined}
     */
    loadPanel: function() {
        $.ajax({
            url: "ustadmobile_panel_" + UstadMobile.getInstance().getZoneName() 
                    + ".html",
            dataType: "text"
        }).done(function(data) {
            UstadMobile.getInstance().panelHTML = data;
            $(document).on('pagebeforecreate', 
                UstadMobile.getInstance().initPanel);
            UstadMobile.getInstance().initPanel();
        }).fail(function() {
            console.log("Panel load failed");
        });
    },
    
    /**
     * Setup the menu panel for JQueryMobile - this will use a common innerHTML
     * element and then set the panel for the page.  It uses the id attribute
     * of the page to set the link for the menu to make sure we don't create
     * two elements with the same id.
     * 
     * @param evt {Event} event object
     * @param ui JQueryMobile UI
     * @method initPanel
     */
    initPanel: function (evt, ui) {
        
        
        var pgEl = null;
        
        if(evt) {
            pgEl = $(evt.target);
        }else {
            pgEl = $.mobile.activePage;
        }
        
        if(typeof pgEl === "undefined" || pgEl === null) {
            //has not yet really loaded
            return;
        }
        
        if(UstadMobile.getInstance().panelHTML === null) {
            UstadMobile.getInstance().loadPanel();
            return;
        }

        
        var thisPgId = pgEl.attr("id") || UstadMobile.getInstance().pageURLToID(
                pgEl.attr('data-url'));
        
        var newPanelId = "ustad_panel_" + thisPgId;

        if(pgEl.children(".ustadpaneldiv").length === 0) {
            var htmlToAdd = "<div id='" + newPanelId + "'>";
            htmlToAdd += UstadMobile.getInstance().panelHTML;
            htmlToAdd += "</div>";

            pgEl.prepend(htmlToAdd);
            
            $("#"+newPanelId).trigger("create");
            console.log("Appended panel to page");
        }

        $("#" + newPanelId).panel({
            theme: 'b',
            display: 'push'
        }).trigger("create");
        $("#" + newPanelId).addClass("ustadpaneldiv");

        if(pgEl.find(".ustad_panel_href").length === 0) {
            pgEl.find("[data-role='header']").prepend("<a href='#mypanel' "
                + "data-role='button' data-inline='true' class='ustad_panel_href ui-btn ui-btn-left'>"
                + "<i class='lIcon fa fa-bars'></i></a>");
        }
        
        pgEl.find(".ustad_panel_href").attr("href", "#" + newPanelId);
        
        if(pgEl.children(".ustad_fb_popup") !== 0) {
            pgEl.children(".ustad_fb_popup").attr("id", "ustad_fb_" + thisPgId);
        }
        
        var zoneObj = null;
        try {
            zoneObj = UstadMobile.getInstance().getZoneObj();
        }catch (err) {
            //do nothing
        }
        
        if(zoneObj) {
            var currentUsername = zoneObj.getCurrentUsername();
            if(currentUsername) {
                pgEl.find("#ustad_user_button").text(currentUsername);
            }
        }
    },
    
    /**
     * Update username displayed in header
     * 
     * DISABLED : DO NOTHING
     * 
     * @param pageSelector String jQuery page selector.  Optional - if ommitted user .ui-page-active
     */
    displayUsername: function(pageSelector) {
        /*
        alert("display user");
        debugger;
        if(typeof pageSelector === "undefined") {
            pageSelector = ".ui-page-active";
        }
        var currentUsername = UstadMobile.getZoneObj().getCurrentUsername();
        if($(pageSelector).find(".ustad_user_label").length === 0) {
            $(pageSelector).find("[data-role='header'").append("<div class='ustad_user_label'>"
                + currentUsername + "</div>");
        }else {
            $(pageSelector).find(".ustad_user_label").html(currentUsername);
        }*/
    },
    
    /**
     * Turn a page URL into something that can be used as an ID - stip the path, 
     * query, suffix (e.g. .html)
     * 
     * @param String url
     * @returns url without characters that cannot be used as an ID
     */
    pageURLToID: function(url) {
        var lastSlash = url.lastIndexOf("/");
        var pageId = ""+url;
        if(lastSlash !== -1) {
            pageId = pageId.substring(lastSlash+1);
        }
        
        var queryStart = pageId.indexOf("?");
        if(queryStart !== -1) {
            pageId = pageId.substring(0, queryStart);
        }
        
        pageId = UstadMobile.getInstance().stripHTMLURLSuffix(pageId);
        
        return pageId;
    },
    
    /**
     * Strip .html off the end of a name
     * 
     * @param String pageName
     * @returns pageName without trailing .html if it was there
     */
    stripHTMLURLSuffix: function(pageName){
        var htmlSuffix = ".html";
        if(pageName.indexOf(htmlSuffix) === pageName.length - htmlSuffix.length) {
            pageName = pageName.substring(0, pageName.length - htmlSuffix.length);
        }
        
        return pageName;
    },
    
    /**
     * Close the menu panel
     * 
     * @method closePaenl
     */
    closePanel: function() {
        $(".ui-page-active .ustadpaneldiv").panel("close");
    },
    
    /**
     * Check if we are in the app or content zone in this context
     * 
     * @method getZone
     * 
     * @return UstadMobile.ZONE_APP or UstadMobile.ZONE_CONTENT
     */
    getZone: function() {
        if(typeof USTADAPPZONE !== "undefined" && USTADAPPZONE === true) {
            return UstadMobile.ZONE_APP;
        }else {
            return UstadMobile.ZONE_CONTENT;
        }
    },
    
    /**
     * Gives a string for the name of the current zone - content or app
     * 
     * @returns String the name of the current zone "content" or "app"
     */
    getZoneName: function() {
        return UstadMobile.ZONENAMES[UstadMobile.getInstance().getZone()];
    },
    
    
    /**
     * Open the specified page - normally pass to the zone Object
     * 
     * @param pageName string name of page to open using UstadMobile.PAGE_ constants
     * @method 
     */
    goPage: function(pageName) {
        this.getZoneObj().goPage(pageName);
    },
    
    /**
     * Returns an instance of UstadMobileAppZone or UstadMobileContentZone 
     * 
     * @return Object for the zone we are in now
     */
    getZoneObj: function() {
        var curZone = this.getZone();
        if(curZone === UstadMobile.ZONE_APP) {
            return UstadMobileAppZone.getInstance();
        }else if(curZone === UstadMobile.ZONE_CONTENT) {
            return UstadMobileContentZone.getInstance();
        }
    },
    
    localizePage: function(pgEl) {
        if(typeof pgEl === "undefined" || pgEl === null) {
            pgEl = $(".ui-page-active");
        }
        
        console.log("[setlocalisation][ustadmobile] In localizePage()");
        pgEl.find(".exeTranslated").each(function(index, value) {
            var textToTranslate = $(this).attr("data-exe-translation");
            //var attrTextToTranslate = $(this).attr("data-exe-translation-attr");
            console.log("text to translate: " + textToTranslate);
            console.log(" translated value: " + x_(textToTranslate)); // Need to include the locale/lang.js file before this is called. 
            $(value).text(x_(textToTranslate));
        });

        pgEl.find(".exeTranslated2").each(function(index, value){
            var attrText = $(this).attr("data-exe-translation-attr");
            console.log("TEST: attrText is: " + attrText);
            var attrTextToTranslate = $(this).attr(attrText);
            var idTextToTranslate = $(this).attr("id");

            console.log("For the attribute: " + attrText + " and id: " + idTextToTranslate + " of value: " + attrTextToTranslate + ", Translation is: " + x_(attrTextToTranslate));
            $("#" + idTextToTranslate).attr(attrText, x_(attrTextToTranslate));
        });
    },
    
    /**
     * Check and see if the page given is actually already open.
     * 
     * @returns {boolean} true if page already open, false otherwise.
     */
    isPageOpen: function(fileName) {
       var currentPage = new String(document.location.href);
       var currentPageFile = currentPage.substr(currentPage.lastIndexOf("/")+1);
       if(currentPageFile === fileName) {
           return true;
       }else {
           return false;
       }
   }

    
};

var UstadMobileServerSettings;

UstadMobileServerSettings = function(serverName, xapiBaseURL, getCourseIDURL) {
    this.serverName = serverName;
    this.xapiBaseURL = xapiBaseURL;
    this.getCourseIDURL = getCourseIDURL;
};

var UstadMobileUtils;

/**
 * Holds static utility methods
 * 
 * @class UstadMobileUtils
 * @constructor
 */
UstadMobileUtils = function() {
};

/**
 * Utility function to run all functions in an array (e.g. event listeners)
 * Removes them from the list using .pop as we go
 * 
 * @method
 * @param Array arr Array holding function objects - MUST be an array
 * @param thisObj Object that will be 'this' inside function when called
 * @param Array args to send (optional)
 * 
 */
UstadMobileUtils.runAllFunctions = function(arr, args, thisObj) {
    while(arr.length > 0) {
        var fn = arr.pop();
        fn.apply(thisObj, args);
    }
};

/**
 * Utility method that can be used to run optional callback functions
 * 
 * @param function fn - function to run - can be null or undefined in which case this function does nothing
 * @param mixed args - arguments array to pass function
 * @param {Object} thisObj what to use for this in function 
 * 
 */
UstadMobileUtils.runCallback = function(fn, args, thisObj) {
    if(typeof fn !== "undefined" && fn !== null) {
        fn.apply(thisObj, args);
    }
};

/**
 * Utility method to run a function if a property is true, if not apppend to
 * waiting listeners
 * 
 * @returns {UstadMobileAppImplementation}
 */
UstadMobileUtils.runOrWait = function(runNow, fn, args, thisObj, waitingList) {
    if(runNow) {
        UstadMobileUtils.runCallback(fn, args, thisObj);
    }else {
        waitingList.push(fn);
    }
};

/**
 * Simplify callback hell situation; run each function in the array.  
 * Each function must have it's successFn and failFn as the last two arguments
 * 
 * When a function succeed all the arguments that it provided to the success callback
 * will be passed in the same order to the next function in the array,
 * 
 * When any part fails the final failFn will be called
 *  
 *    
 * @param {type} fnList
 * @param {type} successFn
 * @param {type} failFn
 * @returns {undefined}
 */
UstadMobileUtils.waterfall = function(fnList, successFn, failFn) {
    if(fnList.length < 1) {
        UstadMobileUtils.runCallback(successFn, [], this);
        return;
    }
    
    var lastResultArgs = [];
    var runItFn = function(index) {       
        //success function
        lastResultArgs.push(function() {
            lastResultArgs = Array.prototype.slice.call(arguments);
            if(index < (fnList.length - 1)) {
                runItFn(index+1);
            }else {
                UstadMobileUtils.runCallback(successFn, lastResultArgs, this);
            }
        });
        lastResultArgs.push(failFn);
        fnList[index].apply(this, lastResultArgs);
    };
    
    runItFn(0);
};

UstadMobileUtils.asyncMapAdvanced = function(fn, argArr, options, successFn, failFn) {
    var resultMap = [];
    
    var numFns = (fn.constructor === Array) ? fn.length : argArr.length;
    
    if(fn.constructor === Array && fn.length === 0) {
        UstadMobileUtils.runCallback(successFn, [], this);
        return;
    }
    
    var runItFn = function(index) {
        var thisArgArr = argArr[index] ? UstadMobileUtils.ensureIsArray(
                argArr[index]) : [];
        if(options.beforerun) {
            options.beforerun(index, argArr, resultMap);
        }
        
        thisArgArr.push(function() {
            var successArgArr = Array.prototype.slice.call(arguments);
            resultMap.push(successArgArr);
            if(index < (numFns-1)) {
                runItFn(index+1);
            }else {
                UstadMobileUtils.runCallback(successFn, [resultMap], this);
            }
        });
        thisArgArr.push(failFn);
        var fn2Apply = fn.constructor === Array ? fn[index] : fn;
        
        var fnContext = options.context ? options.context : this;
        fn2Apply.apply(fnContext, thisArgArr);
    };
    
    runItFn(0);
};

/**
 * For each item in arg array call the given function.  
 * It will be assumed that the last arguments will be the successFn and failFn
 * 
 */
UstadMobileUtils.asyncMap = function(fn, argArr, successFn, failFn) {
    var resultMap = [];
    
    var numFns = (fn.constructor === Array) ? fn.length : argArr.length;
    
    if(fn.constructor === Array && fn.length === 0) {
        UstadMobileUtils.runCallback(successFn, [], this);
        return;
    }
    
    var runItFn = function(index) {
        var thisArgArr = argArr[index] ? UstadMobileUtils.ensureIsArray(
                argArr[index]) : [];
        thisArgArr.push(function() {
            var successArgArr = Array.prototype.slice.call(arguments);
            resultMap.push(successArgArr);
            if(index < (numFns-1)) {
                runItFn(index+1);
            }else {
                UstadMobileUtils.runCallback(successFn, [resultMap], this);
            }
        });
        thisArgArr.push(failFn);
        var fn2Apply = fn.constructor === Array ? fn[index] : fn;
        fn2Apply.apply(this, thisArgArr);
    };
    
    runItFn(0);
};

/**
 * Make sure that the given arg is an array so it can be used in function.apply
 * etc.
 * 
 * If it's already an array - return as is, otherwise return a new single item
 * array
 * 
 * @param {Object|Array} arg
 * @returns {Array} original array if that was provided, or array with one entry otherwise
 */
UstadMobileUtils.ensureIsArray = function(arg) {
    if(arg.constructor === Array) {
        return arg;
    }else {
        return [arg];
    }
};

/**
 * Used to flatten the result of asyncMap - e.g. when asyncResult returns
 * it will provide an array for each entry.  If the callback provided one value
 * then it will be an array of arrays each with one entry.
 * 
 * flattenArray will turn this into a single array.
 * 
 * @param {Array} arr - Array which contains arrays with one entry each
 * @returns {Array}
 */
UstadMobileUtils.flattenArray = function(arr) {
    var retVal = [];
    for(var i = 0; i < arr.length; i++) {
        retVal.push(arr[i].length >= 1 ? arr[i][0] : null);
    }
    
    return retVal;
};

/**
 * If parameter seperator is specified; use it; otherwise use /
 * 
 * @param {String} seperator (optional)
 * @returns {undefined}
 */
UstadMobileUtils.getSeperator = function(seperator) {
    if(typeof seperator === "undefined" || seperator === null) {
        return "/";
    }else {
        return seperator;
    }
};

/**
 * Gets the extension of a file - e.g. ".epub" for somefile.epub
 * 
 * @param {String} name
 * @returns {String} the extension of the file
 */
UstadMobileUtils.getExtension = function(name) {
    var lastDotPos = name.lastIndexOf(".");
    if(lastDotPos === -1) {
        return null;
    }else {
        return name.substring(lastDotPos);
    }
};

/**
 * Split up the path into components according to the seperator
 * 
 * @param {type} path
 * @param {type} seperator
 * @returns {undefined}
 */
UstadMobileUtils.splitPath = function(path, seperator) {
    seperator = UstadMobileUtils.getSeperator(seperator);
    return path.split(seperator);
};

/**
 * Chop off the last part of the filename 
 * 
 */
UstadMobileUtils.getFilename = function(path, seperator) {
    var pathParts = UstadMobileUtils.splitPath(path, seperator);
    return pathParts[pathParts.length-1];
};

/**
 * Use to remove trailing slash when needed - will remove as many trailing
 * slashes as occur at the end of a path
 * 
 * e.g. /file/dir/ -> /file/dir and /file/somedir/// to /file/somedir
 * 
 * @param {string} path
 * @param {string} [seperator=/] the seperator - must be of length 1
 * 
 * @returns the path with any trailing seperators removed
 */
UstadMobileUtils.removeTrailingSeperators = function(path, seperator) {
    var sepChar = UstadMobileUtils.getSeperator(seperator);
    var retVal = path;
    while(retVal.charAt(retVal.length-1) === sepChar) {
        retVal = retVal.substring(0, retVal.length-1);
    }
    
    return retVal;
};

/**
 * Get everything except the last part of the path
 */
UstadMobileUtils.getPath = function(completePath, seperator) {
    seperator = UstadMobileUtils.getSeperator(seperator);
    
    //in case the last character is a trailing slash
    if(completePath.lastIndexOf(seperator) === completePath.length-1) {
        completePath = completePath.substring(0, completePath.length-1);
    }
    
    return completePath.substring(0, completePath.lastIndexOf("/"));
};

/**
 * Chop off the last part of the URL
 * 
 * e.g. file://localhost/some/dir/file - file://localhost/some/dir
 * 
 * @param {type} url
 * @param {type} seperator
 * @returns {undefined}
 */
UstadMobileUtils.getURLParent = function(url, seperator) {
    seperator = UstadMobileUtils.getSeperator(seperator);
    return url.substring(0, url.lastIndexOf(seperator));
}

/**
 * Return the default value if the valProvided is undefined, otherwise
 * the value itself
 * 
 * @return valProvided if defined, defaultVal otherwise
 */
UstadMobileUtils.defaultVal = function(valProvided, defaultVal) {
    if(typeof valProvided === "undefined") {
        return defaultVal;
    }else {
        return valProvided;
    }
};

/**
 * Joins an array of Strings together with one and only one seperator between
 * them
 * 
 * @param {Array} pathArr Array of strings, each a path
 * @param string seperator (optional by default '/' )
 * @returns string Path components joined into one string
 */
UstadMobileUtils.joinPath = function(pathArr, seperator) {
    seperator = UstadMobileUtils.getSeperator(seperator);
    
    if(pathArr.length === 1) {
        return pathArr[0];
    }
    
    var retVal = pathArr[0];
    for(var i = 1; i < pathArr.length; i++) {
        if(retVal.charAt(retVal.length-1) === seperator) {
            retVal = retVal.substring(0, retVal.length-1);
        }
        
        var nextSection = pathArr[i];
        if(nextSection.charAt(0) !== seperator) {
            nextSection = '/' + nextSection;
        }
        
        retVal += nextSection;
    }
    
    return retVal;
};

/**
* Turns search query variables into a dictionary - adapted from
* http://css-tricks.com/snippets/javascript/get-url-variables/
* 
* @param {string} queryStr Input query string
* @method getQueryVariable
*/
UstadMobileUtils.getQueryVariables = function(queryStr) {
    var locationQuery = window.location.search.substring.length >= 1 ?
        window.location.search.substring(1) : "";
    var query = (typeof queryStr !== "undefined") ? queryStr : 
        locationQuery;
    
    var retVal = {};
    if(window.location.search.length > 2) {
        var vars = query.split("&");
        for (var i=0;i<vars.length;i++) {
            var pair = vars[i].split("=");
            retVal[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
        }
    }
    return retVal;
};

UstadMobileUtils.debugLog = function(msg) {
    console.log(msg);
};

/**
 * Format an ISO8601 duration for the given number of milliseconds difference
 * 
 * @param Number duration the duration to format in milliseconds
 * @returns String An ISO8601 Duration e.g. PT4H12M05S
 */
UstadMobileUtils.formatISO8601Duration = function(duration) {
    var msPerHour = (1000*60*60);
    var hours = Math.floor(duration/msPerHour);
    var durationRemaining = duration % msPerHour;

    var msPerMin = (60*1000);
    var mins = Math.floor(durationRemaining/msPerMin);
    durationRemaining = durationRemaining % msPerMin;

    var msPerS = 1000;
    var secs = Math.floor(durationRemaining / msPerS);

    retVal = "PT" + hours +"H" + mins + "M" + secs + "S";
    return retVal;
};

/**
 * 
 * @param Node mediaEl - DOM node representing an audio or video tag
 * @param function onPlayCallback function to call once the item has played
 * 
 * @returns {Boolean}
 */
UstadMobileUtils.playMediaElement = function(mediaEl, onPlayCallback) {
    var played = false;
    console.log("UMMedia: UstadMobileUtils playing audio element " + mediaEl.src);
    
    if(mediaEl.paused === true && mediaEl.currentTime === 0 && mediaEl.readyState >= 2) {
        try {
            mediaEl.play();
            UstadMobileUtils.runCallback(onPlayCallback, [true], mediaEl);
        }catch(err) {
            UstadMobileUtils.runCallback(onPlayCallback, [false], mediaEl);
        }
    }else if(mediaEl.seekable.length > 0 && mediaEl.readyState >= 2){
        try {
            mediaEl.pause();
            var seekedItFn = function() { 
                mediaEl.play(); 
                UstadMobileUtils.runCallback(onPlayCallback, [true], mediaEl);
                onPlayCallback = null;
                
                mediaEl.removeEventListener("seeked", seekedItFn, true);
                mediaEl = null;
            };
            
            mediaEl.addEventListener("seeked", seekedItFn, true); 
            
            mediaEl.currentTime = 0; 
            mediaEl.play();
        }catch(err2) {
            UstadMobileUtils.runCallback(onPlayCallback, [false], mediaEl);
        }
    }else {
        var playItFunction = function(evt) {
            var myMediaEl = evt.target;
            try {
                myMediaEl.play();
            }catch(err3) {
                console.log("Exception attempting to play " + myMediaEl.src
                        + ":" + err3);
            }
            
            myMediaEl.removeEventListener("canplay", playItFunction, true);
            myMediaEl = null;
            UstadMobileUtils.runCallback(onPlayCallback, [true], mediaEl);
            onPlayCallback = null;
        };
        mediaEl.addEventListener("canplay", playItFunction);
        mediaEl.load();
    }
    
    return played;
}


/**
 * 
 * @callback UstadMobileFailCallback
 * @param {Object} error object
 * @param {string} errStr text of error if any
 * 
 */

/**
 * Abstract class that defines what an implementation of the app needs to be 
 * able to do - e.g. get the default language of the system, file system scans, 
 * etc.  There will be an implementation for Cordova and NodeWebKit
 * 
 * @constructor
 * @class UstadMobileAppImplementation
 */
var UstadMobileAppImplementation = function() {
            
    
};

UstadMobileAppImplementation.prototype = {
    
    /**
     * Boolean if the implementation is ready (e.g. cordova.ondeviceready etc)
     * @type Boolean
     */
    implementationReady: false,
    
    /**
     * The HTTP Port that the internal server is running on
     * @type string 
     */
    _httpPort: -1,
    
    /**
     * Host or IP for local access (e.g. localhost)
     * @type string
     */
    _httpInternalHost : null,
    
    /**
     * Host o
     * @type type
     */
    _httpExternalHost : null,
    
    /**
     * Get the port that we are working on - or -1 for no port
     * 
     * @returns {Number} Port number to connect to
     */
    getHttpPort: function() {
       return this._httpPort;
    },
    
    /**
     * Get the internal hostname to use (e.g. localhost) for access by the app
     * 
     * @return string Hostname or IP address for internal usage
     */
    getHttpInternalHost: function() {
        return this._httpInternalHost;
    },
    
    /**
     * 
     * @param function callbackFunction Called when the system returns the 
     * language or a failure occurs with arg 
     * 
     * @method
     */
    getSystemLang: function(callbackFunction) {
        
    },
    
    /**
     * Return a JSON string with system information - e.g. for reporting with
     * bug reports etc.
     * 
     * @param function callback which will receive one JSON arg - the result
     * @returns {Object} with system information
     */
    getSystemInfo: function(callback) {
        
    },
    
    /**
     * Callback when writing string to disk was successful
     * @callback writeStringToFileSuccess
     * @param result {Object} misc result properties
     */
    
    /**
     * 
     * @callback UstadMobileAppImplementation~writeStringToFileFail
     * @param errStr {string} error as a string
     * @param err {Object} error as an object
     */
    
    /**
     * Write a string to a file
     * 
     * @abstract
     * @param dest {FileEntry|string} destination to save text in file to
     * @param str {string} String contents to be written to file
     * @param options {Object} options
     * @param successFn {writeStringToFileSuccess} success callback
     * @param failFn {UstadMobileAppImplementation~writeStringToFileFail} failure callback
     * 
     */
    writeStringToFile: function(dest, str, options, successFn, failFn) {
        
    },
    
    
    readStringFromFile: function(src, options, successFn, failFn) {
        
    },
    
    /**
     * 
     * @callback fileExistsSuccessCB
     * @param {boolean} exists true/false if file exists
     */
    
    /**
     * Check to see if the given file exists as either a file or directory
     * 
     * @abstract
     * @param {FileEntry|string} file the file entry to look for
     * @param {fileExistsSuccessCB} successFn
     * @param {type} failFn
     */
    fileExists: function(file, successFn, failFn) {
        
    },
    
    /**
     * Remove the given file
     * 
     * @abstract
     * @param {FileEntry|string} file file to be removed
     * @param {function} successFn callback to run when successful
     * @param {UstadMobileFailCallback} failFn callback when failed
     */
    removeFile: function(file, successFn, failFn) {
        
    },
    
    /**
     * Delete a file if it exists; if it does not exist, do nothing
     * 
     * @param {FileEntry|string} file FileEntry object or URI string to file
     * @param {function} successFn called when the file is removed OK or does not exist
     * @param {function} failFn called when something goes wrong
     */
    removeFileIfExists: function(file, successFn, failFn) {
        UstadMobile.getInstance().systemImpl.fileExists(file, function(fileFound) {
            if(fileFound) {
                UstadMobile.getInstance().systemImpl.removeFile(file, successFn, 
                    failFn);
            }else {
                UstadMobileUtils.runCallback(successFn, [], this);
            }
        }, failFn);
    },
    
    /**
     * Concatenate multiple files in order into one file
     * 
     * @param {Array} array of type FileEntry or URI strings
     * @param 
     */
    concatenateFiles: function(files, options, successFn, failFn) {
        
    },
    
    /**
     * @callback UstadMobileAppImplementation~downloadSuccessCB
     * @param 
     */
    
    /**
     * Downloads a file or part of a file to a given fileURI.  Makes only one 
     * attempt at download.
     * 
     * @abstract
     * @param {string} url Absolute url to be downloaded
     * @param {string} fileURI Local File URI where this file is to be downloaded
     * @param {Object} options misc options
     * @param {number} [options.frombyte=0] Range to start downloading from 
     * requires range support on the server
     * @param {number} [options.tobyte] Range to download until - requires range
     * support from the server
     * @param {boolean} [options.keepIncompleteFile] - if a download fails, leave it
     * @param 
     * @returns {undefined}
     */
    downloadUrlToFileURI: function(url, fileURI, options, successFn, failFn) {
        
    },
    
    renameFile : function(srcFile, newName, options, successFn, failFn) {
        
    },
    
    fileSize: function(file, successFn, failFn) {
        
    },
    
    mkBlob: function(arrParts, contentType) {
        var blobResult = null;
        try {
            blobResult = new Blob(arrParts, contentType);
        }catch(e) {
            var ourBlobBuilder = window.BlobBuilder || 
                         window.WebKitBlobBuilder || 
                         window.MozBlobBuilder || 
                         window.MSBlobBuilder;
            if(ourBlobBuilder) {
                var bb = new ourBlobBuilder();
                for(var i = 0; i < arrParts.length; i++) {
                    bb.append(arrParts[i]);
                }
                blobResult = bb.getBlob(contentType.type);
            }
        }
        
        return blobResult;
    },
    
    /**
     * Make a new directory
     * 
     * @abstract
     * @param {string} dirURI file URI for the directory to be made
     * @param {Object} options
     * @param {function} successFn callback when completed successfully
     * @param {function} failFn callback when failed
     */
    makeDirectory: function(dirURI, options, successFn, failFn) {
        
    },
    
    /**
     * Remove the directory and it's contents recursively
     * 
     * @param {string} dirURI Directory to remove
     * @param {Object} options
     * @param {function} successFn success callback with no arguments provided
     * @param {function} failFn error callback given the error that occurred
     */
    removeRecursively: function(dirURI, options, successFn, failFn) {
        
    },
    
    /**
     * Unzips a given zip file to a given directory
     * 
     * @param {string|FileEntry} zipSrc source zip file
     * @param {string|DirectoryEntry} destDir directory to unzip into
     * @param {Object} options
     * @param {function} [options.onprogress] progress event handler
     * @param {function} successFn success callback that takes destDirEntry arg
     * @param {function} failFn failure callback
     */
    unzipFile: function(zipSrc, destDir, options, successFn, failFn) {
        
    }
    
};

var UstadMobileResumableDownload = function() {
    this.srcURL = null;
    
    this._onprogress = null;
    
    //the number of bytes we have got so far
    this.bytesDownloadedOK = 0;
    
    //the size of this file
    this.fileSize = "";
    
    //file etag if provided
    this.etag = "";
    
    this.srcURL = "";
    
    //the destination file where this eventually be written
    this.destURI = "";
    
    this.tryCount = 0;
    
    this.maxRetries = 20;
    
    this.onprogress = null;
    
    /** 
     * If we found a previous attempt, where that started from
     */
    this.startedFrom = 0;
};

UstadMobileResumableDownload.prototype.info2JSON = function() {
    return {
        "fileSize" : this.fileSize,
        "srcURL" : this.srcURL,
        "destURI" : this.destURI
    };
};

UstadMobileResumableDownload.prototype.getInfo = function(successFn, failFn) {
    var thatDownload = this;
    UstadMobileUtils.waterfall([
        function(successFnW, failFnW) {
            $.ajax(thatDownload.srcURL, {
                type: "HEAD"
            }).done(successFnW).fail(failFnW);
        },
        function(data, textStatus, jqXHR, successFnW, failFnW) {
            if(jqXHR.getResponseHeader('Content-Length')) {
                thatDownload.fileSize = parseInt(jqXHR.getResponseHeader(
                    'Content-Length'));
            }
            UstadMobile.getInstance().systemImpl.writeStringToFile(
                thatDownload.destURI + ".dlinfo", 
                JSON.stringify(thatDownload.info2JSON()), {},
                successFnW, failFnW);
        }], 
        function() {
            UstadMobileUtils.runCallback(successFn, [thatDownload], this);
        }, failFn);
};

/**
 * Check and see if there was a previous attempt - look for a .dlinfo file.
 * 
 * If found look for a .inprogress file, append this to to the .part file
 * 
 * @param {type} successFn
 * @param {type} failFn
 * @returns {undefined}
 */
UstadMobileResumableDownload.prototype.checkPreviousAttempt = function(successFn, failFn) {
    var thatDownload = this;
    var dlInfoURI = this.destURI + ".dlinfo";
    var inprogURI = this.destURI + ".inprogress";
    var partFileURI = this.destURI + ".part";
    var dlInfoJSON = null;
    
        
    UstadMobile.getInstance().systemImpl.fileExists(dlInfoURI, function(dlInfoExists) {
        if(!dlInfoExists) {
            UstadMobileUtils.runCallback(successFn, [], this);
        }else {
            UstadMobileUtils.waterfall([
                function(successFnW2, failFnW2) {
                    UstadMobile.getInstance().systemImpl.readStringFromFile(
                        dlInfoURI, {}, successFnW2, failFnW2);
                },function(jsonInfoStr, successFnW2, failFnW2) {
                    dlInfoJSON = JSON.parse(jsonInfoStr);
                    UstadMobile.getInstance().systemImpl.fileExists(inprogURI, 
                        successFnW2, failFnW2);
                },
                function(inprogexists, successFnW2, failFnW2) {
                    if(inprogexists) {
                        UstadMobile.getInstance().systemImpl.concatenateFiles(
                            [inprogURI], partFileURI, {"append" : true},
                            successFnW2, failFnW2);
                    }else {
                        UstadMobileUtils.runCallback(successFnW2, 
                            [partFileURI], this);
                    }
                },function(partFile, successFnW2, failFnW2) {
                    UstadMobile.getInstance().systemImpl.removeFileIfExists(
                        inprogURI, successFnW2, failFnW2);
                },function(successFnW2, failFnW2) {
                    UstadMobile.getInstance().systemImpl.fileExists(partFileURI,
                        successFnW2, failFnW2);
                },function(partFileExists, successFnW2, failFnW2) {
                    if(partFileExists) {
                        UstadMobile.getInstance().systemImpl.fileSize(partFileURI,
                            successFnW2, failFnW2);
                    }else {
                        UstadMobileUtils.runCallback(successFnW2, [0], this);
                    }
                },function(bytesDownloaded, successFnW2, failFnW2) {
                    thatDownload.bytesDownloadedOK = bytesDownloaded;
                    thatDownload.startedFrom = bytesDownloaded;
                    UstadMobileUtils.runCallback(successFnW2, [], this);
                }
            ], successFn, failFn);
        }
    }, failFn);
};

/**
 * 
 * @param {type} url
 * @param {type} destFileURI
 * @param {Object} options 
 * @param {number} [options.maxRetries=20]
 * @param {progresscallback} [options.onprogress] onprogress handler
 * @param {fileEntryCallback} successFn success function with the resulting fileentry
 * @param {errorCallback} failFn failfunction called with error info
 */
UstadMobileResumableDownload.prototype.download = function(url, destFileURI, options, successFn, failFn) {
    this.destURI = destFileURI;
    this.srcURL = url;
    
    this.retryCount = 0;
    this.maxRetries = UstadMobileUtils.defaultVal(options.maxRetries, 20);
    if(options.onprogress) {
        this.onprogress = options.onprogress;
    }
    
    UstadMobileUtils.waterfall([
        this.checkPreviousAttempt.bind(this),
        this.getInfo.bind(this),
        (function(dlObj, successFnW, failFnW) {
            this.continueDownload(successFnW, failFnW);
        }).bind(this)
    ], successFn, failFn);
};

UstadMobileResumableDownload.prototype._handleProgressUpdate = function(evt) {
    if(evt.lengthComputable) {
        //how many bytes we want from this request in total
        var bytesRemaining = this.fileSize - this.bytesDownloadedOK;
        var bytesInRequest = (evt.loaded / evt.total) * bytesRemaining;
        var bytesComplete = this.bytesDownloadedOK + bytesInRequest;
        var ourProgEvt = {
            lengthComputable : true,
            total: this.fileSize,
            loaded : Math.round(bytesComplete),
            target: this
        };
        
        if(this.onprogress) {
            this.onprogress.apply(this, [ourProgEvt]);
        }
    }
};

UstadMobileResumableDownload.prototype.continueDownload = function(successFn, failFn) {
    var inProgressFileURI = this.destURI + ".inprogress";
    var partFileURI = this.destURI + ".part";
    var destFileURI = this.destURI;
    var destFileName = UstadMobileUtils.getFilename(this.destURI);
    var srcURL = this.srcURL;
    
    var downloadOptions = {
        frombyte : this.bytesDownloadedOK,
        keepIncompleteFile : true,
        onprogress: this._handleProgressUpdate.bind(this)
    };
    var downloadedResultFile = null;
    var thatDownload = this;
    
    UstadMobileUtils.waterfall([
        function(successFnW, failFnW) {
            thatDownload.tryCount++;
            UstadMobile.getInstance().systemImpl.downloadUrlToFileURI(srcURL,
                inProgressFileURI, downloadOptions, successFnW, failFnW);
        }, 
        function(downloadedResultFileVal, successFnW, failFnW) {
            downloadedResultFile = downloadedResultFileVal;
            UstadMobile.getInstance().systemImpl.fileExists(
                partFileURI, successFnW, failFnW);
        }, 
        function(downloadedPartExists, successFnW, failFnW) {
            if(downloadedPartExists) {
                //concatenate and finish - here we need to return a file entry,
                var thatDestFile = null;
                UstadMobileUtils.waterfall([
                    function(successFnW2, failFnW2) {
                        UstadMobile.getInstance().systemImpl.concatenateFiles(
                            [partFileURI, inProgressFileURI], destFileURI, {},
                            successFnW2, failFnW2);
                    },function(destFileEntry, successFnW2, failFnW2) {
                        thatDestFile = destFileEntry;
                        thatDownload.removePartialFiles(successFnW2, failFnW2);
                    },function(successFnW2, failFnW2) {
                        UstadMobileUtils.runCallback(successFnW2, [thatDestFile],
                            this);
                    }
                ], successFnW, failFnW);
                
            }else {
                //move and finish
                UstadMobile.getInstance().systemImpl.renameFile(
                    downloadedResultFile, destFileName, {},
                    successFnW, failFnW);
            }
        },function(destFile, successFnW, failFnW) {
            thatDownload.removeDLInfoFile(function() {
                UstadMobileUtils.runCallback(successFnW, [destFile], this);
            }, failFnW);
        }
    ], successFn, (function(err) {
        //TODO: logic to see if we should retry...
        if(this.tryCount < this.maxRetries) {
            //concatenate the results of the last attempt into the .part file
            UstadMobileUtils.waterfall([
                function(successFnW, failFnW) {
                    UstadMobile.getInstance().systemImpl.concatenateFiles(
                        [inProgressFileURI], partFileURI, {append : true},
                        successFnW, failFnW);
                },
                function(writeFinishEvt, successFnW, failFnW) {
                    UstadMobile.getInstance().systemImpl.fileSize(
                        partFileURI, successFnW, failFnW);
                },
                (function(downloadedSize, successFnW, failFnW) {
                    this.bytesDownloadedOK = downloadedSize;
                    UstadMobile.getInstance().systemImpl.removeFile(
                        this.destURI + ".inprogress", successFnW, failFnW);
                }).bind(this),
                this.continueDownload.bind(this)
                ], successFn, failFn);
                
        }else {
            //we have exceeded the maximum number of retries 
           UstadMobileUtils.runCallback(failFn, [err], this);
        }
    }).bind(this));
};

UstadMobileResumableDownload.prototype.removeDLInfoFile = function(successFn, failFn) {
    var dlInfoFileURI = this.destURI + ".dlinfo";
    UstadMobile.getInstance().systemImpl.removeFileIfExists(dlInfoFileURI, function() {
        UstadMobileUtils.runCallback(successFn, [], this);
    }, failFn);
};

UstadMobileResumableDownload.prototype.removePartialFiles = function(successFn, failFn) {
    var destURI = this.destURI;
    UstadMobileUtils.asyncMap(
        UstadMobile.getInstance().systemImpl.removeFileIfExists,
            [destURI + ".inprogress", destURI + ".part"], 
            function(mapResult) { 
                UstadMobileUtils.runCallback(successFn, [], this);
            }, failFn);
};

var UstadMobileResumableDownloadList = function() {
    this.resumableDownloads = [];
    
    this.totalSize = 0;
    
    this.retryCount = 0;
    
    this.fileBytesCompleted = 0;
    
    this.currentDownloadIndex = 0;
    
    this.onprogress = null;
};

UstadMobileResumableDownloadList.prototype.downloadList = function(urlList, destFileURIList, options, successFn, failFn) {
    
    var prepArguments = [];
    for(var i = 0; i < urlList.length; i++) {
        prepArguments[i] = [i, urlList[i], destFileURIList[i]];
    }
    
    if(options.onprogress) {
        this.onprogress = options.onprogress;
    }
    
    UstadMobileUtils.waterfall([
        (function(successFnW, failFnW) {
            UstadMobileUtils.asyncMap((function(index, url, destURI, successFnM, failFnM) {
                var resumableDownload = new UstadMobileResumableDownload();
                resumableDownload.srcURL = url;
                resumableDownload.destURI = destURI;
                this.resumableDownloads.push(resumableDownload);
                resumableDownload.getInfo(successFnM, failFnM);
            }).bind(this), prepArguments, successFnW, failFnW);
        }).bind(this),
        (function(infoResults, successFnW, failFnW) {
            var sizeCounter = 0;
            var fnList = [];//functions to be called -always the download function
            var argList = [];//arg list - array of srcURL, destURI
            for(var i = 0; i < this.resumableDownloads.length; i++) {
                sizeCounter += this.resumableDownloads[i].fileSize;
                fnList.push(this.resumableDownloads[i].download.bind(
                    this.resumableDownloads[i]));
                argList.push([this.resumableDownloads[i].srcURL,
                    this.resumableDownloads[i].destURI,
                    {
                        onprogress : this._handleProgress.bind(this)
                    }]);
            }
            this.totalSize = sizeCounter;
            
            var advMapArgs = {
                beforerun: (function(index, args, result) {
                    if(index > 0) {
                        this.fileBytesCompleted  += 
                            this.resumableDownloads[index-1].fileSize;
                    }
                    this.currentDownloadIndex = index;
                }).bind(this)
            };
            
            UstadMobileUtils.asyncMapAdvanced(fnList, argList, advMapArgs, 
                successFnW, failFnW);
        }).bind(this)
        
    ], successFn, failFn);
};

UstadMobileResumableDownloadList.prototype._handleProgress = function(evt) {
    var thisDlBytesDone = 0;
    if(evt.lengthComputable) {
        thisDlBytesDone = evt.loaded;
    }
    var loadedSize = this.fileBytesCompleted + thisDlBytesDone;
    
    var progressEvt = {
        lengthComputable: true,
        loaded: loadedSize,
        total: this.totalSize
    };
    
    if(this.onprogress) {
        this.onprogress(progressEvt);
    }
};

// Put this in a central location in case we don't manage to load it
var messages = [];
//default lang

$(function() {
    UstadMobile.getInstance().preInit();    
});

$(document).on("mobileinit", function() {
    console.log("App doing main init");
    UstadMobile.getInstance().init(); 
});

//Set to 1 for Debug mode, otherwise 0 (will silence console.log messages)
var USTADDEBUGMODE = 1;
//var USTAD_VERSION = "0.0.86";

/*
Output msg to console.log if in debug mode
*/
function debugLog(msg) {
    if(USTADDEBUGMODE === 1) {
        console.log(msg);
    }
}

var currentUrl = document.URL; 
debugLog("Ustad Mobile: Current Location: " + currentUrl); //For testing purposes.
 
//useful to get TOC link from Menu Page triggered in Content.
var platform="";
var userAgent=navigator.userAgent; //User agent
console.log("User agent is: " + userAgent);



//Cordova device ready event handler
//document.addEventListener("deviceready", onAppDeviceReady, false);

//Global variable set in scroll login. Can be disabled from the Content (!1) to disable scroll.
var scrollEnabled = 1;

/*
 There is an issue with the cloze because eXe sets the width
 according to the number of letters; jQuery mobile wants to 
 make this the width of the screen.  That results in what
 looks like a text box but only a small part being selectable.
*/
function setupClozeWidth() {
    $(".ClozeIdevice input[type=text]").css("width", "");
}


/*
 Localization function - will return original English if not in JSON
*/
function x_(str) {
    if(typeof messages !== "undefined") {
        if(messages[str]) {
            return messages[str];
        }else {
            return str;
        }
    }
    
    return str;
}

/*
 Dummy function to allow strings to be picked up by babel script
*/
function x__(str) {
    return str;
}

/*
Gets called on page load (e.g. before is shown)

pageSelector - class or id selector e.g. .ui-page-active
*/
function localizePage(containerEl) { 
    UstadMobile.getInstance().localizePage(containerEl);
}



//This function is called from the Book List Meny to go to the download pakcages Page from the Menu.
//We have decided to not allow user to access the Download Packages page whilist in a book (for reduction in complexity).
function openGetPackagesPage(){
    openMenuLink("ustadmobile_getPackages.html", "slide");
}


/** 
 * Dummy function ONLY for compatibility purposes to avoid something throwing
 * an exception - this is now handled in ustadmobile-contentzone checkTOC.
 * 
 * This is required because old HTML content being opened will ask for this.
 */
function initTableOfContents() {
    
}

//Dummy onload function
//leave me
function _onLoadFunction(){
}

/**
 * Common debuglog function to log only if debug mode is enabled
 * 
 * @param msg string message to log to console if in debug mode
 */
function debugLog(msg) {
    if(USTADDEBUGMODE === 1) {
        console.log(msg);
    }
}

function _(msgid) {
    if (msgid in messages) {
        return messages[msgid];
    }else {
        return msgid;
    }
}

