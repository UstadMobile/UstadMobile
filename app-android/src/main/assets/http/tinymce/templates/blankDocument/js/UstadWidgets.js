
let QuestionWidget = function(element) {
    this.element =  $.parseHTML(element);
};
QuestionWidget._widgets = {};
QuestionWidget._widgetListeners = {};
QuestionWidget.WIDGET_NAME_MULTICHOICE = "multi-choice";
QuestionWidget.WIDGET_NAME_FILL_BLANKS = "fill-the-blanks";
QuestionWidget.isNewQuestion = false;
QuestionWidget.setNewQuestion = function(newQuestion){
    QuestionWidget.isNewQuestion = newQuestion === "true";
};

/**
 * English constant placeholders
 * */
QuestionWidget.PLACEHOLDERS_LABELS = {
    labelForQuestionBodyText: 'Question text',
    labelForAddChoiceBtn: 'Add choice',
    labelForChoiceBodyText: 'Choice text',
    labelForFeedbackBodyText: 'Feedback text',
    labelForQuestionRetryOption: 'Can this be retried?',
    labelForRightAnswerOption: 'Is this the right answer?',
    labelForFillTheBlanksAnswerBodyText: 'Question answer',
    labelForFillTheBlanksPromptInput: 'Input answer',
    labelForQuestionRightFeedbackText: 'Right input feedback',
    labelForQuestionWrongFeedbackText: 'Wrong input feedback',
    labelForTrueOptionText:'Yes',
    labelFalseOptionText: 'No',
    labelForCheckAnswerInputPromptBtn: 'Check answer',
    labelForTryAgainOptionBtn:'Try again',
    placeholderForTheQuestionText: 'Placeholder text for the question body',
    placeholderForTheChoiceText: 'Placeholder text for the choice body',
    placeholderForTheBlanksInput: 'Type answer here',
    placeholderForTheChoiceFeedback: 'Feedback placeholder text',
    placeholderForTheRightChoiceFeedback: 'Feedback placeholder text for the right choice',
    placeholderForTheWrongChoiceFeedback: 'Feedback placeholder text for the wrong choice',
    warningOnSubmitEmptyField: '<b>Whoops!</b> Please type your answer before you press submit',
};

QuestionWidget.prototype.init = function() {};

QuestionWidget.prototype.editOn = function(){

    tinymce.activeEditor.dom.removeClass(tinymce.activeEditor.dom.select('div.question'),'card default-margin-bottom default-padding-top');

    $(this.element).find("label").remove();
    $(this.element).find(".question-retry-option")
        .before("<label class='um-labels col-sm-12 col-lg-10 no-left-padding'>"+QuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRetryOption+"</label><br/>");
    $(this.element).find(".question-retry-option").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".pg-break").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".btn-delete").removeClass("hide-element").addClass("show-element").html(' <span aria-hidden="true">&times;</span>');
    $(this.element).find(".question-answer").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-choice-answer").removeClass("hide-element").addClass("show-element");
    $(this.element).find('[data-um-preview="main"]').removeClass("preview-main default-margin-top");
    $(this.element).find('[data-um-preview="alert"]').removeClass("preview-alert default-margin-top");
    $(this.element).find('[data-um-preview="support"]').removeClass("preview-support default-margin-top");
    $(this.element).find(".question-body").removeClass("default-margin-bottom").before("<label class='um-labels'>"+QuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionBodyText+"</label><br/>");
    if(QuestionWidget.isNewQuestion){
        $(this.element).find(".question-body").html(QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheQuestionText);
        $(this.element).find(".question-choice-body").html(QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceText);
        $(this.element).find(".question-choice-feedback").html(QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceFeedback);
        $(this.element).find(".question-choice-feedback-correct").html(QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheRightChoiceFeedback);
        $(this.element).find(".question-choice-feedback-wrong").html(QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheWrongChoiceFeedback);
        $(this.element).find(".fill-the-blanks-check").text(QuestionWidget.PLACEHOLDERS_LABELS.labelForCheckAnswerInputPromptBtn);
        $(this.element).find(".qn-retry").text(QuestionWidget.PLACEHOLDERS_LABELS.labelForTryAgainOptionBtn);
        $(this.element).find(".fill-the-blanks-input").attr("placeholder",QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheBlanksInput);
    }

    $(this.element).find(".question-retry-option").html("" +
        "<select>" +
        "  <option value=\"true\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText+"</option>" +
        "  <option value=\"false\" selected=\"selected\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelFalseOptionText+"</option>" +
        "</select>");
    $(this.element).find(".question-choice-answer").html("" +
        "<select>" +
        "  <option value=\"true\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText+"</option>" +
        "  <option value=\"false\" selected=\"selected\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelFalseOptionText+"</option>" +
        "</select>");

    return this.element;
};

