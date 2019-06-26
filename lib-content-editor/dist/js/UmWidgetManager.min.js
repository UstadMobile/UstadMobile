/**
 * Class which handle all widgets on the document like adding and removing elements on editing and preview mode, 
 * handling events and placeholders.
 * @param widget widget currently loaded on the editor
 * @constructor
 *
 * @author kileha3
 */
let UmWidgetManager = function(widget) {
    this.widget =  widget;
};

/** Object to store locale details */
UmWidgetManager._placeholder = {};

/** Selector for all editable content div's */
const editableSectionsSelector = ".question-body, .extra-content, .question-choice-body , .question-choice-feedback , .question-choice-feedback-correct , .question-choice-feedback-wrong"

/** Tracking the current widget type */
let currentWidgetType = null;

/** Question id prefix */
UmWidgetManager.QUESTION_ID_TAG = "id-question-";

/** Choice id prefix */
UmWidgetManager.CHOICE_ID_TAG = "id-choice-"

/** Other editor node id prefix */
UmWidgetManager.EXTRA_CONTENT_ID_TAG = "id-";

/** Map to store widget type when editing */
UmWidgetManager._widgets = {};

/** Object to store locale details */
UmWidgetManager._placeholder = {};

/** Object to store widget listeners reference */
UmWidgetManager._widgetListeners = {};

/** Flag which indicate if the question is new or existing one */
UmWidgetManager.isNewWidget = false;

/** Flag which indicate editor mode */
UmWidgetManager.isEditingMode = false;

/** Flag which used to identify widget type multi choice question */
UmWidgetManager.WIDGET_NAME_MULTICHOICE = "multi-choice";

/** Flag which used to identify widget type fill in the blanks question */
UmWidgetManager.WIDGET_NAME_FILL_BLANKS = "fill-the-blanks";

/** Flag which used to identify all non question content widget */
UmWidgetManager.WIDGET_NAME_EXTRA_CONTENT = "extra-content";

/** Holds clipboard value after cut action */
UmWidgetManager.CLIPBOARD_CONTENT = null;

/** Default page break element to be used when new node is added */
UmWidgetManager.PAGE_BREAK = '<p style="page-break-before: always" class="pg-break">';

/** Content widget which hold all non question content in the editor */
UmWidgetManager.EXTRA_CONTENT_WIDGET = '<div data-um-widget="extra-content" class="um-row um-editable default-margin-top extra-content"><p></p></div>' +
UmWidgetManager.PAGE_BREAK;

/** Instantiate multiple choice question widget */
let UmMultipleChoiceWidget = function(widget){UmWidgetManager.apply(this, arguments);};

/** Instantiate fill in the blanks question widget */
let UmFillTheBlanksWidget = function(widget){UmWidgetManager.apply(this, arguments);};

/** Instantiate extra content widget */
let UmExtraContentWidget = function(widget){UmWidgetManager.apply(this, arguments);};

/** Create multiple choice question widget */
UmMultipleChoiceWidget.prototype = Object.create(UmWidgetManager.prototype);

/** Create fill in the blanks question widget */
UmFillTheBlanksWidget.prototype = Object.create(UmWidgetManager.prototype);

/** Create extra content widget */
UmExtraContentWidget.prototype = Object.create(UmWidgetManager.prototype);


/**
 * Manage question and other plain text nodes in the editor.
 * @param widgetNode any html blocked added to the editor
 * @param newWidget indicates if the widget was in the document or is a new insert.
 */
