/**
 * Class which handle all content editor operation
 * @returns {*|UmContentEditorCore}
 * @constructor
 *
 * @author kileha3
 */
let UmContentEditorCore = function() {};

 /* Tracker to track if the key event was allowed even after passing the condition to be prevented. 
    This occurs only on content selection, instead it will be handled on keyup event */
let eventOcurredAfterSelectionProtectionCheck = false;

/**
 * Template location path
 * @type {string} path
 */
const questionTemplatesDir = "templates/";

/**
 * Check if rollback action ocurred
 */
let rollbackActionPerformed = false;

/**
 * Tracks state of the editor
 */
let currentActiveElementTracker = [];

/**
 * Stores element to focus after rollback action.
 */
let elementToFocusAfterRollbackAction = {};

/**
 * Acceptable state before rolling back check
 */
let acceptedEditorStateBeforeChange = null;


/**
 * Flag to indicate keyboard type used
 */
let isSoftKeyboard = true;

/**
 * Tag which used to save previous state of the editor on localStorage.
 */
const editorContentStateKey = "editor-content-state";

/**
 * Index which is used to get previous state of the editor
 */
const previousEditorStateIndex = 0;

/**
 * Allow mutation to affect the editor, we will only undo if the actions aren't delete/cut.
 */
let isDeleteOrCutAction = false;

/**
 * Longer time used by editor to change values. i.e Longer operation like observer callbacks
 */
const longerEventTimeout = 500;

/**
 * Shorter time used by editor to change values. i.e Cursor movement.
 */
const shorterEventTimeout = 100;

/**
 * Average time used by editor to change values, i.e delete/cut events and cursor movement.
 */
const averageEventTimeout = 300;

/**
 * Language locate path
 */
const languageLocaleDir = "locale/";

/**
 * Index of the multiple choice question document template in the template list
 * @type {number} index
 */
const indexMultipleChoiceQuestionType = 0;

/**
 * Index of the fill in the blank document template in the template list
 * @type {number} index
 */
const indexFillTheBlanksQuestionType = 1;

/**
 * Flag which shows if by any chance content was selected
 * @type {boolean} True when selection was performed otherwise false.
 */
let wasContentSelected = false;

/**
 * Flag which hold a value when content get selected and it includes protected content
 * @type {boolean} True if
 */
let isProtectedElement = false;

/**
 * Tracks mutation if came from question insertion.
 */
let isQuestionWidgetInsert = false;


/**
 * List of all tinymce formatting commands which will be used by native side.
 * @type {string[]} list of commands
 */
UmContentEditorCore.formattingCommandList = [
    'Bold','Underline','Italic','Strikethrough','Superscript','Subscript','JustifyCenter','JustifyLeft',
    'JustifyRight','JustifyFull', 'Indent','Outdent','JustifyLeft','JustifyCenter', 'JustifyRight',
    'JustifyFull','InsertUnorderedList','InsertOrderedList','mceDirectionLTR','mceDirectionRTL','FontSize'
];


