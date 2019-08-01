/**
 * Test cases to make sure text direction change behaves as expected
 */
chai.should();
describe('#Directionality', function() {
    describe('givenDirectionality_whenChangedToLTR_thenShouldAddDirectionalityToAnode', function() {
        it('Node directionality changed to LTR', function() {
            const callback = UmEditorCore.textDirectionLeftToRight();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });

    describe('givenDirectionality_whenChangedToRTL_thenShouldAddDirectionalityToAnode', function() {
        it('Node directionality changed to RTL', function() {
            const callback = UmEditorCore.textDirectionRightToLeft();
            'true'.should.equal(atob(callback.content).split("-")[formatStatusIndex]);
        });
    });
});
