
const maxAttempts = 4;

/* Retry should not be needed, but as this can run over a real network, it is implemented just for
 * rare occasions
 */
for(var i = 0; i < maxAttempts; i++) {
    try {
        const csvText = http.get(output.testServerControllerUrl + "testcontroller/test-files/content/" + IMPORT_FROM_CSV_FILE).body;
        http.post(output.SERVER_URL+ "api/person/bulkadd/import", {
            body: csvText,
            headers: {
                Authorization: "Basic " + "YWRtaW46dGVzdHBhc3M="
            }
        });
        console.log("import_users_via_http.js: SUCCESS : attempt: " + i );
        break;
    }catch(err) {
        console.log("import_users_via_http.js: FAIL : attempt " + i + " failed: " + err);
        if(i == maxAttempts - 1) {
            throw err
        }
    }
}

