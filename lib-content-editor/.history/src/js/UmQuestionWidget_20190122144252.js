/**
 * Class which handle all widgets on the tinymce editor
 * @param element node element in the editor
 * @constructor
 *
 * @author kileha3
 */
let UmQuestionWidget = function(element) {
    this.element =  $.parseHTML(element);
};

/** Default page break element to be used when new node is added */
UmQuestionWidget.PAGE_BREAK = '<p style="page-break-before: always" class="pg-break">';

/** Content widget which hold all non question content in the editor */
UmQuestionWidget.EXTRA_CONTENT_WIDGET = '<div data-um-widget="content" class="um-row col-sm-12 col-md-12 col-lg-12 default-margin-top extra-content"><p></p></div>' +
    UmQuestionWidget.PAGE_BREAK;
/** Question id prefix */
UmQuestionWidget.QUESTION_ID_TAG = "id-question-";

/** Other editor node id prefix */
UmQuestionWidget.ELEMENT_ID_TAG = "id-";

/** Map to store widget type when editing */
UmQuestionWidget._widgets = {};

/** Object to store locale details */
UmQuestionWidget._locale = {};

/** Object to store widget listeners reference */
UmQuestionWidget._widgetListeners = {};

/** Flag which indicate if the question is new or existing one */
UmQuestionWidget.isNewQuestion = false;

/** Flag which indicate editor mode */
UmQuestionWidget.isEditingMode = false;

/** Flag which used to identify widget type multi choice question */
UmQuestionWidget.WIDGET_NAME_MULTICHOICE = "multi-choice";

/** Flag which used to identify widget type fill in the blanks question */
UmQuestionWidget.WIDGET_NAME_FILL_BLANKS = "fill-the-blanks";

/** Flag which used to identify all non question content widget */
UmQuestionWidget.WIDGET_NAME_OTHER_CONTENT = "content";

UmQuestionWidget.isChoiceAdded = false;

/** Holds clipboard value after cut action */
UmQuestionWidget.CLIPBOARD_CONTENT = null;

let UmFillTheBlanksQuestionWidget = function(element){UmQuestionWidget.apply(this, arguments);};

let UmMultiChoiceQuestionWidget = function(element){UmQuestionWidget.apply(this, arguments);};

let UmOtherContentWidget = function(element){UmQuestionWidget.apply(this, arguments);};

UmMultiChoiceQuestionWidget.prototype = Object.create(UmQuestionWidget.prototype);

UmFillTheBlanksQuestionWidget.prototype = Object.create(UmQuestionWidget.prototype);

UmOtherContentWidget.prototype = Object.create(UmQuestionWidget.prototype);

/** Set question status
 * @param isNewQuestion TRUE if new question is inserted otherwise it exists.
 */
UmQuestionWidget.setQuestionStatus = (isNewQuestion) =>{
    UmQuestionWidget.isNewQuestion = isNewQuestion;
};

/** Set editor mode 
 * @param isEditingMode TRUE if the editor is activated otherwise editor is not activated.
*/
UmQuestionWidget.setEditingMode = (isEditingMode) => {
    UmQuestionWidget.isEditingMode = isEditingMode;
};

/** Check empty objects */
UmQuestionWidget.prototype.isEmpty = (obj) =>{
    for(let key in obj) {
        if(obj.hasOwnProperty(key))
            return false;
    }
    return true;
}

/**
 * Set default placeholders based on selected locale
 */
UmQuestionWidget.loadPlaceholders = (locale, isTest = false) => {
    if(UmQuestionWidget.prototype.isEmpty(UmQuestionWidget._locale)){
        const localeFileUrl = (isTest ? "/":"") + languageLocaleDir+"locale."+locale+".json";
    $.ajax({url: localeFileUrl, success: (localeFileContent) => {
            UmQuestionWidget._locale = localeFileContent;
        },error:() => {
            if(locale !== "en"){
                UmQuestionWidget.loadPlaceholders("en",isTest);
            }
    }});
    }
};


/**
 * Create html version for stand alone document preview which will run without tinymce.
 * @param content tinymce editor content
 * @returns {string} generated html content in base 64 format
 */
