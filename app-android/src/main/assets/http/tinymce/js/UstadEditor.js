function UstadEditor() {if (!(this instanceof UstadEditor))
    return new UstadEditor();}

const ustadEditor = new UstadEditor();
let activeEditor = null;
let preventDeleteSelection = false;
let wasContentSelected = false;
let selectedContentLength = 0;

/**
 * Initialize UstadEditor with active editor instance
 * @param activeEditor TinyMCE active editor instance
 */
ustadEditor.init = function(activeEditor){
    this.activeEditor = activeEditor;
};

/**
 * Check if a toolbar button is active or not
 * @param buttonIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
ustadEditor.isToolBarButtonActive = function(buttonIdentifier){
    return this.activeEditor.queryCommandState(buttonIdentifier);
};

/**
 * Check if the control was executed at least once.
 * @param controlCommand
 */
ustadEditor.isControlActivated = function(controlCommand){
    return this.activeEditor.queryCommandValue(controlCommand) != null;
};

/**
 * Change editor blankDocument font size
 * @param fontSize Size to change to
 * @returns {string | * | void}
 */
ustadEditor.setFontSize = function(fontSize){
    this.executeCommand("FontSize",""+fontSize+"pt");
    const activeFont = this.activeEditor.queryCommandValue("FontSize");
    const isActive = this.isControlActivated("FontSize");
    return {action:'activeControl',content:btoa("FontSize-"+isActive+"-"+activeFont)};
};

/**
 * Undo previously performed action
 * @returns {Object} TRUE if succeed FALSE otherwise
 */
ustadEditor.editorActionUndo = function(){
    this.executeCommand("Undo",null);
    const isActive = this.isControlActivated("Undo");
    return {action:'activeControl',content:btoa("Undo-"+isActive)};
};

/**
 * Redo previously performed action
 * @returns {Object} TRUE if succeed FALSE otherwise
 */
ustadEditor.editorActionRedo = function(){
    this.executeCommand("Redo",null);
    const isActive = this.isControlActivated("Redo");
    return {action:'activeControl',content:btoa("Redo-"+isActive)};
};

/**
 * Set text direction from Left to Right
 * @returns {Object} TRUE if direction changed otherwise FALSE
 */
ustadEditor.textDirectionLeftToRight = function(){
    this.executeCommand('mceDirectionLTR');
    const isActive = this.isControlActivated("mceDirectionLTR");
    return {action:'activeControl',content:btoa("mceDirectionLTR-"+isActive)};
};

/**
 * Set text direction from Right to Left
 * @returns {Object} TRUE if direction changed otherwise FALSE
 */
ustadEditor.textDirectionRightToLeft = function(){
    this.executeCommand("mceDirectionRTL",null);
    const isActive = this.isControlActivated("mceDirectionRTL");
    return {action:'activeControl',content:btoa("mceDirectionRTL-"+isActive)};
};

/**
 * Remove or insert un-ordered list
 * @returns {Object} TRUE inserted and FALSE otherwise
 */
ustadEditor.paragraphUnOrderedListFormatting = function(){
    this.executeCommand("InsertUnorderedList",null);
    const isActive = this.isToolBarButtonActive("InsertUnorderedList");
    return {action:'activeControl',content:btoa("InsertUnorderedList-"+isActive)};
};

/**
 * Remove or insert ordered list
 * @returns {Object} TRUE inserted and FALSE otherwise
 */
ustadEditor.paragraphOrderedListFormatting = function(){
    this.executeCommand("InsertOrderedList",null);
    const isActive = this.isToolBarButtonActive("InsertOrderedList");
    return {action:'activeControl',content:btoa("InsertOrderedList-"+isActive)};
};