/**
 * Check if a toolbar button is active or not
 * @param commandIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
UmContentEditorCore.prototype.checkCommandState = (commandIdentifier) => {
    return tinyMCE.activeEditor.queryCommandState(commandIdentifier);
};


/**
 * Check if the control was executed at least once.
 * @param commandIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
UmContentEditorCore.prototype.checkCommandValue = (commandIdentifier) => {
    return tinyMCE.activeEditor.queryCommandValue(commandIdentifier) != null;
};


/**
 * Change editor font size
 * @param fontSize font size to change to
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.setFontSize = (fontSize) => {
    UmContentEditorCore.executeCommand("FontSize",""+fontSize+"pt");
    const activeFont = tinyMCE.activeEditor.queryCommandValue("FontSize");
    const isActive = UmContentEditorCore.prototype.checkCommandState("FontSize");
    return {action:'activeControl',content:btoa("FontSize-"+isActive+"-"+activeFont)};
};

/**
 * Undo previously performed action
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.editorActionUndo = () => {
    rollbackActionPerformed = true;
    UmContentEditorCore.executeCommand("Undo",null);
    UmContentEditorCore.prototype.checkCommandState("Undo");
};


/**
 * Redo previously performed action
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.editorActionRedo = () => {
    UmContentEditorCore.executeCommand("Redo",null);
    UmContentEditorCore.prototype.checkCommandState("Redo");
};

/**
 * Set text direction from Left to Right
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textDirectionLeftToRight = () => {
    UmContentEditorCore.executeCommand('mceDirectionLTR');
    const isActive = UmContentEditorCore.prototype.getNodeDirectionality() === "ltr";
    return {action:'activeControl',content:btoa("mceDirectionLTR-"+isActive)};
};

/**
 * Set text direction from Right to Left
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textDirectionRightToLeft = () => {
    UmContentEditorCore.executeCommand('mceDirectionRTL');
    const isActive = UmContentEditorCore.prototype.getNodeDirectionality() === "rtl";
    return {action:'activeControl',content:btoa("mceDirectionRTL-"+isActive)};
};

/**
 * Remove or insert un-ordered list
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphUnOrderedListFormatting = () => {
    UmContentEditorCore.executeCommand("InsertUnorderedList",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("InsertUnorderedList");
    return {action:'activeControl',content:btoa("InsertUnorderedList-"+isActive)};
};

/**
 * Remove or insert ordered list
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphOrderedListFormatting = () => {
    UmContentEditorCore.executeCommand("InsertOrderedList",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("InsertOrderedList");
    return {action:'activeControl',content:btoa("InsertOrderedList-"+isActive)};
};

/**
 * Justify editor content to the left
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphLeftJustification = () => {
    UmContentEditorCore.executeCommand("JustifyLeft",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyLeft");
    return {action:'activeControl',content:btoa("JustifyLeft-"+isActive)};
};

/**
 * Justify editor content to the right.
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphRightJustification = () => {
    UmContentEditorCore.executeCommand("JustifyRight",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyRight");
    return {action:'activeControl',content:btoa("JustifyRight-"+isActive)};
};

/**
 * Justify content editor fully
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphFullJustification = () => {
    UmContentEditorCore.executeCommand("JustifyFull",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyFull");
    return {action:'activeControl',content:btoa("JustifyFull-"+isActive)};
};

/**
 * Justify editor content at the center
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphCenterJustification = () => {
    UmContentEditorCore.executeCommand("JustifyCenter",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyCenter");
    return {action:'activeControl',content:btoa("JustifyCenter-"+isActive)};
};

/**
 * Indent editor content
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphOutDent = () => {
    UmContentEditorCore.executeCommand("Outdent",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("Outdent");
    return {action:'activeControl',content:btoa("Outdent-"+isActive)};
};

/**
 * Indent editor content
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphIndent = () => {
    UmContentEditorCore.executeCommand("Indent",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("Indent");
    return {action:'activeControl',content:btoa("Indent-"+isActive)};
};

/**
 * Apply bold format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingBold = () => {
    UmContentEditorCore.executeCommand("Bold",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Bold");
    return {action:'activeControl',content:btoa("Bold-"+isActive)};
};

/**
 * Apply italic format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingItalic = () => {
    UmContentEditorCore.executeCommand("Italic",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Italic");
    return {action:'activeControl',content:btoa("Italic-"+isActive)};
};

/**
 * Apply underline format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingUnderline = () => {
    UmContentEditorCore.executeCommand("Underline",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Underline");
    return {action:'activeControl',content:btoa("Underline-"+isActive)};
};

/**
 * Apply strike-through format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingStrikeThrough = () => {
    UmContentEditorCore.executeCommand("Strikethrough",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Strikethrough");
    return {action:'activeControl',content:btoa("Strikethrough-"+isActive)};
};

/**
 * Apply superscript format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingSuperScript = () => {
    UmContentEditorCore.executeCommand("Superscript",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Superscript");
    return {action:'activeControl',content:btoa("Superscript-"+isActive)};
};

/**
 * Apply subscript format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingSubScript = () => {
    UmContentEditorCore.executeCommand("Subscript",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Subscript");
    return {action:'activeControl',content:btoa("Subscript-"+isActive)};
};

/**
 * Check if the current selected editor node has controls activated to it
 * @param commandValue control to check from
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.checkCurrentActiveControls = (commandValue) => {
    const isActive = UmContentEditorCore.prototype.checkCommandState(commandValue);
    return {action:'activeControl',content:btoa(commandValue+"-"+isActive)};
};

UmContentEditorCore.base64Encode = (content) => {
    return btoa(encodeURIComponent(content).replace(/%([0-9A-F]{2})/g, function(match, p1) {
        return String.fromCharCode(parseInt(p1, 16));
    }));
};

UmContentEditorCore.base64Decode = (content) =>{
    return decodeURIComponent(Array.prototype.map.call(atob(content), function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
};


/**
 * Insert multiple choice question template to the editor
 */
UmContentEditorCore.insertMultipleChoiceQuestionTemplate =  () =>  {
    UmContentEditorCore.prototype.insertQuestionTemplate(indexMultipleChoiceQuestionType);
};

/**
 * Insert fill in the blanks question template to the editor
 */
UmContentEditorCore.insertFillInTheBlanksQuestionTemplate =  () =>  {
    UmContentEditorCore.prototype.insertQuestionTemplate(indexFillTheBlanksQuestionType);
};

