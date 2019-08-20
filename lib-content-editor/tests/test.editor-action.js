/**
 * Test cases to make sure editor actions (Undo & Redo) behaves as expected
 */
chai.should();
describe('#Editor Actions', function() {
    const newEditorContent = "<span>New inserted Content</span>";
    describe('givenActiveEditorWithActionPerformed_whenUndoIsActivated_thenActionShouldBeReverted', function() {
        it('Previously performed action was reverted', function() {
            const initialEditorContent = UmEditorCore.getContent();
            UmEditorCore.insertContentRaw(newEditorContent);
            UmEditorCore.editorActionUndo();
            UmEditorCore.getContent().should.equal(initialEditorContent);
        });
    });

    describe('givenActiveEditorWithActionReverted_whenRedoIsActivated_thenShouldPerformTheAction', function() {
        it('Reverted action performed', function() {
            UmEditorCore.insertContentRaw(newEditorContent);
            const initialEditorContent = UmEditorCore.getContent();
            UmEditorCore.editorActionUndo();
            UmEditorCore.editorActionRedo();
            UmEditorCore.getContent().should.equal(initialEditorContent);
        });
    });
});
