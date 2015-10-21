/*
 <!-- This file is part of Ustad Mobile.  
 
 Ustad Mobile Copyright (C) 2011-2014 UstadMobile Ltd
 
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
 
 All names, links, and logos of Ustad Mobile and Toughra Technologies FZ LLC must be kept as they are in the original distribution.  If any new screens are added you must include the Ustad Mobile logo as it has been used in the original distribution.  You may not create any new functionality whose purpose is to diminish or remove the Ustad Mobile Logo.  You must leave the Ustad Mobile logo as the logo for the application to be used with any launc   her (e.g. the mobile app launcher).  
 
 If you need a commercial license to remove these restrictions please contact us by emailing info@ustadmobile.com 
 
 -->
 
 */

/*
 Ustad Mobile Book List will scan a list of root directories for sub directories.
 Each sub directory will be queried for a marker file.  If that file exists it will
 be considered an EXE content directory and it will be displayed in a JQuery Mobile 
 UI list that the user can open a chosen content entry.


 Callback hell scheme is as follows:

  1. Call onBLDeviceReady on device ready: 
    foldersToScan is set to an array depending on the platform, does first call
    of populateNextDir
  2. populateNextDir will go through foldersToScan incrementing currentFolderIndex,
        call populate
  3. populate will call window.requestFileSystem for the path; if successful 
     calls dirReader, otherwise failbl (which will call populateNextDir)
     
     -> dirReader: calls either successDirectoryReader or failDirectoryReader - request entries
            -> successDirectoryReader: will put the paths in the directory into
                an array called *currentEntriesToScan*, then call 
                scanNextDirectoryIndex:

                -> findEXEFileMarkerSuccess : Put in coursesFound array
                -> findEXEFileMarkerFail : call scanNextDirectoryIndex to look at next dir

            -> failDirectoryReader

     -> failbl: simply logs and calls populateNextDir
    
 */

/*
 The file to look for in a sub directory to determine if it is EXE
 content or not
 */

/**
 * 
 * @module UstadMobileBookList
 */
var UstadMobileBookList;

/**
 * Stored instance of booklist object
 */
var ustadMobileBookListInstance = null;

/**
 * @class UstadMobileBookList
 * @constructor
 */
function UstadMobileBookList() {
    this.exeContentFileName = "META-INF/container.xml";
    
    //The file that should be present in a directory to indicate this is exe content
    //var exeFileMarker = "index.html";
    this.exeFileMarker = "META-INF/container.xml";

    /** Courses found */
    this.coursesFound = [];

}

UstadMobileBookList.getInstance = function() {
    if(ustadMobileBookListInstance === null) {
        ustadMobileBookListInstance = new UstadMobileBookList();
    }
    
    return ustadMobileBookListInstance;
};

/** Classname used to find iframes we made for content */
UstadMobileBookList.IRAME_CLASSNAME = "umcontentiframe";

