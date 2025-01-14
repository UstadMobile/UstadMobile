
console.log("TESTCONTROLLER_URL = " + TESTCONTROLLER_URL);
if(typeof(TESTCONTROLLER_URL) != "undefined" && TESTCONTROLLER_URL != null && TESTCONTROLLER_URL != "null") {
    output.testServerControllerUrl = TESTCONTROLLER_URL;
    console.log("TESTCONTROLLER_URL set " + output.testServerControllerUrl);
}else {
    output.testServerControllerUrl = "http://localhost:8075/";
    console.log("TESTCONTROLLER_URL not set " + output.testServerControllerUrl);
}
const response = http.get(output.testServerControllerUrl + "testcontroller/start");
const responseJson = JSON.parse(response.body);
output.SERVER_URL = responseJson.url;
output.adminUsername = responseJson.adminUsername;
output.adminPassword = responseJson.adminPassword;