UmQuestionWidget.saveContentEditor = (content) => {
    let editorContent = $("<div/>").html($.parseHTML(content));
    const elementToHideSelector = ".select-option , .fill-blanks, .question-choice-answer," +
        " .question-retry-btn, .question-choice-feedback , .pg-break, .action-delete-inner," +
        " .question-add-choice, .question-action-holder";
    const elementToShowSelector = ".question-answer";
    $(editorContent).find("br").remove();
    $(editorContent).find("label").remove();
    $(editorContent).find("p.pg-break").remove();
    $(editorContent).find("button.add-choice").remove();
    $(editorContent).find('div.question-choice').addClass("question-choice-pointer").removeClass("default-margin-top");
    $(editorContent).find('div.multi-choice').addClass("default-margin-bottom").removeClass("default-margin-top");
    $(editorContent).find('div.question-choice').addClass('alert alert-secondary');
    $(editorContent).find('.default-theme').removeClass('no-padding');
    $(editorContent).find('.question-body').addClass("default-margin-bottom no-left-padding");
    $(editorContent).find('.question-answer').addClass("no-padding no-left-padding default-margin-bottom");
    $(editorContent).find('.fill-answer-inputs').addClass("no-padding");
    $(editorContent).find('[data-um-preview="main"]').addClass('preview-main default-margin-top');
    $(editorContent).find('[data-um-preview="alert"]').addClass('preview-alert default-margin-top');
    $(editorContent).find('[data-um-preview="support"]').addClass('preview-support default-margin-top');
    $(editorContent).find('div.question').removeClass("default-padding-top default-padding-bottom").addClass('card default-padding');
    $(editorContent).find(elementToHideSelector).removeClass("show-element").addClass("hide-element");
    $(editorContent).find(elementToShowSelector).removeClass("hide-element").addClass("show-element");

    //remove all empty extra content widget on preview
    $(editorContent).find(".extra-content").each((index,widget) => {
        if(UmQuestionWidget.prototype.isExtraContentEmpty(widget)){
            $(widget).removeClass("show-element").addClass("hide-element");
        }
    });
    editorContent = $('<div/>').html(editorContent).contents().html();
    return UmContentEditorCore.base64Encode(editorContent);
};

/**
 * Check if there is an empty extra content widget
 */