/**
 * Request focus to the tinymce
 * @returns focus object
 */
UmContentEditorCore.prototype.requestFocus =  () =>  {
    UmContentEditorCore.executeCommand("mceFocus",null);
};

/**
 * Select all content in document body
 */
UmContentEditorCore.selectAll =  () => {
    const body = $('body');
    body.on("click",function () {
        tinymce.activeEditor.selection.select(tinymce.activeEditor.getBody(), true);
    });
    body.click();
};

/**
 * Execute normal formatting commands
 * @param command command to be executed
 * @param args extra value to be passed on eg. font size
 */
UmContentEditorCore.executeCommand = (command, args) => {
    try{
        tinyMCE.activeEditor.execCommand(command, false,args);
    }catch(e){
        console.log("executeCommand: "+e);
    }
};


/**
 * Get current node directionality, if it was not set then it should inherit parent's directionality
 * @returns {string} directionality tag.
 */
UmContentEditorCore.prototype.getNodeDirectionality = () => {
    const currentNode = $(tinymce.activeEditor.selection.getNode());
    const parentDirectionality = getComputedStyle(currentNode.parent().get(0)).direction;
    const currentNodeDirectionality = getComputedStyle(currentNode.get(0)).direction;

    if(currentNodeDirectionality !== parentDirectionality){
        return currentNodeDirectionality;
    }
    return parentDirectionality;
};


/**
 * Get current position of the cursor placed on editable element relative to the content.
 * @returns {number} position of the cursor on character based index
 */
UmContentEditorCore.prototype.getCursorPositionRelativeToTheEditableElementContent = ()  =>  {
    try{
        if (window.getSelection && window.getSelection().getRangeAt) {
            const range = window.getSelection().getRangeAt(0);
            const selectedObj = window.getSelection();
            let rangeCount = 0;
            let childNodes = selectedObj.anchorNode.parentNode.childNodes;
            for (let i = 0; i < childNodes.length; i++) {
                if (childNodes[i] === selectedObj.anchorNode) {
                    break;
                }
                if (childNodes[i].outerHTML)
                    rangeCount += childNodes[i].outerHTML.length;
                else if (childNodes[i].nodeType === 3) {
                    rangeCount += childNodes[i].textContent.length;
                }
            }
            return range.startOffset + rangeCount;
        }
    }catch (e) {
        console.log("getCursorPositionRelativeToTheEditableElementContent:",e);
    }
    return 0;
};

/**
 * Check tinymce toolbar menu controls state.
 */
UmContentEditorCore.prototype.checkActivatedControls = () => {
    const commandStatus = [];
    for(let command in UmContentEditorCore.formattingCommandList){
        if(!UmContentEditorCore.formattingCommandList.hasOwnProperty(command))
          continue;
          const commandString = UmContentEditorCore.formattingCommandList[command];
          const commandState = {};
          let status = null;
          if(commandString === "FontSize"){
              status = tinyMCE.activeEditor.queryCommandValue(commandString).replace("px","");
          }else if(commandString ==="mceDirectionLTR"){
              status = UmContentEditorCore.prototype.getNodeDirectionality() === "ltr";
          }else if(commandString ==="mceDirectionRTL"){
              status = UmContentEditorCore.prototype.getNodeDirectionality() === "rtl";
          }else{
              status = UmContentEditorCore.prototype.checkCommandState(commandString);
          }
          commandState.command = commandString;
          commandState.status = status === null ? false : status;
          commandStatus.push(commandState);
    }

    try{
        UmContentEditor.onControlsStateChanged(JSON.stringify({action:'onActiveControlCheck',content:btoa(JSON.stringify(commandStatus))}));
    }catch(e){
        console.log(e);
    }
};

/**
 * Prepare and Insert question template on the editor
 * @param questionTypeIndex index of the question type in the list.
 * @param isTest Flag to indicate if executed from test environment
 */
UmContentEditorCore.prototype.insertQuestionTemplate = (questionTypeIndex,isTest = false) => {
    isQuestionWidgetInsert = true;
    const questionTemplateList = ['template-qn-multiple-choice.html','template-qn-fill-the-blanks.html'];
    const nextQuestionId = UmQuestionWidget.QUESTION_ID_TAG+UmQuestionWidget.getNextUniqueId();
    const nextChoiceId = UmQuestionWidget.CHOICE_ID_TAG+UmQuestionWidget.getNextUniqueId();
    const templateUrl = (isTest ? "/":"")+ questionTemplatesDir+questionTemplateList[questionTypeIndex];
    $.ajax({url: templateUrl, success: (templateHtmlContent) => {
            let questionNode = $(templateHtmlContent).attr("id",nextQuestionId);
            $(questionNode).find(".question-choice").attr("id",nextChoiceId);
            questionNode = $(questionNode).prop('outerHTML');
            questionNode = $("<div>").append(questionNode).append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
            questionNode = $(questionNode).html();
            UmContentEditorCore.prototype.insertQuestionNodeContent(questionNode);
            setTimeout(() => {
                isQuestionWidgetInsert = false;
            }, shorterEventTimeout);
        }});
};

