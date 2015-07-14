/*
 * Simple NodeJS server that can receive testing results from qunit.
 *
 * Can also control another http server using the start-http.sh and 
 * stop-http.sh to serve static assets from ../test-assets
 * 
 * Usage: node-qunit-server.js [ControlServerPort] [AssetServerPort] [result_dump_dir] [asset_dir]
 * 
 * result_dump_dir will default to current working dir
 *
 * Send an http POST request with params: numPass, numFail, logtext 
 *  Optional: jscover - will be written to result_dump_dir/coverage_report/jscoverage.json
 * 
 * Saves two files:
 *  testresults.txt : text received from parameter logtext
 *  result : contains PASS if numFail = 0, FAIL otherwise
 * 
 *
 */
var http = require("http");
var childprocess = require('child_process');
var path = require('path');
var qs = require('querystring');
var mime = require("mime");
var fs = require("fs");
var querystring = require("querystring");
var urlmod = require("url");
var SlowStream = require('slow-stream');
var args = process.argv;



/** The HTTP server server assets directory */
var assetServer = {
    httpProc : null,
    waitInterval : 0,
    forceErrorAfter : 0,
    httpBaseDir : args[5] ? args[5] : "assets",
    port: args[3] ? args[3] : 10001,
    bufferSize: 64
};

var controlServer = {
    httpProc : null,
    resultDumpDir: args[4] ? args[4] : process.cwd(),
    serverPort : args[2] ? args[2] : 8620.
};


function onFileWritten(err) {
    if(err) {
        throw err;
    }
    console.log("Saved results");
}

/**
 * Borrowed from: 
 * http://www.codeproject.com/Articles/813480/HTTP-Partial-Content-In-Node-js
 * 
 * @param {type} range
 * @param {type} totalLength
 * @returns {ustadmobile-http-server_L310.result}
 */
function readRangeHeader(range, totalLength) {
    /*
     * Example of the method 'split' with regular expression.
     * 
     * Input: bytes=100-200
     * Output: [null, 100, 200, null]
     * 
     * Input: bytes=-200
     * Output: [null, null, 200, null]
     */

    if (!range) {
        return null;
    }

    var array = range.split(/bytes=([0-9]*)-([0-9]*)/);
    var start = parseInt(array[1]);
    var end = parseInt(array[2]);
    var result = {
        Start: isNaN(start) ? 0 : start,
        End: isNaN(end) ? (totalLength - 1) : end
    };

    if (!isNaN(start) && isNaN(end)) {
        result.Start = start;
        result.End = totalLength - 1;
    }

    if (isNaN(start) && !isNaN(end)) {
        result.Start = totalLength - end;
        result.End = totalLength - 1;
    }

    return result;
}

function startAssetServer() {
    if(assetServer.httpProc) {
        return false;//the server is already running
        
    }
    assetServer.httpProc = http.createServer(function (request, response) {
        //this can happen when keepalive etc. is going on
        var url = new String(request.url);
        if(!assetServer.httpProc) {
            console.log("Asset server is supposed to be dead; won't reply.  "
                + " Maybe someone has keepalive.  Destroying request for " + url);
            response.destroy();
            return;
        }
        
        //the complete request url e.g. /ustadmobileContent/file.epub/EPUB/picture.jpg
        
        console.log("Asset request: " + url);
        
        var filename = path.basename(url);
        var filePath = decodeURI(url);
        
        var passThroughStream = require("stream").PassThrough();        
        
        var httpQuery = urlmod.parse(request.url,true).query;
        
        var queryPos = url.indexOf("?");
        
        var endOfFilePos = queryPos === -1 ? url.length : queryPos;
        
        var httpHeaders = {
        };
        
        
        if(filePath.indexOf("?") !== -1) {
            filePath = filePath.substring(0, filePath.indexOf("?"));
        }
        
        var mimeType = mime.lookup(filePath);        
        
        if(httpQuery && httpQuery.download) {
            console.log("Force download");
            httpHeaders['Content-Disposition'] = "attachment; filename=\"" 
                    + decodeURI(filename) + "\"";
        }
        
        if(assetServer.waitInterval) {
            console.log("Forcing error after : " +assetServer.forceErrorAfter);
            passThroughStream = new SlowStream({
                maxWriteInterval : assetServer.waitInterval,
                forceErrorAfter: assetServer.forceErrorAfter});
        }
        
        var fileURI = path.join(assetServer.httpBaseDir, decodeURI(filePath));
        
        if(!fs.existsSync(fileURI)) {
            response.writeHead(404);
            response.end("404: File Not Found : " + fileURI);
            return;
        }
        
        var fileStat = fs.statSync(fileURI);
        var fileSize = fileStat.size;
        
        if(fileStat.isDirectory()) {
            response.writeHead(401);
            response.end("Directory listings are unauthorized");
            return;
        }
        
        var rangeRequest = readRangeHeader(request.headers['range'], 
            fileSize);
        
        var fileBuf = fs.readFileSync(fileURI);
        var md5 = require("MD5");
        var fileMD5Sum = md5(fileBuf);
        fileBuf = null;
        
        if(rangeRequest === null || (rangeRequest.Start === 0 && rangeRequest.End === fileSize-1)) {
            fs.readFile(fileURI, function(err,data) {
                if(err) {
                    response.writeHead(500);
                    response.end("Error accessing file: " + err);
                } else {
                    response.setHeader("Content-Type", mimeType);
                    response.setHeader("Content-Length", fileSize);
                    response.setHeader("Accept-Ranges", "bytes");
                    response.setHeader("ETag", fileMD5Sum);
                    
                    response.writeHead(200, httpHeaders);
                    if(request.method !== "HEAD") {
                        var fsReadable = fs.createReadStream(fileURI, 
                            { bufferSize: assetServer.bufferSize });
                        fsReadable.pipe(passThroughStream).on('error', function(err) {
                            console.log("found error");
                            fsReadable.destroy();
                            response.destroy();
                        }).pipe(response);
                    }else {
                        response.end();
                    }
                    
                }
             });
        }else {
            var start = rangeRequest.Start;
            var end = rangeRequest.End;
            if(start <= fileSize && end <= fileSize) {
                var contentLength = start === end ? 0 : (end - start + 1);
                response.setHeader('Content-Range', 'bytes ' + start + '-' + end + '/' + fileSize);
                response.setHeader("Content-Length", contentLength);
                response.setHeader("Content-Type", mimeType);
                response.setHeader("ETag", fileMD5Sum);
                response.setHeader("Accept-Ranges", "bytes");

                response.writeHead(206);
                if(request.method !== "HEAD") {
                    console.log("partial response: from " + start + " to " + end + " bytes ");
                    var fsReadable = fs.createReadStream(fileURI, 
                        {'start' : start, 'end' : end, bufferSize: assetServer.bufferSize });

                    fsReadable.pipe(passThroughStream).on('error', function(err) {
                            console.log("Error on partial response stream");
                            fsReadable.destroy();
                            response.destroy();
                        }).pipe(response);
                }else {
                    response.end();
                }
            }else {
                response.writeHead(416);
                response.end("Range request cannot be satisfied on this file");
            }
        }
        
    }).listen(assetServer.port);
    console.log("Asset server listening on http://*:" + assetServer.port);
    return true;
}