/**
 * Justify left editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphLeftJustification = function(){
    this.executeCommand("JustifyLeft",null);
    const isActive = this.isToolBarButtonActive("JustifyLeft");
    return {action:'activeControl',content:btoa("JustifyLeft-"+isActive)};
};

/**
 * Justify left editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphRightJustification = function(){
    this.executeCommand("JustifyRight",null);
    const isActive = this.isToolBarButtonActive("JustifyRight");
    return {action:'activeControl',content:btoa("JustifyRight-"+isActive)};
};

/**
 * Justify fully editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphFullJustification = function(){
    this.executeCommand("JustifyFull",null);
    const isActive = this.isToolBarButtonActive("JustifyFull");
    return {action:'activeControl',content:btoa("JustifyFull-"+isActive)};
};

/**
 * Justify center editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphCenterJustification = function(){
    this.executeCommand("JustifyCenter",null);
    const isActive = this.isToolBarButtonActive("JustifyCenter");
    return {action:'activeControl',content:btoa("JustifyCenter-"+isActive)};
};

/**
 * Indent editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphOutDent = function(){
    this.executeCommand("Outdent",null);
    const isActive = this.isControlActivated("Outdent");
    return {action:'activeControl',content:btoa("Outdent-"+isActive)};
};

/**
 * Indent editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphIndent = function(){
    this.executeCommand("Indent",null);
    const isActive = this.isControlActivated("Indent");
    return {action:'activeControl',content:btoa("Indent-"+isActive)};
};

/**
 * Apply bold ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingBold = function(){
    this.executeCommand("Bold",null);
    const isActive = this.isToolBarButtonActive("Bold");
    return {action:'activeControl',content:btoa("Bold-"+isActive)};
};

/**
 * Apply italic ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingItalic = function(){
    this.executeCommand("Italic",null);
    const isActive = this.isToolBarButtonActive("Italic");
    return {action:'activeControl',content:btoa("Italic-"+isActive)};
};

/**
 * Apply underline ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingUnderline = function(){
    this.executeCommand("Underline",null);
    const isActive = this.isToolBarButtonActive("Underline");
    return {action:'activeControl',content:btoa("Underline-"+isActive)};
};

/**
 * Apply strike-through ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingStrikeThrough = function(){
    this.executeCommand("Strikethrough",null);
    const isActive = this.isToolBarButtonActive("Strikethrough");
    return {action:'activeControl',content:btoa("Strikethrough-"+isActive)};
};

/**
 * Apply superscript ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingSuperScript = function(){
    this.executeCommand("Superscript",null);
    const isActive = this.isToolBarButtonActive("Superscript");
    return {action:'activeControl',content:btoa("Superscript-"+isActive)};
};

/**
 * Apply subscript ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingSubScript = function(){
    this.executeCommand("Subscript",null);
    const isActive = this.isToolBarButtonActive("Subscript");
    return {action:'activeControl',content:btoa("Subscript-"+isActive)};
};

/**
 * Check if the current selected editor node has controls activated to it
 * @param commandValue control to check from
 * @returns {{action: string, content: string}}
 */
ustadEditor.checkCurrentActiveControls = function(commandValue){
    const isActive = ustadEditor.isToolBarButtonActive(commandValue);
    return {action:'activeControl',content:btoa(commandValue+"-"+isActive)};
};

/**
 * Start checking for active controls  and reactivate
 * @returns {{action: string, content: string}}
 */
ustadEditor.startCheckingActivatedControls = function(){
    return {action:'onActiveControlCheck',content:btoa("yes")};
};

/**
 * Initialize tinymce on a document
 * @param showToolbar True when you want to show toolbar menus, False otherwise.
 */
