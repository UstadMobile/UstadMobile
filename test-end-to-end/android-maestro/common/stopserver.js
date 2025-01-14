var response = http.get(CONTROLSERVER_URL + "testcontroller/stop?device=" + TESTSERIAL + "&testName=" + TESTNAME + "&adbRecord=true");
if (response.body && response.body.trim() === "OK") {
    console.log("Test successfully stopped.");
    }
