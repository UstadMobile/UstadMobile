/**
 * Class which handles all Xapi implementations, it handle creation of statements and post them to the end-point
 */

let UmXapiManager = function() {};

/**
 * Temp xapi statement
 */
UmXapiManager.xapiStatement = null

/**
 * Xapi commonnly used verbs
 */
UmXapiManager.xapiVerbs = {
    experienced: "experienced", attended: "attended", attempted: "attempted", completed: "completed",
    passed: "passed", failed: "failed", answered: "answered", interacted:"interacted", imported:"imported",
    created: "created", shared:"shared", voided:"voided"
}

/**
 * Construct xapi statement for mutiple choice questions
 * @param widgetNode  question node
 * @param choice all question choice list
 * @param response correct response ID
 * @param feedback feedback text upon choice selection
 * @param isCorrect flag to indicate whether choice was correct or not
 * @param canRetry flag to indicate if a question can be retried
 */
UmXapiManager.prototype.makeStatement = (widgetNode,choices, response, feedback, locale, isCorrect = false, canRetry = false) => {
    UmXapiManager.xapiStatement = []
    const params = UmXapiManager.getQueryParms(),
    objectId = (params.umId ? params.umId :"") + "/" + $(widgetNode).attr("id"),
    widgetType = $(widgetNode).attr("data-um-widget")
    
    let xapiStatement = {
        actor: UmXapiManager.getActor(), 
        verb: {
            id: canRetry ? UmXapiManager.xapiVerbs.attempted: UmXapiManager.xapiVerbs.answered,
            display:{}
        },
        object: {
            id: objectId,
            objectType: widgetType, 
            definition: { name: {}, description: {}, type: "http://adlnet.gov/expapi/activities/cmi.interaction"}
        }
    }

    const question = $(widgetNode).find(".question-body p").text()
    xapiStatement.verb.display[locale] = feedback
    xapiStatement.object.definition.name[locale] = question
    xapiStatement.object.definition.description[locale] = question

    switch(widgetType) {
        case UmWidgetManager.WIDGET_NAME_MULTICHOICE:
            xapiStatement = {...xapiStatement, 
                result: {
                success: isCorrect,
                response: response
            }}
            xapiStatement.object.definition = {
                ...xapiStatement.object.definition,
                interactionType: "choice",
                correctResponsesPattern: [response],
                choices: choices
            }
            break;
        case UmWidgetManager.WIDGET_NAME_FILL_BLANKS:
                xapiStatement.object.definition = {
                    ...xapiStatement.object.definition,
                    interactionType: "fill-in"
                }
            break;
    }
    UmXapiManager.xapiStatement = xapiStatement
}


/**
 * Send statement/status to the endpoint
 */
UmXapiManager.prototype.send = (callback = null) => {
    const data = JSON.stringify(UmXapiManager.xapiStatement ? UmXapiManager.xapiStatement: {}),
    path = UmXapiManager.xapiStatement ? "statements/":"activities/state"
    $.post(UmXapiManager.getQueryParms().endpoint + path, data, (response, status) => {
        if(callback != null){
            callback(response, status)
        }
        console.log(status !== 200 ? "Saved successfully": "Failed to save statement")

        if(status !== 200){
            console.error(response)
        }
    })
}


/**
 * Construct an actor object from params
 */
UmXapiManager.getActor = ()=> {
    const actorParam = JSON.parse(UmXapiManager.getQueryParms().actor),
    actor = {name: null, mbox: null, objectType: null, account: {name: null,homePage: null}}

    if(actorParam.mbox){
        actor.mbox = actorParam.mbox
    }
    if(actorParam.objectType){
        actor.objectType = actorParam.objectType
    }

    if(actorParam.name){
        actor.name = actorParam.name
    }

    if(actorParam.account && actorParam.account.name){
        actor.account.name = actorParam.account.name
    }
    if(actorParam.account && actorParam.account.homePage){
        actor.account.homePage = actorParam.account.homePage
    }
    return actor
}

/**
 * Get query params from URL
 */
UmXapiManager.getQueryParms = () =>{
    var locationQuery = window.location.search.substring.length >= 1 ? window.location.search.substring(1) : "";
    var query = (typeof queryStr !== "undefined") ? queryStr : locationQuery;

    var params = {};
    if(window.location.search.length > 2) {
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            params[UmXapiManager.decodeURIComponent(pair[0])] = UmXapiManager.decodeURIComponent(pair[1])
        }
    }
    return params
}

/**
 * Decode Url component
 */
UmXapiManager.decodeURIComponent = (param)=>{
    return decodeURIComponent(param.replace("+", "%20"))
}