UmQuestionWidget.prototype.isExtraContentEmpty = (content) =>{
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

/**
 * Check if the selected text is one of the labels defined
 * @param selectedText text from selected section
 * @returns {boolean} True is among label list otherwise false
 */
UmQuestionWidget.isLabelText = (selectedText) => {
    let isLabel = false;
    for (let placeholder in UmQuestionWidget._locale.placeholders) {
        if (UmQuestionWidget._locale.placeholders.hasOwnProperty(placeholder)) {
            isLabel = UmQuestionWidget.removeSpaces(UmQuestionWidget._locale.placeholders[placeholder].toLocaleLowerCase())
                === UmQuestionWidget.removeSpaces(selectedText).toLocaleLowerCase();
            if(isLabel){
                break;
            }
        }
    }
    return isLabel;
};

/**
 * Genrate random unique id for the question and choices.
 * @param idLength
 * @returns {string}
 */
UmQuestionWidget.getNextUniqueId = (idLength = 16) => {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_';
    let questionId = '';
    for (let i = idLength; i > 0; --i) questionId += chars[Math.floor(Math.random() * chars.length)];
    return questionId;
};

/**
 * Manage question and other plain text nodes in the editor.
 * @param editorNode any html blocked added to the editor
 */
UmQuestionWidget.handleWidgetNode =  (editorNode) => {
    const questionId =  $(editorNode).attr("id");
    if(!UmQuestionWidget._widgets[questionId]) {
        const widgetType = $(editorNode).attr("data-um-widget");
        switch(widgetType) {
            case UmQuestionWidget.WIDGET_NAME_MULTICHOICE:
                UmQuestionWidget._widgets[questionId] = new UmMultiChoiceQuestionWidget(editorNode);
                break;
            case UmQuestionWidget.WIDGET_NAME_FILL_BLANKS:
                UmQuestionWidget._widgets[questionId] = new UmFillTheBlanksQuestionWidget(editorNode);
                break;
            case UmQuestionWidget.WIDGET_NAME_OTHER_CONTENT:
                UmQuestionWidget._widgets[questionId] = new UmOtherContentWidget(editorNode);
                break;
        }
    }
    return UmQuestionWidget._widgets[questionId];
};


/**
 * Switch on editing mode to the question widget
 * @returns question widget with controls
 */
UmQuestionWidget.prototype.startEditing = function() {
    $(this.element).find("label").remove();
    $(this.element).find(".question-action-holder").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-body").removeClass("default-margin-bottom").before("<label class='um-labels' style='z-index: 3;'>"+UmQuestionWidget._locale.placeholders.labelForQuestionBodyText+"</label><br/>");
    $(this.element).find(".question-retry-btn").html("<button class='float-right qn-retry extra-btn' data-um-preview='support'>"+UmQuestionWidget._locale.placeholders.labelForTryAgainOptionBtn+"</button>");
    $(this.element).find(".question").removeClass("default-padding").addClass("default-padding-top default-padding-bottom");
    $(this.element).find('.question-action-holder').removeClass("hide-element").addClass("show-element");
    $(this.element).find('.action-inner').removeClass("hide-element").addClass("show-element");
    $(this.element).find('.question-answer').removeClass("show-element").addClass("hide-element");
    if(UmQuestionWidget.isNewQuestion){
            UmQuestionWidget.prototype.handleNewQuestionNode(this.element);
        }else{
            UmQuestionWidget.prototype.handleExistingQuestionNode(this.element);
    }

    $(this.element).find(".question-retry-option").html("" +
        "<select class='question-retry-option-select'>" +
        "  <option value=\"true\">"+UmQuestionWidget._locale.placeholders.labelForTrueOptionText+"</option>" +
        "  <option value=\"false\" selected=\"selected\">"+UmQuestionWidget._locale.placeholders.labelFalseOptionText+"</option>" +
        "</select>");

    $(this.element).find(".question-retry-option")
        .before("<label class='um-labels no-left-padding'>"+UmQuestionWidget._locale.placeholders.labelForQuestionRetryOption+"</label><br/>");
    $(this.element).find('div[class^="question"], .extra-content').each((index,element)=>{
        if(!$(element).attr("id")){
            $(element).attr("id",UmQuestionWidget.ELEMENT_ID_TAG+UmQuestionWidget.getNextUniqueId())
        }
    });
    return this.element;
};

/**Handle new question node */
UmQuestionWidget.prototype.handleNewQuestionNode = (element)=>{
    const choiceOrAnswerLabel = $(element).attr("data-um-widget") === UmQuestionWidget.WIDGET_NAME_MULTICHOICE ?
        UmQuestionWidget._locale.placeholders.placeholderForTheChoiceText:UmQuestionWidget._locale.placeholders.placeholderForTheAnswerText;

    $(element).find(".question-body").html("<p>"+UmQuestionWidget._locale.placeholders.placeholderForTheQuestionText+"</p>");
    $(element).find(".question-choice-body").html("<p>"+choiceOrAnswerLabel+"</p>");
    $(element).find(".question-choice-feedback").html("<p>"+UmQuestionWidget._locale.placeholders.placeholderForTheChoiceFeedback+"</p>");
    $(element).find(".question-choice-feedback-correct").html("<p>"+UmQuestionWidget._locale.placeholders.placeholderForTheRightChoiceFeedback+"</p>");
    $(element).find(".question-choice-feedback-wrong").html("<p>"+UmQuestionWidget._locale.placeholders.placeholderForTheWrongChoiceFeedback+"</p>");
    $(element).find(".fill-the-blanks-check").text(UmQuestionWidget._locale.placeholders.labelForCheckAnswerInputPromptBtn);
    $(element).find(".fill-the-blanks-input").attr("placeholder",UmQuestionWidget._locale.placeholders.placeholderForTheBlanksInput);
};

/** Handle existing question node */
UmQuestionWidget.prototype.handleExistingQuestionNode = (element) => {
    $(element).find(".question-choice").removeClass("question-choice-pointer selected-choice alert alert-secondary").addClass("default-margin-top");
    $(element).find('[data-um-preview="main"]').removeClass("preview-main default-margin-top");
    $(element).find('[data-um-preview="alert"]').removeClass("preview-alert default-margin-top");
    $(element).find('[data-um-preview="support"]').removeClass("preview-support default-margin-top");
    $(element).find(".default-theme").addClass("no-padding");
    $(element).find(".question-feedback-container").addClass("hide-element").removeClass("show-element");
    $(element).removeClass("card default-margin-bottom default-padding-top");
    $(element).find(".multi-choice").removeClass("default-margin-top").addClass("default-margin-bottom");
    $(element).find(".question-answer").removeClass("default-margin-bottom");
    $(element).find("p.pg-break, .question-choice-answer , .question-choice-feedback, .fill-blanks,.select-option , .question-action-holder")
    .addClass("show-element").removeClass("hide-element");
};

/**Handle question choices */
UmQuestionWidget.prototype.handleQuestionChoice = (element) => {
    $(element).find(".question-add-choice").removeClass("hide-element").addClass("show-element")
        .html("<button class='float-right add-choice default-margin-top extra-btn'>" +UmQuestionWidget._locale.placeholders.labelForAddChoiceBtn+"</button>");
    $(element).find(".question-choice-body").before("<label class='um-labels'>"
        +UmQuestionWidget._locale.placeholders.labelForChoiceBodyText+"</label>");
    $(element).find(".question-choice-feedback").before("<label class='um-labels'>"
        +UmQuestionWidget._locale.placeholders.labelForFeedbackBodyText+"</label>");
    $(element).find(".question-choice-answer").before("<label class='um-labels'>"
        +UmQuestionWidget._locale.placeholders.labelForRightAnswerOption+"</label>");
    $(element).find(".question-choice-answer").html("" +
        "<select class='question-choice-answer-select'>" +
        "  <option value=\"true\">"+UmQuestionWidget._locale.placeholders.labelForTrueOptionText+"</option>" +
        "  <option value=\"false\" selected=\"selected\">"+UmQuestionWidget._locale.placeholders.labelFalseOptionText+"</option>" +
        "</select>");

};

/** Handle immutable nodes - labels */
UmQuestionWidget.handleImmutableContent = () => {
    $(document).find(".um-labels").addClass("immutable-content");
};

/**
 * Switch on editing mode to the other widget
 * @returns question widget with controls
 */
UmOtherContentWidget.prototype.startEditing = function() {
    UmQuestionWidget.prototype.startEditing.apply(this, arguments);
};

/**
 * Switch on editing mode to the multiple choice question widget
 * @returns question widget with controls
 */
UmMultiChoiceQuestionWidget.prototype.startEditing = function(){
    UmQuestionWidget.prototype.startEditing.apply(this, arguments);
    UmQuestionWidget.prototype.handleQuestionChoice(this.element);
    $(this.element).find("label").css("z-index","1");
    return this.element;
};

/**
 * Switch on editing mode to the fill the blanks question widget
 * @returns question widget with controls
 */
UmFillTheBlanksQuestionWidget.prototype.startEditing = function(){
    UmQuestionWidget.prototype.startEditing.apply(this, arguments);
    $(this.element).find(".fill-blanks").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>"
        +UmQuestionWidget._locale.placeholders.labelForFillTheBlanksAnswerBodyText+"</label>");
    $(this.element).find(".input-group").before("<label class='um-labels '>"
        +UmQuestionWidget._locale.placeholders.labelForFillTheBlanksPromptInput+"</label>");
    $(this.element).find(".question-choice-feedback-correct").before("<label class='um-labels'>"
        +UmQuestionWidget._locale.placeholders.labelForQuestionRightFeedbackText+"</label>");
    $(this.element).find(".question-choice-feedback-wrong").before("<label class='um-labels'>"
        +UmQuestionWidget._locale.placeholders.labelForQuestionWrongFeedbackText+"</label>");
    return this.element;
};