ustadEditor.initTinyMceEditor = function(showToolbar = false){
    this.showToolbar = showToolbar;
    const inlineConfig = {
        selector: '#umPreview',
        menubar: this.showToolbar,
        statusbar: this.showToolbar,
        inline: true,
        force_br_newlines : true,
        force_p_newlines : false,
        forced_root_block : '',
        plugins: ['ustadmobile','directionality','lists','noneditable','visualblocks'],
        toolbar: ['undo redo | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | fontsizeselect ustadmobile'],
        valid_styles: {
            '*': 'font-size,font-family,color,text-decoration,text-align'
        },
        extended_valid_elements : 'label[class],div[onclick|class|data-um-correct|data-um-widget|id],option[selected|value],br[class]',
        style_formats: [{ title: 'Containers', items: [
                { title: 'section', block: 'section', wrapper: true, merge_siblings: false },
                { title: 'article', block: 'article', wrapper: true, merge_siblings: false },
                { title: 'blockquote', block: 'blockquote', wrapper: true },
                { title: 'hgroup', block: 'hgroup', wrapper: true },
                { title: 'aside', block: 'aside', wrapper: true },
                { title: 'figure', block: 'figure', wrapper: true }
            ]
        }],
        init_instance_callback: function (ed) {
            /**
             * Listen for text selection event
             * @type {[type]}
             */

            ed.on('SelectionChange', () => {
                const selection = rangy.getSelection();
                const range = selection.rangeCount ? selection.getRangeAt(0) : null;
                selectedContentLength = range.toHtml().length;
                preventDeleteSelection = ($(range.toHtml()).find("label").length > 0 ||
                    $(range.toHtml()).find("label").length > 0) &&
                    (range.toHtml().includes("<div") || range.toHtml().includes("<label"));
            });

            /**
             * Listen for node change event
             * @type {[type]}
             */
            ed.on('NodeChange', (e) => {
                QuestionWidget.handleListeners();
                console.log("UmEditor","NodeChange:",e);
            });

            /**
             * Listen for click event inside the editor
             * @type {[type]}
             */
            ed.on('click', (e) => {
                console.log("UmEditor","Click:",e);
                try{
                    UmContentEditor.onControlActivatedCheck(JSON.stringify(ustadEditor.startCheckingActivatedControls()));
                    UmContentEditor.onClickEvent(JSON.stringify({action:'onClickEvent',content:btoa("yes")}));
                }catch(e){
                    console.log(e);
                }
            });

            /**
             * Listen for the key up event
             * @type {[type]}
             */
            ed.on('keyup', (e) => {
                console.log("UmEditor","KeyUp:",e);
                if(wasContentSelected){
                    wasContentSelected = false;
                    ustadEditor.editorActionUndo();
                }
            });

            /**
             * Listen for the keyboard keys and prevent important label and divs from being deleted from the editor
             * @type {[type]}
             */
            ed.on('keydown', e => {
                console.log("UmEditor","KeyDown:",e);
                const deleteKeys = e.key === "Backspace" || e.key === "Delete";
                const enterKey = e.key === "Enter";

                const selection = tinymce.activeEditor.selection;
                const activeNode = selection.getNode();
                const isLabel = $(activeNode).hasClass("um-labels");
                const isPgBreak = $(activeNode).hasClass("pg-break");
                const isDeleteQuestionBtn = $(activeNode).hasClass("close");
                const isCloseSpan = $(activeNode).is("span");
                const isButtonLabels = $(activeNode).is("button");
                const isDiv = $(activeNode).is("div");
                const isChoiceSelector = $(activeNode).hasClass("question-retry-option");
                let innerDivEmpty = false;

                if(isDiv){
                    const divContent = $(activeNode).text();
                    innerDivEmpty = divContent.length <= 0 || ustadEditor.getCursorPositionRelativeToTheEditableElementContent() <= 0;
                }

                const preventLabelsDeletion = deleteKeys && isLabel;
                const preventTagDeletion = deleteKeys && innerDivEmpty;

                if(preventDeleteSelection || (enterKey && isLabel)){
                    wasContentSelected = true;
                }

                const disableDeleteOrEnterKey = isPgBreak || isDeleteQuestionBtn || isCloseSpan || isButtonLabels || preventLabelsDeletion || isChoiceSelector;

                if(selectedContentLength === 0){
                    if(preventTagDeletion || disableDeleteOrEnterKey){
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        return false
                    }
                }else{
                    if(disableDeleteOrEnterKey && preventDeleteSelection || disableDeleteOrEnterKey){
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        return false
                    }
                }


            });
        }
    };
    try{
        tinymce.init(inlineConfig).then(function () {

            ustadEditor.init(tinymce.activeEditor);
            QuestionWidget.handleListeners();
            QuestionWidget.handleEditOn();
            setTimeout(ustadEditor.requestFocus(), 20);
            ustadEditor.hideToolbarMenu();
            setTimeout(ustadEditor.switchOnEditorController());

            let filters = {
                attributes: true, characterData: true, childList: true, subtree: true,
                attributeOldValue: true, characterDataOldValue: true
            };

            //add observer to watch content changes
            const contentChangeObserver = new MutationObserver(function() {
                ustadEditor.handleContentChange();
            });
            contentChangeObserver.observe(document.querySelector('#umPreview'),filters);

            //add observer to watch toolbar style change
            const toolbarChangeObserver = new MutationObserver(function(mutations) {
                mutations.forEach(function() {
                    ustadEditor.hideToolbarMenu();
                });
            });
            filters = { attributes : true, attributeFilter : ['style'] };
            toolbarChangeObserver.observe(document.querySelector('.mce-container'),filters);

            try{
                UmContentEditor.onInitEditor(JSON.stringify({action:'onInitEditor',content:"true"}));
                UmContentEditor.onControlActivatedCheck(JSON.stringify(ustadEditor.startCheckingActivatedControls()));
                UmContentEditor.onClickEvent(JSON.stringify({action:'onClickEvent',content:btoa("yes")}));
            }catch(e){
                console.log(e);
            }

        });
    }catch (e) {
        console.log("initTinyMceEditor: "+e);
    }
};

