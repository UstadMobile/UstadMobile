/**
 * Test cases to make sure when editor is in preview mode and edit mode behaves as expected
 */
chai.should();
describe('#Content Editing/Previewing)', function () {
    describe('givenEditorIsTurnedOn_whenContentInserted_thenContentShouldHaveLabels', function () {
        it('Content labels added', function () {
            UmEditorCore.insertMultipleChoiceWidget();
            setTimeout(() => {
                //assert labels found as indication that it is in editing mode
                const foundLabels = $(".um-editor").find(".um-labels").length
                const expectedLabels = 5;
                expectedLabels.should.equal(foundLabels);
            }, TEST_CASE_TIMEOUT);
        });
    });

    describe('givenEditorIsTurnedOff_whenPreviewing_thenAllLabelsShouldBeRemoved', function () {
        it('Content labels was removed', function () {
            UmEditorCore.insertMultipleChoiceWidget();
            setTimeout(() => {
                UmEditorCore.disableEditingMode();
                //assert no labels found
                const foundLabels = $(".um-editor").find(".um-labels").length
                const expectedLabels = 0;
                expectedLabels.should.equal(foundLabels);
                console.log("found", foundLabels)

            }, TEST_CASE_TIMEOUT);
        });
    });
});