UmWidgetManager.handleWidgetNode =  (widgetNode , newWidget = false) => {
    const questionId =  $(widgetNode).attr("id");
    UmWidgetManager.isNewWidget = newWidget;
    if(!UmWidgetManager._widgets[questionId]) {
        currentWidgetType = $(widgetNode).attr("data-um-widget");
        switch(currentWidgetType) {
            case UmWidgetManager.WIDGET_NAME_MULTICHOICE:
                UmWidgetManager._widgets[questionId] = new UmMultipleChoiceWidget(widgetNode);
                break;
            case UmWidgetManager.WIDGET_NAME_FILL_BLANKS:
                UmWidgetManager._widgets[questionId] = new UmFillTheBlanksWidget(widgetNode);
                break;
            case UmWidgetManager.WIDGET_NAME_EXTRA_CONTENT:
                UmWidgetManager._widgets[questionId] = new UmExtraContentWidget(widgetNode);
                break;
        }
    }
    return UmWidgetManager._widgets[questionId];
};

/**
 * Handle all document editing and previewing events
 * @param editMode flag to indicate if the editor is in editing mode or not.
 */
UmWidgetManager.handleWidgetListeners = (editMode = false) => {
    const umBody = $('body');
    umBody.off('click').off('change').off('Paste');
    umBody.on('click', event => {
        
        if(editMode){
            
            if($(event.target).hasClass("um-editable")){
                event.stopPropagation();
                tinymce.init(UmEditorCore.editorConfig);
            }else if($(event.target).hasClass("action-delete")){
                event.stopPropagation();
                UmWidgetManager.prototype.onWidgetDeleted(event);
            }else if($(event.target).hasClass("action-delete-inner")){
                UmWidgetManager.prototype.onWidgetChoiceDeleted(event);
            }else if($(event.target).hasClass("action-cut")){
                event.stopPropagation();
                UmWidgetManager.prototype.cutWidgetFromEditor(event);
            }else if($(event.target).hasClass("add-choice")){
                event.stopPropagation();
                UmMultipleChoiceWidget.prototype.addChoice(event);
            }else if($(event.target).hasClass("um-editor")){
                event.stopPropagation();
                UmEditorCore.prototype.setCursorToAnyEditableElement(null);
            }else if($(event.target).is("label")){
                event.stopPropagation();
                const focusEl = $($(event.target).closest("[data-um-widget]"))
                .find(".um-editable:first p:first-of-type");
                UmEditorCore.prototype.setCursorToAnyEditableElement(focusEl.get(0));
            }

            UmEditorCore.prototype.checkActivatedControls();

        }else{
            if($(event.target).hasClass("qn-retry")){
                event.stopPropagation();
                UmWidgetManager.prototype.onRetryButtonClicked(event);
            }
            
            if($(event.target).hasClass("fill-the-blanks-check")){
                if(!UmWidgetManager.isEditingMode){
                    event.stopPropagation();
                    UmFillTheBlanksWidget.prototype.onQuestionAnswerChecked(event);
                }
            }
            
            if($(event.target).hasClass("question-choice")
                || $(event.target).hasClass("question-choice-body")
                || $($(event.target).parent()).hasClass("question-choice-body")) {
                if (!UmWidgetManager.isEditingMode) {
                    event.stopPropagation();
                    UmMultipleChoiceWidget.prototype.onQuestionAnswerChecked(event);
                }
            }
        }
           
        });

    umBody.on("change", event => {
        if(editMode){
            if($(event.target).hasClass("question-choice-answer-select")){
                event.stopPropagation();
                UmMultipleChoiceWidget.prototype.onChoiceStateChange(event);
            }else if($(event.target).hasClass("question-retry-option-select")){
                event.stopPropagation();
                UmWidgetManager.prototype.onQuestionRetrySelectionChange(event);
            }
        }
    });
};

/** Add and remove editable class selector to be used by TinyMCE 
 * @param editEnabled Flag to indicate whether the editor is in editing mode or preview mode.
 *                    TRUE when in editing mode FALSE otherwise
*/
UmWidgetManager.handleEditableContent = (editEnabled = true) => {
    const editableElement = $(".um-editor").find(editableSectionsSelector);
    if(editEnabled){
        $(editableElement).addClass("um-editable");
    }else{
        $(editableElement).removeClass("um-editable");
    }
}


