/**
 * Tes cases to make sure ustadmobile blankDocument templates works as expected
 * 1. Insert Multiple choice questions
 * 2. Insert in fill the blanks questions
 * 3. Insert multimedia blankDocument.
 */
chai.should();
describe('#Protected content', function() {

    describe('givenActiveEditor_whenProtectedElementIsSelectedAndDeleteKeyIsPressed_thenShouldNotBeDeleted', function() {
        it('Content deletion not allowed', function() {
            const currentNode = "<label class='um-labels immutable-content'>Sample label</label>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(currentNode,true,currentNode.length,{});
            isKeyAllowed.should.equal(false);
        });
    });

    describe('givenActiveEditor_whenProtectedElementIsSelectedAndAnyKeyIsPressed_thenShouldNotBeDeleted', function() {
        it('Content deletion not allowed', function() {
            const currentNode = "<label class='um-labels immutable-content'>Sample label</label>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(currentNode,true,currentNode.length,{},0);
            isKeyAllowed.should.equal(false);
        });
    });

    describe('givenActiveEditor_whenNotProtectedElementIsSelectedAndDeleteKeyIsPressed_thenShouldBeDeleted', function() {
        it('Content deletion allowed', function() {
            const currentNode = "<p>Sample label</p>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(currentNode,true,currentNode.length,{});
            isKeyAllowed.should.equal(true);
        });
    });

    describe('givenActiveEditor_whenNoElementContentIsSelectedAndDeleteKeyIsPressed_thenShouldBeDeleted', function() {
        it('Content deletion allowed', function() {
            const currentNode = "<p>Sample label</p>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(currentNode,false,0,{});
            isKeyAllowed.should.equal(true);
        });
    });
});