function stopAssetServer() {
    if(assetServer.httpProc) {
        assetServer.httpProc.close();
        assetServer.httpProc = null;
        return true;
    }else {
        return false;
    }
}

//start the control server
controlServer.httpProc = http.createServer(function (req, res) {
  res.writeHead(200, {'Content-Type': 'text/plain'});
  
  console.log("URL: " + req.url);
  
  if (req.method === 'POST') {
        var body = '';
        console.log("post request");
        req.on('data', function (data) {
            body += data;

            // Too much POST data, kill the connection!
            if (body.length > 1e6)
                req.connection.destroy();
        });
        req.on('end', function () {
            var post = qs.parse(body);
            var numPass = post['numPass'];
            var numFail = post['numFail'];
            var logtext = post['logtext'];
            if(post['jscover']) {
                var coverageFilename = path.join(controlServer.resultDumpDir, 
                    "coverage_report/jscoverage.json");
                console.log("Saving jscover report to " + coverageFilename);
                fs.writeFile(coverageFilename, post['jscover'], onFileWritten);
            }
            
            var textToSave = "Passed: " + numPass + "\n Failed " + numFail + "\n"
                + logtext;
            
            var testResultsFilename = path.join(controlServer.resultDumpDir,
                "node-qunit-testresults.txt");
            fs.writeFile(testResultsFilename, textToSave, onFileWritten);
            
            var result = "";
            if(parseInt(numFail) === 0) {
                result = "PASS";
            }else {
                result = "FAIL";
            }
            
            var resultFilename = path.join(controlServer.resultDumpDir, 
                "result");
            console.log("saving result to " + resultFilename);
            fs.writeFile(resultFilename, result,
                 onFileWritten);
        });
    }else {
        if(req.url.substring(0, 5) === "/http") {
            var httpQuery = urlmod.parse(req.url,true).query;
            if(httpQuery.action === "start") {
                console.log("Starting http server");
                var result = startAssetServer();
                res.end(JSON.stringify({'result' : result}));
            }else if(httpQuery.action === "stop") {
                console.log("stopping http server");
                var result = stopAssetServer();
                res.end(JSON.stringify({'result' : result}));
            }else if(httpQuery.action === "slowdownon") {
                var msg = "";
                var maxSpeed = parseInt(httpQuery.maxspeed);
                var forceErrorAfter = httpQuery.forceerrorafter;
                if(httpQuery.buffersize) {
                    assetServer.bufferSize = parseInt(httpQuery.buffersize);
                    msg += " - Set buffer size to " + assetServer.bufferSize + 
                            " KB";
                }
                
                
                assetServer.waitInterval = Math.round((1000/maxSpeed) * 
                        assetServer.bufferSize);
                msg += "- Set asset server wait interval to " +
                        assetServer.waitInterval + "ms " +
                        "(speed limit of " + maxSpeed + " KB/s with buffer of " +
                        assetServer.bufferSize + " KB)\n";
                console.log(msg);
                if(forceErrorAfter) {
                    assetServer.forceErrorAfter = Math.round(
                        parseInt(forceErrorAfter) / assetServer.bufferSize);
                    msg += "- Will force error + hangup HTTP request " +
                        "after " + forceErrorAfter + " KB (" + 
                        assetServer.forceErrorAfter  + " buffer discharges )";
                }
                res.end(msg);
            }else if(httpQuery.action === "slowdownoff") {
                var result = false;
                if(assetServer.waitInterval) {
                    assetServer.waitInterval = 0;
                    assetServer.forceErrorAfter = 0;
                    assetServer.bufferSize = 64;
                    result = true;
                }
                res.end("Slow down / force error OFF");
            }
        }else {
            res.end('Hello World\n');
        }
    }
}).listen(controlServer.serverPort);

startAssetServer();

console.log('Control server running at http://*:' + controlServer.serverPort);