UstadMobileBookList.prototype = {
    
    /**
     *  The URL of the next page to open, used by nodewebkit code
     *  
     * @type {String}
     */
    nextPageToOpen: "",
    
    /**
     * Files that should be copied from the app into the content
     * Map in the form of src : dest
     * 
     * @type {Object}
     */
    appFilesToCopyToContent: {"js/ustadmobile.js" : "ustadmobile.js",
        "js/ustadmobile-booklist.js" :  "ustadmobile-booklist.js",
        "jqm/jqm-app-theme.css" : "jqm-app-theme.css",
        "jqm/jqm-content-theme.css" : "jqm-content-theme.css",
        "jqm/jquery.mobile.icons.min.css" : "jquery.mobile.icons.min.css",
        "js/ustadmobile-contentzone.js" : "ustadmobile-contentzone.js",
        "js/ustadmobile-localization.js" : "ustadmobile-localization.js",
        "js/ustadmobile_menupage_content.html" : "ustadmobile_menupage_content.html",
        "js/ustadmobile_panel_content.html" : "ustadmobile_panel_content.html",
        "js/feedback_dialog.html": "feedback_dialog.html",
        "jqm/jqm-base.css" : "jqm-base.css",
        "css/ustadmobile.css" : "ustadmobile.css"
    },
    
    /** 
      * Will run a scan when device is ready to do so... This relies on 
      * UstadMobile runAfterPathsCreated, which if running cordova can
      * run only after the deviceready event occurs.
      *
      *@param queueCallback function callback to run after scan is done
      *
      *@method onBookListLoad
      */
    queueScan: function(queueCallback) {
        UstadMobile.getInstance().runAfterPathsCreated(function() {
            UstadMobileBookList.getInstance().coursesFound = [];
            UstadMobile.getInstance().systemImpl.scanCourses(queueCallback);
        });
    },
    
    addCourseToList: function(courseObj) {
        var courseIndex = this.coursesFound.length;
        this.coursesFound.push(courseObj);
        courseObj.courseIndex = courseIndex;
    },
    
    
    updateCourseListDisplay: function() {
        $("#UMBookList").empty().append();
        for(var i = 0; i < this.coursesFound.length; i++) {
            $("#UMBookList").append(this.coursesFound[i].getButtonHTML()
                    ).trigger("create");
        }
    },
    
    
    /**
      *Log out function to set localStorage to null (remove) and redirect to 
      *login page from then.
      * @method umLogout    
    */
    umLogout: function() {
       localStorage.removeItem('username');
       localStorage.removeItem('password');
       $.mobile.changePage("index.html"); //BB10 specific changes.
    },
    
    /**
     * Move to the epubrunner page, when it opens, setup the frame viewer
     * 
     * @param {UstadMobileCourseEntry} courseObj Course to display
     * @param {function} onloadCallback callback to run once content loads
     * 
     */
    showEPubPage: function(courseObj, onloadCallback) {
        $( ":mobile-pagecontainer" ).one("pagecontainershow", function() {
            UstadMobileBookList.getInstance().setEpubFrame(courseObj, onloadCallback);
        });
        
        $( ":mobile-pagecontainer" ).pagecontainer( "change", 
            "ustadmobile_epubrunner.html");
    },
    
    checkButtons: function() {
        var showNext = $("#ustad_epub_frame").opubframe("option", "spine_pos") <
                $("#ustad_epub_frame").opubframe("option", "num_pages")-1;
        var showPrev = $("#ustad_epub_frame").opubframe("option", "spine_pos") > 
                0;
        
        if(showPrev) {
            $("#umBack").show();
        }else {
            $("#umBack").hide();
        }
        
        if(showNext) {
            $("#umForward").show();
        }else {
            $("#umForward").hide();
        }
    },
    
    setEpubFrame: function(courseObj, onloadCallback) {
        if(!$("#ustad_epub_frame").is(".umjs-opubframe")) {
            $("#ustad_epub_frame").opubframe();
            var height = UstadMobile.getInstance().getJQMViewportHeight() - 8;
            
            $("#ustad_epub_frame").opubframe("option", "height", height + "px");
            $("#ustad_epub_frame").css("margin", "0px");
            $("#ustad_epub_frame").on("pageloaded", $.proxy(this.checkButtons,
                this));
        }
        
        $("#ustad_epub_frame").one("pageloaded", function(evt, params) {
            UstadMobileUtils.runCallback(onloadCallback, [evt, params], this);
        });
        
        var fullURI = UstadMobile.getInstance().systemImpl.getHTTPBaseURL() +
                UstadMobile.CONTENT_DIRECTORY + "/" + courseObj.relativeURI;
        $("#ustad_epub_frame").opubframe("loadfromopf", fullURI);
        
        //pagecontainerbeforehide
        $( ":mobile-pagecontainer" ).one("pagecontainerbeforehide", function() {
            console.log("leaving page");
            UstadMobile.getInstance().systemImpl.unmountEpub(courseObj.getEpubName(), function() {
                console.log("Unmount complete");
            });
        });
    },
    
    /**
     * Show a course in an iframe 
     * 
     * @param string httpURL URL of the course - e.g. that running on the http server
     * @param function onshowCallback run when the course element is on screen 
     * @param boolean show whether or not to actually show (if false set display: none css
     * @param function onloadCallback function to run when iframe has loaded
     * @param function onerrorCallback function to run if an error occurs with iframe load
     */
    showCourseIframe: function(httpURL, onshowCallback, show, onloadCallback, onerrorCallback) {
        var iframeEl = $("<iframe src='" + httpURL + "' nwdisable nwfaketop></iframe>");
        iframeEl.css("border-width", "0px");
        iframeEl.css("position", "absolute");
        iframeEl.css("width", "100%");
        iframeEl.css("height", "100%");
        iframeEl.css("z-index", "50000");
        //so it can be found to close it
        iframeEl.addClass(UstadMobileBookList.IRAME_CLASSNAME);
        if(show === false) {
            iframeEl.css("display", "none");
        }
        if(typeof onloadCallback !== "undefined" && onloadCallback !== null) {
            iframeEl.load(onloadCallback);
        }
        
        if(typeof onerrorCallback !== "undefined" && onerrorCallback !== null) {
            iframeEl.error(onerrorCallback);
        }

        $(".ustadbooklistpage").css("display", "none");
        $("BODY").prepend(iframeEl);
        UstadMobileUtils.runCallback(onshowCallback, [iframeEl], this);
    },
      
    /**
     * Close any iframes that are being used to display content
     * 
     * @return {Number} number of iframes closed
     */
    closeBlCourseIframe: function() {
       var elResult = $("." + UstadMobileBookList.IRAME_CLASSNAME);
       $(".ustadbooklistpage").css("display", "inherit");
       
       var numRemoved = elResult.length;
       elResult.remove();
       
       
       return numRemoved;
    },
   
    /**
     * Open the given booklist page
     * @param courseIndex {Number} Index of the course object in UstadMobileBookList.coursesFound
     */
    openBLPage: function(courseIndex, onshowCallback, show, onloadCallback, onerrorCallback) {
        var umBookListObj = UstadMobileBookList.getInstance();
        var courseObj = umBookListObj.coursesFound[courseIndex];
        var openFile = courseObj.coursePath;

        umBookListObj.currentBookPath = openFile;
        var bookpath = umBookListObj.currentBookPath.substring(0,
                umBookListObj.currentBookPath.lastIndexOf("/"));


        jsLoaded = false;
        if (UstadMobile.getInstance().isNodeWebkit() || window.cordova) {
            //use the open course handler
            var courseObj = umBookListObj.coursesFound[courseIndex];
            UstadMobile.getInstance().systemImpl.showCourse(courseObj,
                onshowCallback, show, onloadCallback, onerrorCallback);
            return;
        } 
    }
};