/**
 * Inserting question template to the editor for testing purpose
 */
UmContentEditorCore.insertTestQuestionTemplate = () =>{
    UmContentEditorCore.prototype.insertQuestionTemplate(indexFillTheBlanksQuestionType,true);
};


/**
 * Get last extra content widget in the content editor
 * @returns content widget element
 */
UmContentEditorCore.prototype.getLastExtraContentWidget = () => {
    const extraContentWidgets = $("#umEditor").find('.extra-content');
    return $($(extraContentWidgets[extraContentWidgets.length - 1]).get(0)).children().get(0);
};

/**
 * Get last non protected and focusable element in the editor
 * @returns content widget element
 */
UmContentEditorCore.prototype.getLastNonProtectedFucasableElement = () =>{
    const editor = $("#umEditor");
    const pageBreaks = editor.find('.pg-break');
    if(pageBreaks.length === 0){
        editor.append(UmQuestionWidget.PAGE_BREAK);
        getLastEditableElement();
    }
    return $(pageBreaks[pageBreaks.length - 1]).get(0);
};


/**
 * Insert media content in the editor.
 * @param source file source path relative to the tinymce editor.
 * @param mimeType file mimetype.
 */
UmContentEditorCore.insertMediaContent = (source, mimeType) => {
    let mediaContent = null;
    if(mimeType.includes("image")){
        mediaContent = "<div align=\"center\" class=\"embed-responsive embed-responsive-16by9\">" +
            "<img src=\""+source+"\" class=\"embed-responsive-item\">" +
            "</div>";
    }else if(mimeType.includes("audio")){
        mediaContent = '<audio controls controlsList="nodownload" class="media-audio"><source src="'+source+'" type="'+mimeType+'"></audio>';

    }else{
       mediaContent = "<div align=\"center\" class=\"embed-responsive embed-responsive-16by9\">" +
            "    <video controls controlsList=\"nodownload\" preload=\"meta\" class=\"embed-responsive-item\">" +
            "        <source src=\""+source+"\" type=\""+mimeType+"\">" +
            "    </video>" +
            "</div>";
    }
    mediaContent = mediaContent + "<p><br/></p>";
    let currentElement = $(tinymce.activeEditor.selection.getNode());
    if(currentElement.is("p")){
        $(currentElement).after(mediaContent);
    }else{
        UmContentEditorCore.insertContentRaw(mediaContent)
    }

    const parentChildren = $($(currentElement.parent()).children()).last().get(0);
    UmContentEditorCore.prototype.setCursor(parentChildren,false);

};


/**
 * Tinymce command to insert content in the editor
 * @param content content to be inserted in the editor.
 */
UmContentEditorCore.insertContentRaw = (content) => {
    tinymce.activeEditor.execCommand('mceInsertContent', false, content);
};


/**
 * Handle question node when inserted/modified to the editor (This will be called on editor preInit)
 * @param questionNode Question html content
 * @param isFromClipboard False when the node was inserted from template else will
 * be coming from clipboard.
 */
UmContentEditorCore.prototype.insertQuestionNodeContent = (questionNode,isFromClipboard = false) => {
    UmQuestionWidget.setQuestionStatus(true);
    if(!isFromClipboard){
        tinymce.activeEditor.dom.add(tinymce.activeEditor.getBody(), 'p', {class: 'pg-break',style:'page-break-before: always'}, '');
    }else{
        const activeNode = tinymce.activeEditor.selection.getNode();
        const extraContent = $(activeNode).closest("div div.extra-content");
        $(extraContent).after(UmQuestionWidget.PAGE_BREAK);
    }
    UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(UmContentEditorCore.prototype.getLastNonProtectedFucasableElement());
    UmContentEditorCore.insertContentRaw(questionNode);
    UmContentEditorCore.prototype.scrollToElement(UmContentEditorCore.prototype.getLastNonProtectedFucasableElement());
    UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(UmContentEditorCore.prototype.getLastExtraContentWidget());
    tinymce.activeEditor.dom.remove(tinymce.activeEditor.dom.select('p.pg-break'));
};


/**
 * Move cursor to next focusable unprotected element in the question block.
 * @param eventTargetElement target element from click or keydown event.
 *
 * Since the entire body will be editable, there is no easy way to move cursor to the next focusable element
 * (all elements are focusable).Instead we have to tell the cursor where to go.
 * @returns next focusable element (For testing purpose since we can't emit keyboard events);
 */