QuestionWidget.prototype.addListeners = function(){
    $(".question-retry-option select").on("change",this.setRetryOption.bind(this));

};


/**
 * Get node by ID from the Editor question DOM
 * @param id node - question element
 * @returns {*}
 */
QuestionWidget.getById = function(id) {
    if(!QuestionWidget._widgets[id]) {
        const widgetElement = document.getElementById(id);
        const widgetType = widgetElement.getAttribute("data-um-widget");
        switch(widgetType) {
            case MultiChoiceQuestionWidget.WIDGET_NAME_MULTICHOICE:
                QuestionWidget._widgets[id] = new MultiChoiceQuestionWidget(widgetElement);
                break;
            case MultiChoiceQuestionWidget.WIDGET_NAME_FILL_BLANKS:
                QuestionWidget._widgets[id] = new FillTheBlanksQuestionWidget(widgetElement);
                break;
        }
    }

    return QuestionWidget._widgets[id];
};

/**
 * Handle question node as element for editor controls
 * @param serializedNode Serialized node HTMl
 * @returns {*}
 */
QuestionWidget.handleQuestionNode = function (serializedNode) {
    const questionId =  $(serializedNode).attr("id");
    if(!QuestionWidget._widgets[questionId]) {
        const widgetType = $(serializedNode).attr("data-um-widget");
        switch(widgetType) {
            case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                QuestionWidget._widgets[questionId] = new MultiChoiceQuestionWidget(serializedNode);
                break;
            case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                QuestionWidget._widgets[questionId] = new FillTheBlanksQuestionWidget(serializedNode);
                break;
        }
    }
    return QuestionWidget._widgets[questionId];
};

/**
 * Handle the editor controls when the editing mode is ON i.e attaching listeners
 */
QuestionWidget.handleListeners = function () {
    const questionList = window.document.querySelectorAll(".question");
    if(questionList != null){
        for(const question in questionList){
            if(!questionList.hasOwnProperty(question))
                continue;
            const questionElement = questionList[question].outerHTML;
            const questionId =  $(questionElement).attr("id");
            if(!QuestionWidget._widgetListeners[questionId]) {
                const widgetType = $(questionElement).attr("data-um-widget");
                switch(widgetType) {
                    case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                        const multiChoice = new MultiChoiceQuestionWidget(questionElement);
                        multiChoice.attachEditListeners();
                        QuestionWidget._widgetListeners[questionId] = multiChoice;
                        break;
                    case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                        const fillTheBlanks = new FillTheBlanksQuestionWidget(questionElement);
                        fillTheBlanks.attachEditListeners();
                        QuestionWidget._widgetListeners[questionId] = fillTheBlanks;
                        break;
                }
            }
        }
    }
};


/**
 * Handle the editor controls when editing mode is OFF ie. attaching listeners
 */
QuestionWidget.handleEditOff = function(){
    const questionList = window.document.querySelectorAll(".question");
    for(const question in questionList){
        if(!questionList.hasOwnProperty(question))
            continue;
        const questionElement = questionList[question].outerHTML;
        const widgetType = $(questionElement).attr("data-um-widget");
        switch(widgetType) {
            case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                const multiChoice = new MultiChoiceQuestionWidget(questionElement);
                multiChoice.attachPreviewListeners();
                break;

            case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                const fillTheBlanks = new FillTheBlanksQuestionWidget(questionElement);
                fillTheBlanks.attachPreviewListeners();
                break;
        }
    }

};

QuestionWidget.handleEditOn = function(){

    const questionList = window.document.querySelectorAll(".question");
    if(questionList != null){
        for(const question in questionList){
            if(!questionList.hasOwnProperty(question))
                continue;
            const questionElement = questionList[question].outerHTML;
            const widgetType = $(questionElement).attr("data-um-widget");
            switch(widgetType) {
                case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                    const multiChoice = new MultiChoiceQuestionWidget(questionElement);
                    multiChoice.editOn();
                    break;

                case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                    const fillTheBlanks = new FillTheBlanksQuestionWidget(questionElement);
                    fillTheBlanks.editOn();
                    break;
            }
        }
    }

};

