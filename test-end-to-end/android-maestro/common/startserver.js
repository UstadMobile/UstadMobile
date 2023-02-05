var response = http.get(CONTROLSERVER +"start?device=" + TESTSERIAL + "&testName=" + TESTNAME
    + "&adbRecord=true");
output.result = "started"
