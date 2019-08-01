/**
 * Test cases to make sure text ustadEditor behaves as expected
 */
chai.should();
describe('#Text Formatting', function() {
    describe('givenTextNode_whenBoldFormatIsActivated_thenShouldApplyBoldnessToSelectedText', function() {
        it('Boldness applied to selected text', function() {
            const callback = UmEditorCore.textFormattingBold();
          'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenTextNode_whenItalicFormatIsActivated_thenShouldItalicizeSelectedText', function() {
        it('Italicize selected text', function() {
            const callback = UmEditorCore.textFormattingItalic();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenTextNode_whenUnderlineFormatIsActivated_thenShouldUnderlineSelectedText', function() {
        it('Underlined selected text', function() {
            const callback = UmEditorCore.textFormattingUnderline();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenTextNode_whenStrikeThroughFormatIsActivated_thenShouldBeAppliedOnSelectedText', function() {
        it('Strike-through selected text', function() {
            const callback = UmEditorCore.textFormattingStrikeThrough();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenTextNode_whenSuperScriptFormatIsActivated_thenShouldMakeSelectedTextSuperScript', function() {
        it('Selected text made Superscript', function() {
            const callback = UmEditorCore.textFormattingSuperScript();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenTextNode_whenSubScriptFormatIsActivated_thenShouldMakeSelectedTextSubScript', function() {
        it('Selected text made subscript', function() {
            const callback = UmEditorCore.textFormattingSubScript();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });
});
