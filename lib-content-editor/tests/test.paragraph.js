/**
 * Test cases to make sure paragraph ustadEditor behaves as expected
 */
chai.should();
describe('#Paragraph Formatting', function() {

    describe('givenActiveEditor_whenParagraphIsLeftJustified_thenShouldJustify', function() {
        it('Left justification applied', function() {
            const callback = UmEditorCore.paragraphLeftJustification();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenActiveEditor_whenParagraphIsRightJustified_thenShouldJustify', function() {
        it('Right justification applied', function() {
            const callback = UmEditorCore.paragraphRightJustification();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenActiveEditor_whenParagraphIsFullyFully_thenShouldJustify', function() {
        it('Full justification applied', function() {
            const callback = UmEditorCore.paragraphFullJustification();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenActiveEditor_whenParagraphIsCenterJustified_thenShouldJustify', function() {
        it('Center justification', function() {
            const callback = UmEditorCore.paragraphCenterJustification();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenActiveEditor_whenParagraphIsIndented_thenIndentShouldBeApplied', function() {
        it('Indent applied', function() {
            const callback = UmEditorCore.paragraphIndent();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });


    describe('givenActiveEditor_whenParagraphIsOutdented_thenOutdentShouldBeApplied', function() {
        it('Outdent applied', function() {
            const callback = UmEditorCore.paragraphOutDent();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenActiveEditor_whenOrderedListIsInserted_thenItemsShouldBeNumbered', function() {
        it('Numbers applied to a list item', function() {
            const callback = UmEditorCore.paragraphOrderedListFormatting();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenActiveEditor_whenUnOrderedListIsInserted_thenItemsShouldBulletized', function() {
        it('Bullets applied to list items', function() {
            const callback = UmEditorCore.paragraphUnOrderedListFormatting();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });
});