/**
 * Add labels and texts on the document when question templates is used
 */
ustadEditor.switchOnEditorController = function(){
    try{
        document.getElementById("editor-on").click();
    }catch (e) {
        console.log("switchOnEditorController: "+e);
    }
};

/**
 * Get blankDocument from the active blankDocument editor
 * @returns {*|void}
 */
ustadEditor.getContent = function(){
    return {action:'getContent',content:btoa(this.activeEditor.getContent())};
};

/**
 * Request focus to the active editor
 * @returns {boolean}
 */
ustadEditor.requestFocus = function () {
    this.executeCommand("mceFocus",null);
    return this.isControlActivated("mceFocus");
};

/**
 * Hide toolbar menu after successfully initializing the editor
 */
ustadEditor.hideToolbarMenu = function () {
    if(!this.showToolbar){
        try{
            $("div[id^='mceu_']").addClass("hide-element");
            $("#ustadmobile-menu").click();
            $(".mce-container-body").hide();
        }catch (e) {
            console.log("hideToolbarMenu: "+e);
        }
    }
};

/**
 * Select all blankDocument on the editor
 * ForTesting only.
 */
ustadEditor.selectAll = function () {
    const body = $('body');
    body.on("click",function () {
        tinymce.activeEditor.selection.select(tinymce.activeEditor.getBody(), true);
    });
    body.click();

};



/**
 * Insert multiple choice question template to the editor
 */
ustadEditor.insertMultipleChoiceQuestionTemplate = function () {
    try{
        QuestionWidget.setNewQuestion("true");
        document.getElementById("multiple-choice").click();
        return "inserted multiple choice question";
    }catch (e) {
        console.log("insertMultipleChoiceQuestionTemplate: "+e);
        return null;
    }
};

/**
 * Insert fill in the blanks question template to the editor
 */
ustadEditor.insertFillInTheBlanksQuestionTemplate = function () {
    try{
        QuestionWidget.setNewQuestion("true");
        document.getElementById("fill-the-blanks").click();
        return "inserted fill the blanks question";
    }catch (e) {
        console.log("insertFillInTheBlanksQuestionTemplate: "+e);
        return null;
    }
};

/**
 * Insert multimedia blankDocument to the editor
 * @param source media absolute path
 * @param mimeType media mime type
 */
ustadEditor.insertMedia = function(source,mimeType){
    const width = $(window).width();
    let mediaContent = null;
    if(mimeType.includes("image")){
        mediaContent = "<img src=\""+source+"\" class=\"um-media img-fluid\" width=\""+width+"\"/>";
    }else if(mimeType.includes("audio")){
        mediaContent =
            "<video controls class='media-audio'>" +
            "    <source src=\""+source+"\" type=\""+mimeType+"\">" +
            "</video>";
    }else{
        mediaContent =
            "<video controls class='um-media img-fluid' width='"+width+"'>" +
            "    <source src=\""+source+"\" type=\""+mimeType+"\">" +
            "</video>";
    }
    this.executeRawContent("<p class='text-center'>"+mediaContent+"</p><p style=\"page-break-before: always\" class=\"pg-break\">");
};


