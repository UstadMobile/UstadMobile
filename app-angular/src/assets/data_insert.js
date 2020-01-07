const axios = require('axios'),
serverUrl = "http://localhost:8087/"

function executeGetRequest(url){
    return new Promise((resolve, reject) => {
      const mUrl = serverUrl + url;
      console.log("GET Request",mUrl)
      axios.get(mUrl).then(function (response) {
        resolve(response.data)
      }).catch(function (error) {
        resolve(error)
      })
    })
  }
  
  function executePostRequest(request){
    return new Promise((resolve, reject) => {
      const fs = require('fs'), path = require('path'),
      filePath = path.join(__dirname, request.path),
      content = fs.readFileSync(filePath, {encoding: 'utf-8'});
      axios.post(serverUrl+ "UmAppDatabase/" + request.url, JSON.parse(content)).then(function (response) {
       resolve(response.data)
      }).catch(function (error) {
       resolve(error)
      });
      
    })
  }

  function clearDb(){
    return executeGetRequest("UmAppDatabase/clearAllTables")
  }
  function insertDummyData(){
    
    const requestPromises = [
      executeGetRequest("UmContainer/addContainer?type=epub&entryid=31228&resource=test.epub")
    ];

    [
      {url:"ContentEntryDao/insertListAsync", path: "data_entries.json"},
      {url:"ContentEntryParentChildJoinDao/insertListAsync",path:"data_entries_parent_join.json"},
      {url:"LanguageDao/insertListAsync", path:"data_languages.json"}, 
      {url:"PersonDao/insertListAsync", path:"data_persons.json"},
      {url:"StatementDao/insertListAsync",path:"data_statements.json"}, 
      {url:"XLangMapEntryDao/insertListAsync", path: "data_xlangmap.json"},
      {url:"VerbDao/insertListAsync", path: "data_verbs.json"}].forEach( request =>{
        requestPromises.push(executePostRequest(request))
      });
      Promise.all(requestPromises).then(responses => {
        console.log("Data inserted", responses)
      })
  }

  //clear db and insert data
  clearDb().then(response => {
    console.log("Executed db clearing", response)
    insertDummyData();
  })
  