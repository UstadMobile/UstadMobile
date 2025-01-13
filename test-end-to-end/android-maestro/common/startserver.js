
const testServerControllerUrl = TESTCONTROLLER_URL || "http://localhost:8075/";
const response = http.get(testServerControllerUrl +"testcontroller/start");
const responseJson = JSON.parse(response.body);
output.SERVER_URL = responseJson.url;
console.log("server url=" + responseJson.url)
output.adminUsername = responseJson.adminUsername;
output.adminPassword = responseJson.adminPassword;
console.log(response.body);
