var response = http.get(CONTROLSERVER_URL +"start?device=" + TESTSERIAL + "&testName=" + TESTNAME + "&adbRecord=true");
output.result = "started"