/**
 * Generate next question ID
 * @param idLength length of the alpha numeric question ID
 * @returns {string} Generated question ID
 */
QuestionWidget.getNextQuestionId = function(idLength = 8){
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let questionId = '';
    for (let i = idLength; i > 0; --i) questionId += chars[Math.floor(Math.random() * chars.length)];
    return questionId;
};



let FillTheBlanksQuestionWidget = function(element){
    QuestionWidget.apply(this, arguments);
};

let MultiChoiceQuestionWidget = function(element){
    QuestionWidget.apply(this, arguments);
};

/**
 * Make FillTheBlanksQuestionWidget Extends QuestionWidget
 * @type {QuestionWidget}
 */
FillTheBlanksQuestionWidget.prototype = Object.create(QuestionWidget.prototype);

/**
 * Make MultiChoiceQuestionWidget Extends QuestionWidget
 * @type {QuestionWidget}
 */
MultiChoiceQuestionWidget.prototype = Object.create(QuestionWidget.prototype);


/**
 * Attach listeners when editing mode is ON on Multiple choice questions
 */
MultiChoiceQuestionWidget.prototype.attachEditListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $(".question-add-choice-btn button.add-choice").on('click', this.addChoice.bind(this));
    $("button.add-choice").on('click', this.addChoice.bind(this));
    $("button.btn-delete").on('click', this.deleteQuestion.bind(this));
    $(".question-choice-answer select").on("change",this.setCorrectChoice.bind(this));
};

/**
 * Attach listeners when editing mode is ON on fill the blanks questions
 */
FillTheBlanksQuestionWidget.prototype.attachEditListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $("button.btn-delete").on('click', this.deleteQuestion.bind(this));
};


/**
 * Attach listeners when editing mode is OFF on multiple choice questions
 */
MultiChoiceQuestionWidget.prototype.attachPreviewListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $(".question-choice").on('click', this.handleClickAnswer.bind(this));
    $(".qn-retry").on('click', this.handleClickQuestionRetry.bind(this));
};

/**
 * Attach listeners when editing mode is OFF on fill the blanks questions
 */
FillTheBlanksQuestionWidget.prototype.attachPreviewListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $(".fill-the-blanks-check").on('click', this.handleProvidedAnswer.bind(this));
    $(".qn-retry").on('click', this.handleClickQuestionRetry.bind(this));
};


/**
 * Enable editing mode on multiple fill the multiple choice questions
 */