UmContentEditorCore.setFocusToNextUnprotectedFocusableElement = (eventTargetElement) => {
    let nextElementFocusSelector = "p:not(.immutable-content)";
    let questionActionHolderSelector = ".question-action-holder";
    const labelSelector = ".immutable-content";
    let nextFocusableElement = null;
    if(!eventOcurredAfterSelectionProtectionCheck && ($(eventTargetElement).is(labelSelector) || $(eventTargetElement).is(questionActionHolderSelector))){
        //check if event came from question action holder.
        eventTargetElement = $(eventTargetElement).is(questionActionHolderSelector) ? $(eventTargetElement).next():eventTargetElement;
        
        nextFocusableElement = UmContentEditorCore.prototype.getNextElementMatchingSelector(eventTargetElement,nextElementFocusSelector);
        //If walking didn't find a node matching selector, start traversing
        if(!nextFocusableElement){
            nextFocusableElement = UmContentEditorCore.prototype.getPrevElementMatchingSelector(eventTargetElement,nextElementFocusSelector);
        }
        UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(nextFocusableElement);
        UmContentEditorCore.prototype.scrollToElement(nextFocusableElement);
        return nextFocusableElement;
    }else if($(eventTargetElement).is(".add-choice,.action-delete-inner")){
        const isDeletion = $(eventTargetElement).is(".action-delete-inner");
        eventTargetElement =  isDeletion ? $($(eventTargetElement).closest("span")).closest("div div.question"): eventTargetElement;
        setTimeout(function () {
            nextElementFocusSelector = (isDeletion ? ".question-body":".question-choice-body") + " p:first-of-type";
            eventTargetElement = isDeletion ? eventTargetElement: $($(eventTargetElement).closest("div div.question")).find(".question-choice").last();
            nextFocusableElement = $(eventTargetElement).find(nextElementFocusSelector).get(0);
            UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(nextFocusableElement);
            UmContentEditorCore.prototype.scrollToElement(nextFocusableElement);
            return nextFocusableElement;
        },isDeletion ? 0:averageEventTimeout);

    }
    return nextFocusableElement;
};

/**
 * Walk dom tree to find the next focusable element
 * @param currentNode Event targeted node
 * @param selector css selector which satisfies next focusable element 
 */
UmContentEditorCore.prototype.getNextElementMatchingSelector = (currentNode,selector) => {
  try{
    if($(currentNode).is(selector)){
        return currentNode;
    }else{
        if($(currentNode).children().length > 0){
          $(currentNode).children().each(function(index,childNode){
              currentNode = childNode;
              if($(currentNode).is("select")){
                  currentNode = null;
                  return false;
              }
              if(currentNode){
                  currentNode = UmContentEditorCore.prototype.getNextElementMatchingSelector(currentNode,selector);
              }
              return !$(currentNode).is(selector) || currentNode === null;
          });
        }else {
          currentNode = $(currentNode).next();
          currentNode = UmContentEditorCore.prototype.getNextElementMatchingSelector(currentNode,selector);
          if(currentNode === null){
              return currentNode;
          }
        }
    }
  }catch(e){
    currentNode = null;
    console.log("getNextElementMatchingSelector",e);
  }
  return currentNode;
};

/**
 * Traverse through to find the next focusable element
 * @param currentNode Event targeted node
 * @param selector css selector which satisfies next focusable element 
 */
UmContentEditorCore.prototype.getPrevElementMatchingSelector = (currentNode,selector) => {
   try{
    const targetNode = currentNode;
    currentNode = $(targetNode).prev();
    const prevElementSelector = ":not(.question-feedback-container), :not(.question-choice-answer)";
    if($(currentNode).is(selector)){
        return currentNode;
    }else{
        const parentNode = currentNode;
        if($(parentNode).children().length > 0 && $(parentNode).is(prevElementSelector)){
            $(parentNode).children().each(function(index,childNode){
                currentNode = childNode;
                const foundNode = UmContentEditorCore.prototype.getNextElementMatchingSelector(currentNode,selector);
                if(!foundNode){
                    currentNode = UmContentEditorCore.prototype.getPrevElementMatchingSelector(parentNode,selector,true);
                }
                return !$(currentNode).is(selector);
            });
        }else {
            currentNode = currentNode.length > 0 ? $(currentNode).prev():$(targetNode).closest("div").prev();
            currentNode = UmContentEditorCore.prototype.getPrevElementMatchingSelector(currentNode,selector);
        }
    }
    currentNode = $(currentNode).is(".question-choice-feedback-wrong") ? $($(currentNode).children()).first().get(0):currentNode;
   }catch(err){
       currentNode = null;
   }
   return currentNode;
  };

