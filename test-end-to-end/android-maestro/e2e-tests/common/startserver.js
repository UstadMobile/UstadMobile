
console.log("TESTCONTROLLER_URL = " + TESTCONTROLLER_URL);
const maxAttempts = 4;

if(typeof(TESTCONTROLLER_URL) == "string" &&
    (TESTCONTROLLER_URL.toLowerCase().startsWith("http://") ||
    TESTCONTROLLER_URL.toLowerCase().startsWith("https://"))
) {
    output.testServerControllerUrl = TESTCONTROLLER_URL;
    console.log("TESTCONTROLLER_URL set " + output.testServerControllerUrl);
}else {
    output.testServerControllerUrl = "http://localhost:8075/";
    console.log("TESTCONTROLLER_URL not set " + output.testServerControllerUrl);
}

/* Retry should not be needed, but as this can run over a real network, it is implemented just for
 * rare occasions
 */
for(var i = 0; i < maxAttempts; i++) {
    try {
        const response = http.get(output.testServerControllerUrl + "testcontroller/start");
        const responseJson = JSON.parse(response.body);
        output.SERVER_URL = responseJson.url;
        output.adminUsername = responseJson.adminUsername;
        output.adminPassword = responseJson.adminPassword;
        console.log("startserver.js: SUCCESS : server available on " + output.SERVER_URL);
        break;
    }catch(err) {
        console.log("startserver.js: FAIL : attempt " + i + " failed: " + err);
        if(i == maxAttempts - 1)
            throw err
    }
}

