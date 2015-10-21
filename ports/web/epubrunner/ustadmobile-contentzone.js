/* 
<!-- This file is part of Ustad Mobile.  
    
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

var UstadMobileContentZone;

/**
 * Object that handles logic and functions that work within the content context
 * (as opposed to the app context)
 * 
 * Content Zone will trigger the following events on document:
 * 
 *  execontentpageshow everytime a new div of content is put up on screen
 *   evt.target = containing page div
 *  
 * Content Zone will trigger the following events on every item with the 
 * iDevice_wrapper class
 *   ideviceshow
 * 
 * @class UstadMobileContentZone
 * @constructor
 */
UstadMobileContentZone = function() {
    
};

/**
 * Main single instance of UstadMobileContentZone
 * 
 * @type {UstadMobileContentZone}
 */
UstadMobileContentZone.mainInstance = null;

/**
 * Gets an instance of UstadMobileContentZone
 * 
 * @returns {UstadMobileContentZone}
 */
UstadMobileContentZone.getInstance = function() {
    if(UstadMobileContentZone.mainInstance === null) {
        UstadMobileContentZone.mainInstance = new UstadMobileContentZone();
    }
    return UstadMobileContentZone.mainInstance;
};

UstadMobileContentZone.prototype = {
        
    /**
     * JQuery selectors for the left (previous), right (next) page and current page
     * @type Number
     */
    contentPageSelectors: [null, null, null],
    
    
    /**
     * Duration to run animations for when changing pages
     * 
     * @type {Number}
     */
    contentPageTransitionTime: 1000,
    
    /**
     * Boolean tracker if a transition is already in progress.  If it is - we 
     * block further pageChanges until it's done
     * 
     * @type Boolean
     */
    transitionInProgress: false,
    
    /**
     * Run startup routines for the content zone - setup event handlers for making
     * Table of Content links safe, etc.
     * 
     * @method
     */
    init: function() {
        $( ":mobile-pagecontainer" ).on("pagecontainershow",
            this.triggerPageShowOnCurrent);
    },
    
    
    
    /**
     * Return the active username (e.g. from tincan actor)
     * 
     * @returns String current active username
     */
    getCurrentUsername: function() {
        var retVal = null;
        if(EXETinCan.getInstance().getActor()) {
            var tcMbox = EXETinCan.getInstance().getActor().mbox;
            retVal = tcMbox.substring(tcMbox.indexOf(":")+1, 
                tcMbox.indexOf("@"));
        }
        
        return retVal;
    },
    
    
    /**
     * Pre-Process the content items to make sure that they are displayed as 
     * desired 
     * 
     * @param {jQuery} contentEl
     */
    processPageContent: function(contentEl) {
        var fixItFunction = function() {
            var alreadyFixed=$(this).attr("data-exefixed");
            if(alreadyFixed !== "true") {
                var answerFor = $(this).attr("for");
                //ID of radio button is going to be iELEMENTID
                //eg i0_100 idevice=0, field=100
                var answerId = "";
                if(answerFor.substring(0, 1) === 'i') {
                    //mcq radio button
                    answerId = answerFor.substring(1);
                }else {
                    //multi select checkbox
                    answerId = $(this).children("a").first().attr("href");
                    answerId = answerId.split("-")[1];
                }

                var ideviceAnswerContainer = $(this).closest(".iDevice_answer-field");
                ideviceAnswerContainer.css("width", "auto").css("float", "none");

                contentEl.find("#answer-"+ answerId).css("padding-left", "0px");
                $(this).removeClass("sr-av");

                $(this).html("");
                contentEl.find("#answer-"+ answerId).detach().appendTo($(this));
                $(this).attr("data-exefixed", "true");
            }
        };
        
        //restore value of textinput
        contentEl.find(".TextInputIdevice").each(function(index){
            var questionId = $(this).find(".exetextinput_container").attr(
                    'data-exetextinput-questionid');
            
            if(EXETinCan) {
                var regVal = EXETinCan.getInstance().getRegistrationVal(
                        questionId);
                if(regVal) {
                    $(this).find("textarea").val(regVal);
                }
            }
        });
        
        contentEl.find(".MultiChoiceIdevice .question").each(function(index) {
            var idAttr = $(this).find(".block").attr("id");
            if(!idAttr) {
                //not in the right place here...
                return;
            }
            var prefix = "taquestion";
            var questionId = idAttr.substring(prefix.length);
            if(EXETinCan) {
                var questionVal = EXETinCan.getInstance().getRegistrationVal(
                        questionId);
                if(questionVal) {
                    $(this).find("#i" + questionVal).attr("checked", "checked");
                }
            }
        });

        contentEl.find(".MultichoiceIdevice label, "
                + ".MultiSelectIdevice label").each(fixItFunction);
        
        //stop JQueryMobile AJAX transition on internal links
        contentEl.find("a").attr("data-ajax", "false");
        
        UstadMobile.getInstance().runAfterRuntimeInfoLoaded(function() {
            if(UstadMobile.getInstance().getRuntimeInfoVal("FixAttachmentLinks") === true) {
                contentEl.find(".FileAttachIdeviceInc .exeFileList a").each(function() {
                    var href= $(this).attr('href');
                    if(!$(this).attr("data-startdownload-url")) {
                        var ajaxHref = href + "?startdownload=true";
                        $(this).attr("data-startdownload-url", ajaxHref);
                        $(this).attr("href", "#");
                        $(this).on("click", function() {
                            var hrefToOpen = $(this).attr("data-startdownload-url");
                            $.ajax({
                                url: hrefToOpen,
                                dataType : "text"
                            });
                        });
                    }
                });

                contentEl.find("a").each(function() {
                    var href = $(this).attr("href");
                    if(typeof href !== "undefined" && href !== null){
                        if(href.substring(0,7)==="http://" ||
                            href.substring(0, 8) === "https://") {
                            $(this).attr("href", "#");
                            $(this).on("click", function(evt) {
                                evt.preventDefault();
                                $.ajax({
                                    url: "/browse/" + encodeURI(href),
                                    dataType: "text"
                                });
                            });
                        }
                    }
                });
            }
        });
    },
    
    /**
     * Trigger an event on every idevice element in the given parentElement selector
     * 
     * @param parentElementSelector string JQuery selector object representing the parentElemetn
     * @param evtName string
     */
    triggerEventOnPageIdevices: function(parentElementSelector, evtName) {
        $(parentElementSelector).find(".iDevice_wrapper").each(function() {
            var evt = $.Event(evtName, {
                target : this
            });
            
            var ideviceId = $(this).attr("id");
            if(ideviceId.indexOf("id") === 0) {
                ideviceId = ideviceId.substring(2);
            }
            
            evt.ideviceId = ideviceId;
            
            $(this).trigger(evt);
        });
    },
    
    triggerPageShowOnCurrent: function() {
        UstadMobileContentZone.getInstance().pageShow(
            UstadMobileContentZone.getInstance().contentPageSelectors[
            UstadMobile.MIDDLE]);
    },
    
    
    
    /**
     * Things to run when the page is actually displayed for the user
     * 
     * @param pageSelector string selector for the page to show
     * @returns number number of elements played
     */
    pageShow: function(pageSelector) {        
        this.triggerEventOnPageIdevices(pageSelector, "ideviceshow");
        
        var docEvt = $.Event("execontentpageshow", {
            target: $(pageSelector),
            targetSelector: pageSelector
        });
        console.assert($(pageSelector).length === 1);
        
        $(document).trigger(docEvt);
        console.group("Running PageShow Event");
        UstadMobileUtils.debugLog("Trigger execontentpageshow on document");
        
        //start time recording for the TinCan API for the page we are about to show
        var pageName = $(pageSelector).attr("data-url");
        pageName = UstadMobile.getInstance().stripHTMLURLSuffix(
                pageName);
        
        if($exe.updateCurrentPageName) {
            $exe.updateCurrentPageName($(pageSelector).attr("data-url"));
        }
        
        UstadMobileContentZone.getInstance().startPageTimeCounter(pageName);
        
        var mediaToPlay = $(pageSelector).find("audio[data-autoplay]");
        var numToPlay = mediaToPlay.length;
        for(var i = 0; i < numToPlay; i++) {
            var playMediaEl = mediaToPlay.get(i);
            UstadMobileUtils.playMediaElement(playMediaEl);
        }
        

        console.groupEnd();
        return numToPlay;    
    },
    
    /**
     * Things to do when the page is hidden from the user - e.g. stop sounds
     * @param pageSelector string selector for page element being hidden
     */
    pageHide: function(pageSelector) {
        var mediaToStopArr = $(pageSelector).find("audio");
        for(var i = 0; i < mediaToStopArr.length; i++) {
            var mediaToStop = mediaToStopArr.get(i);
            
            if(mediaToStop.readyState >= 2 && mediaToStop.currentTime !== 0 && !mediaToStop.ended) {
                mediaToStop.pause();
            }
        }
        
        //record the TinCan API statement for having seen this page
        UstadMobileContentZone.getInstance().stopPageTimeCounter(pageSelector);
    },
    
    
    
    /**
     * Stop counting the current page, make a TinCan API statement about it and
     * record it using EXETinCan.recordStatement
     * 
     * @param pageSelector Selector of the page div being finished
     * 
     * @method
     */
    stopPageTimeCounter: function(pageSelector) {
        var pageDurationMS = (new Date().getTime()) - this.pageOpenUtime;
        
        var pageTitle = $(pageSelector).attr("data-title");
        
        
        if(EXETinCan.getInstance().getActor()) {
            var stmt = EXETinCan.getInstance().makePageExperienceStmt(
                this.pageOpenXAPIName, pageTitle, pageTitle, pageDurationMS);
            EXETinCan.getInstance().recordStatement(stmt);
            EXETinCan.getInstance().recordTextFillInStmts(pageSelector);
        }
    },
   
    
    /**
     * Will remove audio/video autoplay and replace with data-ustad-autoplay
     * which can then be triggered when we actually show the content
     * 
     * Strangely this does not work if we do this using dom manipulation etc.
     * 
     * @param pageHTML string to process
     */
    preProcessMediaTags: function(pageHTML) {
        pageHTML = pageHTML.replace(/autoplay(=\"autoplay\")/, function(match, $1) {
            return "data-autoplay" +$1;
        });
        
        return pageHTML;
    }
    
};

//Enhance JQueryMobile based items
$(function() {
    UstadMobileContentZone.getInstance().processPageContent($("#main"));
    $("#main").enhanceWithin();
});