/** Switch editing mode on and starting adding some controls */
UmWidgetManager.prototype.switchEditingModeOn = function(){

    if(currentWidgetType != UmWidgetManager.WIDGET_NAME_EXTRA_CONTENT && 
        !UmWidgetManager.prototype.isEmpty(UmWidgetManager._placeholder)){
        $(this.widget).find("label , br").remove();
        $(this.widget).find(".question-body").removeClass("default-margin-bottom").before("<label class='um-labels' style='z-index: 3;'>" 
        + UmWidgetManager._placeholder.labelForQuestionBodyText + "</label><br/>");
        $(this.widget).find(".question-retry-btn").html("<button class='float-right qn-retry extra-btn' data-um-preview='support'>"
        + UmWidgetManager._placeholder.labelForTryAgainOptionBtn + "</button>");
        $(this.widget).find(".question").removeClass("default-padding").addClass("default-padding-top default-padding-bottom");
        $(this.widget).find('.question-answer').removeClass("show-element").addClass("hide-element");
        $(this.widget).find('.question-action-holder, .action-inner').removeClass("hide-element").addClass("show-element");
        
        if(UmWidgetManager.isNewWidget){
            UmWidgetManager.prototype.handleNewWidget(this.widget);
        }else{
            UmWidgetManager.prototype.handleExistingWidget(this.widget);
        }

        $(this.widget).find(".question-retry-option").html("" +
            "<select class='question-retry-option-select'>" +
            "  <option value=\"true\">" + UmWidgetManager._placeholder.labelForTrueOptionText + "</option>" +
            "  <option value=\"false\" selected=\"selected\">" + UmWidgetManager._placeholder.labelFalseOptionText + "</option>" +
            "</select>");

        $(this.widget).find(".question-retry-option")
            .before("<label class='um-labels no-left-padding'>" + UmWidgetManager._placeholder.labelForQuestionRetryOption + "</label><br/>");
        $(this.widget).find('div[class^="question"], .extra-content').each((index,widget)=>{
            if(!$(widget).attr("id")){
                $(widget).attr("id",UmWidgetManager.EXTRA_CONTENT_ID_TAG + UmWidgetManager.getNextUniqueId())
            }
        });
    }
    if(UmWidgetManager.prototype.isEmpty(UmWidgetManager._placeholder)){
        UmEditorCore.prototype.logUtil("switchEditingModeOn","Locale was not loaded successfully");
    }
    UmWidgetManager.handleEditableContent(true);
    UmWidgetManager.handleWidgetListeners(true);
}

/**
 * Switch on editing mode to the multiple choice question widget
 * @returns question widget with controls
 */
UmMultipleChoiceWidget.prototype.switchEditingModeOn = function(){
    UmWidgetManager.prototype.switchEditingModeOn.apply(this, arguments);
    UmWidgetManager.prototype.handleWidgetChoice(this.widget);
    $(this.widget).find("label").css("z-index","1");
    $(this.widget).find('.action-delete-inner').removeClass("hide-element").addClass("show-element");
    return this.widget;
};

/**
 * Switch on editing mode to the fill the blanks question widget
 * @returns question widget with controls
 */
UmFillTheBlanksWidget.prototype.switchEditingModeOn = function(){
    UmWidgetManager.prototype.switchEditingModeOn.apply(this, arguments);
    $(this.widget).find(".fill-blanks").removeClass("hide-element").addClass("show-element");
    $(this.widget).find(".question-choice-body").before("<label class='um-labels'>"
        + UmWidgetManager._placeholder.labelForFillTheBlanksAnswerBodyText + "</label>");
    $(this.widget).find(".input-group").before("<label class='um-labels '>"
        + UmWidgetManager._placeholder.labelForFillTheBlanksPromptInput + "</label>");
    $(this.widget).find(".question-choice-feedback-correct").before("<label class='um-labels'>"
        + UmWidgetManager._placeholder.labelForQuestionRightFeedbackText + "</label>");
    $(this.widget).find(".question-choice-feedback-wrong").before("<label class='um-labels'>"
        + UmWidgetManager._placeholder.labelForQuestionWrongFeedbackText + "</label>");
    return this.widget;
};