/**
 * Set cursor to the last non protected and focusable element in the editor.
 * @param rootElement editor root element.
 */
UmContentEditorCore.prototype.setCursorPositionToTheLastNonProtectedElement = (rootElement) =>{
    if(rootElement == null){
        rootElement = document.getElementById("umEditor");
    }
    const range = document.createRange();
    const selection = window.getSelection();
    range.selectNodeContents(rootElement);
    range.collapse(false);
    selection.removeAllRanges();
    selection.addRange(range);
    rootElement.focus();
    range.detach();
    rootElement.scrollTop = rootElement.scrollHeight;
    if($(document).height() > $(window).height()){
        $("html, body").animate({ scrollTop: $(document).height()-$(window).height()});
    }
};


/**
 * Scroll page to the targeted element in the editor.
 * @param element target element to scroll to
 */
UmContentEditorCore.prototype.scrollToElement = (element) => {
    if (!!element && element.scrollIntoView) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center'});
    }
};

/**
 * Set cursor to a specific editor node
 * @param element target element cursor to be set on.
 */
UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement = (element) => {
   try{
       element = element === null ? $("#umEditor").children().get(0):element;
       const range = document.createRange();
       const sel = window.getSelection();
       range.setStart(element, 0);
       range.collapse(false);
       sel.removeAllRanges();
       sel.addRange(range);
       element.focus();
   }catch (e) {
       console.log("setCursorToAnyNonProtectedFucusableElement",e)
   }
};

/** 
 * Set cursor to specific element in the editor
 * @param element target element to focus
 * @param isRoot flag to indicate if target is root element.
  */
UmContentEditorCore.prototype.setCursor = (element = null,isRoot) =>{
    if(isRoot){
        UmContentEditorCore.prototype.setCursorPositionToTheLastNonProtectedElement(element);
    }else{
        UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(element);
    }
};

/**
 * Check if the current action is worth taking place
 * @param currentNode current selected node
 * @param isDeleteKey true if the key is either delete or backspace, false otherwise
 * @param selectedContentLength length of the selected content. Used only to determine if the length is non-zero.
 *                              If length is 0, the check only continues if the key is delete or backspace.
 * @param event keydown event
 * @returns {boolean} True is the action should take place otherwise false.
 */
UmContentEditorCore.checkProtectedElements = (currentNode,isDeleteKey,selectedContentLength, event) => {
    const doNotRemoveSelector = ":not(.embed-responsive),.dont-remove, .dont-remove p:first-of-type, .immutable-content, " +
        ".question + .extra-content, .question + .extra-content p:first-of-type";
    const cursorPositionIndex = UmContentEditorCore.prototype.getCursorPositionRelativeToTheEditableElementContent();
    const preventSelection = selectedContentLength > 0 && $(currentNode).find(doNotRemoveSelector).length > 0;
    const matchesSelector = $(currentNode).is(doNotRemoveSelector);
    if((isDeleteKey && matchesSelector && cursorPositionIndex === 0) || preventSelection){
        eventOcurredAfterSelectionProtectionCheck = preventSelection;
        return UmContentEditorCore.prototype.preventDefaultAndStopPropagation(event);
    }
    return true;
};

/**
 * Prevent key event and stop event propagation (Physical keyboard key events handling)
*/
UmContentEditorCore.prototype.preventDefaultAndStopPropagation = (event) =>  {
    try{
        event.preventDefault();
        event.stopImmediatePropagation();
        return false;
    }catch(e){
        console.log("preventDefaultAndStopPropagation",e);
    }
    return false;
};



/**  
 * Prevent protected element from being deleted, this applies only to soft keyboard.
 * @param mutations mutation records from the mutation observer. 
 */
UmContentEditorCore.prototype.rollbackChangesAfterMutation = (mutations) => {
    let rollbackChanges = false;
     if(isSoftKeyboard){
        $(mutations).each((index,record) =>{
            const isValidElementToCheck = (record.type === "childList" && record.removedNodes.length > 0 && !isQuestionWidgetInsert && !rollbackActionPerformed) 
            && !isDeleteOrCutAction;
            if(isValidElementToCheck){
                let selector = ".dont-remove, .immutable-content, div[class^='question'], #umEditor, .question";
                console.log("roll matches",$(record.target).is(selector),record.target);
                if($(record.target).is(selector)){
                    rollbackChanges = true;
                    return false;
                }
            }
        });
     }
     return rollbackChanges;
};

/**
 * Save editor state and keep tracking of previous acceptable state of the editor.
 */