/**
 * Switch on editing mode to the extra content widget
 * @returns question widget with controls
 */
UmOtherContentWidget.prototype.startEditing = function(){
    UmQuestionWidget.prototype.startEditing.apply(this, arguments);
    $(this.element).find(".extra-content").removeClass("hide-element").addClass("show-element");
    return this.element;
};

/**
 * Handle all document editing and previewing events
 */
UmQuestionWidget.handleWidgetListeners = () => {
    const bodySelector = $('body');
    bodySelector.off('click').off('change');
    bodySelector.on('click', event => {
        if($(event.target).hasClass("qn-retry")){
            UmQuestionWidget.prototype.onQuestionRetryButtonClicked(event);
        }else if($(event.target).hasClass("fill-the-blanks-check")){
            if(!UmQuestionWidget.isEditingMode){
                UmFillTheBlanksQuestionWidget.prototype.onQuestionAnswerChecked(event);
            }
        }else if($(event.target).hasClass("add-choice")){
            UmMultiChoiceQuestionWidget.prototype.addChoice(event);
        }else if($(event.target).hasClass("action-delete")){
            UmQuestionWidget.prototype.onQuestionDeletion(event);
        }else if($(event.target).hasClass("action-delete-inner")){
            UmQuestionWidget.prototype.onQuestionChoiceDeletion(event);
        }else if($(event.target).hasClass("action-cut")){
            UmQuestionWidget.prototype.onQuestionCut(event);
        }else if($(event.target).hasClass("question-choice")
            || $(event.target).hasClass("question-choice-body")
            || $($(event.target).parent()).hasClass("question-choice-body")) {
            if (!UmQuestionWidget.isEditingMode) {
                UmMultiChoiceQuestionWidget.prototype.onQuestionAnswerChecked(event);
            }
        }
    });

    //Option change events
    bodySelector.on("change", event => {
        if($(event.target).hasClass("question-choice-answer-select")){
           UmMultiChoiceQuestionWidget.prototype.onChoiceStateChange(event);
        }else if($(event.target).hasClass("question-retry-option-select")){
            UmQuestionWidget.prototype.onQuestionRetrySelectionChange(event);
        }

    });
};