/**
 * Switch on editing mode to the extra content widget
 * @returns question widget with controls
 */
UmExtraContentWidget.prototype.switchEditingModeOn = function(){
    UmWidgetManager.prototype.switchEditingModeOn.apply(this, arguments);
    $(this.widget).removeClass("hide-element").addClass("show-element");
    return this.widget;
};


/**
 * Handle new question node 
 * @param widget newly widget loaded to the tinymce
*/
UmWidgetManager.prototype.handleNewWidget = (widget)=>{

    const choiceOrAnswerLabel = $(widget).attr("data-um-widget") === UmWidgetManager.WIDGET_NAME_MULTICHOICE ?
        UmWidgetManager._placeholder.placeholderForTheChoiceText : UmWidgetManager._placeholder.placeholderForTheAnswerText;

    $(widget).find(".question-body").html("<p>" + UmWidgetManager._placeholder.placeholderForTheQuestionText + "</p>");
    $(widget).find(".question-choice-body").html("<p>" + choiceOrAnswerLabel+"</p>");
    $(widget).find(".question-choice-feedback").html("<p>" + UmWidgetManager._placeholder.placeholderForTheChoiceFeedback + "</p>");
    $(widget).find(".question-choice-feedback-correct").html("<p>" + UmWidgetManager._placeholder.placeholderForTheRightChoiceFeedback + "</p>");
    $(widget).find(".question-choice-feedback-wrong").html("<p>" + UmWidgetManager._placeholder.placeholderForTheWrongChoiceFeedback + "</p>");
    $(widget).find(".fill-the-blanks-check").text(UmWidgetManager._placeholder.labelForCheckAnswerInputPromptBtn);
    $(widget).find(".fill-the-blanks-input").attr("placeholder",UmWidgetManager._placeholder.placeholderForTheBlanksInput);
};

/** 
 * Handle existing question node 
 * @param widget existing widget loaded to the tinymce
 * */
UmWidgetManager.prototype.handleExistingWidget = (widget) => {
    $(widget).find(".question-choice").removeClass("question-choice-pointer no-padding choice-no-margin-top selected-choice alert alert-secondary").addClass("default-margin-top");
    $(widget).find('[data-um-preview="main"]').removeClass("preview-main default-margin-top");
    $(widget).find('[data-um-preview="alert"]').removeClass("preview-alert default-margin-top");
    $(widget).find('[data-um-preview="support"]').removeClass("preview-support default-margin-top");
    $(widget).find(".default-theme").addClass("no-padding");
    $(widget).find(".question-feedback-container").addClass("hide-element").removeClass("show-element");
    $(widget).removeClass("um-card default-margin-bottom default-padding-top");
    $(widget).find(".multi-choice").removeClass("default-margin-top").addClass("default-margin-bottom");
    $(widget).find(".question-answer").removeClass("default-margin-bottom");
    $(widget).find("p.pg-break, .question-choice-answer , .question-choice-feedback,.fill-blanks,.select-option , .question-action-holder")
    .removeClass("hide-element").addClass("show-element");
};

/**
 * Handle question choices 
 * @param widget loaded widget
 * */
