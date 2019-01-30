/**
 * Test cases to make sure editor actions (Undo & Redo) behaves as expected
 */
chai.should();
describe('#Editor Actions', function() {
    const newEditorContent = "<span>New inserted Content</span>";
    describe('givenActiveEditorWithActionPerformed_whenUndoIsActivated_thenActionShouldBeReverted', function() {
        it('Previously performed action was reverted', function() {
            const initialEditorContent = UmContentEditorCore.getContent();
            UmContentEditorCore.insertContentRaw(newEditorContent);
            UmContentEditorCore.editorActionUndo();
            UmContentEditorCore.getContent().should.equal(initialEditorContent);
        });
    });

    describe('givenActiveEditorWithActionReverted_whenRedoIsActivated_thenShouldPerformTheAction', function() {
        it('Reverted action performed', function() {
            UmContentEditorCore.insertContentRaw(newEditorContent);
            const initialEditorContent = UmContentEditorCore.getContent();
            UmContentEditorCore.editorActionUndo();
            UmContentEditorCore.editorActionRedo();
            UmContentEditorCore.getContent().should.equal(initialEditorContent);
        });
    });
});
