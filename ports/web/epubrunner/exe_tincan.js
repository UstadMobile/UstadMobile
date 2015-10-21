/*
    EXE Tin Can Support - enables EXE to talk tin can 

    Copyright (C) 2014 Michael Dawson mike@mike-dawson.net

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/


/**
 * Provides the base TinCanQueue
 * 
 * @module EXETinCan
 */

var EXETinCan;

var exeTinCanInstance;


/**
 * Helps exe to talk tin can
 * 
 * https://github.com/RusticiSoftware/TinCanJS
 * 
 * @class EXETinCan
 * @constructor
 */
EXETinCan = function() {
    this.tinCanQueue = new TinCanQueue();
    this.tinCan = new TinCan();
    
    this.actor = null;
    
    // TODO: Fix this
    this.idActivityPrefix = "http://www.ustadmobile.com/tincan/";
};

EXETinCan.mainInstance = null;

EXETinCan.getInstance = function() {
	if(EXETinCan.mainInstance === null) {
		EXETinCan.mainInstance = new EXETinCan();
	}
	
	return EXETinCan.mainInstance;
};


EXETinCan.prototype = {
    
	/**
	 * The attempt registration UID to be applied
	 */
	registrationUUID : null,
	
	/**
	 * Time that the current registration started
	 */
	registrationStartTime : 0,
		
	/**
	 * The proxy destination to send statements to (e.g. queue)
	 */
	tinCanProxyURL : null,
		
	/**
	 * 
	 * @method setActor
	 */
	setActor : function(actor) {
		this.actor = actor;
	},

	/**
	 * 
	 * @method getActor
	 */
	getActor : function() {
		return this.actor;
	}, 
		

    /**
	 * Set how we are working based on parameters in URL
	 * 
	 * If exetincanproxy is set all statements will be sent to the proxy without
	 * authentication (e.g. Ustad Mobile) to a local server
	 * 
	 * Otherwise the statement will be serialized and sent to the LRS if Rustici
	 * launch method parameters are in the URL
	 * 
	 * @method setLRSParamsFromLaunchURL
	 */
    setLRSParamsFromLaunchURL : function() {
    	var queryVars = this.getQueryVariables();
    	// Handle Rustici method
    	if(queryVars['actor']) {
    		var queryActorStr = queryVars['actor'];
	    	var ourActor = TinCan.Agent.fromJSON(queryActorStr);
	    	this.setActor(ourActor);
    	}
    	
    	if(queryVars['exetincanproxy']) {
	    	this.tinCanProxyURL = queryVars['exetincanproxy']; 
    	}else if (this.getActor()){
			var newLRS = new TinCan.LRS({
	            "endpoint" : queryVars['endpoint'],
	            "version" : "1.0.0",
	            "user" : ourActor,
	            'auth' : queryVars['auth']
	        }); 
	    	
			this.tinCan.recordStores[0] = newLRS;
    	}
    },
    
    /**
     * Get the TinCan URL prefix for this content package
     * 
     * @return String TinCan URL prefix e.g. http://www.ustadmobile.com/dir/ELP-ID
     * 
	 * @method
	 */
    getTinCanIDURLPrefix: function() {
    	if($("BODY").attr("data-tincan-prefix")) {
    		return $("BODY").attr("data-tincan-prefix");
    	}else {
    		return "http://www.ustadmobile.com/um-tincan/";
    	}
    },
    
    /**
	 * Record a statement - send it to the proxy or queue
	 * 
	 * @param stmt
	 *            TinCan.Statement Statement to record
	 */
    recordStatement : function(stmt) {
    	console.log("record: " + stmt.originalJSON);
    	
    	if(this.tinCanProxyURL) {
    		$.ajax({
    			url : this.tinCanProxyURL,
    			data : {
    				"statement" : stmt.originalJSON
    			}
    		}).done(function() {
    			console.log("Sent statement to proxy");
    		});
    	}else {
    		// TODO: Put directly into queue
    	}
    },
    
    makeLRSQueryParams : function(endpoint, actorName, actorMbox, user, password) {
    	var queryStr = "endpoint=" + encodeURI(endpoint) + "&";
    	var actorStr = JSON.stringify({
    		"name" : [actorName],
    		"mbox" : ["mailto:" + actorMbox]
    	});
    	queryStr += "actor=" + encodeURI(actorStr) + "&";
    	
    	var authStr = "Basic " + Base64.encode(user, + ":" + password);
    	queryStr += "auth=" + encodeURI(authStr);
    	
    	return queryStr;
    },
    
    /**
     * Format an ISO8601 duration for the given number of milliseconds difference
     * 
     * @param Number duration the duration to format in milliseconds
     * @returns String An ISO8601 Duration e.g. PT4H12M05S
     */
    formatISO8601Duration: function(duration) {
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
    },
    
    /**
     * Start a TinCan registration (attempt tracking) - call to get a
     * registration statement and then record the statement
     * 
     * @return TinCan.Statement with registration
     */
    startRegistration: function() {
    	if(this.getActor()) {
	    	var stmt = this.makeInitializeRegistrationStmt();
	    	this.recordStatement(stmt);
	    	return stmt;
    	}else {
    		return null;
    	}
    },
    
    /**
     * Make a TinCan initialize statement for attempt tracking -
     * return the statement
     */
    makeInitializeRegistrationStmt: function() {
    	var startTime = new Date().getTime();
    	this.registrationUUID = TinCan.Utils.getUUID();
    	this.registrationStartTime = startTime;
    	
    	var myVerb = new TinCan.Verb({
    		id : "http://adlnet.gov/expapi/verbs/initalized",
    		display : {
                "en-US": "initalized"
            }
    	});
	    	
    	//TODO: change way id is created here
    	var myActivity = new TinCan.Activity({
    		id : this.getTinCanIDURLPrefix()+"um_assessment",
    		definition : {
    			type : "http://adlnet.gov/expapi/activities/assessment",
        		name : {
        			"en-US" : "Start Assessment" 
        		},
        		description : {
        			"en-US" : "Start Assessment"
        		}
        	}
    	});
    	
    	var myContext = new TinCan.Context({
    		"registration" : this.registrationUUID
    	});
    	
    	var stmt = new TinCan.Statement({
    		 actor : this.getActor(),
    		 verb: myVerb,
    		 target : myActivity,
    		 context : myContext
    	}, {'storeOriginal' : true});
    	
    	return stmt;
    },
    
    
    /**
     * Finish the TinCan registration (attempt tracking) - make a 
     * completed statement
     * 
     */
    finishRegistration: function() {
    	var stmt = null;
    	if(this.registrationUUID) {
    		var endTime = new Date().getTime();
        	var duration = endTime - this.registrationStartTime;
        	
        	if(this.getActor()) {
        		stmt = this.makeCompleteStatement(duration);
        		this.recordStatement(stmt);
        	}
        	
        	this.registrationUUID = null;
    	}
    	
    	return stmt;
    },
    
    /**
     * 
     */
    makeCompleteStatement: function(duration) {
    	var myVerb = new TinCan.Verb({
    		id : "http://adlnet.gov/expapi/verbs/completed",
    		display : {
                "en-US": "completed"
            }
    	});
    	
    	//TODO: change way id is created here
    	var myActivity = new TinCan.Activity({
    		id : this.getTinCanIDURLPrefix()+"um_assessment",
    		definition : {
    			type : "http://adlnet.gov/expapi/activities/assessment",
        		name : {
        			"en-US" : "Finish Assessment" 
        		},
        		description : {
        			"en-US" : "Finish Assessment"
        		}
        	}
    	});
    	
    	var myContext = new TinCan.Context({
    		"registration" : this.registrationUUID
    	});
    	
    	var myResult = new TinCan.Result({
    		duration : this.formatISO8601Duration(duration)
    	});
    	
    	var stmt = new TinCan.Statement({
    		actor: this.getActor(),
    		verb: myVerb,
    		target: myActivity,
    		result : myResult,
    		context : myContext
    	}, {'storeOriginal' : true});
    	
    	return stmt;
    },
    
    /**
     * Return the active username (e.g. from tincan actor)
     * 
     * @returns String current active username
     */
    getCurrentUsername: function() {
        var retVal = null;
        if(this.getActor()) {
            var tcMbox = EXETinCan.getInstance().getActor().mbox;
            retVal = tcMbox.substring(tcMbox.indexOf(":")+1, 
                tcMbox.indexOf("@"));
        }
        
        return retVal;
    },
    
    /**
     * Make a TINCAN launch statement for this ELP file
     * 
     * @param launchedID String TinCan ID of activity being launched
     * @param launchedName String Name of the activity being launched
     * @param launchedDesc String description of the activity being launched
     * @param parentID String (optional) Parent TINCAN ID of activity being launched
     */
    makeLaunchedStmt : function(launchedID, launchedName, launchedDesc, parentID) {
    	var myVerb = new TinCan.Verb({
    		id : "http://adlnet.gov/expapi/verbs/launched",
    		display : {
                "en-US": "launched"
            }
    	});
    	
    	var myDefinition = {
			type : "http://adlnet.gov/expapi/activities/lesson",
    		name : {
    			"en-US" : launchedName 
    		},
    		description : {
    			"en-US" : launchedDesc
    		}
    	};
    	
    	var myActivity = new TinCan.Activity({
    		id : launchedID,
    		definition : myDefinition
    	});
    	
    	var stmtVals = {
			actor: this.getActor(),
			verb : myVerb,
			target : myActivity
    	};
    	
    	if(typeof parentID !== "undefined") {
    		//Add contextActivity
    		stmtVals['context'] = new TinCan.Context({
    			"contextActivities" : {
    				"parent" : [ {
    					"id" : parentID
    				}]
    			}
    		});
    	}
    	
    	var stmt = new TinCan.Statement(stmtVals, 
    			{'storeOriginal' : true});
    	return stmt;
    },
    
    
    /**
     * Make a TINCAN Statement for user experiencing the page.  Actor must
     * be set.
     * 
     * @param pagename String page identifier (e.g. filename without .html)
     * @param name String Name of activity (e.g. short title) for activity def
     * @param desc String Description of activity for activity def
     * @param duration the duration the page was used (in milliseconds)
     * 
     * @returns TinCan.Statement statement for input args
     */
    makePageExperienceStmt : function(pagename, name, desc, duration) {
    	var myVerb = new TinCan.Verb({
			id : "http://adlnet.gov/expapi/verbs/experienced",
			display: {
	            "en-US": "experienced"
	        }
		});
    	
    	var myDefinition = {
    		type : "http://adlnet.gov/expapi/activities/module",
    		name : {
    			"en-US" : name
    		},
    		description : {
    			"en-US" : desc
    		}
    	};
    	
    	var myActivity = new TinCan.Activity({
			id : this.getTinCanIDURLPrefix() + "/" + pagename,
			definition : myDefinition
		});
    	
    	var myResult = new TinCan.Result({
    		duration : this.formatISO8601Duration(duration)
    	});
    	
    	var stmt = new TinCan.Statement({
			actor : this.getActor(),
			verb : myVerb,
			result : myResult,
			target : myActivity,
			},{'storeOriginal' : true});
		
		return stmt;
    },
    
    
    makeTextFillInStmts : function(selector) {
    	var tincanIdPrefix =  this.getTinCanIDURLPrefix();
    	var textStmts = [];
    	$(selector).find(".exetextinput_container").each(function(index){
    		var descText = $(this).find(
				".exetextinput_instructions").text();
    		descText = descText.replace(/^\s+|\s+$/gm,'');
    		
			var nameText = descText;
			if(nameText.length > 32) {
				nameText = descText.substring(0, 32);
			}
			
			var questionId = $(this).attr(
					"data-exetextinput-questionid");
    		
			var inputText = $(this).find("textarea").val();
			
    		var myVerb = new TinCan.Verb({
    			id : "http://adlnet.gov/expapi/verbs/answered",
    			display: {
    	            "en-US": "answered"
    	        }
    		});
    		
    		var fullQuestionId =  tincanIdPrefix + "/"
    			+ $exe.getCurrentPageName() + "/" + questionId;
    		
    		var myActivity = new TinCan.Activity({
    			id : fullQuestionId,
    			definition :  {
	        		type : "http://adlnet.gov/expapi/activities/module",
	        		name : {
	        			"en-US" : nameText
	        		},
	        		description : {
	        			"en-US" : descText
	        		}
    			}
        	});
    		
    		var myResult = new TinCan.Result({
    			"response" : inputText
    		});
    		
    		var stmtArgs = {
				verb: myVerb,
				target : myActivity,
				result : myResult,
				actor : EXETinCan.getInstance().getActor()
    		};
    		
    		EXETinCan.getInstance().addRegistration(stmtArgs);
    		EXETinCan.getInstance().saveRegistrationVal(questionId, inputText);
    		
    		var stmt = new TinCan.Statement(stmtArgs, 
    				{'storeOriginal' : true});
    		textStmts.push(stmt);
    	});
    	
    	return textStmts;
    },
    
    
    /**
     * If there is an active registration, make a new TinCan.Context
     * object with that included
     * 
     * @method
     * @return {TinCan.Context} context object with registration field set
     */
    makeRegistrationContext: function() {
    	var ctx = null;
    	if(EXETinCan.getInstance().registrationUUID) {
			ctx = new TinCan.Context({
				"registration" : 
					EXETinCan.getInstance().registrationUUID
			});
		}
    	
    	return ctx;
    },
    
    /**
     * If there is a valid registration - add this to the JSON
     * object.  This both modifies the argument object itself and
     * returns that value
     * 
     * @param stmtArgs {Object} JSON object to be used with Statement contstructor
     * 
     * @return stmtArgs {Object} JSON object with context as appropriate
     * 
     * @method
     */
    addRegistration: function(stmtArgs) {
    	var ctx = this.makeRegistrationContext();
    	if(ctx != null) {
    		stmtArgs['context'] = ctx;
    	}
    	
    	return stmtArgs;
    },
    
    /**
     * Return an object with the current registration saved values,
     * or a blank object if there isn't anything yet saved.  
     * 
     * @return
     */
    getRegistrationVals: function() {
    	var regObj = {};
    	if(this.registrationUUID) {
    		var regStr = localStorage.getItem("regvalues-" 
    				+ this.registrationUUID);
    		if(regStr) {
    			regObj = JSON.parse(regStr);
    		}
    	}
    	
    	return regObj;
    },
    
    saveRegistrationVals: function(obj) {
    	if(this.registrationUUID) {
    		localStorage.setItem("regvalues-" + this.registrationUUID, 
    				JSON.stringify(obj));
    	}
    },
    
    /**
     * Temp function being used to save values during a registered attempt
     * (if one is currently active)
     */
    saveRegistrationVal: function(id, val) {
    	if(this.registrationUUID) {
    		var regObj = this.getRegistrationVals();
    		
    		regObj[id] = val;
    		
    		this.saveRegistrationVals(regObj);
    		console.log("Save " + id + " = " + val);
    	}
    },
    
    /**
     * Get a value for the current attempt registration
     * Return null if not available
     * 
     */
    getRegistrationVal: function(id) {
    	var retVal = null;
    	if(this.registrationUUID) {
    		var regObj = this.getRegistrationVals();
    		if(regObj.hasOwnProperty(id)) {
    			retVal = regObj[id];
    		}
    	}
    	
    	return retVal;
    },
    
    
    clearRegistrationVals: function() {
    	
    },
    
    recordTextFillInStmts: function(selector) {
    	var textStmts = this.makeTextFillInStmts(selector);
    	for(var i = 0; i < textStmts.length; i++) {
    		this.recordStatement(textStmts[i]);
    	}
    },

    /**
	 * Turns search query variables into a dictionary - adapted from
	 * http://css-tricks.com/snippets/javascript/get-url-variables/
	 * 
	 * @method getQueryVariable
	 */
	getQueryVariables : function() {
		var retVal = {};
		if(window.location.search.length > 2) {
			var query = window.location.search.substring(1);
	        var vars = query.split("&");
	        for (var i=0;i<vars.length;i++) {
	        	var pair = vars[i].split("=");
	            retVal[pair[0]] = decodeURIComponent(pair[1]);
	        }
		}
        return retVal;
	},
	    
	/**
	 * Make a tin can statement for a multi choice item
	 * 
	 * @param activityDefinition
	 *            {TinCan.ActivityDefinition} definition of MCQ question ativity
	 * @param responseId
	 *            {String} ID of response chosen
	 * @param responseIsCorrect
	 *            {Boolean} is this a correct answer?
	 * 
	 * @method makeMCQTinCanStatement
	 * @return {TinCan.Statement} Statement for MCQ given arguments
	 */
	makeMCQTinCanStatement: function(activityDefinition, questionId, responseId, responseIsCorrect) {
		var stmtArgs = {};
		
		stmtArgs['verb'] = new TinCan.Verb({
			id : "http://adlnet.gov/expapi/verbs/answered",
			display: {
	            "en-US": "answered"
	        }
		});
		
		stmtArgs['target'] = new TinCan.Activity({
			id : this.idActivityPrefix+ "/" + questionId,
			definition : activityDefinition
			
		});
		
		stmtArgs['result'] = new TinCan.Result({
			success : responseIsCorrect,
			response : responseId
		});
		
		stmtArgs['actor'] = this.getActor();
		
		this.addRegistration(stmtArgs);
		
		var myStmt = new TinCan.Statement(stmtArgs,
				{'storeOriginal' : true});
		
		this.recordStatement(myStmt);
		this.saveRegistrationVal(questionId, responseId);
	},
	
	restoreValsFromRegistration: function(containerSelector) {
		
	}
	
}

// check and see if we should take TINCAN parameters from the URL
EXETinCan.getInstance().setLRSParamsFromLaunchURL();
