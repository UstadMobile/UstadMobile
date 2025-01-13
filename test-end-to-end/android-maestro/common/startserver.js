var response = http.get(CONTROLSERVER_URL +"testcontroller/start?device=" + TESTSERIAL + "&testName=" + TESTNAME + "&adbRecord=true");
//output.result = "started"
//var response = http.get(`${process.env.CONTROLSERVER_URL}/testcontroller/start`);
const responseBody = JSON.parse(response.body); // Parse the JSON response
output.SERVER_URL = responseBody.url; // Extract 'url'

console.log(response.body);
