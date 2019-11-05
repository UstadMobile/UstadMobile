/**
 * Test cases to make sure when editor is in preview mode and edit mode behaves as expected
 */
chai.should();
describe('#XAPI Create statement)', function () {
    describe('givenPreviewIsOn_whenQuestionIsAnswered_thenXapiStatementShouldBeCreated', function () {
        it('Xapi Statement was created', function () {
            $($(".um-editor").find(".question-choice-body").get(0)).click()
            setTimeout(() => {
                //assert that new statmemt was added
                "true".should.equal(UmXapiManager.xapiStatement != null ? "true":"false");
            }, TEST_CASE_TIMEOUT);
        });
    });
});
