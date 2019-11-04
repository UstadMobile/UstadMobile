/**
 * Class which handles are Xapi implementations
 */

let UmXapiManager = function() {};

/**
 * Temp xapi statement
 */
UmXapiManager.xapiStatement = {}

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
    const params = UmXapiManager.prototype.getQueryParms(),
    objectId = (params.umId ? params.umId :"") + "/" + $(widgetNode).attr("id")
    widgetType = $(widgetNode).attr("data-um-widget")

    let xapiStatement = {
        actor: UmXapiManager.prototype.getActor(), 
        verb: {
            id: canRetry ? UmXapiManager.xapiVerbs.attempted: UmXapiManager.xapiVerbs.answered,
            display:{}
        },
        object: {
            id: objectId,
            objectType: widgetType, 
            definition: { name: {}, description: {}, type: widgetType}
        }
    }
    switch($(widgetNode).attr("data-um-widget")) {
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
                choices: [choices]
            }
            break;
        case UmWidgetManager.WIDGET_NAME_FILL_BLANKS:
                xapiStatement.object.definition = {
                    ...xapiStatement.object.definition,
                    interactionType: "fill-in"
                }
            break;
    }

    const question = $(widgetNode).find(".question-body p").text()
    xapiStatement.verb.display[locale] = feedback
    xapiStatement.object.definition.name[locale] = question
    xapiStatement.object.definition.description[locale] = question
    UmXapiManager.xapiStatement = xapiStatement
}


/**
 * Send statement to the endpoint
 */
UmXapiManager.prototype.send = () => {
    console.log(UmXapiManager.xapiStatement)
}


/**
 * Get actor object from query params
 */
UmXapiManager.prototype.getActor = ()=> {
    const actor = JSON.parse(UmXapiManager.prototype.getQueryParms().actor)
    let actorMBox = "", actorObjectType = "", actorName = ""
    if(actor.mbox){
        actorMBox = actor.mbox.join(",")
    }
    if(actor.objectType){
        actorObjectType = actor.objectType.join(",")
    }

    if(actor.name){
        actorName = actor.name.join(",")
    }
    return {name: actorName, mbox: actorMBox, objectType: actorObjectType}
}

/**
 * Get query params from URL
 */
UmXapiManager.prototype.getQueryParms = () =>{
    var locationQuery = window.location.search.substring.length >= 1 ?
        window.location.search.substring(1) : "";
    var query = (typeof queryStr !== "undefined") ? queryStr : 
        locationQuery;

    var retVal = {};
    if(window.location.search.length > 2) {
        var vars = query.split("&");
        for (var i=0;i<vars.length;i++) {
            var pair = vars[i].split("=");
            retVal[decodeURIComponent(pair[0].replace("+", "%20"))] = decodeURIComponent(pair[1].replace("+", "%20"))
        }
    }
    console.log(retVal)
    return retVal
}