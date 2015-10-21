/*
 <!-- This file is part of Ustad Mobile.  
 
 Ustad Mobile Copyright (C) 2011-2013 Toughra Technologies FZ LLC.
 
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
 The javascript associated with ustad mobile login actions and login page on ustad mobil app.
 */

/**
Provides the base TinCanQueue

@module UstadMobileLogin
**/
var UstadMobileLogin;

/*
 * 
 * @type UstadMobileLogin
 */
var ustadMobileLoginInstance;

/**
 * Create a new UstadMobileLogin Object
 * 
 * @class UstadMobileLogin
 * @constructor
 */
function UstadMobileLogin() {
    
}

UstadMobileLogin.getInstance = function() {
    if(!ustadMobileLoginInstance) {
        ustadMobileLoginInstance = new UstadMobileLogin();
    }
    
    return ustadMobileLoginInstance;
};

UstadMobileLogin.prototype = {
    
    /**
     * 
     * @returns {undefined}
     */
    getLoginMessageByStatus: function(statusCode) {
        var msg = null;
        switch(statusCode) {
            case 0:
                msg = "Could not reach server; please check connection";
                break;
            case 401:
                msg = "Invalid username/password";
                break
            case 500:
                msg = "Error on server; please contact support";
                break;
        }
        
        if(!msg) {
            msg = "Generic login error: " + statusCode;
        }
        
        return msg;
    },
    
        
    /** This runs when login is triggered
      * arguments with actual function of login for
      * testing and code-reuse purposes.
      * 
      * @method umloginFromForm
      */ 
    umloginFromForm: function() {
        $.mobile.loading('show', {
            text: x_("Logging in to umcloud.."),
            textVisible: true,
            theme: 'b',
            html: ""});

        var username = $("#username").val();
        var password = $("#password").val();

        if (!url) {
            var url = "http://umcloud1.ustadmobile.com/umlrs/statements?limit=1";
        }
        
        this.umlogin(username, password, url, $.proxy(
            function(statusCode, pass) {
                $.mobile.loading('hide');
                if(statusCode === 200) {
                    //UstadCatalogController.setupUserCatalog({show: true});
		    console.log("Yay, success. Username and Password autneticated. Now time to make the OPDS");
	  	    var opdsEndpoint = "http://umcloud1.ustadmobile.com/opds/";
		    var opdsPage = "opds.html?url=" + opdsEndpoint;
		    document.location.href = opdsPage;
		    //Load opdsFeed by URL or something..
                }else {
                    //change this alert
                    $("#ustad_loginerrortext").text(
                            this.getLoginMessageByStatus(statusCode));
                
                    $("#ustad_login_popup_dialog").popup("open");
                }
            }, this));
    },
    
    /**
     * Checks the given user credentials.  If valid saves them to localstorage
     * 
     * @param username {String} username to authenticate with
     * @param password {String} password to authenticate with
     * @param url {String} optional: tincan statements url or null to use default
     * @param callback {function} 
     */
    umlogin: function(username, password, url, callback) {
        if(typeof url === "undefined" || url === null) {
           url = UstadMobile.getInstance().getDefaultServer().xapiBaseURL 
                + "statements?limit=1";
        }
              
        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'text',
            
            beforeSend: function(request) {
                request.setRequestHeader("X-Experience-API-Version", "1.0.1");
                request.setRequestHeader("Authorization", "Basic " + btoa(username + ":" + password));
            }
        }).done($.proxy(function(data, textStatus, jqxhr) {
            debugLog("Logging to server: " + url + " a success with code:"
                + jqxhr.status);
            localStorage.setItem('username', username);
            localStorage.setItem('password', password);
	    localStorage.setItem('baseurl', "http://umcloud1.ustadmobile.com/");
            UstadMobileUtils.runCallback(callback, [jqxhr.status, password], 
                        this);
            /*
            UstadMobile.getInstance().systemImpl.checkUserContentDirectory(username, {},
                function() {
                    UstadMobileUtils.runCallback(callback, [jqxhr.status, password], 
                        this);
                }, function(err) {
                    console.log("Error in evil old deprecated method of umlogin: " + err);
                });
            */
        }, this)).fail($.proxy(function(jqxhr, b, c) {
            debugLog("Wrong username/password combination or server error. Status Code:" + jqxhr.status);
            UstadMobileUtils.runCallback(callback, [jqxhr.status, password], 
                this);
        }, this));
    },
   

   /**
     * Skips if user doesn't want to log in but still access Books.
     * @method umSkip
     */
   umSkip: function() {
       //temp       
       localStorage.removeItem('username');
       console.log("Skipping. Showing all public / publisher / provider courses.");
	console.log("TODO!");
	var opdsPage = "opds.html"
        document.location.href = opdsPage;
       //$.mobile.changePage("opds.html");
   },
   
};