/**
 * Insert raw blankDocument to the active editor
 * @param content blankDocument to be inserted
 */
ustadEditor.executeRawContent= function(content){
    this.activeEditor.execCommand('mceInsertContent', false, content,{format: 'raw'});
};

/**
 * Execute normal formatting commands
 * @param command command to be executed
 * @param args extra value to be passed on eg. font size
 */
ustadEditor.executeCommand = function(command,args){
    try{
        this.activeEditor.execCommand(command, false,args);
    }catch(e){
        console.log("executeCommand: "+e);
    }
};



/**
 * Callback to listen for any changes on the active editor
 */
ustadEditor.handleContentChange = function(){
    try{
        UmContentEditor.onContentChanged(JSON.stringify({action:'onContentChanged',content:btoa(this.activeEditor.getContent())}));
    }catch(e){
        console.log(e);
    }
};

/**
 * Start content live preview on the editor
 */
ustadEditor.startLivePreview = function () {
    try{
        document.getElementById("editor-off").click();
        return {action: 'savePreview', content: btoa(this.activeEditor.getContent()),extraFlag:null};
    }catch (e) {
        console.log("startLivePreview:"+e)
    }
};

/**
 * Load content into a preview
 * @param fileContent content to be manipulated for preview
 */
ustadEditor.preparePreviewContent = function (fileContent) {
    const editorContent = $("<div/>").html($.parseHTML(atob(fileContent)));
    $(editorContent).find("br").remove();
    $(editorContent).find("label").remove();
    $(editorContent).find("button.add-choice").remove();
    $(editorContent).find('div.question-choice').addClass("question-choice-pointer").removeClass("default-margin-top");
    $(editorContent).find('div.multi-choice').addClass("default-margin-bottom").removeClass("default-margin-top");
    $(editorContent).find('div.select-option').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.fill-blanks').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice-answer').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('.question-retry-btn').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice-feedback').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice').addClass('alert alert-secondary');
    $(editorContent).find('p.pg-break').addClass('hide-element');
    $(editorContent).find('button.btn-delete').removeClass("show-element").addClass('hide-element');
    $(editorContent).find('.default-theme').removeClass('no-padding');
    $(editorContent).find('.question-body').addClass("default-margin-bottom no-left-padding");
    $(editorContent).find('.question-answer').addClass("no-padding no-left-padding default-margin-bottom");
    $(editorContent).find('.fill-answer-inputs').addClass("no-padding");
    $(editorContent).find('[data-um-preview="main"]').addClass('preview-main default-margin-top');
    $(editorContent).find('[data-um-preview="alert"]').addClass('preview-alert default-margin-top');
    $(editorContent).find('[data-um-preview="support"]').addClass('preview-support default-margin-top');
    $(editorContent).find('div.question').addClass('card default-padding-top default-padding-bottom');
    return {action:'onSaveContent',content:btoa($('<div/>').html(editorContent).contents().html())};
};


/**
 * Changing editor editing mode
 */
ustadEditor.changeEditorMode = function(mode){
    if(mode === 'off'){
        this.activeEditor.getBody().setAttribute('contenteditable', false);
    }else{
        this.activeEditor.getBody().setAttribute('contenteditable', true);
    }
    return mode;
};



/**
 * Start blankDocument live preview on the editor
 */
ustadEditor.startLivePreview = function () {
    try{
        document.getElementById("editor-off").click();
        return {action: 'savePreview', content: btoa(this.activeEditor.getContent()),extraFlag:null};
    }catch (e) {
        console.log("startLivePreview:"+e)
    }
};



/**
 * Find the cursor position relative to the current selected editable area
 */
ustadEditor.getCursorPositionRelativeToTheEditableElementContent = function() {
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
    return -1;
};