MultiChoiceQuestionWidget.prototype.editOn = function() {
    QuestionWidget.prototype.editOn.apply(this, arguments);
    $(this.element).find("button.btn-default.add-choice").remove();
    $("<button class='btn btn-primary float-right default-margin-top add-choice show-element'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForAddChoiceBtn+"</button>").appendTo(this.element);
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForChoiceBodyText+"</label><br/>");
    $(this.element).find(".question-choice-feedback").before("<label class='um-labels'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForFeedbackBodyText+"</label><br/>");
    $(this.element).find(".question-choice-answer").before("<label class='um-labels'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForRightAnswerOption+"</label><br/>");
    const choices = $(this.element).find(".question-choice");
    for(let choice in choices){
        if(!choices.hasOwnProperty(choice))
            continue;
        if($(choices[choice]).hasClass("question-choice")){
            $(choices[choice]).attr("id",QuestionWidget.getNextQuestionId());
        }

    }
    return this.element;
};


/**
 * Enable editing mode on multiple fill the banks questions
 * @returns {Array|*}
 */
FillTheBlanksQuestionWidget.prototype.editOn = function() {
    QuestionWidget.prototype.editOn.apply(this, arguments);
    $(this.element).find(".fill-blanks").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksAnswerBodyText+"</label><br/>");
    $(this.element).find(".input-group").before("<label class='um-labels '>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksPromptInput+"</label><br/>");
    $(this.element).find(".question-choice-feedback-correct").before("<label class='um-labels'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRightFeedbackText+"</label><br/>");
    $(this.element).find(".question-choice-feedback-wrong").before("<label class='um-labels'>"
        +QuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionWrongFeedbackText+"</label><br/>");
    return this.element;
};


/**
 * Add choice to the choice list
 * @param event
 */
MultiChoiceQuestionWidget.prototype.addChoice = function(event) {
    const choiceUiHolder = "<div class=\"question-choice\" data-um-correct=\"false\" data-um-preview=\"support\" id='"
        +QuestionWidget.getNextQuestionId()+"'>" +
        "<label class=\"um-labels\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelForChoiceBodyText+"</label><br>" +
        "<div class=\"question-choice-body\">"+QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceText+"</div>" +
        "<label class=\"um-labels\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelForFeedbackBodyText+"</label><br>" +
        "<div class=\"question-choice-feedback\" data-um-edit-only=\"true\">"
        +QuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceFeedback+"</div>" +
        "<label class=\"um-labels\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelForRightAnswerOption+"</label><br>" +
        "<div class=\"question-choice-answer select-option col-sm-3 show-element col-lg-3\">" +
        "<select><option value=\"true\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText+"</option>" +
        "<option selected=\"selected\" value=\"false\">"+QuestionWidget.PLACEHOLDERS_LABELS.labelFalseOptionText+"</option>" +
        "</select></div></div>";
    $(event.target).prev().prev().before(choiceUiHolder);
    $('body').on('change', '.question-choice-answer select', function(event) {
        MultiChoiceQuestionWidget.prototype.setCorrectChoice(event);
    });
};

/**
 * Check if the selected question answer is correct
 * @param event
 */
MultiChoiceQuestionWidget.prototype.handleClickAnswer = function(event) {
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
 * Handle when user fill the answer on the input filed
 * @param event
 */
FillTheBlanksQuestionWidget.prototype.handleProvidedAnswer = function(event){
    const questionElement = $(event.target).closest("div div.question");
    const choiceElement = $(questionElement).find(".fill-blanks");
    const wrongChoiceText = $(choiceElement).find(".question-choice-feedback-wrong").html();
    const correctChoiceText = $(choiceElement).find(".question-choice-feedback-correct").html();
    let defaultAnswerText = $(choiceElement).find(".question-choice-body").text().toLowerCase();
    let userAnswerText = $(questionElement).find(".fill-the-blanks-input").val().toLowerCase();
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    userAnswerText = userAnswerText.replace(/(\r\n|\n|\r)/gm,"").replace(/\s/g, "");
    defaultAnswerText = defaultAnswerText.replace(/(\r\n|\n|\r)/gm,"").replace(/\s/g, "");

    const isCorrectChoice = defaultAnswerText === userAnswerText;
    const message = userAnswerText.length > 0 ?
        (isCorrectChoice ? correctChoiceText: wrongChoiceText):QuestionWidget.PLACEHOLDERS_LABELS.warningOnSubmitEmptyField;
    console.log("Answer","user:"+userAnswerText+" - "+defaultAnswerText+": correct "+isCorrectChoice);
    $(feedbackContainer).find(".question-feedback-container-text").html(message);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning");
    $(feedbackContainer).addClass(userAnswerText.length > 0 ? (isCorrectChoice ? "alert-success":"alert-danger")
        :"alert-info"+ " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if((!isCorrectChoice && canBeRetried) || userAnswerText.length <= 0){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};

/**
 * Set correct question choice
 * @param event
 */
MultiChoiceQuestionWidget.prototype.setCorrectChoice = function(event){
    $(event.target).closest("div .question-choice").attr("data-um-correct",$(event.target).val());
    ustadEditor.handleContentChange();
};


/**
 * Add retry button on question which was set to be retried
 * @param event onChange event from selector
 */
QuestionWidget.prototype.setRetryOption = function(event){
    const questionElement = $(event.target).closest("div div.question");
    const canBeRetried = $(event.target).val() === 'true';
    $(questionElement).attr("data-um-retry",canBeRetried);
    $(this.element).find("br").remove();
    ustadEditor.handleContentChange();
};

/**
 * Delete question element from the eactive editor
 * @param event emmitted event from button click
 */
QuestionWidget.prototype.deleteQuestion = function(event){
    const questionElement = $(event.target).closest("div div.question");
    $(questionElement).nextUntil(".question").remove();
    $(questionElement).remove();

    if($.find(".question").length === 0){
        $.find("p").remove();
    }
};


/**
 * Handle when retry button is clicked (Hide feedback box and retry button)
 * @param event onClick event from button
 */
QuestionWidget.prototype.handleClickQuestionRetry = function(event){
    const questionElement = $(event.target).closest("div.question");
    $(questionElement).find(".question-choice-pointer").removeClass("selected-choice");
    $(questionElement).find(".question-feedback-container").removeClass("show-element").addClass("hide-element");
    $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
};
