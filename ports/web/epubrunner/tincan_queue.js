
/*
    TIN CAN Queue - This is designed to make it easier to make Tin Can
    apps that work offline using HTML5 and then queue up statements
    to send to the LRS when connectivity is available

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
Provides the base TinCanQueue

@module TinCanQueue
**/
var TinCanQueue;

var tinCanQueueInstance = null;


/**
 Queues up tincan statements using HTML5 localStorage
 
 Designed to work with Rustici's TINCAN Javascript API implementation from
  https://github.com/RusticiSoftware/TinCanJS
 
 @class TinCanQueue
 @constructor
*/
TinCanQueue = function() {
    this.TINCAN_LOCALSTORAGE_INDEXVAR = "tincanindex";
    this.TINCAN_LOCALSTORAGE_PENDINGSTMTSVAR = "tincan_pendingstmts";
    this.TINCAN_LOCALSTORAGE_STATEMENTPREFIX = "tincan_statements.";       
    
    //statements that are right now being sent to the LRS and we are waiting 
    //for callbacks
    this.statementsBeingTransmitted = [];
};

function getTinCanQueueInstance() {
    if(tinCanQueueInstance === null) {
        tinCanQueueInstance = new TinCanQueue();
    }
        
    return tinCanQueueInstance;
}

TinCanQueue.prototype = {
    
    
    queueDoneCallback : null,
    
    queueLengthPending : 0,
    
    /**
     * 
     * @param {Array} results Array of results
     * @param {Object|TinCan.Statement} tinCanStmt
     * 
     */
    statementSentCallback: function(results, tinCanStmt) {
        var tinCanQueue = getTinCanQueueInstance();
        
        var thisStmtSentOK = false;
        for(var j = 0; j < results.length; j++) {
            //see if it was sent OK
            var err = results[j]['err'];
            var xhr = results[j]['xhr'];
            if((err === null|| err === 0) && xhr.status >= 200  && xhr.status < 300) {
                //this one sent OK
                thisStmtSentOK = true;
            }
        }
        
        //If this has sent OK ... remove it from the queue
        if(thisStmtSentOK) {
            var stmtQueueId = tinCanQueue.statementsBeingTransmitted[tinCanStmt.id];
            var pendingStatementsArr = tinCanQueue.getPendingStatementsArr();
            var stmtQueueIndex = pendingStatementsArr.indexOf(stmtQueueId);

            pendingStatementsArr.splice(stmtQueueIndex, 1);
            tinCanQueue.setPendingStatementsArr(pendingStatementsArr);
        }
        
        tinCanQueue.queueLengthPending -= 1;
        
        if(tinCanQueue.queueLengthPending === 0) {
            //all statements have returned now
            if(typeof tinCanQueue.queueDoneCallback === "function") {
                tinCanQueue.queueDoneCallback();
            }
        }
    },
    
    /**
    
    Try to send tin can statements to LRS now
    
    @method sendStatementQueue
    @param {function} queueDoneCallBackArg - callback to run once the queue is done
    @param {TinCan} tinCanObj TinCan object that is to be used to send statements 
    */
    sendStatementQueue: function(tinCanObj, queueDoneCallbackArg) {
        var tinCanPendingStmts = this.getPendingStatementsArr();
        this.queueDoneCallback = queueDoneCallbackArg;
        this.queueLengthPending = tinCanPendingStmts.length;
        
        var numSent = 0;
        for(var i = 0; i < tinCanPendingStmts.length; i++) {
            var stmt = this.getPendingStatementById(tinCanPendingStmts[i]);
             
            //map this to the statementQueue 
            this.statementsBeingTransmitted[stmt.id] = tinCanPendingStmts[i];
            tinCanObj.sendStatement(stmt,  this.statementSentCallback);
        }
        
        return numSent;
    },
    
    /**
     * Returns the Statement object for the given id
     * 
     * @method getPendingStatementById
     * @param {Number} id number of pending statement
     * 
     * @return {Object|TinCan.Statement}
     */
    getPendingStatementById: function(id) {
        var stmtStr = localStorage.getItem(
            this.TINCAN_LOCALSTORAGE_STATEMENTPREFIX + id);
        var stmt = TinCan.Statement.fromJSON(stmtStr);
        return stmt;
    },
     
    
    /**
    Returns an array with the index numbers of pending statements that
    need sent to LRS
    
    @method getPendingStatementsArr
    @return {Array} Array with integers of pending statement indexes
    */
    getPendingStatementsArr: function() {
        var pendingStmts = localStorage.getItem(
            this.TINCAN_LOCALSTORAGE_PENDINGSTMTSVAR);
        var pendingStmtsArr = null;
        if(pendingStmts) {
            pendingStmtsArr = JSON.parse(pendingStmts);
        }else {
            pendingStmtsArr = [];
        }
        return pendingStmtsArr;
    },
    
    /**
     Sets the list of statements that are in the queue and need sent to the LRS
    
     @method setPendingStatementsArr
     @param {Array} arr Array of ids
     */        
    setPendingStatementsArr: function(arr) {
        localStorage.setItem(this.TINCAN_LOCALSTORAGE_PENDINGSTMTSVAR,
            JSON.stringify(arr));
    },
    
    /**
    Puts a Statement object in the queue
     
    @method exeTinCanQueueStatement
    @return {Number} Index number of statement in Queue -1 if something is wrong
    */
    queueStatement: function(stmt) {
        //find the next sequence number
        var tinCanIndexVar = localStorage.getItem(
            this.TINCAN_LOCALSTORAGE_INDEXVAR);
        var tinCanStmtIndex = -1;
        if(tinCanIndexVar) {
            tinCanStmtIndex = parseInt(tinCanIndexVar);
            localStorage.setItem(this.TINCAN_LOCALSTORAGE_INDEXVAR, 
                ""+(tinCanStmtIndex+1));
        }else {
            tinCanStmtIndex = 0;
            localStorage.setItem(this.TINCAN_LOCALSTORAGE_INDEXVAR, "0");
        }
        
        localStorage.setItem(
            this.TINCAN_LOCALSTORAGE_STATEMENTPREFIX + tinCanStmtIndex,
            stmt.originalJSON);
        
        var pendingStmts = this.getPendingStatementsArr();
        
        pendingStmts[pendingStmts.length]= tinCanStmtIndex;
        
        localStorage.setItem(this.TINCAN_LOCALSTORAGE_PENDINGSTMTSVAR,
            JSON.stringify(pendingStmts));

        return tinCanStmtIndex;
    }
}; 