UmWidgetManager.prototype.handleWidgetChoice = (widget) => {
    $(widget).find(".question-add-choice").removeClass("hide-element").addClass("show-element")
        .html("<button class='float-right dont-remove add-choice default-margin-top extra-btn'>" 
        + UmWidgetManager._placeholder.labelForAddChoiceBtn + "</button>");
    $(widget).find(".question-choice-body").before("<label class='um-labels'>"
        + UmWidgetManager._placeholder.labelForChoiceBodyText + "</label>");
    $(widget).find(".question-choice-feedback").before("<label class='um-labels'>"
        + UmWidgetManager._placeholder.labelForFeedbackBodyText + "</label>");
    $(widget).find(".question-choice-answer").before("<label class='um-labels'>"
        + UmWidgetManager._placeholder.labelForRightAnswerOption + "</label>");
    $(widget).find(".question-choice-answer").html("" +
        "<select class='question-choice-answer-select'>" +
        "  <option value=\"true\">" + UmWidgetManager._placeholder.labelForTrueOptionText + "</option>" +
        "  <option value=\"false\" selected=\"selected\">" + UmWidgetManager._placeholder.labelFalseOptionText + "</option>" +
        "</select>");

};


/**
 * Action invoked when choice is added to the multiple choice question
 * @param event choice addition event object
 * @param testEnv flag to indicate test or live environment
 */
UmMultipleChoiceWidget.prototype.addChoice = function(event,testEnv = false){
    const choiceTemplateUrl = (testEnv ? "/":"") + questionTemplatesDir+"template-qn-choice.html";
    $.ajax({url: choiceTemplateUrl, success: (choice) => {
        choice = $(choice).attr("id",UmWidgetManager.EXTRA_CONTENT_ID_TAG+UmWidgetManager.getNextUniqueId());
        UmWidgetManager.prototype.handleNewWidget(choice);
        UmWidgetManager.prototype.handleWidgetChoice(choice);
        let widgetNode = $($(event.target).closest("div .question")).children();
        widgetNode = $(widgetNode.get(widgetNode.length - 4));
        widgetNode.after(choice);
        UmWidgetManager.handleEditableContent(true);
        UmWidgetManager.handleWidgetListeners(true);
        UmEditorCore.requestFocusFromElementWithId(choice);
    }});

};


/**
 * Action invoked when fill in the blanks question check button is clicked.
 * @param event check answer click event object
 */