/**
 * Action invoked when choice is added to the multiple choice question
 * @param event choice addition event object
 * @param isTest
 */
UmMultiChoiceQuestionWidget.prototype.addChoice = function(event,isTest = false){
    UmQuestionWidget.isChoiceAdded = true;
    const choiceTemplateUrl = (isTest ? "/":"") + questionTemplatesDir+"template-qn-choice.html";
    $.ajax({url: choiceTemplateUrl, success: (choice) => {
        choice = $(choice).attr("id",UmQuestionWidget.ELEMENT_ID_TAG+UmQuestionWidget.getNextUniqueId());
        UmQuestionWidget.prototype.handleNewQuestionNode(choice);
        UmQuestionWidget.prototype.handleQuestionChoice(choice);
        let questionElement = $($(event.target).closest("div .question")).children();
        questionElement = $(questionElement.get(questionElement.length - 4));
        questionElement.after(choice);
        UmQuestionWidget.handleWidgetListeners();
    }});

};


/**
 * Action invoked when fill in the blanks question check button is clicked.
 * @param event check answer click event object
 */
UmMultiChoiceQuestionWidget.prototype.onQuestionAnswerChecked = function(event){
    const choiceElement = $(event.target).closest("div .question-choice");
    const questionElement = $(event.target).closest("div div.question");

    const allChoices = $(questionElement).find("[data-um-correct]");
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
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    $(feedbackContainer).find(".question-feedback-container-text").html(feedbackText);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning");
    $(feedbackContainer).addClass((isCorrectChoice ? "alert-success":"alert-danger")+ " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if(!isCorrectChoice && canBeRetried){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};

/**
 * Strip all white space on answers for comparison
 * @param value string to be manipulated
 * @returns plain text
 */
UmQuestionWidget.removeSpaces = (value)=>{
    return value.replace(/(\r\n|\n|\r)/gm,"").replace(/\s/g, "");
};

/**
 * Action invoked when multiple choice question choice is clicked
 * @param event choice selection event object
 */
UmFillTheBlanksQuestionWidget.prototype.onQuestionAnswerChecked = (event) =>{
    const questionElement = $(event.target).closest("div div.question");
    const choiceElement = $(questionElement).find(".fill-blanks");
    const wrongChoiceText = $(choiceElement).find(".question-choice-feedback-wrong").html();
    const correctChoiceText = $(choiceElement).find(".question-choice-feedback-correct").html();
    let defaultAnswerText = $(choiceElement).find(".question-choice-body").text().toLowerCase();
    let userAnswerText = $(questionElement).find(".fill-the-blanks-input").val().toLowerCase();
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    userAnswerText = UmQuestionWidget.removeSpaces(userAnswerText);
    defaultAnswerText = UmQuestionWidget.removeSpaces(defaultAnswerText);

    const isCorrectChoice = defaultAnswerText === userAnswerText;
    const message = isCorrectChoice ? correctChoiceText: wrongChoiceText;
    $(feedbackContainer).find(".question-feedback-container-text").html(message);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning alert-info");
    $(feedbackContainer).addClass((isCorrectChoice ? "alert-success":"alert-danger") + " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if((!isCorrectChoice && canBeRetried) || userAnswerText.length <= 0){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};

/**
 * Action invoked when correct answer choice value changed
 * @param event Correct answer value change event object
 */
UmMultiChoiceQuestionWidget.prototype.onChoiceStateChange = (event) => {
    $(event.target).closest("div .question-choice").attr("data-um-correct",$(event.target).val());
};

/**
 * Action invoked when question retry value changed changed (When deciding whether the question can be retried or not)
 * @param event question retry value change event object
 */
UmQuestionWidget.prototype.onQuestionRetrySelectionChange = (event) => {
    const questionElement = $(event.target).closest("div div.question");
    const canBeRetried = $(event.target).val() === 'true';
    $(questionElement).attr("data-um-retry",canBeRetried);
    $(this.element).find("br").remove();
};

/**
 * Action invoked when question retry button is clicked
 * @param event question answer retry event object
 */
UmQuestionWidget.prototype.onQuestionRetryButtonClicked = (event) => {
    const questionElement = $(event.target).closest("div.question");
    $(questionElement).find(".question-choice-pointer").removeClass("selected-choice");
    $(questionElement).find(".question-feedback-container").removeClass("show-element").addClass("hide-element");
    $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
};

/**
 * Action invoked when question is deleted from the editor
 * @param event question delete event object
 */
UmQuestionWidget.prototype.onQuestionDeletion = (event) => {
    isDeleteOrCutAction = true;
    const questionElement = $(event.target).closest("div div.question");
    const extraOrEmptyContent = $(questionElement).next();
    const innerParagraph = $(extraOrEmptyContent).children().get(0);
    if(UmQuestionWidget.removeSpaces($(innerParagraph).text()).length === 0){
        $(extraOrEmptyContent).remove();
    }
    $(questionElement).remove();
    setTimeout(() => {
        isDeleteOrCutAction = false;
    }, averageEventTimeout);
};

/**
 * Action invoked when question choice is being deleted from the editor
 * @param event question choice delete event object
 */
UmQuestionWidget.prototype.onQuestionChoiceDeletion = (event) => {
    isDeleteOrCutAction = true;
    const questionChoice = $(event.target).closest("div div.question-choice");
    $(questionChoice).remove();
    setTimeout(() => {
        isDeleteOrCutAction = false;
    }, averageEventTimeout);
};

/**
 * Action invoked when question is cut from the editor
 * @param event cut event object
 */
UmQuestionWidget.prototype.onQuestionCut = event => {
    isDeleteOrCutAction = true;
    let questionElement = $(event.target).closest("div div.question");
    $(questionElement).select();
    questionElement = questionElement.get(0).outerHTML;
    UmQuestionWidget.prototype.onQuestionDeletion(event);

    const clipboardContent = JSON.stringify({action:'onContentCut', content:UmContentEditorCore.base64Encode(questionElement)});
    setTimeout(() => {
        isDeleteOrCutAction = false;
    }, averageEventTimeout);
    try{
        UmContentEditor.onContentCut(clipboardContent);
    }catch (e) {
        console.log("onContentCut:",e);
    }
};

