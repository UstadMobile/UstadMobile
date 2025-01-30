 const csvText = http.get(output.testServerControllerUrl + "testcontroller/test-files/content/Ustad_Teacher_and_Students.csv").body;
http.post(output.SERVER_URL+ "api/person/bulkadd/import", {
    body: csvText,
    headers: {
        Authorization: "Basic " + "YWRtaW46dGVzdHBhc3M="   }
});