UmContentEditorCore.prototype.saveEditorState = () => {
    acceptedEditorStateBeforeChange = tinymce.activeEditor.getContent();
    const activeIndex = 0;
    //track element to be focused
    if(!rollbackActionPerformed){
        currentActiveElementTracker.push($(tinymce.activeEditor.selection.getNode()).parent());
        const lastIndexToTrack  = currentActiveElementTracker.length - 3;
        currentActiveElementTracker.forEach((element,index) => {
            if(index <= lastIndexToTrack){
                currentActiveElementTracker.splice(index,1);
            }
        });
        elementToFocusAfterRollbackAction = {
            element: currentActiveElementTracker[activeIndex],
            parent: $(currentActiveElementTracker[activeIndex]).parent()
        }
    }
};

/**
 * Set default locale to the editor
 * @param locale language locale to be set
 * @param isTest Flag to indicate if the pages are running under test invironment
 *               If so, resource path will be changes.
  */
UmContentEditorCore.setDefaultLanguage = (locale, isTest = false)=>{
    UmQuestionWidget.loadPlaceholders(locale,isTest);
}

/**
 * Initialize tinymce editor to the document element
 * @param locale Default UMEditor language locale
 * @param showToolbar Flag to show and hide default tinymce toolbar
 */
UmContentEditorCore.initEditor = (locale = "en", showToolbar = false) => {
    UmQuestionWidget.loadPlaceholders (locale);
    const configs = {
        selector: '#umEditor',
        height: $(window).height(),
        menubar: showToolbar,
        statusbar: showToolbar,
        inline:true,
        force_br_newlines : false,
        force_p_newlines : true,
        forced_root_block : '',
        plugins: ['directionality','lists'],
        toolbar: showToolbar,
        extended_valid_elements: "span[*],i[*]",
        setup: (ed) => {
            ed.on('preInit', () => {
                //selection status tracker.
                this.currentSelectionIsProtected = false;
                ed.parser.addAttributeFilter("data-um-widget", (nodes) =>{
                    for(let node in nodes) {
                        if(!nodes.hasOwnProperty(node))
                            continue;
                        const questionNode = tinymce.html.Serializer().serialize(nodes[node]);
                        if($(questionNode).attr("id") != null){
                            let questionWidget = UmQuestionWidget.handleWidgetNode(questionNode);
                            questionWidget = questionWidget.startEditing();
                            questionWidget = $("<div>").append(questionWidget);
                            const tempNode =  tinymce.html.DomParser().parse($(questionWidget).html());
                            nodes[node].replace(tempNode);
                        }
                        UmQuestionWidget.handleWidgetListeners(true);
                    }
                });
            });
        },
        init_instance_callback: (ed) => {
            /**
             * Listen for text selection event
             * @type {[type]}
             */

            ed.on('SelectionChange', (e) => {
                const currentNode = tinymce.activeEditor.selection.getNode();
                UmContentEditorCore.setFocusToNextUnprotectedFocusableElement(currentNode);
                try{
                    const sel = rangy.getSelection();
                    const range = sel.rangeCount ? sel.getRangeAt(0) : null;
                    let selectedContent = "";
                    if (range) {        
                        selectedContent = range.toHtml();
                    }
                    this.selectedContent = selectedContent;
                }catch (e) {
                    console.log(e);
                }
            });

            ed.on('Paste', e => {
                e.stopPropagation();
                e.preventDefault();

                let clipboardData = e.clipboardData || window.clipboardData;
                const content = clipboardData.getData('Text');
                if($(content).hasClass("question")){
                    UmContentEditorCore.prototype.insertQuestionNodeContent(content,true);
                }else{
                    UmContentEditorCore.insertContentRaw(content)
                }
            });


            /**
             * Listen for click event inside the editor
             * @type {[type]}
             */
            ed.on('click', e => {
                UmContentEditorCore.prototype.checkActivatedControls();
                UmContentEditorCore.prototype.getNodeDirectionality();
                UmContentEditorCore.setFocusToNextUnprotectedFocusableElement(e.target);
            });

            /**
             * Listen for the key up event
             * @type {[type]}
             */
            ed.on('keyup', () => {
                if(eventOcurredAfterSelectionProtectionCheck){
                    eventOcurredAfterSelectionProtectionCheck = false;
                    UmContentEditorCore.editorActionUndo();
                }
            });


            /**
             * Listen for the keyboard keys and prevent important label and divs from being deleted from the editor
             * @type {[type]}
             */
            ed.on('keydown', (e) => {
                isSoftKeyboard =  e.key === "Unidentified";
                if(!isSoftKeyboard){
                    const currentNode = this.selectedContent.length > 0 ? $("<div />").append($(this.selectedContent).clone()).get(0):tinymce.activeEditor.selection.getNode(); 
                    UmContentEditorCore.checkProtectedElements(currentNode,e.key === "Backspace" || e.key === "Delete",this.selectedContent.length,e);
                    UmContentEditorCore.setFocusToNextUnprotectedFocusableElement(e.target);
                }
            });
        }
    };

    //set toolbar menus if will be shown
    if(showToolbar){
        configs.toolbar = ['undo redo | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | fontsizeselect'];
    }

    tinymce.init(configs).then(() => {

        rangy.init();
        const editorContainer = $("#umEditor");
        
        //save editor state at start
        UmContentEditorCore.prototype.saveEditorState();

        //set default directionality
        $(editorContainer).attr("dir",UmQuestionWidget._locale.directionality);

        //request focus to the editor
        UmContentEditorCore.prototype.requestFocus();

        //enable editing mode
        UmQuestionWidget.setEditingMode(true);

        //Check if is a document edit or creating new document
        if($(".question").length > 0){
            UmContentEditorCore.prototype.setCursorPositionToTheLastNonProtectedElement();
        }else{
            if(UmQuestionWidget.removeSpaces($(editorContainer.children().get(0)).text()).length === 0){
                $(editorContainer.children().get(0)).remove();
            }
            const extra_widget = $(UmQuestionWidget.EXTRA_CONTENT_WIDGET).attr("id",
            UmQuestionWidget.ELEMENT_ID_TAG+UmQuestionWidget.getNextUniqueId())
            editorContainer.append(extra_widget);
            UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(editorContainer.children().get(0))
        }
        tinymce.activeEditor.dom.remove(tinymce.activeEditor.dom.select('p.pg-break'));
        try{
            UmContentEditor.onInitEditor(JSON.stringify({action:'onInitEditor',content:"true"}));
        }catch (e) {
            console.log("onInitEditor: "+e);
        }

        try{

            //add observer to watch content changes
            let contentWatcherFilters = {
                attributes: true, characterData: true, childList: true, subtree: true,
                attributeOldValue: true, characterDataOldValue: true
            };

            const contentChangeObserver = new MutationObserver(function(mutations) {
                const textHolder = editorContainer.find(".extra-content");
                if(textHolder.length === 0){
                    editorContainer.find("p").remove();
                    editorContainer.append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
                    UmContentEditorCore.prototype.setCursor(null,false);
                }

                //check if change made is allowed or not, if not roll back to the last allowed change.
                if(UmContentEditorCore.prototype.rollbackChangesAfterMutation(mutations)){
                    rollbackActionPerformed = true;
                    editorContainer.html(acceptedEditorStateBeforeChange);
                    setTimeout(() => {
                        rollbackActionPerformed = false;
                        const classSelector = $(elementToFocusAfterRollbackAction.element).attr("class").split(/\s+/)[0];
                        const elementToFocus = editorContainer.find("."+classSelector);
                        //Find an element to focus after rolling back the changes
                        elementToFocus.each((index,element) => {
                            if($(elementToFocusAfterRollbackAction.parent).attr("id") === $($(element).parent().get(0)).attr("id")){
                                UmContentEditorCore.prototype.setCursorToAnyNonProtectedFucusableElement(element);
                                UmContentEditorCore.prototype.scrollToElement(element);
                            }
                        
                        });
                    }, longerEventTimeout);
                }else{
                    //Save editor state
                    UmContentEditorCore.prototype.saveEditorState();
                }


                //add immutable class to all labels
                UmQuestionWidget.handleImmutableContent();
                
                //Check editor controls state
                setTimeout(() => {UmContentEditorCore.prototype.checkActivatedControls()},averageEventTimeout);
                const previewContent = JSON.stringify({
                    action:'onSaveContent',
                    content:UmQuestionWidget.saveContentEditor(tinyMCE.activeEditor.getContent())
                });

                try{
                    UmContentEditor.onSaveContent(previewContent);
                }catch (e) {
                    console.log("onContentChanged:",e);
                }

            });
            contentChangeObserver.observe(document.querySelector('#umEditor'),contentWatcherFilters);
            
            
            //add observer to watch controls state changes
            const menuWatcherFilter = {
                attributes : true,
                attributeFilter : ['style']
            };
            const menuStateChangeObserver = new MutationObserver(function() {
                setTimeout(() => {UmContentEditorCore.prototype.checkActivatedControls()},averageEventTimeout);
            });
            menuStateChangeObserver.observe(document.querySelector('.mce-panel'),menuWatcherFilter);

        }catch (e) {
            console.log("Observers ",e);
        }
    });

};

/**
 * Get content editor content (For testing)
 */
UmContentEditorCore.getContent = () => {
    return tinymce.activeEditor.getContent();
};