/**
 * 
 * @param {String} courseTitle Title to be displayed
 * @param {String} courseDesc Description of course to show
 * @param {String} coursePath Full path to open the course
 * @param {String} coverImg Path to image
 * @param {String} relativeURI URI relative to UstadMobileContent directory
 * 
 * @return {UstadMobileCourseEntry}
 */
function UstadMobileCourseEntry(courseTitle, courseDesc, coursePath, coverImg, relativeURI) {
    this.courseTitle = courseTitle;
    
    this.courseDesc = courseDesc;
    
    this.coursePath = coursePath;
    
    this.coverImg = coverImg;
    
    /** 
     * URI from the UstadMobileContent Dir to the package.opf .
     * e.g. filename.epub/EPUB/package.opf
     */
    this.relativeURI = null;
    
    /** The index of this course in the list - used to generate HTML button */
    this.courseIndex = -1;
    
    /** The UstadJSOPF Open Packaging Format Object for this object */
    this.opf = null;
    
    if(typeof relativeURI !== "undefined" && relativeURI !== null) {
        this.relativeURI = relativeURI;
    }
}

UstadMobileCourseEntry.prototype = {
    
    /**
     * Find the epub name from a correctly set relative URI
     * 
     * @return {String}
     */
    getEpubName: function() {
        return this.relativeURI.substring(0, this.relativeURI.indexOf("/"));
    },
    
    /**
     * 
     * @return {String} URL relative to /ustadmobileContent/
     */
    getHttpURI : function() {
        return this.relativeURI + "/exetoc.html";
    },
    
    /**
     * Make the HTML needed for this items button
     * 
     * @return string JQueryMobile HTML for a button to open this course
     */
    getButtonHTML: function() {
        return "<a onclick='UstadMobileBookList.getInstance().openBLPage(\"" 
                + this.courseIndex
                + "\")' href=\"#\" data-role=\"button\" "
                + "data-icon=\"star\" data-ajax=\"false\">" + this.courseTitle 
                + "</a>";
    }
}


function UstadMobileAppToContentCopyJob(fileDestMap, destDir, completeCallback) {
    this.fileDestMap = fileDestMap;
    this.fileList = [];
    
    for(srcFile in fileDestMap) {
        this.fileList.push(srcFile);
    }
    
    this.destDir = destDir;
    this.completeCallback = completeCallback;
}

UstadMobileAppToContentCopyJob.prototype = {
    
    currentFileIndex: 0,
    
    //Needs to be overriden by implementation - access using Impl.makeCopyJob
    copyNextFile: function() {
        
    }
    
};