UmMultipleChoiceWidget.prototype.onQuestionAnswerChecked = function(event){
    const choiceElement = $(event.target).closest("div .question-choice");
    const widgetNode = $(event.target).closest("div div.question");

    const allChoices = $(widgetNode).find("[data-um-correct]");
    for(let choice in allChoices){
        if(!allChoices.hasOwnProperty(choice))
            continue;
        const choiceNode = allChoices[choice];
        if($(choiceNode).hasClass("question-choice-pointer")){
            const isClicked = $(choiceNode).attr("id") === $(choiceElement).attr("id");
            if(isClicked){
                $(choiceNode).addClass("selected-choice");
            }else{
                $(choiceNode).removeClass("selected-choice");
            }
        }
    }
    const isCorrectChoice = choiceElement.attr("data-um-correct")==='true';
    const feedbackText = $(choiceElement).find(".question-choice-feedback").html();
    const feedbackContainer = $(widgetNode).find(".question-feedback-container");
    $(feedbackContainer).find(".question-feedback-container-text").html(feedbackText);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning");
    $(feedbackContainer).addClass((isCorrectChoice ? "alert-success":"alert-danger")+ " show-element");
    const canBeRetried = widgetNode.attr("data-um-retry")==='true';
    if(!isCorrectChoice && canBeRetried){
        $(widgetNode).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(widgetNode).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};

/**
 * Strip all white space on answers for comparison
 * @param value string to be manipulated
 * @returns plain text
 */
UmWidgetManager.removeSpaces = (value)=>{
    return value.replace(/(\r\n|\n|\r)/gm,"").replace(/\s/g, "");
};

/**
 * Action invoked when multiple choice question choice is clicked
 * @param event choice selection event object
 */
UmFillTheBlanksWidget.prototype.onQuestionAnswerChecked = (event) =>{
    const widgetNode = $(event.target).closest("div div.question");
    const choiceElement = $(widgetNode).find(".fill-blanks");
    const wrongChoiceText = $(choiceElement).find(".question-choice-feedback-wrong").html();
    const correctChoiceText = $(choiceElement).find(".question-choice-feedback-correct").html();
    let defaultAnswerText = $(choiceElement).find(".question-choice-body").text().toLowerCase();
    let userAnswerText = $(widgetNode).find(".fill-the-blanks-input").val().toLowerCase();
    const feedbackContainer = $(widgetNode).find(".question-feedback-container");
    userAnswerText = UmWidgetManager.removeSpaces(userAnswerText);
    defaultAnswerText = UmWidgetManager.removeSpaces(defaultAnswerText);

    const isCorrectChoice = defaultAnswerText === userAnswerText;
    const message = isCorrectChoice ? correctChoiceText: wrongChoiceText;
    $(feedbackContainer).find(".question-feedback-container-text").html(message);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning alert-info");
    $(feedbackContainer).addClass((isCorrectChoice ? "alert-success":"alert-danger") + " show-element");
    const canBeRetried = widgetNode.attr("data-um-retry")==='true';
    if((!isCorrectChoice && canBeRetried) || userAnswerText.length <= 0){
        $(widgetNode).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(widgetNode).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};

/**
 * Action invoked when correct answer choice value changed
 * @param event Correct answer value change event object
 */
UmMultipleChoiceWidget.prototype.onChoiceStateChange = (event) => {
    $(event.target).closest("div .question-choice").attr("data-um-correct",$(event.target).val());
};

/**
 * Action invoked when question retry value changed changed (When deciding whether the question can be retried or not)
 * @param event question retry value change event object
 */
UmWidgetManager.prototype.onQuestionRetrySelectionChange = (event) => {
    const widgetNode = $(event.target).closest("div div.question");
    const canBeRetried = $(event.target).val() === 'true';
    $(widgetNode).attr("data-um-retry",canBeRetried);
    $(this.widget).find("br").remove();
};

/**
 * Action invoked when question retry button is clicked
 * @param event question answer retry event object
 */
UmWidgetManager.prototype.onRetryButtonClicked = (event) => {
    const widgetNode = $(event.target).closest("div.question");
    $(widgetNode).find(".question-choice-pointer").removeClass("selected-choice");
    $(widgetNode).find(".question-feedback-container").removeClass("show-element").addClass("hide-element");
    $(widgetNode).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
};

/**
 * Action invoked when question is deleted from the editor
 * @param event question delete event object
 */
UmWidgetManager.prototype.onWidgetDeleted = (event) => {
    const widgetNode = $(event.target).closest("div div.question");
    const extraOrEmptyContent = $(widgetNode).next();
    const innerParagraph = $(extraOrEmptyContent).children().get(0);
    if(UmWidgetManager.removeSpaces($(innerParagraph).text()).length === 0){
        $(extraOrEmptyContent).remove();
    }
    $(widgetNode).remove();
    const focusEl = $($.find(".um-editable:last p:last-of-type"));
    UmEditorCore.prototype.setCursorToAnyEditableElement(focusEl.get(0));
    UmEditorCore.prototype.scrollToElement(focusEl.get(0));
};

/**
 * Action invoked when widget choice is being deleted from the editor
 * @param event widget choice delete event object
 */
UmWidgetManager.prototype.onWidgetChoiceDeleted = (event) => {
    const questionChoice = $(event.target).closest("div div.question-choice");
    $(questionChoice).remove();
};

/**
 * Action invoked when widget is cut from the editor
 * @param event cut event object
 */
UmWidgetManager.prototype.cutWidgetFromEditor = event => {
    let widgetNode = $(event.target).closest("div div.question");
    $(widgetNode).select();
    widgetNode = widgetNode.get(0).outerHTML;
    UmWidgetManager.prototype.onWidgetDeleted(event);
    $(widgetNode).find("label").remove();
    $(widgetNode).find(".um-editable").removeClass(".um-editable");
    const clipboardContent = JSON.stringify({
        action:'onContentCut',
        directionality: '',
        content:UmEditorCore.base64Encode(widgetNode)}
        );
    try{
        UmEditor.handleJsCallbackValue(clipboardContent);
    }catch (e) {
        UmEditorCore.prototype.logUtil("onContentCut:",e);
    }
};

/**
 * Check if there is an empty extra content widget
 */
UmWidgetManager.prototype.isExtraContentEmpty = (content) =>{
    let contentData = "";
    $(content).children().each((index,childElement)=>{
        if($(childElement).is('p')){
           contentData = contentData + $(childElement).html().replace("&nbsp;","");
        }else{
            contentData = $(childElement)[0].nodeName;
        }
    });
    return contentData.length <= 0;
}


/** Check empty objects 
 * @param obj object to be checked
*/
UmWidgetManager.prototype.isEmpty = (obj) => {
    for(let key in obj) {
        if(obj.hasOwnProperty(key))
            return false;
    }
    return true;
}


/**
 * Generate random unique id for the question and choices.
 * @param idLength length of the unique ID to be generated
 * @returns generated unique id 
 */
UmWidgetManager.getNextUniqueId = (idLength = 16) => {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_';
    let questionId = '';
    for (let i = idLength; i > 0; --i) questionId += chars[Math.floor(Math.random() * chars.length)];
    return questionId;
};


/** 
 * Prepare content for preview 
 * @param content document content
*/
UmWidgetManager.preparingPreview = (content) => {
    let umdocument = $("<div/>").html(content);
    const elementToHideSelector = ".select-option , .fill-blanks, .question-choice-answer," +
        " .question-retry-btn, .question-choice-feedback , .pg-break, .action-delete-inner," +
        " .question-add-choice, .question-action-holder";
    const elementToShowSelector = ".question-answer";
    $(umdocument).find("br").remove();
    $(umdocument).find("label").remove();
    $(umdocument).find(".mce-content-body, .um-editable").removeClass("mce-content-body mce-edit-focus um-editable")
    $(umdocument).find(".pg-break").remove();
    $(umdocument).find(".add-choice").remove();
    $(umdocument).find('.question-choice').addClass("um-alert alert-secondary choice-no-margin-top question-choice-pointer")
    .removeClass("default-margin-top");
    $(umdocument).find('.multi-choice').addClass("default-margin-bottom").removeClass("default-margin-top");
    $(umdocument).find('.default-theme').removeClass('no-padding');
    $(umdocument).find('.question-body').addClass("default-margin-bottom no-left-padding");
    $(umdocument).find('.question-answer').addClass("no-padding no-left-padding default-margin-bottom");
    $(umdocument).find('.fill-answer-inputs').addClass("no-padding");
    $(umdocument).find('[data-um-preview="main"]').addClass('preview-main default-margin-top');
    $(umdocument).find('[contenteditable="true"]').removeAttr('contenteditable').removeAttr("spellcheck");
    $(umdocument).find('[data-um-preview="alert"]').addClass('preview-alert default-margin-top');
    $(umdocument).find('[data-um-preview="support"]').addClass('preview-support default-margin-top');
    $(umdocument).find('.question')
    .removeClass("um-editable default-padding-top default-padding-bottom")
    .addClass('um-card default-padding');
    $(umdocument).find(elementToHideSelector).removeClass("show-element").addClass("hide-element");
    $(umdocument).find(elementToShowSelector).removeClass("hide-element").addClass("show-element");

    //hide all empty extra content widget on preview
    $(umdocument).find(".extra-content").each((index,widget) => {
        if(UmWidgetManager.prototype.isExtraContentEmpty(widget)){
            $(widget).removeClass("show-element").addClass("hide-element");
        }
    });
    umdocument = $('<div/>').html(umdocument).contents().html();
    return umdocument;
};