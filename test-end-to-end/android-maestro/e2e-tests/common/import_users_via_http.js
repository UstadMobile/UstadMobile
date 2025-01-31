const csvText = http.get(output.testServerControllerUrl + "testcontroller/test-files/content/" + IMPORT_FROM_CSV_FILE).body;
http.post(output.SERVER_URL+ "api/person/bulkadd/import", {
    body: csvText,
    headers: {
        Authorization: "Basic " + "YWRtaW46dGVzdHBhc3M="
    }
});
